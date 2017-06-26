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
    
    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Notification other = (Notification)obj;
        return (id == other.getId() && 
                notification.equals(other.getNotification()) && 
                (date != null ? date.equals(other.getDate()): other.getDate() == null)
                );
    }
    
    @Override
    public int hashCode() {
        return Integer.toString(id).hashCode();
    }
}
