package services.stateservices.user;

import java.util.List;
import services.stateservices.entities.Feedback;
import services.stateservices.entities.Notification;
import services.stateservices.errors.NoRightsException;
import services.stateservices.institutions.Institution;

public interface UserInterface {
    User getUser();
    void setId(int id);
    boolean isAuthenticated();
    boolean signIn(String password);
    void signOut();
    void addNotification(String notification);
    List<Notification> getNotifications();
    boolean addFeedback(Institution institution, String text) throws NoRightsException;
    boolean addFeedbackTo(Institution institution, String text, User userTo) throws NoRightsException;
}