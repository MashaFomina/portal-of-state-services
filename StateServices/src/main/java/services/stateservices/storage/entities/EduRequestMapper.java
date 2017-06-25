package services.stateservices.storage.entities;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import services.stateservices.entities.Child;
import services.stateservices.entities.EduRequest;
import services.stateservices.institutions.EducationalInstitution;
import services.stateservices.institutions.Institution;
import services.stateservices.storage.Gateway;
import services.stateservices.storage.Mapper;
import services.stateservices.storage.StorageRepository;
import services.stateservices.storage.institutions.EducationalInstitutionMapper;
import services.stateservices.storage.user.UserMapper;
import services.stateservices.user.Citizen;
import services.stateservices.user.Doctor;
import services.stateservices.user.User;

public class EduRequestMapper implements Mapper<EduRequest> {

    private static Set<EduRequest> eduRequests = new HashSet<>();
    private static Connection connection;
    /*private static EducationalInstitutionMapper educationalInstitutionMapper;
    private static UserMapper userMapper;*/
    private static ChildMapper childMapper;
    private static StorageRepository repository = null;

    public EduRequestMapper() throws IOException, SQLException {
        if (connection == null)
            connection = Gateway.getInstance().getDataSource().getConnection();
        if (educationalInstitutionMapper == null)
            educationalInstitutionMapper = new EducationalInstitutionMapper();
        if (userMapper == null)
            userMapper = new UserMapper(educationalInstitutionMapper, null);
        if (repository == null) {System.out.println("Wow!");
            repository = StorageRepository.getInstance();
            if (repository == null) {System.out.println("Wow999!");}
        }
        System.out.println("Here!");
        if (childMapper == null)
            childMapper = new ChildMapper();
    }

    public List<EduRequest> getForInstitution(int institution) throws SQLException {
        List<EduRequest> all = new ArrayList<>();

        for (EduRequest it : eduRequests) {
            if (it.getInstitution().getId() == institution)
                all.add(it);
        }
        
        return all;
    }
        
    public List<EduRequest> getForUser(int user) throws SQLException {
        List<EduRequest> all = new ArrayList<>();

        for (EduRequest it : eduRequests) {
            if (it.getParent().getId() == user)
                all.add(it);
        }
        
        return all;
    }
    
    public List<EduRequest> findAllForInstitution(int institution) throws SQLException {
        List<EduRequest> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM edu_requests WHERE institution_id = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, institution);
        ResultSet rs = selectStatement.executeQuery();

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }

    public List<EduRequest> findAllForUser(int user) throws SQLException {
        List<EduRequest> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM edu_requests WHERE parent = ?;";
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
    public EduRequest findByID(int id) throws SQLException {
        for (EduRequest it : eduRequests)
            if (it.getId() == id) return it;

        String selectSQL = "SELECT * FROM edu_requests WHERE id = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, id);
        ResultSet rs = selectStatement.executeQuery();

        if (!rs.next()) return null;

        int rid = rs.getInt("id");
        EduRequest.Status status = EduRequest.Status.fromString(rs.getString("status"));
        int childId = rs.getInt("child"); 
        int parentId = rs.getInt("parent"); 
        int institutionId = rs.getInt("institution_id");
        Date creationDate = rs.getDate("creation_date");
        Date appointmentDate = rs.getDate("appointment");
        int classNumber = rs.getInt("class_number");

        selectStatement.close();

        EduRequest newEduRequest = null;
        User parent = userMapper.findByID(parentId);
        Child child = childMapper.findByID(childId);
        EducationalInstitution institution = educationalInstitutionMapper.findByID(institutionId);
        if (parent != null && parent.isCitizen() && child != null && institution != null) {
            Citizen citizen = (Citizen) parent;
            if (citizen.getChilds().containsKey(child.getBirthCertificate())) {
                newEduRequest = new EduRequest(status, child, citizen, institution, creationDate, appointmentDate, classNumber);
            }   
        }
        
        if (newEduRequest != null) {
            newEduRequest.setId(rid); 
            eduRequests.add(newEduRequest);
        }
        
        return newEduRequest;
    }

    @Override
    public List<EduRequest> findAll() throws SQLException {
        List<EduRequest> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM edu_requests;";
        Statement selectStatement = connection.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectSQL);

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }

    public void delete(EduRequest item) throws SQLException {
        eduRequests.remove(item);
        String deleteSQL = "DELETE FROM edu_requests WHERE id = ? LIMIT 1;";
        PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
        deleteStatement.setInt(1, item.getId());
        deleteStatement.execute();
        deleteStatement.close();
    }
        
    @Override
    public void update(EduRequest item) throws SQLException {
        if (eduRequests.contains(item)) {
            // eduRequest object is immutable, don't need to update
        } else {
            String insertSQL = "INSERT INTO edu_requests (status, child, parent, institution_id, creation_date, appointment, class_number) VALUES (?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            insertStatement.setString(1, item.getStatus().getText());
            insertStatement.setInt(2, item.getChild().getId());
            insertStatement.setInt(3, item.getParent().getId());
            insertStatement.setInt(4, item.getInstitution().getId());
            insertStatement.setDate(5, new java.sql.Date(item.getCreationDate().getTime()));
            Date appointment = item.getAppointment();
            if (appointment != null) {
                insertStatement.setDate(6, new java.sql.Date(appointment.getTime()));
            }
            else {
                insertStatement.setNull(6, java.sql.Types.DATE);
            }
            insertStatement.setInt(7, item.getClassNumber());
            insertStatement.execute();
            ResultSet rs = insertStatement.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                item.setId((int) id);
            }
            eduRequests.add(item);
            insertStatement.close();
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        educationalInstitutionMapper.closeConnection();
        userMapper.closeConnection();
        childMapper.closeConnection();
        connection.close();
    }

    @Override
    public void clear() {
        educationalInstitutionMapper.clear();
        userMapper.clear();
        childMapper.clear();
        eduRequests.clear();
    }

    @Override
    public void update() throws SQLException {
        for (EduRequest it : eduRequests)
            update(it);
    }
}
