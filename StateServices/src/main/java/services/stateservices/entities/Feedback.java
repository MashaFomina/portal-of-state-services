package services.stateservices.entities;

import java.util.Date;
import services.stateservices.user.User;
import services.stateservices.institutions.Institution;

public class Feedback {
    private int id;
    private Date date;
    private User user;
    private Institution institution;
    private String text;
    private User toUser = null;

    public Feedback(Date date, User user, Institution institution, String text) {
        this.date = date;
        this.user = user;
        this.institution = institution;
        this.text = text;
    }
        
    public Feedback(Date date, User user, Institution institution, String text, User toUser) {
        this.date = date;
        this.user = user;
        this.institution = institution;
        this.text = text;
        this.toUser = toUser;
    }
   
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }
    
    public Date getDate() {
        return date;
    }
    
    public Institution getInstitution() {
        return institution;
    }

    public void setInstitution(Institution institution) {
        this.institution = institution;
    }
    
    public User getUser() {
        return user;
    }
    
    public User getToUser() {
        return toUser;
    }
    
    public String getText() {
        return text;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        builder.append(date);
        builder.append("] ");
        if (user != null) {
            builder.append(user.getFullName());
        }
        builder.append(": ");
        if (toUser != null) {
            builder.append(toUser).append(", ");
        }
        builder.append(text);
        return builder.toString();
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Feedback other = (Feedback) obj;
        return (id == other.getId() && 
                text.equals(other.getText()) && 
                (user != null ? user.equals(other.getUser()) : other.getUser() == null) && 
                (institution != null ? institution.equals(other.getInstitution()) : other.getInstitution() == null) && 
                (toUser == null ? toUser.equals(other.getToUser()) : other.getToUser() == null) && 
                (date != null ? date.equals(other.getDate()) : other.getDate() == null)
                );
    }
    
    @Override
    public int hashCode() {
        return Integer.toString(id).hashCode();
    }
}
