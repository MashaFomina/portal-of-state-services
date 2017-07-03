package services.stateservices.storage.institutions;

import java.io.IOException;
import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import services.stateservices.entities.Child;
import services.stateservices.entities.EduRequest;
import services.stateservices.entities.Feedback;
import services.stateservices.entities.Ticket;
import services.stateservices.errors.NoRightsException;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.storage.Gateway;
import services.stateservices.storage.Mapper;
import services.stateservices.storage.StorageRepository;
import services.stateservices.storage.entities.*;
import services.stateservices.storage.user.UserMapper;
import services.stateservices.user.Citizen;
import services.stateservices.user.Doctor;
import services.stateservices.user.User;

public class MedicalInstitutionMapper extends InstitutionMapper implements Mapper<MedicalInstitution> {

    private static Set<MedicalInstitution> medicalInstitutions = new HashSet<>();
    private static StorageRepository repository = null;
    private static TicketMapper ticketMapper;
    private static FeedbackMapper feedbackMapper;

    public MedicalInstitutionMapper() throws IOException, SQLException {
        if (connection == null) {
            connection = Gateway.getInstance().getDataSource().getConnection();
        }
        if (repository == null) {
            repository = StorageRepository.getInstance();
        }
        if (ticketMapper == null) {
            ticketMapper = new TicketMapper();
        }
        if (feedbackMapper == null) {
            feedbackMapper = new FeedbackMapper();
        }
    }

    public boolean canAddFeedback(int userId, int institutionId) throws SQLException {
        String selectSQL = "SELECT count(*) as count FROM tickets WHERE user = ? and institution_id = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, userId);
        selectStatement.setInt(2, institutionId);
        ResultSet rs = selectStatement.executeQuery();

        if (rs.next() && rs.getInt("count") > 0) {
            return true;
        }
        selectStatement.close();
        return false;
    }

    public List<MedicalInstitution> findAllByDistrict(String city, String district) throws SQLException {
        List<MedicalInstitution> all = new ArrayList<>();

        String selectSQL = "SELECT * FROM institutions WHERE city = ? and district = ? and is_edu = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setString(1, city);
        selectStatement.setString(2, district);
        selectStatement.setInt(3, getInstitutionType());
        ResultSet rs = selectStatement.executeQuery();

        MedicalInstitution institution;
        while (rs.next()) {
            institution = findByID(rs.getInt("id"));
            if (institution != null) {
                all.add(institution);
            }
        }

        selectStatement.close();

        return all;
    }

    @Override
    public MedicalInstitution findByID(int id) throws SQLException {
        return findByID(id, false);
    }
    
    public MedicalInstitution findByID(int id, boolean refresh) throws SQLException {
        MedicalInstitution medicalInstitution = null;

        for (MedicalInstitution it : medicalInstitutions) {
            if (it.getId() == id) {
                medicalInstitution = it;
                if (!refresh) {
                    return it;
                }
                break;
            }
        }

        String selectSQL = "SELECT * FROM institutions WHERE id = ? and is_edu = 0;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, id);
        ResultSet rs = selectStatement.executeQuery();

        if (!rs.next()) {
            return null;
        }

        int mid = rs.getInt("id");
        String title = rs.getString("title");
        String city = rs.getString("city");
        String district = rs.getString("district");
        String telephone = rs.getString("telephone");
        String fax = rs.getString("fax");
        String address = rs.getString("address");

        selectStatement.close();

        if (medicalInstitution == null) {
            medicalInstitution = new MedicalInstitution(title, city, district, telephone, fax, address);
            medicalInstitution.setId(mid);
            medicalInstitutions.add(medicalInstitution);
        } else {
            medicalInstitution.edit(title, city, district, telephone, fax, address);
            medicalInstitution.resetUpdated();
        }

        List<Ticket> tickets = ticketMapper.findAllForInstitution(id);
        Iterator<Ticket> i = tickets.iterator();
        while (i.hasNext()) {
            Ticket it = i.next(); // must be called before you can call i.remove()
            Integer userId = ticketMapper.getUserId(it.getId());
            if (userId > 0 && it.getUser() == null) {
                Citizen citizen = repository.getCitizen(userId);
                it.setUser(citizen);
                if (it.getChild() != null) {
                    it.getChild().setParent(citizen);
                }
            }

            if (userId > 0 && it.getUser() == null) {
                i.remove();
            }
        }
        medicalInstitution.setTickets(tickets);

        List<Doctor> doctors = repository.findAllDoctors(medicalInstitution);
        medicalInstitution.setDoctors(doctors);

        List<Feedback> feedbacks = feedbackMapper.findAllForInstitution(id);
        medicalInstitution.setFeedbacks(feedbacks);

        return medicalInstitution;
    }

    @Override
    public List<MedicalInstitution> findAll() throws SQLException {
        List<MedicalInstitution> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM institutions WHERE is_edu = 0;";
        Statement selectStatement = connection.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectSQL);

        MedicalInstitution institution;
        while (rs.next()) {
            institution = findByID(rs.getInt("id"));
            if (institution != null) {
                all.add(institution);
            }
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
            repository.updateUser(it);
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        ticketMapper.closeConnection();
        feedbackMapper.closeConnection();
        connection.close();
    }

    @Override
    public void clear() {
        ticketMapper.clear();
        feedbackMapper.clear();
        medicalInstitutions.clear();
        cities.clear();
        districts.clear();
    }

    @Override
    public void update() throws SQLException {
        ticketMapper.update();
        feedbackMapper.update();
        for (MedicalInstitution it : medicalInstitutions) {
            update(it);
        }
    }

    @Override
    public int getInstitutionType() {
        return 0; // is_edu must be 0
    }
}
