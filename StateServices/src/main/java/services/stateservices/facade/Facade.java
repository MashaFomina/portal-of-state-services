package services.stateservices.facade;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import javafx.fxml.FXML;
import org.omg.CORBA.TIMEOUT;
import services.stateservices.storage.StorageRepository;
import services.stateservices.user.*;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import services.stateservices.entities.*;
import services.stateservices.errors.NoRightsException;
import services.stateservices.institutions.*;

public class Facade implements FacadeInterface {

    private StorageRepository repository;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    DateFormat dateFormatBirthDate = new SimpleDateFormat("yyyy-MM-dd");

    public Facade() {
        repository = StorageRepository.getInstance();
    }
    
    @Override
    public boolean authenticate(String login, String password) throws Exception {
        return repository.authenticateUser(login, password);
    }
    
    @Override
    public void signOut(String login) {
        repository.signOut(login);
    }
    
    public boolean isMedicalRepresentative(String login) {
        return repository.getUser(login).isMedicalRepresentative();
    }
    
    public boolean isEducationalRepresentative(String login) {
        return repository.getUser(login).isEducationalRepresentative();
    }
     
    public boolean isCitizen(String login) {
        return repository.getUser(login).isCitizen();
    }
        
    @Override
    public List<Struct> getAllNotificationsForUser(String login) {
        List<Notification> notifications = repository.getUser(login).getNotifications();
        List<Struct> fields = new ArrayList<>();
        for (Notification n: notifications) {
            Struct struct = new Struct();
            struct.add("id", Integer.toString(n.getId()));
            struct.add("date", dateFormat.format(n.getDate()));
            struct.add("notification", n.getNotification());
            fields.add(struct);
        }
        return fields;
    }
    
    @Override
    public List<Struct> getAllChildsForUser(String login) {
        Collection<Child> childs = repository.getCitizen(login).getChilds().values();
        List<Struct> fields = new ArrayList<>();
        for (Child c: childs) {
            Struct struct = new Struct();
            struct.add("id", Integer.toString(c.getId()));
            struct.add("fullName", c.getFullName());
            struct.add("birthCertificate", c.getBirthCertificate());
            struct.add("birthDate", dateFormatBirthDate.format(c.getBirthDate()));
            fields.add(struct);
        }
        return fields;
    }
    
        
    @Override
    public List<Struct> getAllTicketsForUser(String login)
    {
        List<Ticket> tickets = repository.getCitizen(login).getTickets();
        List<Struct> fields = new ArrayList<>();
        for (Ticket t: tickets) {
            Struct struct = new Struct();
            struct.add("id", Integer.toString(t.getId()));
            struct.add("date", dateFormat.format(t.getDate()));
            struct.add("institution", t.getInstitution().getTitle());
            struct.add("doctor", t.getDoctor().getFullName() + " (" + t.getDoctor().getPosition() + ")");
            struct.add("child", t.getChild() != null ? t.getChild().getFullName() : "");
            struct.add("visited", t.isVisited() ? "yes" : "no");
            struct.add("summary", t.getSummary());
            struct.add("canRefuse", t.canBeRefused() ? "yes" : "no");
            fields.add(struct);
        }
        return fields;
    }
    
    @Override
    public String getCitizenPassport(String login) {
         return repository.getCitizen(login).getPassport();
    }
    
    @Override
    public String getCitizenPolicy(String login) {
        return repository.getCitizen(login).getPolicy();
    }
    
    @Override
    public String getCitizenBirthDate(String login) {
        return dateFormatBirthDate.format(repository.getCitizen(login).getBirthDate());
    }
    
    @Override
    public String getUserEmail(String login) {
         return repository.getCitizen(login).getEmail();
    }
    
    @Override
    public String getUserFullName(String login) {
        return repository.getCitizen(login).getFullName();
    }
    
    @Override 
    public boolean addChild(String login, String fullName, String birthCertificate, String birthDate) {
        Citizen citizen = repository.getCitizen(login);
        boolean result = false;
        try {
            result = citizen.createChildInfo(fullName, birthCertificate, dateFormatBirthDate.parse(birthDate));
            repository.updateUser(citizen);
        } catch (ParseException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    @Override
    public boolean deleteChild(String login, String id) {
        boolean result = false;
        try {
            Citizen citizen = repository.getCitizen(login);
            Child child = repository.getChild(new Integer(id));
            result = citizen.removeChildInfo(child);
            if (result) {
                repository.removeChild(child);
            }
        } catch (NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    @Override
    public boolean refuseTicket(String login, String ticketId) {
        boolean result = false;
        try {
            Citizen citizen = repository.getCitizen(login);
            Ticket ticket = repository.getTicket(new Integer(ticketId));
            citizen.cancelTicket(ticket);
            repository.updateUser(citizen);
            result = true;
        } catch (NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public List<Struct> getAllEduRequestsForUser(String login)
    {
        List<EduRequest> requests = repository.getCitizen(login).getEduRequests();
        List<Struct> fields = new ArrayList<>();
        for (EduRequest r: requests) {
            Struct struct = new Struct();
            struct.add("id", Integer.toString(r.getId()));
            struct.add("creationDate", dateFormat.format(r.getCreationDate()));
            struct.add("status", r.getStatus().getBeautifulText());
            struct.add("child", r.getChild().getFullName());
            struct.add("institution", r.getInstitution().getTitle());
            struct.add("classNumber", Integer.toString(r.getClassNumber()));
            struct.add("appointment", r.getAppointment() != null ?  dateFormat.format(r.getAppointment()) : "");
            struct.add("mustAccept", r.getStatus().equals(EduRequest.Status.ACCEPTED_BY_INSTITUTION) ? "yes" : "no");
            struct.add("canRemove", (!r.getStatus().equals(EduRequest.Status.CHILD_IS_ENROLLED)) ? "yes" : "no");
            fields.add(struct);
        }
        return fields;
    }
    
    public boolean acceptEduRequest(String login, String requestId) {
        boolean result = false;
        try {
            Citizen citizen = repository.getCitizen(login);
            EduRequest request = repository.getEduRequest(new Integer(requestId));
            citizen.acceptEduRequest(request);
            repository.updateUser(citizen);
            result = true;
        } catch (NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public boolean removeEduRequest(String login, String requestId) {
        boolean result = false;
        try {
            Citizen citizen = repository.getCitizen(login);
            EduRequest request = repository.getEduRequest(new Integer(requestId));
            citizen.removeEduRequest(request);
            repository.removeEduRequest(request);
            result = true;
        } catch (NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    @Override
    public Map<Integer, Integer> getEducationalInstitutionSeats(int id) {
        EducationalInstitution institution = repository.getEducationalInstitution(id);
        return (institution != null ? institution.getSeats() : null);
    }
    
    @Override
    public Map<Integer, Integer> getEducationalInstitutionBusySeats(int id) {
        EducationalInstitution institution = repository.getEducationalInstitution(id);
        return (institution != null ? institution.getBusySeats() : null);
    }
   
    @Override
    public String getEducationalInstitutionTitle(int id) {
        EducationalInstitution institution = repository.getEducationalInstitution(id);
        return (institution != null ? institution.getTitle() : null);
    }
    
    @Override
    public String getEducationalInstitutionCity(int id) {
        EducationalInstitution institution = repository.getEducationalInstitution(id);
        return (institution != null ? institution.getCity() : null);
    }
 
    @Override
    public String getEducationalInstitutionDistrict(int id) {
        EducationalInstitution institution = repository.getEducationalInstitution(id);
        return (institution != null ? institution.getDistrict() : null);
    }
    
    @Override
    public String getEducationalInstitutionTelephone(int id) {
        EducationalInstitution institution = repository.getEducationalInstitution(id);
        return (institution != null ? institution.getTelephone() : null);
    }

    @Override
    public String getEducationalInstitutionFax(int id) {
        EducationalInstitution institution = repository.getEducationalInstitution(id);
        return (institution != null ? institution.getFax() : null);
    }
    
    @Override
    public String getEducationalInstitutionAddress(int id) {
        EducationalInstitution institution = repository.getEducationalInstitution(id);
        return (institution != null ? institution.getAddress() : null);
    }
}
