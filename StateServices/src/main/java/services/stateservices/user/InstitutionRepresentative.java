package services.stateservices.user;

import java.util.Date;
import services.stateservices.entities.Feedback;
import services.stateservices.errors.NoRightsException;
import services.stateservices.institutions.Institution;

public interface InstitutionRepresentative {
    public Institution getInstitution();
    public boolean isApproved(); // Check if representative is approved by administrator or by representative (while saving)
    public void approve(User user) throws NoRightsException; // Approve by administrator
    public boolean addFeedback(String text) throws NoRightsException;
    public boolean addFeedbackTo(String text, User userTo) throws NoRightsException;
}
