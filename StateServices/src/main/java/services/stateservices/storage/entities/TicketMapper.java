package services.stateservices.storage.entities;

import java.io.IOException;
import java.sql.*;
import java.time.chrono.MinguoEra;
import java.util.*;
import java.util.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;
import services.stateservices.entities.Child;
import services.stateservices.entities.Ticket;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.storage.Gateway;
import services.stateservices.storage.institutions.EducationalInstitutionMapper;
import services.stateservices.storage.institutions.MedicalInstitutionMapper;
import services.stateservices.storage.user.UserMapper;
import services.stateservices.user.User;
import services.stateservices.storage.Mapper;
import services.stateservices.storage.StorageRepository;
import services.stateservices.user.Citizen;
import services.stateservices.user.Doctor;

public class TicketMapper implements Mapper<Ticket> {

    private static Set<Ticket> tickets = new HashSet<>();
    private static Connection connection;
    //private static MedicalInstitutionMapper medicalInstitutionMapper;
    //private static UserMapper userMapper;
    private static ChildMapper childMapper;
    private static StorageRepository repository = null;

    public TicketMapper() throws IOException, SQLException {
        if (connection == null) {System.out.println("Wow111!");
            connection = Gateway.getInstance().getDataSource().getConnection();}
        if (repository == null) {System.out.println("Wow!");
            repository = StorageRepository.getInstance();
            if (repository == null) {System.out.println("Wow999!");}
        }
        System.out.println("Here!");
        /*if (medicalInstitutionMapper == null)
            medicalInstitutionMapper = new MedicalInstitutionMapper();
        if (userMapper == null)
            userMapper = new UserMapper(null, medicalInstitutionMapper);*/
        if (childMapper == null) {System.out.println("Wow10000!");
            childMapper = new ChildMapper();}
    }

    public List<Ticket> getForInstitution(int institution) throws SQLException {
        List<Ticket> all = new ArrayList<>();

        for (Ticket it : tickets) {
            if (it.getInstitution().getId() == institution)
                all.add(it);
        }
        
        return all;
    }
        
    public List<Ticket> getForUser(int user) throws SQLException {
        List<Ticket> all = new ArrayList<>();

        for (Ticket it : tickets) {
            if (it.getUser().getId() == user)
                all.add(it);
        }
        
        return all;
    }
     
    public List<Ticket> getForDoctor(int doctor) throws SQLException {
        List<Ticket> all = new ArrayList<>();

        for (Ticket it : tickets) {
            if (it.getDoctor().getId() == doctor)
                all.add(it);
        }
        
        return all;
    }
    
    // For institution get tickets not older than month ago
    public List<Ticket> findAllForInstitution(int institution) throws SQLException {
        List<Ticket> all = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        LocalDate monthAgo = currentDate.minus(1, ChronoUnit.MONTHS);
        String selectSQL = "SELECT id FROM tickets WHERE institution_id = ? and ticket_date > ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, institution);
        selectStatement.setDate(2, java.sql.Date.valueOf(monthAgo));
        ResultSet rs = selectStatement.executeQuery();

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }

    public List<Ticket> findAllForUser(int user) throws SQLException {
        List<Ticket> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM tickets WHERE user = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, user);
        ResultSet rs = selectStatement.executeQuery();

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }
    
    // For doctor get tickets not older than month ago
    public List<Ticket> findAllForDoctor(int doctor) throws SQLException {
        List<Ticket> all = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        LocalDate monthAgo = currentDate.minus(1, ChronoUnit.MONTHS);
        String selectSQL = "SELECT id FROM tickets WHERE doctor = ? and ticket_date > ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, doctor);
        selectStatement.setDate(2, java.sql.Date.valueOf(monthAgo));
        ResultSet rs = selectStatement.executeQuery();

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }
        
    @Override
    public Ticket findByID(int id) throws SQLException {
        for (Ticket it : tickets)
            if (it.getId() == id) return it;

        String selectSQL = "SELECT * FROM tickets WHERE id = ?;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, id);
        ResultSet rs = selectStatement.executeQuery();

        if (!rs.next()) return null;

        int tid = rs.getInt("id");
        int userId = rs.getInt("user");
        int childId = rs.getInt("child"); 
        int institutionId = rs.getInt("institution_id");
        int doctorId = rs.getInt("doctor");
        Date ticketDate = rs.getDate("ticket_date");
        int visited = rs.getInt("visited");
        String summary = rs.getString("summary");
        
        selectStatement.close();

        Ticket newTicket = null;
        User userDoctor = repository.getUser(doctorId);
        MedicalInstitution institution = repository.getMedicalInstitution(institutionId);
        if (userDoctor != null && userDoctor.isDoctor() && institution != null) {
            Doctor doctor = (Doctor) userDoctor;
            if (userId < 0) {
                newTicket = new Ticket(doctor, ticketDate);
            }
            else {
                User user = repository.getUser(userId);
                Child child = childMapper.findByID(childId);
                if (user != null && user.isCitizen()) {
                    Citizen citizen = (Citizen) user;
                    newTicket = (childId < 0) ? new Ticket(doctor, ticketDate, (visited == 1), citizen, summary) : ((child != null && citizen.getChilds().containsKey(child.getBirthCertificate())) ? new Ticket(doctor, ticketDate, (visited == 1), citizen, child, summary): null);
                }
            }   
        }
        
        if (newTicket != null) {
            newTicket.setId(tid); 
            tickets.add(newTicket);
        }
        
        return newTicket;
    }

    @Override
    public List<Ticket> findAll() throws SQLException {
        List<Ticket> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM tickets;";
        Statement selectStatement = connection.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectSQL);

        while (rs.next()) {
            all.add(findByID(rs.getInt("id")));
        }

        selectStatement.close();
        
        return all;
    }

    public void delete(Ticket item) throws SQLException {
        tickets.remove(item);
        String deleteSQL = "DELETE FROM tickets WHERE id = ? LIMIT 1;";
        PreparedStatement deleteStatement = connection.prepareStatement(deleteSQL);
        deleteStatement.setInt(1, item.getId());
        deleteStatement.execute();
        deleteStatement.close();
    }
        
    @Override
    public void update(Ticket item) throws SQLException {
        if (tickets.contains(item)) {
            // ticket object is immutable, don't need to update
        } else {
            String insertSQL = "INSERT INTO tickets (user, child, institution_id, doctor, ticket_date, visited, summary) VALUES (?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            Citizen citizen = item.getUser();
            Child child = item.getChild();
            insertStatement.setInt(1, (citizen != null) ? citizen.getId() : 0);
            insertStatement.setInt(2, (child != null) ? child.getId() : 0);
            insertStatement.setInt(3, item.getInstitution().getId());
            insertStatement.setInt(4, item.getDoctor().getId());
            insertStatement.setDate(5, new java.sql.Date(item.getDate().getTime()));
            insertStatement.setInt(6, item.isVisited() ? 1 : 0);
            insertStatement.setString(7, item.getSummary());
            insertStatement.execute();
            ResultSet rs = insertStatement.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                item.setId((int) id);
            }
            tickets.add(item);
            insertStatement.close();
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        childMapper.closeConnection();
        connection.close();
    }

    @Override
    public void clear() {
        childMapper.clear();
        tickets.clear();
    }

    @Override
    public void update() throws SQLException {
        for (Ticket it : tickets)
            update(it);
    }
}