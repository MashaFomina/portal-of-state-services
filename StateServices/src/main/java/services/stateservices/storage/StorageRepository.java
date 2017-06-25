package services.stateservices.storage;


import java.util.Date;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import services.stateservices.user.User;
import services.stateservices.user.EducationalRepresentative;
import services.stateservices.user.MedicalRepresentative;
import services.stateservices.user.Doctor;
import services.stateservices.user.Administrator;
import services.stateservices.user.Citizen;
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

    private StorageRepository() {}

    public  static StorageRepository getInstance() {
        if (instance == null) {
            System.out.println("Wow0000!");
            instance = new StorageRepository();
        }
        try {
            if (educationalInstitutionMapper == null) {System.out.println("educationalInstitutionMapper");educationalInstitutionMapper = new EducationalInstitutionMapper();}
            if (medicalInstitutionMapper == null) {System.out.println("medicalInstitutionMapper");medicalInstitutionMapper = new MedicalInstitutionMapper();}
            if (userMapper == null) {System.out.println("userMapper");userMapper = new UserMapper(educationalInstitutionMapper, medicalInstitutionMapper);}
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("instance!");
        return instance;
    }
    
    /*public boolean addEducationalRepresentative(String login, String password, String fullName, String email, EducationalInstitution institution, boolean approved) {
        if (users.containsKey(login)) return false;

        EducationalRepresentative newUser = new EducationalRepresentative(login, password, fullName, email, institution, approved);
        synchronized (this) {
            users.put(login, newUser);
            educationalRepresentatives.put(login, newUser);
        }
        return true;
    }

    public boolean addMedicalRepresentative(String login, String password, String fullName, String email, MedicalInstitution institution, boolean approved) {
        if (users.containsKey(login)) return false;

        MedicalRepresentative newUser = new MedicalRepresentative(login, password, fullName, email, institution, approved);
        synchronized (this) {
            users.put(login, newUser);
            medicalRepresentatives.put(login, newUser);
        }
        return true;
    }*/
    
    public boolean addDoctor(String login, String password, String fullName, String email, MedicalInstitution institution, String position, String summary, boolean approved) {
        /*if (users.containsKey(login)) return false;

        Doctor newUser = new Doctor(login, password, fullName, email, institution,position, summary, approved);
        synchronized (this) {
            users.put(login, newUser);
            doctors.put(login, newUser);
        }*/
        return true;
    }
    
    /*public boolean addAdministrator(String login, String password, String fullName, String email) {
        if (users.containsKey(login)) return false;

        Administrator newUser = new Administrator(login, password, fullName, email);
        synchronized (this) {
            users.put(login, newUser);
            administrators.put(login, newUser);
        }
        return true;
    }
    
    public boolean addCitizen(String login, String password, String fullName, String email, String policy, String passport, Date birthDate) throws AlreadyExistsException {
        try {
            if (userMapper.findByLogin(login) != null) throw new AlreadyExistsException("User with login " + login + " already exists");
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        Citizen newUser = new Citizen(login, password, fullName, email, policy, passport, birthDate);
        try {
            citizenMapper.addCitizen(newUser);
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
        public Project addProject(String name, Manager manager) {
        Project project = new Project(name, manager);
        try {
            projectMapper.update(project);
            return project;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Project getProject(String name) {
        try {
            return projectMapper.findByName(name);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (EndBeforeStartException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Project getProject(int id) {
        try {
            return projectMapper.findByID(id);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (EndBeforeStartException e) {
            e.printStackTrace();
        }
        return null;
    }*/
    
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
        
    public MedicalInstitution addMedicalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        /*if (medicalInstitutions.containsKey(telephone)) return medicalInstitutions.get(telephone);
        MedicalInstitution institution = new MedicalInstitution(title, city, district, telephone, fax, address);
        synchronized (this) {
            medicalInstitutions.put(telephone, institution);
        }
        return institution;*/
        return new MedicalInstitution(title, city, district, telephone, fax, address);
    }
    
    public EducationalInstitution getEducationalInstitution(int id) {
        return null;
    }
        
    public MedicalInstitution getMedicalInstitution(int id) {
        try {
            return medicalInstitutionMapper.findByID(id);
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
    
    public void removeDoctor(String login) {
        /*doctors.remove(login);
        users.remove(login);*/
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

    public boolean authenticateUser(String login, String password) {
        User user = getUser(login);
        return userMapper.authenticateUser(user, password);
    }

    public boolean authenticateUser(User user, String password) {
        return userMapper.authenticateUser(user, password);
    }

    public void clear() {
        educationalInstitutionMapper.clear();
        medicalInstitutionMapper.clear();
        userMapper.clear();
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
}