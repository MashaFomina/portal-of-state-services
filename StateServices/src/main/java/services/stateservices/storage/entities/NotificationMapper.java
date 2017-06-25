package services.stateservices.storage.entities;

import services.stateservices.entities.Notification;
import services.stateservices.storage.Gateway;
import services.stateservices.storage.Mapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotificationMapper implements Mapper<Notification> {
    private static Set<Notification> notifications = new HashSet<>();
    private static Connection connection;

    public NotificationMapper() throws IOException, SQLException {
        if (connection == null)
            connection = Gateway.getInstance().getDataSource().getConnection();
    }

    public List<Notification> getForUser(int user) throws SQLException {
        List<Notification> all = new ArrayList<>();

        for (Notification it : notifications) {
            if (it.getOwner().getId() == user)
                all.add(it);
        }
        
        return all;
    }
    
    public List<Notification> findAllForUser(int user) throws SQLException {
        List<Notification> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM notifications WHERE user = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, user);
        ResultSet rs = selectStatement.executeQuery();

        Notification notification;
        while (rs.next()) {
            notification = findByID(rs.getInt("id"));
            if (notification != null)
                all.add(notification);
        }

        selectStatement.close();
        
        return all;
    }

    @Override
    public Notification findByID(int id) throws SQLException {
        for (Notification it : notifications)
            if (it.getId() == id) return it;

        String selectSQL = "SELECT * FROM notifications WHERE id = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, id);
        ResultSet rs = selectStatement.executeQuery();

        if (!rs.next()) return null;

        int nid = rs.getInt("id");
        String notification = rs.getString("notification");
        Timestamp timestamp = rs.getTimestamp("created");
        Date date = timestamp != null ? new Date(timestamp.getTime()) : null;
        
        selectStatement.close();

        Notification newNotification = new Notification(nid, notification, date);
        notifications.add(newNotification);
        return newNotification;
    }

    @Override
    public List<Notification> findAll() throws SQLException {
        List<Notification> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM notifications;";
        Statement selectStatement = connection.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectSQL);

        Notification notification;
        while (rs.next()) {
            notification = findByID(rs.getInt("id"));
            if (notification != null)
                all.add(notification);
        }

        selectStatement.close();
        
        return all;
    }

    @Override
    public void update(Notification item) throws SQLException {
        if (notifications.contains(item)) {
            // message object is immutable, don't need to update
        } else {
            String insertSQL = "INSERT INTO notifications(user, notification, created) VALUES (?, ?, ?);";
            PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setInt(1, item.getOwner().getId());
            insertStatement.setString(2, item.getNotification());
            insertStatement.setTimestamp(3, new Timestamp(item.getDate().getTime()));
            insertStatement.execute();
            ResultSet rs = insertStatement.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                item.setId((int) id);
            }
            notifications.add(item);
            insertStatement.close();
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        connection.close();
    }

    @Override
    public void clear() {
        notifications.clear();
    }

    @Override
    public void update() throws SQLException {
        for (Notification it : notifications)
            update(it);
    }
}