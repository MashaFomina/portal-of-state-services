package services.stateservices.entities;

import java.util.Date;
import services.stateservices.user.Citizen;

public class Child {
    private int id;
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

    public Child(int id, String fullName, String birthCertificate, Date birthDate) {
        this.id = id;
        this.fullName = fullName;
        this.birthCertificate = birthCertificate;
        this.birthDate = birthDate;
    }
    
        
    public Child(Child child) {
        this.id = child.id;
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
    
    public void setParent(Citizen parent) {
        this.parent = parent;
    }
    
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }
    
    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Child other = (Child) obj;
        return (id == other.getId() && 
                (parent != null ? parent.equals(other.getParent()) : other.getParent() == null) && 
                birthCertificate.equals(other.getBirthCertificate())
                );
    }
    
    @Override
    public int hashCode() {
        return Integer.toString(id).hashCode();
    }
}
