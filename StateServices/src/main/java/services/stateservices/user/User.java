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
    private int id = 0;
    private String login;
    private String password;
    private String fullName;
    private String email;
    private boolean authenticated;
    protected StorageRepository repository;
    private List<Notification> notifications;

    public User(String login, String password, String fullName, String email) {
        this.login = login;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        authenticated = false;
        repository = StorageRepository.getInstance();
        this.notifications = new ArrayList<Notification>();
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

    @Override
    public void setId(int id) {
        this.id = id;
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
        return institution.addFeedback(feedback);
    }

    @Override
    public boolean addFeedbackTo(Institution institution, String text, User userTo) throws NoRightsException {
        Date date = new Date();
        Feedback feedback = new Feedback(date, this, institution, text, userTo);
        return institution.addFeedback(feedback);
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