package services.stateservices.storage;


import java.util.Date;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import services.stateservices.user.User;
import services.stateservices.user.EducationalRepresentative;
import services.stateservices.user.MedicalRepresentative;
import services.stateservices.user.Doctor;
import services.stateservices.user.Administrator;
import services.stateservices.user.Citizen;
import services.stateservices.entities.*;
import services.stateservices.institutions.*;
import services.stateservices.storage.user.*;
import services.stateservices.storage.entities.*;
import services.stateservices.storage.institutions.*;
import services.stateservices.errors.AlreadyExistsException;

public class StorageRepository {
    private static UserMapper userMapper;
    private static EducationalInstitutionMapper educationalInstitutionMapper;
    private static MedicalInstitutionMapper medicalInstitutionMapper;
    private static StorageRepository instance;
    private static ChildMapper childMapper;
    private static TicketMapper ticketMapper;
    private static EduRequestMapper eduRequestMapper;

    private StorageRepository() {}

    public  static StorageRepository getInstance() {
        if (instance == null) {
            instance = new StorageRepository();
            try {
                if (childMapper == null) childMapper = new ChildMapper();
                if (ticketMapper == null) ticketMapper = new TicketMapper();
                if (eduRequestMapper == null) eduRequestMapper = new EduRequestMapper();
                if (educationalInstitutionMapper == null) educationalInstitutionMapper = new EducationalInstitutionMapper();
                if (medicalInstitutionMapper == null) medicalInstitutionMapper = new MedicalInstitutionMapper();
                if (userMapper == null) userMapper = new UserMapper(educationalInstitutionMapper, medicalInstitutionMapper);
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return instance;
    }
    
    public Child getChild(int id) throws SQLException {
        return childMapper.findByID(id);
    }
    
    public Ticket getTicket(int id) throws SQLException {
        Ticket ticket = ticketMapper.findByID(id);
        Integer userId = ticketMapper.getUserId(ticket.getId());
        if (userId > 0 && ticket.getUser() == null) {
            Citizen citizen = getCitizen(userId);
            ticket.setUser(citizen);
            if (ticket.getChild() != null) {
                ticket.getChild().setParent(citizen);
            }
        }
        return ticket;
    }
        
    public EduRequest getEduRequest(int id) throws SQLException {
        return eduRequestMapper.findByID(id);
    }
    
    public void removeEduRequest(EduRequest request) throws SQLException {
        eduRequestMapper.delete(request);
    }
        
    public void removeChild(Child child) throws SQLException {
        childMapper.delete(child);
    }
    
    public boolean addUser(User user) throws AlreadyExistsException {
        try {
            if (userMapper.findByLogin(user.getLogin()) != null) throw new AlreadyExistsException("User with login " + user.getLogin() + " already exists");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
  
        try {
            userMapper.update(user);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    public void updateEducationalInstitution(EducationalInstitution institution) throws SQLException {
        educationalInstitutionMapper.update(institution);
    }
 
    public void updateMedicalInstitution(MedicalInstitution institution) throws SQLException {
        medicalInstitutionMapper.update(institution);
    }
        
    public EducationalInstitution addEducationalInstitution(String title, String city, String district, String telephone, String fax, String address, Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        try {
            EducationalInstitution institution = new EducationalInstitution(title, city, district, telephone, fax, address, seats, busySeats);
            educationalInstitutionMapper.update(institution);
            return institution;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public EducationalInstitution addEducationalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        try {
            EducationalInstitution institution = new EducationalInstitution(title, city, district, telephone, fax, address);
            educationalInstitutionMapper.update(institution);
            return institution;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<EducationalInstitution> getEducationalInstitutions(String city, String district) {
        try {
            return educationalInstitutionMapper.findAllByDistrict(city, district);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<String> getCities(boolean isEdu) {
        List<String> cities = new ArrayList<>();
        try {
            cities = isEdu ? educationalInstitutionMapper.getCities() : medicalInstitutionMapper.getCities();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return cities;
    }
    
    public boolean canAddFeedbackToMedicalInstitution(User user, MedicalInstitution institution) {
        boolean result = false; //in local lists of institution are represented tickets during month, so we make new request to database to sure that count of tickets visited by user is more than zero
        try {
            result = medicalInstitutionMapper.canAddFeedback(user.getId(), institution.getId());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
    
    public List<String> getDistricts(String city, boolean isEdu) {
        List<String> districts = new ArrayList<>();
        try {
            districts = isEdu ? educationalInstitutionMapper.getCityDistricts(city) : medicalInstitutionMapper.getCityDistricts(city);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return districts;
    }
    
    public List<MedicalInstitution> getMedicalInstitutions(String city, String district) {
        try {
            return medicalInstitutionMapper.findAllByDistrict(city, district);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
        
    public MedicalInstitution addMedicalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        try {
            MedicalInstitution institution = new MedicalInstitution(title, city, district, telephone, fax, address);
            medicalInstitutionMapper.update(institution);
            return institution;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public EducationalInstitution getEducationalInstitution(int id, boolean refresh) {
        try {
            return educationalInstitutionMapper.findByID(id, refresh);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
        
    public MedicalInstitution getMedicalInstitution(int id, boolean refresh) {
        try {
            return medicalInstitutionMapper.findByID(id, refresh);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public EducationalInstitution getEducationalInstitution(int id) {
        try {
            return educationalInstitutionMapper.findByID(id, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
        
    public MedicalInstitution getMedicalInstitution(int id) {
        try {
            return medicalInstitutionMapper.findByID(id, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User getUser(String login) {
        try {
            return userMapper.findByLogin(login);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public User getUser(int id) {
        try {
            return userMapper.findByID(id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public EducationalRepresentative getEducationalRepresentative(String login) {
        try {
            User user = userMapper.findByLogin(login);
            if (user != null && user.isEducationalRepresentative())  {
                return (EducationalRepresentative) user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public MedicalRepresentative getMedicalRepresentative(String login) {
        try {
            User user = userMapper.findByLogin(login);
            if (user != null && user.isMedicalRepresentative())  {
                return (MedicalRepresentative) user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Doctor getDoctor(String login) {
        try {
            User user = userMapper.findByLogin(login);
            if (user != null && user.isDoctor())  {
                return (Doctor) user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Doctor getDoctor(int id) {
        try {
            User user = userMapper.findByID(id);
            if (user != null && user.isDoctor())  {
                return (Doctor) user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public List<Doctor> findAllDoctors(MedicalInstitution institution) {
        try {
            List<Doctor> doctors = userMapper.findAllDoctors(institution.getId());
            // To avoid recursion
            Iterator<Doctor> i = doctors.iterator();
            while (i.hasNext()) {
                Doctor it = i.next();
                if (it.getInstitution() == null) {
                    it.setInstitution(institution);
                }
            }
            return doctors;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
        
    }
    
    public void updateUser(User user) {
        try {
            userMapper.update(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }     
    }
    
    public void updateTicket(Ticket ticket) {
        try {
            ticketMapper.update(ticket);
        } catch (SQLException e) {
            e.printStackTrace();
        }     
    }
        
    public void removeDoctor(String login) throws SQLException {
        userMapper.deleteDoctor(login);
    }

    public void removeTicket(Ticket ticket) throws SQLException {
        ticketMapper.delete(ticket);
    }

    public void removeTickets(Doctor doctor, Date ticketsDate) throws SQLException {
        ticketMapper.delete(doctor, ticketsDate);
    }
    
    public Administrator getAdministrator(String login) {
        try {
            User user = userMapper.findByLogin(login);
            if (user != null && user.isAdministrator())  {
                return (Administrator) user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public Citizen getCitizen(String login) {
        try {
            User user = userMapper.findByLogin(login);
            if (user != null && user.isCitizen())  {
                return (Citizen) user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }  
    
    public Citizen getCitizen(int id) {
        try {
            User user = userMapper.findByID(id);
            if (user != null && user.isCitizen())  {
                return (Citizen) user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }  

    public boolean authenticateUser(String login, String password) {
        User user = getUser(login);
        return ((user != null) ? userMapper.authenticateUser(user, password) : false);
    }

    public boolean authenticateUser(User user, String password) {
        return userMapper.authenticateUser(user, password);
    }
    
    public void deleteEduRequestsForChild(Child child) throws SQLException {
        eduRequestMapper.deleteEduRequestsForChild(child);
    }
    
    public void cancelTicketsForChild(Child child) throws SQLException {
        ticketMapper.cancelTicketsForChild(child);
    }

    public void signOut (String login) {
        User user = getUser(login);
        if (user != null) {
            user.signOut();
        }
    }
    
    public void clear() {
        ticketMapper.clear();
        childMapper.clear();
        eduRequestMapper.clear();
        educationalInstitutionMapper.clear();
        medicalInstitutionMapper.clear();
        userMapper.clear();
        eduRequestMapper = null;
        ticketMapper = null;
        childMapper = null;
        educationalInstitutionMapper = null;
        medicalInstitutionMapper = null;
        userMapper = null;
        instance = null;
    }

    public void update() throws SQLException {
        educationalInstitutionMapper.update();
        medicalInstitutionMapper.update();
        userMapper.update();
    }
    
    synchronized public void drop() {
        try {
            Gateway.getInstance().dropAll();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public static String encryptPassword(String password) {
        return userMapper.encryptPassword(password);
    }
}