package services.stateservices.institutions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import services.stateservices.entities.EduRequest;
import services.stateservices.entities.Feedback;
import services.stateservices.entities.Ticket;
import services.stateservices.errors.NoRightsException;
import services.stateservices.user.Doctor;
import services.stateservices.user.Citizen;
import services.stateservices.user.User;

public class MedicalInstitution extends Institution {
    private Set<Doctor> doctors = new HashSet<>();
    private Set<Ticket> tickets = new HashSet<>();
    
    public MedicalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        super(title, city, district, telephone, fax, address);
    }
    
    public MedicalInstitution(MedicalInstitution institution) {
        super(institution);
    }
    
    public void addDoctor(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add doctor of other institution to this institution!");
        }
        if (!doctors.contains(doctor)) {
            doctors.add(doctor);
        }
    }
    
    public void removeDoctor(Doctor doctor) throws NoRightsException {
        // Remove tickets of doctor
        Iterator<Ticket> i = tickets.iterator();
        while (i.hasNext()) {
                Ticket t = i.next(); // must be called before you can call i.remove()
                if (t.getDoctor().equals(doctor)) {
                    if (t.getUser() != null) {
                        t.getUser().removeTicket(t);
                    }
                    i.remove();
                }
        }
        // Remove doctor
        if (doctors.contains(doctor)) {
            doctors.remove(doctor);
        }
    }
    
    public void addTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add ticket of other institution to this institution!");
        }
        if (!tickets.contains(ticket)) {
            tickets.add(ticket);
        }
    }
    
    public void removeTicket(Ticket ticket) {
        if (tickets.contains(ticket)) {
            tickets.remove(ticket);
        }
    }
    
    public Set<Ticket> getTickets() {
        return tickets;
    }
    
    public Set<Ticket> getTickets(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to get tickets for doctor from other institution!");
        }
        Set<Ticket> doctorTickets = new HashSet<>();
        Iterator<Ticket> i = tickets.iterator();
        while (i.hasNext()) {
                Ticket t = i.next(); // must be called before you can call i.remove()
                if (t.getDoctor().equals(doctor)) {
                    doctorTickets.add(t);
                }
        }
        return doctorTickets;
    }
    
    @Override
    public boolean addFeedback(Feedback feedback) throws NoRightsException {
        if (!feedback.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add feedback of other institution to this institution!");
        }
        
        if (feedback.getUser() instanceof Citizen) {
            for (Ticket t : tickets) {
                if (t.getUser().equals(feedback.getUser()) && t.isVisited()) {
                    feedbacks.add(feedback);
                    return true;
                }
            }
            throw new NoRightsException("Citizen must visit medical institution for adding feedbacks!");
        }
        else if (feedback.getUser() instanceof User) {
            feedbacks.add(feedback);
            return true;
        }
        
        return false;
    }
}
