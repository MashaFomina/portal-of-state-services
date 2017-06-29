package services.stateservices.storage.institutions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import services.stateservices.entities.*;
import services.stateservices.errors.NoRightsException;
import services.stateservices.institutions.EducationalInstitution;
import services.stateservices.storage.Gateway;
import services.stateservices.storage.Mapper;
import services.stateservices.storage.StorageRepository;
import services.stateservices.storage.entities.*;
import services.stateservices.storage.user.UserMapper;
import services.stateservices.user.*;

public class EducationalInstitutionMapper extends InstitutionMapper implements Mapper<EducationalInstitution> {
    private static Set<EducationalInstitution> educationalInstitutions = new HashSet<>();
    private static EduRequestMapper eduRequestMapper;
    private static FeedbackMapper feedbackMapper;
    private static StorageRepository repository = null;

    public EducationalInstitutionMapper() throws IOException, SQLException {
        if (connection == null)
            connection = Gateway.getInstance().getDataSource().getConnection();
        if (eduRequestMapper == null)
            eduRequestMapper = new EduRequestMapper();
        if (feedbackMapper == null)
            feedbackMapper = new FeedbackMapper();
        if (repository == null)
            repository = StorageRepository.getInstance();
    }
    
    public List<EducationalInstitution> findAllByDistrict(String city, String district) throws SQLException {
        List<EducationalInstitution> all = new ArrayList<>();

        String selectSQL = "SELECT * FROM institutions WHERE city = ? and district = ? and is_edu = 1;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setString(1, city);
        selectStatement.setString(2, district);
        ResultSet rs = selectStatement.executeQuery();

        EducationalInstitution institution;
        while (rs.next()) {
            institution = findByID(rs.getInt("id"));
            if (institution != null)
                all.add(institution);
        }

        selectStatement.close();
        
        return all;
    }
        
    @Override
    public EducationalInstitution findByID(int id) throws SQLException {
        for (EducationalInstitution it : educationalInstitutions)
            if (it.getId() == id) return it;

        Map<Integer, Integer> seats = new HashMap<>(); // key - class number, value - seats
        Map<Integer, Integer> busySeats = new HashMap<>(); // key - class number, value - busy seats
    
        String selectSQL = "SELECT * FROM institutions WHERE id = ? and is_edu = 1;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, id);
        ResultSet rs = selectStatement.executeQuery();

        if (!rs.next()) return null;

        int eid = rs.getInt("id");
        String title = rs.getString("title");
        String city = rs.getString("city");
        String district = rs.getString("district");
        String telephone = rs.getString("telephone");
        String fax = rs.getString("fax");
        String address = rs.getString("address"); 
        selectStatement.close();
        
        selectSQL = "SELECT * FROM educational_institutions_seats WHERE institution_id = ?;";
        PreparedStatement selectSeatsStatement = connection.prepareStatement(selectSQL);
        selectSeatsStatement.setInt(1, eid);
        ResultSet rs1 = selectSeatsStatement.executeQuery();

        while (rs1.next()) {
            int classNumber = rs1.getInt("class_number");
            seats.put(classNumber, rs1.getInt("seats"));
            busySeats.put(classNumber, rs1.getInt("busy_seats"));
        }
        selectSeatsStatement.close();

        EducationalInstitution newEducationalInstitution = new EducationalInstitution(title, city, district, telephone, fax, address, seats, busySeats);
        newEducationalInstitution.setId(eid); 
        educationalInstitutions.add(newEducationalInstitution);
        
        List<EduRequest> eduRequests = eduRequestMapper.findAllForInstitution(id);
        for (EduRequest it : eduRequests) {
            try {
                if (it.getInstitution() == null) {
                    it.setInstitution(newEducationalInstitution);
                }
                
                if (it.getParent() == null ) {
                    Citizen parent = repository.getCitizen(eduRequestMapper.getParentId(it.getId()));
                    it.setParent(parent);
                    if (it.getChild().getParent() == null) it.getChild().setParent(parent);
                }
                
                if (it.getParent() != null ) {
                    newEducationalInstitution.addEduRequest(it);
                }
            }   catch (NoRightsException ex) {
                Logger.getLogger(EducationalInstitutionMapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        List<Feedback> feedbacks = feedbackMapper.findAllForInstitution(id);
        for (Feedback it : feedbacks) {
            newEducationalInstitution.addFeedback(it);
        }
        
        return newEducationalInstitution;
    }

    @Override
    public List<EducationalInstitution> findAll() throws SQLException {
        List<EducationalInstitution> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM institutions WHERE is_edu = 1;";
        Statement selectStatement = connection.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectSQL);

        EducationalInstitution institution;
        while (rs.next()) {
            institution = findByID(rs.getInt("id"));
            if (institution != null)
                all.add(institution);
        }

        selectStatement.close();
        
        return all;
    }

    @Override
    public void update(EducationalInstitution item) throws SQLException {
        connection.setAutoCommit(false);
        try {
            if (educationalInstitutions.contains(item)) {
                if (item.isUpdated()) {
                    cleanSeats(item.getId());
                    updateItem(item);
                    insertSeats(item);
                }
            } else {
                insertItem(item);
                educationalInstitutions.add(item);
                insertSeats(item);
            }
            
            for (EduRequest it : item.getEduRequests()) {
                eduRequestMapper.update(it);
            }

            for (Feedback it : item.getFeedbacks()) {
                feedbackMapper.update(it);
            }
            
            connection.commit();
        }
        catch (SQLException ex) { connection.rollback(); }
        finally { connection.setAutoCommit(true); }
    }

    private void cleanSeats(int institution_id) throws SQLException{
        String deleteSQL = "DELETE FROM educational_institutions_seats WHERE institution_id = ?;";
        PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
        deleteStatement.setInt(1, institution_id);
        deleteStatement.execute();
        deleteStatement.close();
    }
    
    public void insertSeats(EducationalInstitution item) throws SQLException {
        String insertSQL = "INSERT INTO educational_institutions_seats (institution_id, class_number, seats, busy_seats) VALUES (?, ?, ?, ?);";
        PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
        for (Integer classNumber : item.getSeats().keySet()) {
            insertStatement.setInt(1, item.getId());
            insertStatement.setInt(2, classNumber);
            insertStatement.setInt(3, item.getSeats(classNumber));
            insertStatement.setInt(4, item.getBusySeats(classNumber));
            insertStatement.addBatch();
        }
        insertStatement.executeBatch();
    }
        
    @Override
    public void closeConnection() throws SQLException {
        eduRequestMapper.closeConnection();
        feedbackMapper.closeConnection();
        connection.close();
    }

    @Override
    public void clear() {
        eduRequestMapper.clear();
        feedbackMapper.clear();
        educationalInstitutions.clear();
        cities.clear();
        districts.clear();
    }

    @Override
    public void update() throws SQLException {
        eduRequestMapper.update();
        feedbackMapper.update();
        for (EducationalInstitution it : educationalInstitutions)
            update(it);
    }
    
    @Override
    public int getInstitutionType() {
        return 1; // is_edu must be 1
    }
}