package services.stateservices.entities;

import services.stateservices.institutions.EducationalInstitution;
import java.util.Date;
import services.stateservices.user.Citizen;

public class EduRequest {
    public enum Status {
        OPENED("OPENED"),
        ACCEPTED_BY_INSTITUTION("ACCEPTED_BY_INSTITUTION"),
        ACCEPTED_BY_PARENT("ACCEPTED_BY_PARENT"),
        REFUSED("REFUSED"),
        CHILD_IS_ENROLLED("CHILD_IS_ENROLLED");
        private String text;
        
        Status(String text) {
          this.text = text;
        }

        public String getText() {
          return this.text;
        }
        
        public String getBeautifulText() {
            String text = getText();
            if (text.equals("ACCEPTED_BY_INSTITUTION")) return "ACCEPTED BY INSTITUTION";
            if (text.equals("ACCEPTED_BY_PARENT")) return "ACCEPTED BY PARENT";
            if (text.equals("CHILD_IS_ENROLLED")) return "CHILD IS ENROLLED";
            return text;
        }

        public static Status fromString(String text) {
          for (Status b : Status.values()) {
            if (b.text.equalsIgnoreCase(text)) {
              return b;
            }
          }
          return null;
        }
    }
    
    private int id;
    private Status status;
    private Child child;
    private Citizen parent;
    private EducationalInstitution institution;
    private Date creationDate;
    private Date appointment;
    private int classNumber;
    private boolean updated = false;
    
    public EduRequest(Status status, Child child, Citizen parent, EducationalInstitution institution, Date creationDate, Date appointment, int classNumber) {
        if (status == null) {
            this.status = Status.OPENED;
        }
        else {
            this.status = status;
        }
        this.child = child;
        this.parent = parent;
        this.institution = institution;
        this.creationDate = creationDate;
        this.appointment = appointment;
        this.classNumber = classNumber;
    }
        
    public EduRequest(EduRequest request) {
        this.status = request.status;
        this.child = request.child;
        this.parent = request.parent;
        this.institution = request.institution;
        this.creationDate = request.creationDate;
        this.appointment = request.appointment;
        this.classNumber = request.classNumber;
    }
 
    public boolean isUpdated() {
        return updated;
    }
    
    public void resetUpdated() {
        updated = false;
    }
    
    public void setParent(Citizen parent) { this.parent = parent; }
    public void setInstitution(EducationalInstitution institution) { this.institution = institution; }
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }
    
    public void changeStatus(Status status) {
        this.status = status;
        updated = true;
    }
    
    public void makeAppointment(Date appointment) {
        this.appointment = appointment;
        updated = true;
    }
    
    public EducationalInstitution getInstitution() {
        return institution;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public boolean isAcceptedByInstitution() {
        return (status.equals(Status.ACCEPTED_BY_INSTITUTION));
    }
    
    public boolean isAcceptedByParent() {
        return (status.equals(Status.ACCEPTED_BY_PARENT));
    }
    
    public boolean isOpened() {
        return (status.equals(Status.OPENED));
    }
    
    public boolean isChildEnrolled() {
        return (status.equals(Status.CHILD_IS_ENROLLED));
    }
    
    public Child getChild() {
        return child;
    }
    
    public Citizen getParent() {
        return parent;
    }
    
    public Date getCreationDate() {
        return creationDate;
    }
    
    public Date getAppointment() {
        return appointment;
    }
    
    public int getClassNumber() {
        return classNumber;
    }
     
    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        EduRequest other = (EduRequest) obj;
        return (id == other.getId() && 
                (child != null ? child.equals(other.getChild()) : other.getChild() == null) && 
                (institution != null ? institution.equals(other.getInstitution()) : other.getInstitution() == null) && 
                (creationDate != null ? creationDate.equals(other.getCreationDate()) : other.getCreationDate() == null)
                );
    }
    
    @Override
    public int hashCode() {
        return Integer.toString(id).hashCode();
    }
}
