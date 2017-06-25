package services.stateservices.user;

import java.util.Calendar;
import java.util.Date;
import services.stateservices.entities.Feedback;
import services.stateservices.entities.Ticket;
import services.stateservices.errors.InvalidTicketsDatesException;
import services.stateservices.errors.NoRightsException;
import services.stateservices.institutions.Institution;
import services.stateservices.institutions.MedicalInstitution;

public class Doctor extends User implements InstitutionRepresentative {
    private String position;
    private String summary;
    private MedicalInstitution institution;
    private boolean approved;
    
    public Doctor(String login, String password, String fullName, String email, MedicalInstitution institution, String position, String summary, boolean approved) {
        super(login, password, fullName, email, User.UserType.DOCTOR);
        this.position = position;
        this.summary = summary;
        this.institution = institution;
        this.approved = approved;
    }
    
    public Doctor(Doctor user) {
        super(user);
        this.position = user.position;
        this.summary = user.summary;
        this.institution =  user.institution;
        this.approved = user.approved;
    }
    
    public String getPosition() {
        return position;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void addTickets(Date start, Date end, int intervalMinutes) throws InvalidTicketsDatesException, NoRightsException {
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
            institution.addTicket(new Ticket(this, ticketDate));           
            cal.setTime(ticketDate);
            cal.add(Calendar.MINUTE, intervalMinutes); //minus number would decrement the minutes
            ticketDate = cal.getTime();
        }
    }
        
    public void addTicket(Date date) throws NoRightsException, InvalidTicketsDatesException {
        Date currentDate = new Date();
        if (date.before(currentDate)) {
            throw new InvalidTicketsDatesException("You must determine date for ticket in future!");
        }
        institution.addTicket(new Ticket(this, date));
    }
    
    public void deleteTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getDoctor().equals(this)) {
            throw new NoRightsException("You have no rights to delete tickets of other doctor!");
        }
        ticket.getUser().addNotification("Sorry, but your ticket to " + ticket.getDoctor().getFullName() + " in " + institution.getTitle() + " on " + ticket.getDate() + " was canceled!");
        Citizen user = ticket.getUser();
        if (user != null) {
            user.removeTicket(ticket);
        }
        institution.removeTicket(ticket);
    }
        
    public void confirmVisit(Ticket ticket, String summary) throws NoRightsException {
        if (!ticket.getDoctor().equals(this)) {
            throw new NoRightsException("You have no rights to confirm visits of other doctors!");
        }
        ticket.getUser().addNotification("Now your can leave feedback about your visit to " + institution.getTitle());
        ticket.setVisited(true, summary);
    }
    
    @Override
    public MedicalInstitution getInstitution() {
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
