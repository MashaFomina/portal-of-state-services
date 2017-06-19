package services.stateservices.storage;


import java.util.Date;
import services.stateservices.user.User;
import services.stateservices.user.EducationalRepresentative;
import services.stateservices.user.MedicalRepresentative;
import services.stateservices.user.Doctor;
import services.stateservices.user.Administrator;
import services.stateservices.user.Citizen;
import services.stateservices.entities.Feedback;
import java.util.HashMap;
import java.util.Map;
import services.stateservices.institutions.*;

public class StorageRepository {
    private static Map<String, User> users = new HashMap<>();
    private static Map<String, EducationalRepresentative> educationalRepresentatives = new HashMap<>();
    private static Map<String, MedicalRepresentative> medicalRepresentatives = new HashMap<>();
    private static Map<String, Doctor> doctors = new HashMap<>();
    private static Map<String, Administrator> administrators = new HashMap<>();
    private static Map<String, Citizen> citizens = new HashMap<>();
    private static Map<String, EducationalInstitution> educationalInstitutions = new HashMap<>();
    private static Map<String, MedicalInstitution> medicalInstitutions = new HashMap<>();
    private static StorageRepository instance = null;

    private StorageRepository() {}

    public  static StorageRepository getInstance() {
        if (instance == null) {
            instance = new StorageRepository();
        }
        return instance;
    }
    
    public boolean addEducationalRepresentative(String login, String password, String fullName, String email, EducationalInstitution institution, boolean approved) {
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
    }
    
    public boolean addDoctor(String login, String password, String fullName, String email, MedicalInstitution institution, String position, String summary, boolean approved) {
        if (users.containsKey(login)) return false;

        Doctor newUser = new Doctor(login, password, fullName, email, institution,position, summary, approved);
        synchronized (this) {
            users.put(login, newUser);
            doctors.put(login, newUser);
        }
        return true;
    }
    
    public boolean addAdministrator(String login, String password, String fullName, String email) {
        if (users.containsKey(login)) return false;

        Administrator newUser = new Administrator(login, password, fullName, email);
        synchronized (this) {
            users.put(login, newUser);
            administrators.put(login, newUser);
        }
        return true;
    }
    
    public boolean addCitizen(String login, String password, String fullName, String email, String policy, String passport, Date birthDate) {
        if (users.containsKey(login)) return false;

        Citizen newUser = new Citizen(login, password, fullName, email, policy, passport, birthDate);
        synchronized (this) {
            users.put(login, newUser);
            citizens.put(login, newUser);
        }
        return true;
    }
    
    public EducationalInstitution addEducationalInstitution(String title, String city, String district, String telephone, String fax, String address, Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        if (educationalInstitutions.containsKey(telephone)) return educationalInstitutions.get(telephone);
        EducationalInstitution institution = new EducationalInstitution(title, city, district, telephone, fax, address, seats, busySeats);
        synchronized (this) {
            educationalInstitutions.put(telephone, institution);
        }
        return institution;
    }
    
    public EducationalInstitution addEducationalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        if (educationalInstitutions.containsKey(telephone)) return educationalInstitutions.get(telephone);
        EducationalInstitution institution = new EducationalInstitution(title, city, district, telephone, fax, address);
        synchronized (this) {
            educationalInstitutions.put(telephone, institution);
        }
        return institution;
    }
        
    public MedicalInstitution addMedicalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        if (medicalInstitutions.containsKey(telephone)) return medicalInstitutions.get(telephone);
        MedicalInstitution institution = new MedicalInstitution(title, city, district, telephone, fax, address);
        synchronized (this) {
            medicalInstitutions.put(telephone, institution);
        }
        return institution;
    }
    
    public User getUser(String login) {
        return users.get(login);
    }
    
    public EducationalRepresentative getEducationalRepresentative(User user) {
        return educationalRepresentatives.get(user.getLogin());
    }

    public MedicalRepresentative getMedicalRepresentative(User user) {
        return medicalRepresentatives.get(user.getLogin());
    }
    
    public Doctor getDoctor(User user) {
        return doctors.get(user.getLogin());
    }
    
    public void removeDoctor(String login) {
        doctors.remove(login);
        users.remove(login);
    }

    public Administrator getAdministrator(User user) {
        return administrators.get(user.getLogin());
    }
    
    public Citizen getCitizen(User user) {
        return citizens.get(user.getLogin());
    }    

    public boolean authenticateUser(String login, String password) {
        User user = users.get(login);
        return user != null && user.signIn(password);
    }

    synchronized public void clear() {
        users.clear();
        educationalRepresentatives.clear();
        medicalRepresentatives.clear();
        doctors.clear();
        administrators.clear();
        citizens.clear();
        educationalInstitutions.clear();
        medicalInstitutions.clear();
    }
}