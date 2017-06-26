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

    private static Set<Ticket> tickets;
    private static Connection connection;
    private static ChildMapper childMapper;
    private static Map<Integer, Integer> userIds;
    private static Map<Integer, Integer> institutionIds;
    private static StorageRepository repository = null;

    public TicketMapper() throws IOException, SQLException {
        if (connection == null) 
            connection = Gateway.getInstance().getDataSource().getConnection();
        if (childMapper == null)
            childMapper = new ChildMapper();
        if (repository == null)
            repository = StorageRepository.getInstance();
        if (tickets == null)
            tickets = new HashSet<>();
        if (userIds == null)
            userIds = new HashMap<>();
        if (institutionIds == null) 
            institutionIds = new HashMap<>();
    }

    public Integer getUserId(int eduRequestId) {
        return userIds.get(eduRequestId);
    }
    
    public Integer getInstitutionId(int eduRequestId) {
        return institutionIds.get(eduRequestId);
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
        String selectSQL = "SELECT id FROM tickets WHERE institution_id = ? and ticket_date > ? ORDER BY id DESC;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, institution);
        selectStatement.setDate(2, java.sql.Date.valueOf(monthAgo));
        ResultSet rs = selectStatement.executeQuery();

        Ticket ticket;
        while (rs.next()) {
            ticket = findByID(rs.getInt("id"));
            if (ticket != null)
                all.add(ticket);
        }

        selectStatement.close();
        
        return all;
    }

    public List<Ticket> findAllForUser(int user) throws SQLException {
        List<Ticket> all = new ArrayList<>();

        String selectSQL = "SELECT id FROM tickets WHERE user = ? ORDER BY id DESC;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, user);
        ResultSet rs = selectStatement.executeQuery();

        Ticket ticket;
        while (rs.next()) {
            ticket = findByID(rs.getInt("id"));
            if (ticket != null)
                all.add(ticket);
        }

        selectStatement.close();
        
        return all;
    }
    
    // For doctor get tickets not older than month ago
    public List<Ticket> findAllForDoctor(int doctor) throws SQLException {
        List<Ticket> all = new ArrayList<>();

        LocalDate currentDate = LocalDate.now();
        LocalDate monthAgo = currentDate.minus(1, ChronoUnit.MONTHS);
        String selectSQL = "SELECT id FROM tickets WHERE doctor = ? and ticket_date > ? ORDER BY id DESC;";
        PreparedStatement selectStatement = connection.prepareStatement(selectSQL);
        selectStatement.setInt(1, doctor);
        selectStatement.setDate(2, java.sql.Date.valueOf(monthAgo));
        ResultSet rs = selectStatement.executeQuery();

        Ticket ticket;
        while (rs.next()) {
            ticket = findByID(rs.getInt("id"));
            if (ticket != null)
                all.add(ticket);
        }

        selectStatement.close();
        
        return all;
    }
        
    @Override
    // In other mapper we must set institution and user
    public Ticket findByID(int id) throws SQLException {
        for (Ticket it : tickets) {
            if (it.getId() == id) return it;
        }

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
        Timestamp timestamp = rs.getTimestamp("ticket_date");
        Date ticketDate = timestamp != null ? new Date(timestamp.getTime()) : null;
        int visited = rs.getInt("visited");
        String summary = rs.getString("summary");
        
        selectStatement.close();

        Ticket newTicket = null;
        User userDoctor = repository.getUser(doctorId);
        // To avoid recursion we must set institution and user in other mappers
        if (userDoctor != null && userDoctor.isDoctor()) {
            Doctor doctor = (Doctor) userDoctor;
            if (userId < 0) {
                newTicket = new Ticket(doctor, ticketDate);
            }
            else {
                Child child = childMapper.findByID(childId);
                Citizen citizen = null;
                userIds.put(tid, userId);
                institutionIds.put(tid, institutionId);
                newTicket = (childId < 1) ? new Ticket(doctor, ticketDate, (visited == 1), citizen, summary) : ((child != null) ? new Ticket(doctor, ticketDate, (visited == 1), citizen, child, summary): null);
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

        String selectSQL = "SELECT id FROM tickets ORDER BY id DESC;";
        Statement selectStatement = connection.createStatement();
        ResultSet rs = selectStatement.executeQuery(selectSQL);

        Ticket ticket;
        while (rs.next()) {
            ticket = findByID(rs.getInt("id"));
            if (ticket != null)
                all.add(ticket);
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
            if (item.isUpdated()) {
                String updateSQL = "UPDATE tickets SET user = ?, child = ?, visited = ?, summary = ? WHERE id = ?;";
                PreparedStatement updateStatement = connection.prepareStatement(updateSQL);
                Citizen citizen = item.getUser();
                Child child = item.getChild();
                
                if (citizen == null) {
                    updateStatement.setNull(1, java.sql.Types.INTEGER);
                }
                else {
                    updateStatement.setInt(1, citizen.getId());
                }
                
                if (child == null) {
                    updateStatement.setNull(2, java.sql.Types.INTEGER);
                }
                else {
                    updateStatement.setInt(2, child.getId());
                }
                
                updateStatement.setInt(3, item.isVisited() ? 1 : 0);
                updateStatement.setString(4, item.getSummary());
                updateStatement.setInt(5, item.getId());
                updateStatement.execute();
                item.resetUpdated();
            }
        } else {
            String insertSQL = "INSERT INTO tickets (user, child, institution_id, doctor, ticket_date, visited, summary) VALUES (?, ?, ?, ?, ?, ?, ?);";
            PreparedStatement insertStatement = connection.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS);
            Citizen citizen = item.getUser();
            Child child = item.getChild();
            
            if (citizen == null) {
                insertStatement.setNull(1, java.sql.Types.INTEGER);
            }
            else {
                insertStatement.setInt(1, citizen.getId());
            }
            
            if (child == null) {
                insertStatement.setNull(2, java.sql.Types.INTEGER);
            }
            else {
                insertStatement.setInt(2, child.getId() );
            }
            
            insertStatement.setInt(3, item.getInstitution().getId());
            insertStatement.setInt(4, item.getDoctor().getId());
            insertStatement.setTimestamp(5, new Timestamp(item.getDate().getTime()));
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