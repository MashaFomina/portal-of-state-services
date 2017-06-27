package services.stateservices.storage.user;

import services.stateservices.storage.*;
import services.stateservices.storage.entities.*;
import services.stateservices.user.*;
import services.stateservices.entities.Notification;
import services.stateservices.storage.institutions.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.*;
import services.stateservices.entities.Child;
import services.stateservices.entities.EduRequest;
import services.stateservices.entities.Ticket;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.institutions.EducationalInstitution;

public class UserMapper implements UserMapperInterface<User> {
    private static Set<User> users = new HashSet<>();
    private static Connection connection;
    private static NotificationMapper ntfMapper;
    private static ChildMapper childMapper;
    private static TicketMapper ticketMapper;
    private static EduRequestMapper eduRequestMapper;
    private static EducationalInstitutionMapper educationalInstitutionMapper;
    private static MedicalInstitutionMapper medicalInstitutionMapper;
    
    private Map<User.UserType, String> selectSQLByLogin = new HashMap<User.UserType, String>(){{
       put(User.UserType.ADMINISTRATOR, "SELECT * FROM users WHERE user_type = 'ADMINISTRATOR' and login = ?;");
       put(User.UserType.CITIZEN, "SELECT * FROM users LEFT JOIN citizens ON id = user WHERE user_type = 'CITIZEN' and login = ?;");
       put(User.UserType.DOCTOR, "SELECT * FROM users LEFT JOIN doctors ON id = user WHERE user_type = 'DOCTOR' and login = ?;");
       put(User.UserType.EDUCATIONAL_REPRESENTATIVE, "SELECT * FROM users LEFT JOIN representatives ON id = user WHERE user_type = 'EDUCATIONAL_REPRESENTATIVE' and login = ?;");
       put(User.UserType.MEDICAL_REPRESENTATIVE, "SELECT * FROM users LEFT JOIN representatives ON id = user WHERE user_type = 'MEDICAL_REPRESENTATIVE' and login = ?;");
    }};
    
    private Map<User.UserType, String> selectSQLById = new HashMap<User.UserType, String>(){{
       put(User.UserType.ADMINISTRATOR, "SELECT * FROM users WHERE user_type = 'ADMINISTRATOR' and id = ?;");
       put(User.UserType.CITIZEN, "SELECT * FROM users LEFT JOIN citizens ON id = user WHERE user_type = 'CITIZEN' and id = ?;");
       put(User.UserType.DOCTOR, "SELECT * FROM users LEFT JOIN doctors ON id = user WHERE user_type = 'DOCTOR' and id = ?;");
       put(User.UserType.EDUCATIONAL_REPRESENTATIVE, "SELECT * FROM users LEFT JOIN representatives ON id = user WHERE user_type = 'EDUCATIONAL_REPRESENTATIVE' and id = ?;");
       put(User.UserType.MEDICAL_REPRESENTATIVE, "SELECT * FROM users LEFT JOIN representatives ON id = user WHERE user_type = 'MEDICAL_REPRESENTATIVE' and id = ?;");
    }};
        
    public UserMapper(EducationalInstitutionMapper educationalInstitutionMapper, MedicalInstitutionMapper medicalInstitutionMapper) throws SQLException, IOException {
        if (ntfMapper == null)
            ntfMapper = new NotificationMapper();
        if (childMapper == null)
            childMapper = new ChildMapper();
        if (ticketMapper == null)
            ticketMapper = new TicketMapper();
        if (eduRequestMapper == null)
            eduRequestMapper = new EduRequestMapper();
        if (connection == null)
            connection = Gateway.getInstance().getDataSource().getConnection();
        if (this.educationalInstitutionMapper == null)
            this.educationalInstitutionMapper = (educationalInstitutionMapper != null) ? educationalInstitutionMapper : new EducationalInstitutionMapper();
        if (this.medicalInstitutionMapper == null)
            this.medicalInstitutionMapper = (medicalInstitutionMapper != null) ? medicalInstitutionMapper : new MedicalInstitutionMapper();
    }

    @Override
    public User findByLogin(String login) throws SQLException {
        for (User it : users) {
            if (it.getLogin().equals(login))
                return it;
        }

        // User not found, extract from database
        String userSelectStatement = "SELECT * FROM users WHERE login = ?;";
        PreparedStatement extractUserStatement = connection.prepareStatement(userSelectStatement);
        extractUserStatement.setString(1, login);
        ResultSet rs = extractUserStatement.executeQuery();

        if (!rs.next()) return null;
        int id = rs.getInt("id");
        User.UserType userType = User.UserType.fromString(rs.getString("user_type"));

        User newUser = null;
        if (userType.equals(User.UserType.ADMINISTRATOR)) {
            String password = rs.getString("password");
            String fullName = rs.getString("full_name");
            String email = rs.getString("email");
            Administrator admin = new Administrator(login, password, fullName, email);
            admin.setId(id);
            newUser = admin;
        }
        else {
            String selectSQL = selectSQLByLogin.get(userType);
            PreparedStatement extractStatement;
            extractStatement = connection.prepareStatement(selectSQL);
            extractStatement.setString(1, login);
            ResultSet rs1 = extractStatement.executeQuery();
            if (!rs1.next()) return null;
            switch (userType) {
                case CITIZEN:
                    newUser = formCitizen(rs1);
                    break;
                case DOCTOR:
                    newUser = formDoctor(rs1);
                    break;
                case EDUCATIONAL_REPRESENTATIVE:
                case MEDICAL_REPRESENTATIVE:
                    newUser = formRepresentative(rs1);
                        break;
                default:
                    break;
            }
        }
        
        if (newUser != null)
        {
            users.add(newUser);
            List<Notification> notifications = ntfMapper.findAllForUser(id);
            newUser.setNotifications(notifications);
            for (Notification it : notifications)
                it.setOwner(newUser);
        }

        extractUserStatement.close();
        
        return newUser;
    }

    @Override
    public User findByID(int id) throws SQLException {
        for (User it : users) {
            if (it.getId() == id)
                return it;
        }

        // User not found, extract from database
        String userSelectStatement = "SELECT * FROM users WHERE id = ?;";
        PreparedStatement extractUserStatement = connection.prepareStatement(userSelectStatement);
        extractUserStatement.setInt(1, id);
        ResultSet rs = extractUserStatement.executeQuery();

        if (!rs.next()) return null;
        User.UserType userType = User.UserType.fromString(rs.getString("user_type"));

        User newUser = null;
        if (userType.equals(User.UserType.ADMINISTRATOR)) {
            String login = rs.getString("login");
            String password = rs.getString("password");
            String fullName = rs.getString("full_name");
            String email = rs.getString("email");
            Administrator admin = new Administrator(login, password, fullName, email);
            admin.setId(id);
            newUser = admin;
        }
        else {
            String selectSQL = selectSQLById.get(userType);
            PreparedStatement extractStatement;
            extractStatement = connection.prepareStatement(selectSQL);
            extractStatement.setInt(1, id);
            ResultSet rs1 = extractStatement.executeQuery();
            if (!rs1.next()) return null;
            switch (userType) {
                case CITIZEN:
                    newUser = formCitizen(rs1);
                    break;
                case DOCTOR:
                    newUser = formDoctor(rs1);
                    break;
                case EDUCATIONAL_REPRESENTATIVE:
                case MEDICAL_REPRESENTATIVE:
                    newUser = formRepresentative(rs1);
                        break;
                default:
                    break;
            }
        }
        
        if (newUser != null)
        {
            users.add(newUser);
            List<Notification> notifications = ntfMapper.findAllForUser(id);
            newUser.setNotifications(notifications);
            for (Notification it : notifications)
                it.setOwner(newUser);
        }

        extractUserStatement.close();

        return newUser;
    }

    @Override
    public List<User> findAll() throws SQLException {
        List<User> all = new ArrayList<>();

        String userSelectStatement = "SELECT id FROM users;";
        Statement extractUserStatement = connection.createStatement();
        ResultSet rs = extractUserStatement.executeQuery(userSelectStatement);

        User user;
        while (rs.next()) {
            user = findByID(rs.getInt("id"));
            if (user != null)
                all.add(user);
        }

        return all;
    }

    public List<Doctor> findAllDoctors(int institutionId) throws SQLException {
        List<Doctor> all = new ArrayList<>();

        String doctorSelectStatement = "SELECT user FROM doctors WHERE institution_id = ?;";
        PreparedStatement extractDoctorStatement = connection.prepareStatement(doctorSelectStatement);
        extractDoctorStatement.setInt(1, institutionId);
        ResultSet rs = extractDoctorStatement.executeQuery();

        User user;
        while (rs.next()) {
            user = findByID(rs.getInt("user"));
            if (user != null)
                all.add((Doctor) user);
        }

        return all;
    }

    private Doctor formDoctor(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String login = rs.getString("login");
        String password = rs.getString("password");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String position = rs.getString("position");
        String summary = rs.getString("summary");
        int institutionId = rs.getInt("institution_id");
        int approved = rs.getInt("approved");
        
        MedicalInstitution institution = medicalInstitutionMapper.findByID(institutionId);
        if (institution == null)
            return null;
        
        Doctor newDoctor = new Doctor(login, password, fullName, email, institution, position, summary, (approved == 1));
        newDoctor.setId(id);
        
        return newDoctor;
    }
        
    private Citizen formCitizen(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String login = rs.getString("login");
        String password = rs.getString("password");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        String policy = rs.getString("policy");
        String passport = rs.getString("passport");
        Timestamp timestamp = rs.getTimestamp("birth_date");
        java.util.Date birthDate = timestamp != null ? new java.util.Date(timestamp.getTime()) : null;
        
        Citizen newCitizen = new Citizen(login, password, fullName, email, policy, passport, birthDate);
        newCitizen.setId(id);
        
        List<Child> childs = childMapper.findAllForUser(id);
        for (Child it : childs) {
            it.setParent(newCitizen);
            newCitizen.addChild(it);
        }
        
        List<EduRequest> eduRequests = eduRequestMapper.findAllForUser(id);
        for (EduRequest it : eduRequests) {
            if (it.getInstitution() == null) {
                it.setInstitution(educationalInstitutionMapper.findByID(eduRequestMapper.getInstitutionId(it.getId())));
            }
                
            if (it.getParent() == null ) {
                it.setParent(newCitizen);
                if (it.getChild().getParent() == null) it.getChild().setParent(newCitizen);
            }
                
            if (it.getInstitution() != null ) {
                newCitizen.addEduRequest(it);
            }
        }
        
        List<Ticket> tickets = ticketMapper.findAllForUser(id);
        for (Ticket it : tickets) {
            it.setUser(newCitizen);
            if (it.getChild() != null) it.getChild().setParent(newCitizen);
            newCitizen.addTicket(it);
        }

        return newCitizen;
    }
    
    private User formRepresentative (ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String login = rs.getString("login");
        String password = rs.getString("password");
        String fullName = rs.getString("full_name");
        String email = rs.getString("email");
        int institutionId = rs.getInt("institution_id");
        int approved = rs.getInt("approved");
        User.UserType userType = User.UserType.fromString(rs.getString("user_type"));
        
        User representative = null;
        switch (userType) {
            case MEDICAL_REPRESENTATIVE:
                MedicalInstitution medicalInstitution = medicalInstitutionMapper.findByID(institutionId);
                if (medicalInstitution != null)
                    representative = new MedicalRepresentative(login, password, fullName, email, medicalInstitution, (approved == 1));
                break;
            case EDUCATIONAL_REPRESENTATIVE:
                EducationalInstitution educationalInstitution = educationalInstitutionMapper.findByID(institutionId);
                if (educationalInstitution != null)
                    representative = new EducationalRepresentative(login, password, fullName, email, educationalInstitution, (approved == 1));
                break;
        }
        
        if (representative != null)
            representative.setId(id);
        
        return representative;
    }
    
    @Override
    public void update(User item) throws SQLException {
        // user itself is immutable, he can only have new notifications, tickets, requests, childs
        if (!users.contains(item)) {
            switch (item.getUserType()) {
                case ADMINISTRATOR:
                    int id = insertUser(item);
                    if (id > 0) {
                        item.setId(id);
                        users.add(item);
                    }
                    break;
                case CITIZEN:
                    makeAddCitizenTransaction((Citizen) item);
                    break;
                case DOCTOR:
                    makeAddDoctorTransaction((Doctor) item);
                    break;
                case EDUCATIONAL_REPRESENTATIVE:
                case MEDICAL_REPRESENTATIVE:
                    makeAddRepresentativeTransaction(item);
                    break;
            }
        }
        
        for (Notification it : item.getNotifications()) {
            ntfMapper.update(it);
        }
        
        if (item.getUserType().equals(User.UserType.CITIZEN)) {
            Citizen citizen = (Citizen) item;
            for (Child it : citizen.getChilds().values()) {
                childMapper.update(it);
            }

            for (EduRequest it : citizen.getEduRequests()) {
                eduRequestMapper.update(it);
            }

            for (Ticket it : citizen.getTickets()) {
                ticketMapper.update(it);
            }
        }
    }
    
    private int insertUser(User item) throws SQLException {
        String generatedColumns[] = {"id"};
        String insertSQL = "INSERT INTO users(login, full_name, email, password, user_type) VALUES (?, ?, ?, ?, ?);";
        PreparedStatement insertStatement = connection.prepareStatement(insertSQL, generatedColumns);
        insertStatement.setString(1, item.getLogin());
        insertStatement.setString(2, item.getFullName());
        insertStatement.setString(3, item.getEmail());
        insertStatement.setString(4, item.getPassword());
        insertStatement.setString(5, item.getUserType().getText());
        insertStatement.execute();
        ResultSet rs = insertStatement.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }
    
    private void makeAddCitizenTransaction(Citizen item) throws SQLException {
        try {
            connection.setAutoCommit(false);
            int id = insertUser(item);
            if (id > 0) {
                item.setId(id);
                String insertSQL = "INSERT INTO citizens (user, policy, passport, birth_date) VALUES (?, ?, ?, ?);";
                PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
                insertStatement.setInt(1, id);
                insertStatement.setString(2, item.getPolicy());
                insertStatement.setString(3, item.getPassport());
                insertStatement.setTimestamp(4, new Timestamp(item.getBirthDate().getTime()));
                insertStatement.execute();
                users.add(item);
            } else {
                throw new Exception();
            }
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    private void makeAddDoctorTransaction(Doctor item) throws SQLException {
        try {
            connection.setAutoCommit(false);
            int id = insertUser(item);
            if (id > 0) {
                item.setId((int) id);
                String insertSQL = "INSERT INTO doctors (user, position, summary, institution_id, approved) VALUES (?, ?, ?, ?, ?);";
                PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
                insertStatement.setInt(1, id);
                insertStatement.setString(2, item.getPosition());
                insertStatement.setString(3, item.getSummary());
                insertStatement.setInt(4, item.getInstitution().getId());
                insertStatement.setInt(5, item.isApproved() ? 1 : 0);
                insertStatement.execute();
                users.add(item);
            } else {
                throw new Exception();
            }
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    private void makeAddRepresentativeTransaction(User user) throws SQLException {
        try {
            connection.setAutoCommit(false);
            int id = insertUser(user);
            if (id > 0) {
                user.setId(id);
                InstitutionRepresentative item = (user.getUserType().equals(User.UserType.MEDICAL_REPRESENTATIVE)) ? (MedicalRepresentative) user : (EducationalRepresentative) user;
                String insertSQL = "INSERT INTO representatives (user, institution_id, approved) VALUES (?, ?, ?);";
                PreparedStatement insertStatement = connection.prepareStatement(insertSQL);
                insertStatement.setInt(1, id);
                insertStatement.setInt(2, item.getInstitution().getId());
                insertStatement.setInt(3, item.isApproved() ? 1 : 0);
                insertStatement.execute();
                users.add(user);
            } else {
                throw new Exception();
            }
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
    }
    
    public void deleteDoctor(String login) throws SQLException {
        int doctorId = 0;
        for (User it : users) {
            if (it.getLogin().equals(login)) {
                doctorId = it.getId();
                users.remove(it);
                break;
            }
        }

        if (doctorId < 1) return;
        
        String deleteSQL = "DELETE FROM feedbacks WHERE user = ? or to_user = ?;";
        PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
        deleteStatement.setInt(1, doctorId);
        deleteStatement.setInt(2, doctorId);
        deleteStatement.execute();
        
        deleteSQL = "DELETE FROM tickets WHERE doctor = ?;";
        deleteStatement = connection.prepareStatement(deleteSQL);
        deleteStatement.setInt(1, doctorId);
        deleteStatement.execute();
        
        deleteSQL = "DELETE FROM doctors WHERE user = ?;";
        deleteStatement = connection.prepareStatement(deleteSQL);
        deleteStatement.setInt(1, doctorId);
        deleteStatement.execute();
        
        deleteSQL = "DELETE FROM users WHERE id = ?;";
        deleteStatement = connection.prepareStatement(deleteSQL);
        deleteStatement.setInt(1, doctorId);
        deleteStatement.execute();
        deleteStatement.close();
    }
    
    @Override
    public void closeConnection() throws SQLException {
        ntfMapper.closeConnection();
        childMapper.closeConnection();
        ticketMapper.closeConnection();
        eduRequestMapper.closeConnection();
        educationalInstitutionMapper.closeConnection();
        medicalInstitutionMapper.closeConnection();
        connection.close();
    }

    @Override
    public void clear() {
        ntfMapper.clear();
        childMapper.clear();
        ticketMapper.clear();
        eduRequestMapper.clear();
        educationalInstitutionMapper.clear();
        medicalInstitutionMapper.clear();
        users.clear();
    }

    @Override
    public void update() throws SQLException {
        ntfMapper.update();
        childMapper.update();
        ticketMapper.update();
        eduRequestMapper.update();
        educationalInstitutionMapper.update();
        medicalInstitutionMapper.update();
        for (User it : users)
            update(it);
    }
    
    public boolean authenticateUser(User user, String password) {
        return user.signIn(encryptPassword(password));
    }
        
    public static String encryptPassword(String password) {
        String sha1 = "";
        try
        {
            MessageDigest crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("UTF-8"));
            sha1 = byteToHex(crypt.digest());
        }
        catch(NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
        catch(UnsupportedEncodingException e)
        {
            e.printStackTrace();
        }
        return sha1;
    }

    protected static String byteToHex(final byte[] hash)
    {
        Formatter formatter = new Formatter();
        for (byte b : hash)
        {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }
}