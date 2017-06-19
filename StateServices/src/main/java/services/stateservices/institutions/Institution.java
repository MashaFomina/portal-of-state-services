package services.stateservices.institutions;

import java.util.HashSet;
import java.util.Set;
import services.stateservices.entities.Feedback;
import services.stateservices.errors.NoRightsException;
import services.stateservices.user.User;

public abstract class Institution {
    private String title;
    private String city;
    private String district;
    private String telephone;
    private String fax;
    private String address;
    protected Set<Feedback> feedbacks = new HashSet<>();
    
    protected Institution(String title, String city, String district, String telephone, String fax, String address) {
        this.title = title;
        this.city = city;
        this.district = district;
        this.telephone = telephone;
        this.fax = fax;
        this.address = address;
    }
        
    public Institution(Institution institution) {
        this.title = institution.title;
        this.city = institution.city;
        this.district = institution.district;
        this.telephone = institution.telephone;
        this.fax = institution.fax;
        this.address = institution.address;
    }
    
    public void edit(String title, String city, String district, String telephone, String fax, String address) {
        this.title = title;
        this.city = city;
        this.district = district;
        this.telephone = telephone;
        this.fax = fax;
        this.address = address;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getCity() {
        return city;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public String getTelephone() {
        return telephone;
    }
    
    public String getFax() {
        return fax;
    }
    
    public String getAddress() {
        return address;
    }
    
    public Set<Feedback> getFeedbacks() {
        return feedbacks;
    }
    
    public abstract boolean addFeedback(Feedback feedback) throws NoRightsException;
}
