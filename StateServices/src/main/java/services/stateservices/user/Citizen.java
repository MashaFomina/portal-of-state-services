package services.stateservices.user;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import services.stateservices.entities.*;
import services.stateservices.errors.NoFreeSeatsException;
import services.stateservices.institutions.*;
import services.stateservices.storage.StorageRepository;
import services.stateservices.errors.NoRightsException;

public class Citizen extends User {
    private String policy;
    private String passport;
    private Date birthDate;
    private List<EduRequest> requests = new ArrayList<>();
    private List<Ticket> tickets = new ArrayList<>();
    private Map<String, Child> childs = new HashMap<>();
    
    public Citizen(String login, String password, String fullName, String email, String policy, String passport, Date birthDate) {
        super(login, password, fullName, email, User.UserType.CITIZEN);
        this.policy = policy;
        this.passport = passport;
        this.birthDate = birthDate;
    }
        
    public Citizen(Citizen user) {
        super(user);
        this.policy = user.policy;
        this.passport = user.passport;
        this.birthDate = user.birthDate;
    }
    
    public List<EduRequest> getEduRequests() {
        return requests;
    }
    
    public Map<String, Child> getChilds() {
        return childs;
    }
    
    public List<Ticket> getTickets() {
        return tickets;
    }
    
    public String getPolicy() {
        return policy;
    }
    
    public String getPassport() {
        return passport;
    }
    
    public Date getBirthDate() {
        return birthDate;
    }
    
    public void addChild(Child child) {
        if (child.getParent().equals(this)) {
            childs.put(child.getBirthCertificate(), child);
        }
    }
    
    public void addTicket(Ticket ticket) {
        if (ticket.getUser().equals(this)) {
            tickets.add(ticket);
        }
    }
       
    public void addEduRequest(EduRequest request) {
        if (request.getParent().equals(this)) {
            requests.add(request);
        }
    }
        
    public boolean createChildInfo(String fullName, String birthCertificate, Date birthDate) {
        Child child = new Child(this, fullName, birthCertificate, birthDate);
        Date currentDate = new Date(); 
        if (currentDate.after(birthDate) && !childs.containsKey(child.getBirthCertificate())) {
            childs.put(child.getBirthCertificate(), child);
            return true;
        }
        return false;
    }
    
    public Child getChild(String birthCertificate) {
        if (childs.containsKey(birthCertificate)) {
            return childs.get(birthCertificate);
        }
        return null;
    }
        
    public boolean removeChildInfo(Child child) throws NoRightsException {
        String birthCertificate = child.getBirthCertificate();
        if (!child.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to delete information about this child!");
        }
        if (childs.containsKey(birthCertificate)) {
            Iterator<EduRequest> i = requests.iterator();
            while (i.hasNext()) {
                EduRequest request = i.next(); // must be called before you can call i.remove()
                if (request.getChild().getBirthCertificate().equals(birthCertificate)) {
                    request.getInstitution().removeEduRequest(request);
                    i.remove();
                }
            }
            Iterator<Ticket> k = tickets.iterator();
            while (k.hasNext()) {
                Ticket ticket = k.next(); // must be called before you can call i.remove()
                if (ticket.getChild() != null && ticket.getChild().getBirthCertificate().equals(birthCertificate)) {
                    ticket.getInstitution().removeTicket(ticket);
                    k.remove();
                }
            }
            childs.remove(birthCertificate);
            return true;
        }
        return false;
    }
    
    public EduRequest createEduRequest(Child child, EducationalInstitution institution, int classNumber)  throws NoRightsException, NoFreeSeatsException {
        if (!child.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to create educational request for child!");
        }
        
        if (institution.getFreeSeats(classNumber) < 1) {
            throw new NoFreeSeatsException();
        }
        
        if (childs.containsKey(child.getBirthCertificate())) {
            for (EduRequest request : requests) {
                // Child can be enrolled in just one institution
                if (request.getChild().equals(child) && request.isChildEnrolled()) {
                    return null;
                }
                // Request in institution already exists
                if (request.getChild().equals(child) && request.getInstitution().equals(institution)) {
                    return null;
                }
            }
            Date creationDate = new Date();
            EduRequest request = new EduRequest(null, child, this, institution, creationDate, null, classNumber);
            boolean result = institution.createEduRequest(request);
            if (result) {
                requests.add(request);
                return request;
            }
        }
        return null;
    }
    
    public void removeEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to delete educational request!");
        }
        if (requests.contains(request)) {
            request.getInstitution().removeEduRequest(request);
            requests.remove(request);
        }
    }
    
    public boolean acceptEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to accept educational request!");
        }
        if (requests.contains(request) && request.isAcceptedByInstitution()) {
            request.changeStatus(EduRequest.Status.ACCEPTED_BY_PARENT);
            return true;
        }
        return false;
    }
 
    public boolean acceptTicket(Ticket ticket) {
        boolean result = ticket.acceptTicket(this);
        if (result) {
            tickets.add(ticket);
        }
        return result;
    }
    
    public boolean acceptTicketForChild(Ticket ticket, Child child) throws NoRightsException {
        if (!child.getParent().equals(this)) {
            throw new NoRightsException("You have no rights to accept ticket for this child!");
        }
        boolean result = ticket.acceptTicket(this, child);
        if (result) {
            tickets.add(ticket);
        }
        return result;
    }
    
    public void cancelTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getUser().equals(this)) {
            throw new NoRightsException("You have no rights to refuse this ticket because your not owner!");
        }
        ticket.refuseTicket();
        if (tickets.contains(ticket)) {
            tickets.remove(ticket);
        }
    }
    
    public void removeTicket(Ticket ticket) throws NoRightsException {
        if (!ticket.getUser().equals(this)) {
            throw new NoRightsException("You have no rights to remove this ticket because your not owner!");
        }
        if (tickets.contains(ticket)) {
            tickets.remove(ticket);
        }
    }
    
    public void setChilds(Map<String, Child> childs) {
        this.childs = childs;
    }
    
    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
    
    public void setEduRequests(List<EduRequest> requests) {
        this.requests = requests;
    }
    
    @Override
    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
    }
}
