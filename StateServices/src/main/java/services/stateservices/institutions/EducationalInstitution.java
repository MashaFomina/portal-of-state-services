package services.stateservices.institutions;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Date;
import services.stateservices.entities.EduRequest;
import services.stateservices.entities.Feedback;
import services.stateservices.errors.NoRightsException;
import services.stateservices.user.User;

public class EducationalInstitution extends Institution {
    private Map<Integer, Integer> seats = new HashMap<>(); // key - class number, value - seats
    private Map<Integer, Integer> busySeats = new HashMap<>(); // key - class number, value - busy seats
    private List<EduRequest> requests = new ArrayList<>();
    
    public EducationalInstitution(String title, String city, String district, String telephone, String fax, String address, Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        super(title, city, district, telephone, fax, address);
        if (seats != null && busySeats != null) {
            this.seats = seats;
            this.busySeats = busySeats;
        }
    }
    
    public EducationalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        super(title, city, district, telephone, fax, address);
    }
    
    public EducationalInstitution(EducationalInstitution institution) {
        super(institution);
        this.seats = institution.seats;
        this.busySeats = institution.busySeats;
    }
    
    public void edit(String title, String city, String district, String telephone, String fax, String address, Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        this.edit(title, city, district, telephone, fax, address);
        if (seats != null && busySeats != null) {
            this.seats = seats;
            this.busySeats = busySeats;
        }
        updated = true;
    }
    
    public List<EduRequest> getEduRequests() {
        return requests;
    }
        
    public int getSeats(int classNumber) {
        if (seats.containsKey(classNumber)) {
            return seats.get(classNumber);
        }
        return 0;
    }
    
    public int getBusySeats(int classNumber) {
        if (busySeats.containsKey(classNumber)) {
            return busySeats.get(classNumber);
        }
        return 0;
    }
    
    public int getFreeSeats(int classNumber) {
        if (busySeats.containsKey(classNumber) && seats.containsKey(classNumber)) {
            return (seats.get(classNumber) - busySeats.get(classNumber));
        }
        return 0;
    }
    
    public  Map<Integer, Integer> getSeats() {
        return seats;
    }
    
    public  Map<Integer, Integer> getBusySeats() {
        return busySeats;
    }
    
    public void setSeats(int classNumber, int seats, int busySeats) {
        this.seats.put(classNumber, seats);
        this.busySeats.put(classNumber, busySeats);
        updated = true;
    }
    
    public void setSeats(Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        this.seats = seats;
        this.busySeats = busySeats;
        updated = true;
    }

    public void setEduRequests(List<EduRequest> requests) {
        this.requests = requests;
    }
    
    public void addEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add educational request with other institution to this institution!");
        }
        requests.add(request);
    }
    
    public boolean createEduRequest(EduRequest request) throws NoRightsException {
        if (!request.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add educational request with other institution to this institution!");
        }
        boolean result = false;
        if (getFreeSeats(request.getClassNumber()) > 0) {
            requests.add(request);
            result = true;
        }
        return result;
    }
    
    public void removeEduRequest(EduRequest request) {
        if (requests.contains(request)) {
            requests.remove(request);
        }
    }
    
    @Override
    public boolean saveFeedback(Feedback feedback) throws NoRightsException {
        if (!feedback.getInstitution().equals(this)) {
            throw new NoRightsException("You have no ability to add feedback of other institution to this institution!");
        }
        feedbacks.add(feedback);
        return true;
    }
}