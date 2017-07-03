package services.stateservices.institutions;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import services.stateservices.entities.Feedback;
import services.stateservices.errors.NoRightsException;
import services.stateservices.user.User;

public abstract class Institution {
    private int id;
    private String title;
    private String city;
    private String district;
    private String telephone;
    private String fax;
    private String address;
    protected boolean updated = false;
    protected List<Feedback> feedbacks = new ArrayList<>();
    
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
        updated = true;
    }
    
    public boolean isUpdated() {
        return updated;
    }
    
    public void resetUpdated() {
        updated = false;
    }
    
    public void setId(int id) { this.id = id; }
    public int getId() { return id; }
    
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
    
    public List<Feedback> getFeedbacks() {
        return feedbacks;
    }
    
    public void setFeedbacks(List<Feedback> feedbacks) {
        this.feedbacks = feedbacks;
    }
    
    public abstract boolean saveFeedback(Feedback feedback) throws NoRightsException;
    
    public void addFeedback(Feedback feedback) {
        feedbacks.add(feedback);
    }
    
    @Override
    public boolean equals(Object obj) {
        if ( (obj == null) || (obj.getClass() != this.getClass()) ) return false;
        Institution other = (Institution) obj;
        return (id == other.getId() && title.equals(other.getTitle()) && telephone.equals(other.getTelephone()));
    }
    
    @Override
    public int hashCode() {
        return Integer.toString(id).hashCode();
    }
}
