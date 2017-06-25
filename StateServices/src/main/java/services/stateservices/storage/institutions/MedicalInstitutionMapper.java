package services.stateservices.storage.institutions;

import java.io.IOException;
import java.sql.*;
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
import services.stateservices.entities.Child;
import services.stateservices.entities.Feedback;
import services.stateservices.entities.Ticket;
import services.stateservices.errors.NoRightsException;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.storage.Gateway;
import services.stateservices.storage.Mapper;
import services.stateservices.storage.entities.*;
import services.stateservices.storage.user.UserMapper;
import services.stateservices.user.Citizen;
import services.stateservices.user.Doctor;
import services.stateservices.user.User;

public class MedicalInstitutionMapper extends InstitutionMapper implements Mapper<MedicalInstitution> {
    private static Set<MedicalInstitution> medicalInstitutions = new HashSet<>();
    private static UserMapper userMapper;
    private static TicketMapper ticketMapper;
    private static FeedbackMapper feedbackMapper;

    public MedicalInstitutionMapper() throws IOException, SQLException {
        if (connection == null)
            connection = Gateway.getInstance().getDataSource().getConnection();
        if (userMapper == null)
            userMapper = new UserMapper(null, this);
        if (ticketMapper == null)
            ticketMapper = new TicketMapper();
        if (feedbackMapper == null)
            feedbackMapper = new FeedbackMapper();
    }
    
    public List<MedicalInstitution> findAllByDistrict(String city, String district) throws SQLException {
        List<MedicalInstitution> all = new ArrayList<>();

        String selectSQL = "SELECT * FROM institutions WHERE city = ? and district = ? and is_edu = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setString(1, city);
        selectStatement.setString(2, district);
        selectStatement.setInt(3, getInstitutionType());
        ResultSet rs = selectStatement.executeQuery();

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }
    
    @Override
    public MedicalInstitution findByID(int id) throws SQLException {
        for (MedicalInstitution it : medicalInstitutions)
            if (it.getId() == id) return it;

        String selectSQL = "SELECT * FROM institutions WHERE id = ? and is_edu = 0;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, id);
        ResultSet rs = selectStatement.executeQuery();

        if (!rs.next()) return null;

        int mid = rs.getInt("id");
        String title = rs.getString("title");
        String city = rs.getString("city");
        String district = rs.getString("district");
        String telephone = rs.getString("telephone");
        String fax = rs.getString("fax");
        String address = rs.getString("address");
        
        selectStatement.close();

        MedicalInstitution newMedicalInstitution = new MedicalInstitution(title, city, district, telephone, fax, address);
        newMedicalInstitution.setId(mid); 
        medicalInstitutions.add(newMedicalInstitution);
        
        List<Ticket> tickets = ticketMapper.findAllForInstitution(id);
        for (Ticket it : tickets) {
            try {
                newMedicalInstitution.addTicket(it);
            }   catch (NoRightsException ex) {
                Logger.getLogger(MedicalInstitutionMapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        List<Doctor> doctors = userMapper.findAllDoctors(id);
        for (Doctor it : doctors) {
            try {
                newMedicalInstitution.addDoctor(it);
            }   catch (NoRightsException ex) {
                Logger.getLogger(MedicalInstitutionMapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        List<Feedback> feedbacks = feedbackMapper.findAllForInstitution(id);
        for (Feedback it : feedbacks) {
            try {
                newMedicalInstitution.addFeedback(it);
            }   catch (NoRightsException ex) {
                Logger.getLogger(MedicalInstitutionMapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return newMedicalInstitution;
    }

    @Override
    public List<MedicalInstitution> findAll() throws SQLException {
        List<MedicalInstitution> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM institutions WHERE is_edu = 0;";
        Statement selectStatement = connection.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectSQL);

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }

    @Override
    public void update(MedicalInstitution item) throws SQLException {
        if (medicalInstitutions.contains(item)) {
            updateItem(item);
        } else {
            insertItem(item);
            medicalInstitutions.add(item);
        }
        
        for (Ticket it : item.getTickets()) {
            ticketMapper.update(it);
        }
        
        for (Feedback it : item.getFeedbacks()) {
            feedbackMapper.update(it);
        }
        
        for (Doctor it : item.getDoctors()) {
            userMapper.update(it);
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        userMapper.closeConnection();
        ticketMapper.closeConnection();
        feedbackMapper.closeConnection();
        connection.close();
    }

    @Override
    public void clear() {
        userMapper.clear();
        ticketMapper.clear();
        feedbackMapper.clear();
        medicalInstitutions.clear();
        cities.clear();
        districts.clear();
    }

    @Override
    public void update() throws SQLException {
        userMapper.update();
        ticketMapper.update();
        feedbackMapper.update();
        for (MedicalInstitution it : medicalInstitutions)
            update(it);
    }
    
    @Override
    public int getInstitutionType() {
        return 0; // is_edu must be 0
    }
}