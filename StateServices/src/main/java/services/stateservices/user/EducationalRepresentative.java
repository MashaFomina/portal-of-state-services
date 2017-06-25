package services.stateservices.user;

import services.stateservices.entities.EduRequest;
import services.stateservices.errors.NoRightsException;
import services.stateservices.errors.InvalidAppointmentDateException;
import services.stateservices.institutions.EducationalInstitution;
import services.stateservices.errors.NoFreeSeatsException;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import services.stateservices.entities.Feedback;

public class EducationalRepresentative extends User implements InstitutionRepresentative {
    private EducationalInstitution institution;
    private boolean approved;

    public EducationalRepresentative(String login, String password, String fullName, String email, EducationalInstitution institution, boolean approved) {
        super(login, password, fullName, email, User.UserType.EDUCATIONAL_REPRESENTATIVE);
        this.institution = institution;
        this.approved = approved;
    }
    
    public EducationalRepresentative(EducationalRepresentative user) {
        super(user);
        this.institution = user.institution;
        this.approved = user.approved;
    }
    
    public boolean acceptEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this.getInstitution())) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        if (request.isOpened()) {
            request.changeStatus(EduRequest.Status.ACCEPTED_BY_INSTITUTION);
            return true;
        }
        return false;
    }
    
    public void makeAppointment(EduRequest request, Date date) throws NoRightsException, InvalidAppointmentDateException {
        if (!request.getInstitution().equals(this.getInstitution())) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        Date currentDate = new Date();
        if (!date.after(currentDate)) {
            throw new InvalidAppointmentDateException();
        }
        request.makeAppointment(date);
    }
    
    public void refuseEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this.getInstitution())) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        request.changeStatus(EduRequest.Status.REFUSED);
    }
    
    public boolean makeChildEnrolled(EduRequest request) throws NoRightsException, NoFreeSeatsException {
        if (!request.getInstitution().equals(this.getInstitution())) {
            throw new NoRightsException("You have no rights to accept this educational request!");
        }
        
        int classNumber = request.getClassNumber();
        if (request.isAcceptedByParent()) {
            synchronized (this) {
                if (institution.getFreeSeats(request.getClassNumber()) < 1) {
                    throw new NoFreeSeatsException();
                }
                institution.setSeats(classNumber, institution.getSeats(classNumber), institution.getBusySeats(classNumber) + 1);
            }
            request.changeStatus(EduRequest.Status.CHILD_IS_ENROLLED);
            Citizen parent = request.getParent();
            Set<EduRequest> set = parent.getEduRequests();
            Iterator<EduRequest> i = set.iterator();
            while (i.hasNext()) {
                EduRequest r = i.next(); // must be called before you can call i.remove()
                if (!r.equals(request)) {
                    r.getInstitution().removeEduRequest(r);
                    i.remove();
                }
            }
            return true;
        }
        return false;
    }
    
    @Override
    public EducationalInstitution getInstitution() {
        return institution;
    }
    
    public EducationalInstitution editInstitution(String title, String city, String district, String telephone, String fax, String address, Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        institution.edit(title, city, district, telephone, fax, address, seats, busySeats);
        return institution;
    }
    
    public void setSeats(int classNumber, int seats, int busySeats) {
        institution.setSeats(classNumber, seats, busySeats);
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