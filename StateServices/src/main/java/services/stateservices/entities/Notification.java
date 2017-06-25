package services.stateservices.entities;

import java.util.Date;
import services.stateservices.user.User;

public class Notification {
    private User owner;
    private String notification;
    private Date date;
    private int id;

    public Notification(User owner, String notification, Date date) {
        this.owner = owner;
        this.notification = notification;
        this.date = date;
    }

    public Notification(int id, String notification, Date date) {
        this.id = id;
        this.notification = notification;
        this.date = date;
    }
    
    public void setOwner(User owner) {
        this.owner = owner;
    }
    
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }
    public User getOwner() { return owner; }
    public String getNotification() { return notification; }
    public Date getDate() { return date; }

    @Override
    public String toString() {
        return date + ": " + notification;
    }
}
