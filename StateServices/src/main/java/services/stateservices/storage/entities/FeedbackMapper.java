package services.stateservices.storage.entities;

import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import services.stateservices.entities.Feedback;
import services.stateservices.institutions.*;
import services.stateservices.storage.Gateway;
import services.stateservices.storage.Mapper;
import services.stateservices.storage.StorageRepository;
import services.stateservices.storage.user.UserMapper;
import services.stateservices.storage.institutions.*;
import services.stateservices.user.User;

public class FeedbackMapper implements Mapper<Feedback> {

    private static Set<Feedback> feedbacks = new HashSet<>();
    private static Connection connection;
    private static StorageRepository repository = null;

    public FeedbackMapper() throws IOException, SQLException {
        if (connection == null)
            connection = Gateway.getInstance().getDataSource().getConnection();
        if (repository == null)
            repository = StorageRepository.getInstance();
    }

    // Is used for getting from cache
    public List<Feedback> getForInstitution(int institution) throws SQLException {
        List<Feedback> all = new ArrayList<>();

        for (Feedback it : feedbacks) {
            if (it.getInstitution().getId() == institution)
                all.add(it);
        }
        
        return all;
    }
        
    public List<Feedback> findAllForInstitution(int institution) throws SQLException {
        List<Feedback> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM feedbacks WHERE institution_id = ? ORDER BY id DESC;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, institution);
        ResultSet rs = selectStatement.executeQuery();

        Feedback feedback;
        while (rs.next()) {
            feedback = findByID(rs.getInt("id"));
            if (feedback != null)
                all.add(feedback);
        }

        selectStatement.close();
        
        return all;
    }

    @Override
    public Feedback findByID(int id) throws SQLException {
        for (Feedback it : feedbacks)
            if (it.getId() == id) return it;

        String selectSQL = "SELECT f.*, i.is_edu FROM feedbacks AS f LEFT JOIN institutions AS i ON i.id = f.institution_id WHERE f.id = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, id);
        ResultSet rs = selectStatement.executeQuery();

        if (!rs.next()) return null;

        int fid = rs.getInt("f.id");
        int userId = rs.getInt("f.user");
        String text = rs.getString("f.feedback_text");
        Timestamp timestamp = rs.getTimestamp("f.created");
        Date date = timestamp != null ? new Date(timestamp.getTime()) : null;
        int institutionId = rs.getInt("f.institution_id");
        int toUserId = rs.getInt("f.to_user");
        int isEdu = rs.getInt("i.is_edu");
        
        selectStatement.close();

        Feedback newFeedback = null;
        User user = repository.getUser(userId);
        Institution institution = (isEdu == 1) ? repository.getEducationalInstitution(institutionId) : repository.getMedicalInstitution(institutionId);
        User toUser = (toUserId > 0) ? repository.getUser(toUserId) : null;
        if (user != null && institution != null) {
            newFeedback = (toUserId < 1 || toUser == null) ? new Feedback(date, user, institution, text) : new Feedback(date, user, institution, text, toUser);
            newFeedback.setId(fid); 
            feedbacks.add(newFeedback);
        }
        
        return newFeedback;
    }

    @Override
    public List<Feedback> findAll() throws SQLException {
        List<Feedback> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM feedbacks ORDER BY id DESC;";
        Statement selectStatement = connection.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectSQL);

        Feedback feedback;
        while (rs.next()) {
            feedback = findByID(rs.getInt("id"));
            if (feedback != null)
                all.add(feedback);
        }

        selectStatement.close();
        
        return all;
    }

    @Override
    public void update(Feedback item) throws SQLException {
        if (feedbacks.contains(item)) {
            // feedback object is immutable, don't need to update
        } else {
            User toUser = item.getToUser();
            String insertSQL = (toUser == null) ? "INSERT INTO feedbacks(user, feedback_text, created, institution_id) VALUES (?, ?, ?, ?);" : "INSERT INTO feedbacks(user, feedback_text, created, institution_id, to_user) VALUES (?, ?, ?, ?, ?);";
            PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setInt(1, item.getUser().getId());
            insertStatement.setString(2, item.getText());
            insertStatement.setTimestamp(3, new Timestamp(item.getDate().getTime()));
            insertStatement.setInt(4, item.getInstitution().getId());
            if (toUser != null)
                insertStatement.setInt(5, toUser.getId());
            insertStatement.execute();
            ResultSet rs = insertStatement.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                item.setId((int) id);
            }
            feedbacks.add(item);
            insertStatement.close();
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        connection.close();
    }

    @Override
    public void clear() {
        feedbacks.clear();
    }

    @Override
    public void update() throws SQLException {
        for (Feedback it : feedbacks)
            update(it);
    }
}