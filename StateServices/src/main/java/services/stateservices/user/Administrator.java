package services.stateservices.user;

import java.util.Map;
import services.stateservices.errors.NoRightsException;
import services.stateservices.institutions.EducationalInstitution;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.storage.StorageRepository;

public class Administrator extends User {
    public Administrator(String login, String password, String fullName, String email) {
        super(login, password, fullName, email, User.UserType.ADMINISTRATOR);
    }
    
    public Administrator(Administrator user) {
        super(user);
    }
    
    public EducationalInstitution addEducationalInstitution(String title, String city, String district, String telephone, String fax, String address, Map<Integer, Integer> seats, Map<Integer, Integer> busySeats) {
        return repository.addEducationalInstitution(title, city, district, telephone, fax, address, seats, busySeats);
    }
    
    public EducationalInstitution addEducationalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        return repository.addEducationalInstitution(title, city, district, telephone, fax, address);
    }
        
    public MedicalInstitution addMedicalInstitution(String title, String city, String district, String telephone, String fax, String address) {
        return repository.addMedicalInstitution(title, city, district, telephone, fax, address);
    }
    
    public void approveInstitutionRepresentative(InstitutionRepresentative representative) throws NoRightsException {
        representative.approve(this);
    }
}
