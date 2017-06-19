package services.stateservices.entities;

import java.util.Date;
import services.stateservices.user.Citizen;

public class Child {
    private Citizen parent; 
    private String fullName;
    private String birthCertificate;
    private Date birthDate;
    
    public Child(Citizen parent, String fullName, String birthCertificate, Date birthDate) {
        this.parent = parent;
        this.fullName = fullName;
        this.birthCertificate = birthCertificate;
        this.birthDate = birthDate;
    }

    public Child(Child child) {
        this.parent = child.parent;
        this.fullName = child.fullName;
        this.birthCertificate = child.birthCertificate;
        this.birthDate = child.birthDate;
    }
    
    public Citizen getParent() {
        return parent;
    }
    
    public String getFullName() {
        return fullName;
    }
    
    public String getBirthCertificate() {
        return birthCertificate;
    }
    
    public Date getBirthDate() {
        return birthDate;
    }
}
