package services.stateservices;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import services.stateservices.storage.StorageRepository;
import services.stateservices.user.*;
import services.stateservices.entities.*;

public class Main {
    public static void main(String[] args) {
        StorageRepository repository = StorageRepository.getInstance();
        
        Administrator admin = repository.getAdministrator("admin");
        admin.signIn("admin");
        //institution = admin.addMedicalInstitution("hospital № 1", "Saint-Petersburg", "Kirovskyi", "88127777777", "88127777777", "pr. Veteranov h. 69");
        
        /*MedicalRepresentative representative = repository.getMedicalRepresentative("medr");
        representative.signIn("pass");
        Doctor doctor = repository.getDoctor("doctor");*/
        
        Citizen citizen = repository.getCitizen("citizen");
        for (Child c:  citizen.getChilds().values())
            System.out.println(c.getFullName() + " " + c.getBirthCertificate() + " " + c.getBirthDate());
        for (Notification n:  citizen.getNotifications())
            System.out.println(n.getNotification());
        
        Map<Integer, Integer> seats = new HashMap<>(); // key - class number, value - seats
        Map<Integer, Integer> busySeats = new HashMap<>(); // key - class number, value - busy seats
        seats.put(1, 50);
        busySeats.put(1, 10);
        seats.put(2, 40);
        busySeats.put(2, 30);
        repository.addEducationalInstitution("title", "city", "district", "telephone", "fax", "address", seats, busySeats);
    }
}
