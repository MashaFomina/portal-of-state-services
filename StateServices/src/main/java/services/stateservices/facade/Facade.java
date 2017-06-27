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
import services.stateservices.errors.AlreadyExistsException;
import services.stateservices.errors.InvalidAppointmentDateException;
import services.stateservices.errors.InvalidDataForSavingSeatsException;
import services.stateservices.errors.InvalidTicketsDatesException;
import services.stateservices.errors.NoFreeSeatsException;
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
    
    public List<Struct> getTicketsForMedicalInstitution(int id)
    {
        List<Ticket> tickets = repository.getMedicalInstitution(id).getTickets();
        List<Struct> fields = new ArrayList<>();
        for (Ticket t: tickets) {
            Struct struct = new Struct();
            struct.add("id", Integer.toString(t.getId()));
            struct.add("date", dateFormat.format(t.getDate()));
            struct.add("institution", t.getInstitution().getTitle());
            struct.add("doctor", t.getDoctor().getFullName() + " (" + t.getDoctor().getPosition() + ")");
            struct.add("child", t.getChild() != null ? t.getChild().getFullName() : "");
            struct.add("citizen", t.getUser() != null ? t.getUser().getFullName() : "");
            struct.add("visited", t.isVisited() ? "yes" : "no");
            struct.add("summary", t.getSummary());
            struct.add("canRefuse", t.canBeRefused() ? "yes" : "no");
            struct.add("canSetVisited", (!t.canBeRefused() && !t.isVisited()) ? "yes" : "no");
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
         return repository.getUser(login).getEmail();
    }
    
    @Override
    public String getUserFullName(String login) {
        return repository.getUser(login).getFullName();
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
    
    public List<Struct> getAllFeedbacksForInstitution(int id, boolean is_edu) {
        List<Feedback> feedbacks = is_edu ? repository.getEducationalInstitution(id).getFeedbacks() : repository.getMedicalInstitution(id).getFeedbacks();
        List<Struct> fields = new ArrayList<>();

        for (Feedback f: feedbacks) {
            Struct struct = new Struct();
            struct.add("id", Integer.toString(f.getId()));
            struct.add("user", f.getUser().getFullName());
            struct.add("userLogin", f.getUser().getLogin());
            struct.add("toUser", f.getToUser() != null ? f.getToUser().getFullName() : "");
            struct.add("text", f.getText());
            struct.add("date", dateFormat.format(f.getDate()));
            fields.add(struct);
        }
        return fields;
    }
    
    public List<Struct> getAllDoctorsForInstitution(int id) {
        Set<Doctor> doctors = repository.getMedicalInstitution(id).getDoctors();
        List<Struct> fields = new ArrayList<>();

        for (Doctor d: doctors) {
            Struct struct = new Struct();
            struct.add("id", Integer.toString(d.getId()));
            struct.add("login", d.getLogin());
            struct.add("fullName", d.getFullName());
            struct.add("email", d.getEmail());
            struct.add("position", d.getPosition());
            struct.add("summary", d.getSummary());
            fields.add(struct);
        }
        return fields;
    }
    
    public List<Struct> getAllEduRequestsForInstitution(int id) {
        List<EduRequest> requests = repository.getEducationalInstitution(id).getEduRequests();
        List<Struct> fields = new ArrayList<>();

        for (EduRequest r: requests) {
            Struct struct = new Struct();
            struct.add("id", Integer.toString(r.getId()));
            struct.add("creationDate", dateFormat.format(r.getCreationDate()));
            struct.add("status", r.getStatus().getBeautifulText());
            struct.add("child", r.getChild().getFullName());
            struct.add("childBirthDate", dateFormatBirthDate.format(r.getChild().getBirthDate()));
            struct.add("classNumber", Integer.toString(r.getClassNumber()));
            struct.add("appointment", r.getAppointment() != null ?  dateFormat.format(r.getAppointment()) : "");
            struct.add("mustAccept", r.getStatus().equals(EduRequest.Status.OPENED) ? "yes" : "no");
            struct.add("mustDecide", (r.getAppointment() != null && r.isPassedAppointment()) ? "yes" : "no");
            struct.add("mustMakeAppointment", (r.getStatus().equals(EduRequest.Status.ACCEPTED_BY_PARENT) && r.getAppointment() == null) ? "yes" : "no");
            fields.add(struct);
        }
        return fields;
    }
    
    public List<Struct> getSeatsForEducationalInstitution(int id) {
        EducationalInstitution institution = repository.getEducationalInstitution(id);
        Map<Integer, Integer> seats = institution.getSeats();
        List<Struct> fields = new ArrayList<>();

        for (Integer classNumber: seats.keySet()) {
            Integer totalSeats = seats.get(classNumber);
            Struct struct = new Struct();
            struct.add("classNumber", classNumber.toString());
            struct.add("seats", totalSeats.toString());
            struct.add("freeSeats", Integer.toString(totalSeats - institution.getBusySeats(classNumber)));
            fields.add(struct);
        }
        return fields;
    }
        
    public boolean acceptEduRequestByParent(String login, String requestId) {
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
    
    public boolean setTicketIsVisited(String login, String ticketId, String summary) {
        boolean result = false;
        MedicalRepresentative user = repository.getMedicalRepresentative(login);
        if (user == null) return result;
        
        try {
            Ticket ticket = repository.getTicket(new Integer(ticketId));
            user.confirmVisit(ticket, summary);
            repository.updateMedicalInstitution(user.getInstitution());
            result = true;
        } catch (NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public boolean cancelTicketByRepresentative(String login, String ticketId) {
         boolean result = false;
        MedicalRepresentative user = repository.getMedicalRepresentative(login);
        if (user == null) return result;
        
        try {
            Ticket ticket = repository.getTicket(new Integer(ticketId));
            user.deleteTicket(ticket);
            repository.removeTicket(ticket);
            result = true;
        } catch (NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }     
    
    public boolean acceptEduRequestByInstitution (String login, String requestId) {
        boolean result = false;
        try {
            EducationalRepresentative user = repository.getEducationalRepresentative(login);
            EduRequest request = repository.getEduRequest(new Integer(requestId));
            user.acceptEduRequest(request);
            repository.updateEducationalInstitution(user.getInstitution());
            result = true;
        } catch (NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public boolean addFeedbackByRepresentative(String login, int institutionId, String text, String loginUserTo) {
        boolean result = false;
        User user = repository.getUser(login);
        User userTo = loginUserTo.length() > 0 ? repository.getUser(loginUserTo) : null;
        if (user.isEducationalRepresentative() || user.isMedicalRepresentative()) {
            try {
                if (user.isEducationalRepresentative()) {
                    EducationalRepresentative representative = repository.getEducationalRepresentative(login);
                    if (userTo == null) representative.addFeedback(text);
                    else representative.addFeedbackTo(text, userTo);
                    repository.updateEducationalInstitution(representative.getInstitution());
                }
                else {
                    MedicalRepresentative representative = repository.getMedicalRepresentative(login);
                    if (userTo == null) representative.addFeedback(text);
                    else representative.addFeedbackTo(text, userTo);
                    repository.updateMedicalInstitution(representative.getInstitution());
                }
                result = true;
            } catch (NoRightsException | SQLException ex) {
                Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }
    
    public int getInstitutionIdByRepresentative(String login) {
        InstitutionRepresentative user = (InstitutionRepresentative) repository.getUser(login);
        return user.getInstitution().getId();
    }
    
    public boolean saveDoctorToMedicalInstitution(String login, String doctorLogin, String password, String fullName, String email, String position, String summary) {
        boolean result = false;
        MedicalRepresentative user = repository.getMedicalRepresentative(login);
        Doctor newDoctor = new Doctor(doctorLogin, repository.encryptPassword(password), fullName, email, user.getInstitution(), position, summary, true);
        try {
            result = repository.addUser(newDoctor);
            if (result) user.addDoctor(newDoctor);
        } catch (NoRightsException | AlreadyExistsException ex) {
            Logger.getLogger(MedicalRepresentative.class.getName()).log(Level.SEVERE, null, ex);
            result = false;
        }
        return result;
    }
    
    public boolean removeDoctor(String login, String doctorLogin) {
        boolean result = false;
        MedicalRepresentative user = repository.getMedicalRepresentative(login);
        Doctor doctor = repository.getDoctor(doctorLogin);
        if (doctor != null && user != null) {
            try {
                repository.removeDoctor(doctorLogin);
                user.removeDoctor(doctor);
                result = true;
            } catch (NoRightsException | SQLException ex) {
                Logger.getLogger(MedicalRepresentative.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return result;
    }
    
    public boolean addTicket(String login, String doctorLogin, String date) {
        boolean result = false;
        try {
            MedicalRepresentative user = repository.getMedicalRepresentative(login);
            Doctor doctor = repository.getDoctor(doctorLogin);
            Date dateTicket;
            dateTicket = dateFormat.parse(date);
            user.addTicket(doctor, dateTicket);
            repository.updateMedicalInstitution(user.getInstitution());
            result = true;
        } catch (InvalidTicketsDatesException | ParseException | NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public boolean addTickets(String login, String doctorLogin, String start, String end, String intervalMinutes) {
        boolean result = false;
        try {
            MedicalRepresentative user = repository.getMedicalRepresentative(login);
            Doctor doctor = repository.getDoctor(doctorLogin);
            Date startDate, endDate;
            startDate = dateFormat.parse(start);
            endDate = dateFormat.parse(end);
            user.addTickets(doctor, startDate, endDate, new Integer(intervalMinutes));
            repository.updateMedicalInstitution(user.getInstitution());
            result = true;
        } catch (InvalidTicketsDatesException | ParseException | NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
        
    public boolean makeAppointment(String login, String requestId, String date) {
        boolean result = false;
        try {
            EducationalRepresentative user = repository.getEducationalRepresentative(login);
            EduRequest request = repository.getEduRequest(new Integer(requestId));
            Date dateAppointment;
            dateAppointment = dateFormat.parse(date);
            user.makeAppointment(request, dateAppointment);
            repository.updateEducationalInstitution(user.getInstitution());
            result = true;
        } catch (ParseException | InvalidAppointmentDateException | NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public boolean editInstitutionInformation(String login, String title, String city, String district, String telephone, String fax, String address, int id) {
        boolean result = false;
        try {
            User user = repository.getUser(login);
            if (user.isEducationalRepresentative()) {
                EducationalRepresentative representative = repository.getEducationalRepresentative(login);
                representative.editInstitution(title, city, district, telephone, fax, address, null, null);
                repository.updateEducationalInstitution(representative.getInstitution());
            }
            else {
                MedicalRepresentative representative = repository.getMedicalRepresentative(login);
                representative.editInstitution(title, city, district, telephone, fax, address);
                repository.updateMedicalInstitution(representative.getInstitution());
            }
            result = true;
        } catch (SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    public boolean saveSeatsForEducationalInstitution(String login, int classNumber, int seats, int busySeats) {
        boolean result = false;
        try {
            EducationalRepresentative user = repository.getEducationalRepresentative(login);
            user.setSeats(classNumber, seats, busySeats);
            repository.updateEducationalInstitution(user.getInstitution());
            result = true;
        } catch (InvalidDataForSavingSeatsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
                
    public boolean enrollChildInInstitution(String login, String requestId) {
        boolean result = false;
        try {
            EducationalRepresentative user = repository.getEducationalRepresentative(login);
            EduRequest request = repository.getEduRequest(new Integer(requestId));
            user.makeChildEnrolled(request);
            repository.updateEducationalInstitution(user.getInstitution());
            result = true;
        } catch (NoFreeSeatsException | NoRightsException | SQLException ex) {
            Logger.getLogger(Facade.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result; 
    }
    
    public boolean refuseEduRequestByInstitution(String login, String requestId) {
        boolean result = false;
        try {
            EducationalRepresentative user = repository.getEducationalRepresentative(login);
            EduRequest request = repository.getEduRequest(new Integer(requestId));
            user.refuseEduRequest(request);
            repository.updateEducationalInstitution(user.getInstitution());
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
    
    public String getMedicalInstitutionTitle(int id) {
        MedicalInstitution institution = repository.getMedicalInstitution(id);
        return (institution != null ? institution.getTitle() : null);
    }
    
    public String getMedicalInstitutionCity(int id) {
        MedicalInstitution institution = repository.getMedicalInstitution(id);
        return (institution != null ? institution.getCity() : null);
    }
 
    public String getMedicalInstitutionDistrict(int id) {
        MedicalInstitution institution = repository.getMedicalInstitution(id);
        return (institution != null ? institution.getDistrict() : null);
    }
    
    public String getMedicalInstitutionTelephone(int id) {
        MedicalInstitution institution = repository.getMedicalInstitution(id);
        return (institution != null ? institution.getTelephone() : null);
    }

    public String getMedicalInstitutionFax(int id) {
        MedicalInstitution institution = repository.getMedicalInstitution(id);
        return (institution != null ? institution.getFax() : null);
    }
    
    public String getMedicalInstitutionAddress(int id) {
        MedicalInstitution institution = repository.getMedicalInstitution(id);
        return (institution != null ? institution.getAddress() : null);
    }
}
