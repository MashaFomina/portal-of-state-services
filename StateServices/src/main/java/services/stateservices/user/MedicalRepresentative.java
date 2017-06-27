package services.stateservices.user;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import services.stateservices.errors.NoRightsException;
import services.stateservices.errors.InvalidTicketsDatesException;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.storage.StorageRepository;
import services.stateservices.entities.Ticket;
import org.apache.commons.lang.time.DateUtils;
import services.stateservices.entities.Feedback;
import services.stateservices.errors.AlreadyExistsException;

public class MedicalRepresentative extends User implements InstitutionRepresentative {
    private MedicalInstitution institution;
    private boolean approved;
    private StorageRepository repository;
    
    public MedicalRepresentative(String login, String password, String fullName, String email, MedicalInstitution institution, boolean approved) {
        super(login, password, fullName, email, User.UserType.MEDICAL_REPRESENTATIVE);
        this.institution = institution;
        this.approved = approved;
        repository = StorageRepository.getInstance();
    }
    
    public MedicalRepresentative(MedicalRepresentative user) {
        super(user);
        this.institution = user.institution;
        this.approved = user.approved;
        repository = StorageRepository.getInstance();
    }
    
    public void addDoctor(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to add doctor!");
        }
        institution.addDoctor(doctor);
    }
    
    public void removeDoctor(Doctor doctor) throws NoRightsException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to remove this doctor!");
        }
        institution.removeDoctor(doctor);
    }
    
    public void addTickets(Doctor doctor, Date start, Date end, int intervalMinutes) throws InvalidTicketsDatesException, NoRightsException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to add tickets for doctor from other institution!");
        }
        
        Date currentDate = new Date();
        if (end.before(start) || start.before(currentDate)) {
            throw new InvalidTicketsDatesException();
        }
        if (!(start.getYear() == end.getYear() && start.getDay() == end.getDay()) || !(7 < start.getHours() && start.getHours() < end.getHours() && end.getHours() < 21)) {
            throw new InvalidTicketsDatesException("You must determine start and end in one day between 7:00 and 21:00!");
        }
        if (intervalMinutes < 0) {
            throw new InvalidTicketsDatesException("You must determine valid minute interval!");
        }
        
        Date ticketDate = start;
        Calendar cal = Calendar.getInstance();
        while (ticketDate.before(end)) {
            institution.addTicket(new Ticket(doctor, ticketDate));           
            cal.setTime(ticketDate);
            cal.add(Calendar.MINUTE, intervalMinutes); //minus number would decrement the minutes
            ticketDate = cal.getTime();
        }
    }
        
    public void addTicket(Doctor doctor, Date date) throws NoRightsException, InvalidTicketsDatesException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to add tickets for doctor from other institution!");
        }
        Date currentDate = new Date();
        if (date.before(currentDate)) {
            throw new InvalidTicketsDatesException("You must determine date for ticket in future!");
        }
        institution.addTicket(new Ticket(doctor, date));
    }
    
    public void deleteTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getDoctor().getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to delete tickets of doctor from other institution!");
        }
        
        Citizen user = ticket.getUser();
        if (user != null) {
            user.addNotification("Sorry, but your ticket to " + ticket.getDoctor().getFullName() + " in " + institution.getTitle() + " on " + ticket.getDate() + " was canceled!");
            user.removeTicket(ticket);
        }
        institution.removeTicket(ticket);
    }
    
    public void deleteTickets(Doctor doctor, Date date) throws NoRightsException {
        if (!doctor.getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to delete tickets of doctor from other institution!");
        }
        List<Ticket> set = institution.getTickets();
        Iterator<Ticket> i = set.iterator();
        Citizen user;
        Ticket t;
        while (i.hasNext()) {
            t = i.next(); // must be called before you can call i.remove()
            if (t.getDoctor().equals(doctor) && (date == null || DateUtils.isSameDay(date, t.getDate()))) {
                user = t.getUser();
                if (user != null) {
                    user.removeTicket(t);
                }
                i.remove();
            }   
        }
    }
        
    public void confirmVisit(Ticket ticket, String summary) throws NoRightsException {
        if (!ticket.getDoctor().getInstitution().equals(institution)) {
            throw new NoRightsException("You have no rights to confirm visits of other institution!");
        }
        if (ticket.getUser() != null)
            ticket.getUser().addNotification("Now your can leave feedback about your visit to " + institution.getTitle());
        ticket.setVisited(true, summary);
    }
    
    @Override
    public MedicalInstitution getInstitution() {
        return institution;
    }
    
    public MedicalInstitution editInstitution(String title, String city, String district, String telephone, String fax, String address) {
        institution.edit(title, city, district, telephone, fax, address);
        return institution;
    }
    
    @Override
    public boolean isApproved() {
        return approved;
    }
    
    @Override
    public void approve(User user) throws NoRightsException {
        if (!(user instanceof Administrator) || !user.isAuthenticated()) {
            throw new NoRightsException("Just authenticated administrator can approve representative!");
        }
        approved = true;
    }
    
    @Override
    public boolean addFeedback(String text) throws NoRightsException {
        Date date = new Date();
        Feedback feedback = new Feedback(date, this, institution, text);
        return institution.saveFeedback(feedback);
    }

    @Override
    public boolean addFeedbackTo(String text, User userTo) throws NoRightsException {
        Date date = new Date();
        Feedback feedback = new Feedback(date, this, institution, text, userTo);
        return institution.saveFeedback(feedback);
    }
}