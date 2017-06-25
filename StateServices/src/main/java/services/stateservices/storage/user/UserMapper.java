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

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }
        return all;
    }

    public List<Doctor> findAllDoctors(int institutionId) throws SQLException {
        List<Doctor> all = new ArrayList<>();

        String doctorSelectStatement = "SELECT user FROM doctors WHERE institution_id = ?;";
        PreparedStatement extractDoctorStatement = connection.prepareStatement(doctorSelectStatement);
        extractDoctorStatement.setInt(1, institutionId);
        ResultSet rs = extractDoctorStatement.executeQuery();

        while (rs.next()) {
            all.add((Doctor) findByID(rs.getInt("id")));
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
        
        MedicalInstitution institution = null; //medicalInstitutionMapper.findById(institutionId);
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
        java.util.Date birthDate = rs.getDate("birth_date");
        
        Citizen newCitizen = new Citizen(login, password, fullName, email, policy, passport, birthDate);
        newCitizen.setId(id);
        
        List<Child> childs = childMapper.findAllForUser(id);
        for (Child it : childs) {
            it.setParent(newCitizen);
            newCitizen.addChild(it);
        }
        
        List<EduRequest> eduRequests = eduRequestMapper.findAllForUser(id);
        for (EduRequest it : eduRequests) {
            newCitizen.addEduRequest(it);
        }
        
        List<Ticket> tickets = ticketMapper.findAllForUser(id);
        for (Ticket it : tickets) {
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
                MedicalInstitution medicalInstitution = null; //medicalInstitutionMapper.findById(institutionId);
                if (medicalInstitution != null)
                    representative = new MedicalRepresentative(login, password, fullName, email, medicalInstitution, (approved == 1));
                break;
            case EDUCATIONAL_REPRESENTATIVE:
                EducationalInstitution educationalInstitution = null; //educationalInstitutionMapper.findById(institutionId);
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
                    break;
                case CITIZEN:
                    makeAddCitizenTransaction((Citizen) item);
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
    
    private void makeAddCitizenTransaction(Citizen item) throws SQLException {
        try {
            connection.setAutoCommit(false);
            String generatedColumns[] = {"id"};
            String insertSQL = "INSERT INTO users(login, full_name, email, password, user_type) VALUES (?, ?, ?, SHA1(?), \"CITIZEN\");";
            PreparedStatement insertStatement = connection.prepareStatement(insertSQL, generatedColumns);
            insertStatement.setString(1, item.getLogin());
            insertStatement.setString(2, item.getFullName());
            insertStatement.setString(3, item.getEmail());
            insertStatement.setString(4, item.getPassword());
            insertStatement.execute();
            ResultSet rs = insertStatement.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                item.setId((int) id);
                insertSQL = "INSERT INTO citizens (user, policy, passport, birth_date) VALUES (?, ?, ?, ?);";
                PreparedStatement insertStatement1 = connection.prepareStatement(insertSQL);
                insertStatement1.setInt(1, id);
                insertStatement1.setString(2, item.getPolicy());
                insertStatement1.setString(3, item.getPassport());
                insertStatement1.setDate(4, new java.sql.Date(item.getBirthDate().getTime()));
                insertStatement1.execute();
            } else {
                throw new Exception();
            }
            users.add(item);
            connection.commit();
        } catch (Exception e) {
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
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
        
    protected static String encryptPassword(String password) {
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