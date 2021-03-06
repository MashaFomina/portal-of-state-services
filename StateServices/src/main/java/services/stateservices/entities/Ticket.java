package services.stateservices.entities;

import java.util.Date;
import services.stateservices.user.Citizen;
import services.stateservices.user.Doctor;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.user.User;

public class Ticket {
    private int id;
    private Citizen user = null;
    private Child child = null;
    private Doctor doctor;
    private Date date;
    private boolean visited = false;
    private String summary = "";
    private boolean updated = false;
    
    public Ticket(Doctor doctor, Date date) {
        this.doctor = doctor;
        this.date = date;
    }

    public Ticket(Doctor doctor, Date date, boolean visited, Citizen user, String summary) {
        this.user = user;
        this.doctor = doctor;
        this.date = date;
        this.visited = visited;
        this.summary = summary;
    }

    public Ticket(Doctor doctor, Date date, boolean visited, Citizen user, Child child, String summary) {
        this.user = user;
        this.child = child;
        this.doctor = doctor;
        this.date = date;
        this.visited = visited;
        this.summary = summary;
    }
    
    public void setUser(Citizen user) { this.user = user; }
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }
    
    public boolean isUpdated() {
        return updated;
    }
    
    public void resetUpdated() {
        updated = false;
    }
    
    // Returns false if ticket already busy
    public boolean acceptTicket(Citizen user) {
        if (this.user == null) {
            this.user = user;
            updated = true;
        }
        else {
            return false;
        }
        return true;
    }
    
    // Returns false if ticket already busy, accept ticket for child
    public boolean acceptTicket(Citizen user, Child child) {
        if (this.user == null) {
            this.user = user;
            this.child = child;
            updated = true;
        }
        else {
            return false;
        }
        return true;
    }
    
    public void refuseTicket() {
        this.user = null;
        this.child = null;
        updated = true;
    }
    
    public void setVisited(boolean visited, String summary) {
        this.visited = visited;
        this.summary = summary;
        updated = true;
    }
    
    public Citizen getUser() {
        return user;
    }
    
    public Child getChild() {
        return child;
    }
    
    public Doctor getDoctor() {
        return doctor;
    }
    
    public Date getDate() {
        return date;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public boolean isVisited() {
        return visited;
    }
    
    public boolean canBeRefused() {
        Date currentDate = new Date();
        return date.after(currentDate);
    }
        
    public boolean isTicketForChild() {
        return (child != null);
    }
    
    public MedicalInstitution getInstitution() {
        return (MedicalInstitution) doctor.getInstitution();
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Ticket other = (Ticket)obj;
        return (id == other.getId() && 
                (child != null ? child.equals(other.getChild()) : other.getChild() == null) && 
                doctor.equals(other.getDoctor()) && 
                date.equals(other.getDate())
                );
    }
    
    @Override
    public int hashCode() {
        return Integer.toString(id).hashCode();
    }
}
