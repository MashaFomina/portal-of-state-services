package services.stateservices.storage.entities;

import services.stateservices.entities.Child;
import services.stateservices.storage.Gateway;
import services.stateservices.storage.Mapper;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChildMapper implements Mapper<Child> {
    private static Set<Child> childs = new HashSet<>();
    private static Connection connection;

    public ChildMapper() throws IOException, SQLException {
        if (connection == null)
            connection = Gateway.getInstance().getDataSource().getConnection();
    }

    public List<Child> getForUser(int user) throws SQLException {
        List<Child> all = new ArrayList<>();

        for (Child it : childs) {
            if (it.getParent().getId() == user)
                all.add(it);
        }
        
        return all;
    }
        
    public List<Child> findAllForUser(int user) throws SQLException {
        List<Child> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM childs WHERE parent = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, user);
        ResultSet rs = selectStatement.executeQuery();

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }

    @Override
    public Child findByID(int id) throws SQLException {
        for (Child it : childs)
            if (it.getId() == id) return it;

        String selectSQL = "SELECT * FROM childs WHERE id = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, id);
        ResultSet rs = selectStatement.executeQuery();

        if (!rs.next()) return null;

        int cid = rs.getInt("id");
        String fullName = rs.getString("full_name");
        String birthCertificate = rs.getString("birth_certificate");
        Date birthDate = rs.getDate("birth_date");

        selectStatement.close();
        
        Child newChild = new Child(cid, fullName, birthCertificate, birthDate);
        childs.add(newChild);
        return newChild;
    }

    @Override
    public List<Child> findAll() throws SQLException {
        List<Child> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM childs;";
        Statement selectStatement = connection.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectSQL);

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }
    
    @Override
    public void update(Child item) throws SQLException {
        if (childs.contains(item)) {
            // message object is immutable, don't need to update
        } else {
            String insertSQL = "INSERT INTO childs(parent, full_name, birth_certificate, birth_date) VALUES (?, ?, ?, ?);";
            PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setInt(1, item.getParent().getId());
            insertStatement.setString(2, item.getFullName());
            insertStatement.setString(3, item.getBirthCertificate());
            insertStatement.setDate(4, new java.sql.Date(item.getBirthDate().getTime()));
            insertStatement.execute();
            ResultSet rs = insertStatement.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                item.setId((int) id);
            }
            childs.add(item);
            insertStatement.close();
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        connection.close();
    }

    @Override
    public void clear() {
        childs.clear();
    }

    @Override
    public void update() throws SQLException {
        for (Child it : childs)
            update(it);
    }
}