package services.stateservices.user;

import services.stateservices.storage.StorageRepository;
import services.stateservices.entities.Feedback;
import services.stateservices.entities.Notification;
import services.stateservices.institutions.Institution;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import services.stateservices.errors.NoRightsException;

public abstract class User implements UserInterface {
    public enum UserType {
        ADMINISTRATOR("ADMINISTRATOR"), 
        CITIZEN("CITIZEN"),
        DOCTOR("DOCTOR"),
        EDUCATIONAL_REPRESENTATIVE("EDUCATIONAL_REPRESENTATIVE"),
        MEDICAL_REPRESENTATIVE("MEDICAL_REPRESENTATIVE");
        private String text;
        
        UserType(String text) {
          this.text = text;
        }

        public String getText() {
          return this.text;
        }

        public static UserType fromString(String text) {
          for (UserType b : UserType.values()) {
            if (b.text.equalsIgnoreCase(text)) {
              return b;
            }
          }
          return null;
        }
    }
    
    private int id = 0;
    private String login;
    private String password;
    private String fullName;
    private String email;
    private boolean authenticated;
    private UserType userType;
    protected StorageRepository repository;
    private List<Notification> notifications;

    public User(String login, String password, String fullName, String email, UserType userType) {
        this.login = login;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.userType = userType;
        authenticated = false;
        repository = StorageRepository.getInstance();
        this.notifications = new ArrayList<Notification>();
    }

    @Override
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }
    
    @Override
    public void addNotification(String notification) {
        Date date = new Date();
        Notification n = new Notification(this, notification, date);
        notifications.add(n);
    }

    @Override
    public List<Notification> getNotifications() {
        return notifications;
    }
    
    public User(User user) {
        id = user.id;
        login = user.login;
        fullName = user.fullName;
        email = user.email;
        userType = user.userType;
        authenticated = user.authenticated;
        repository = StorageRepository.getInstance();
        this.notifications = user.notifications;
    }

    public String getLogin() {
        return login;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }
    
    public String getPassword() {
        return password;
    }

    @Override
    public UserType getUserType() {
        return userType;
    }
        
    public boolean isDoctor() {
        return userType.equals(UserType.DOCTOR);
    }
    
    public boolean isAdministrator() {
        return userType.equals(UserType.ADMINISTRATOR);
    }
    
    public boolean isCitizen() {
        return userType.equals(UserType.CITIZEN);
    }
    
    public boolean isEducationalRepresentative() {
       return userType.equals(UserType.EDUCATIONAL_REPRESENTATIVE); 
    }
    
    public boolean isMedicalRepresentative() {
       return userType.equals(UserType.MEDICAL_REPRESENTATIVE); 
    }
        
    @Override
    public void setId(int id) {
        this.id = id;
    }
    
    @Override
    public int getId() {
        return id;
    }
    
    @Override
    public boolean signIn(String password) {
        authenticated = this.password.equals(password);
        return authenticated;
    }

    @Override
    public void signOut() {
        authenticated = false;
    }

    public String toString() {
        return  login + ":" + fullName + "<" + email + ">";
    }

    @Override
    public User getUser() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public boolean addFeedback(Institution institution, String text) throws NoRightsException {
        Date date = new Date();
        Feedback feedback = new Feedback(date, this, institution, text);
        return institution.saveFeedback(feedback);
    }

    @Override
    public boolean addFeedbackTo(Institution institution, String text, User userTo) throws NoRightsException {
        Date date = new Date();
        Feedback feedback = new Feedback(date, this, institution, text, userTo);
        return institution.saveFeedback(feedback);
    }
 
    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        User other = (User)obj;
        return login.equals(other.login);
    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }
}