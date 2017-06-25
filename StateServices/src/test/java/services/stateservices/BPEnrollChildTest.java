package services.stateservices;

import java.util.Date;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import services.stateservices.entities.Child;
import services.stateservices.entities.EduRequest;
import services.stateservices.institutions.EducationalInstitution;
import services.stateservices.user.*;
import services.stateservices.storage.StorageRepository;
import services.stateservices.errors.NoRightsException;
import services.stateservices.errors.InvalidAppointmentDateException;
import services.stateservices.errors.NoFreeSeatsException;

public class BPEnrollChildTest extends TestCase {
    Administrator admin;
    EducationalInstitution institution1, institution2;
    EducationalRepresentative educationalRepresentative;
    Citizen citizen, otherCitizen;
    Child child;
    EduRequest request1, request2;
    StorageRepository repository;

    @Before
    public void setUp() throws Exception {
        /// Adding institution and users to repository
        repository = StorageRepository.getInstance();
        
        admin = repository.getAdministrator("admin");
        admin.signIn("admin");
        institution1 = admin.addEducationalInstitution("school № 1", "Saint-Petersburg", "Kirovskyi", "88127777777", "88127777777", "pr. Veteranov h. 69");
        institution2 = admin.addEducationalInstitution("school № 2", "Saint-Petersburg", "Kirovskyi", "88127777779", "88127777779", "pr. Veteranov h. 79");
        
        educationalRepresentative = repository.getEducationalRepresentative("edur");
        educationalRepresentative.signIn("pass");
        
        citizen = repository.getCitizen("citizen");
        citizen.signIn("pass");
         
        otherCitizen = repository.getCitizen("citizen1");
        otherCitizen.signIn("pass");
    }

    @After
    public void tearDown() throws Exception {
        repository.clear();
        repository = null;
        admin = null;
        institution1 = null;
        institution2 = null;
        request1 = null;
        request2 = null;
        educationalRepresentative = null;
        citizen = null;
        otherCitizen = null;
        child = null;
    }

    @Test
    public void testEnrollChildBP() throws Exception {
        educationalRepresentative.setSeats(1, 50, 10);
        assertTrue(institution1.getFreeSeats(1) == 40);
        institution2.setSeats(1, 20, 5);
        assertTrue(institution2.getFreeSeats(1) == 15);

        Date birthDate = new Date(2015, 0, 14);
        String birthCertificate = "IJ1229394844";
        citizen.createChildInfo("F I O", birthCertificate, birthDate);
        child = citizen.getChild(birthCertificate);
        assertTrue(child != null && child.getBirthCertificate().equals(birthCertificate));
        
        request1 = citizen.createEduRequest(child, institution1, 1);
        assertTrue(request1.isOpened());
        request2 = citizen.createEduRequest(child, institution2, 1);
        assertTrue(request2.isOpened());
        Set<EduRequest> set = citizen.getEduRequests();
        assertTrue(set.contains(request1));
        assertTrue(set.contains(request2));
        set = institution1.getEduRequests();
        assertTrue(set.contains(request1));
        set = institution2.getEduRequests();
        assertTrue(set.contains(request2));

        educationalRepresentative.acceptEduRequest(request1);
        assertTrue(request1.isAcceptedByInstitution());

        citizen.acceptEduRequest(request1);
        assertTrue(request1.isAcceptedByParent());

        Date appointmentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(appointmentDate);
        cal.add(Calendar.DATE, 7); //minus number would decrement the days
        appointmentDate = cal.getTime();
        educationalRepresentative.makeAppointment(request1, appointmentDate);
        assertEquals(appointmentDate, request1.getAppointment());
        
        educationalRepresentative.makeChildEnrolled(request1);
        assertTrue(request1.isChildEnrolled());
        set = citizen.getEduRequests();
        assertTrue(set.contains(request1));
        assertFalse(set.contains(request2));
        set = institution1.getEduRequests();
        assertTrue(set.contains(request1));
        set = institution2.getEduRequests();
        assertFalse(set.contains(request2));
    }


    @Test
    public void testEducationalRepresentativeNoRights() throws Exception {
        educationalRepresentative.setSeats(1, 50, 10);
        assertTrue(institution1.getFreeSeats(1) == 40);
        institution2.setSeats(1, 20, 5);
        assertTrue(institution2.getFreeSeats(1) == 15);

        Date birthDate = new Date(2015, 0, 14);
        String birthCertificate = "IJ1229394844";
        citizen.createChildInfo("F I O", birthCertificate, birthDate);
        child = citizen.getChild(birthCertificate);
        assertTrue(child != null && child.getBirthCertificate().equals(birthCertificate));
        
        request1 = citizen.createEduRequest(child, institution1, 1);
        assertTrue(request1.isOpened());
        request2 = citizen.createEduRequest(child, institution2, 1);
        assertTrue(request2.isOpened());
        Set<EduRequest> set = citizen.getEduRequests();
        assertTrue(set.contains(request1));
        assertTrue(set.contains(request2));
        set = institution1.getEduRequests();
        assertTrue(set.contains(request1));
        set = institution2.getEduRequests();
        assertTrue(set.contains(request2));

        try {
            educationalRepresentative.acceptEduRequest(request2);
            fail("Wrong educational representative accepted educational request!");
        } catch (NoRightsException e) {
            assertTrue(e.getMessage(), true);
        }
    }
    
    @Test
    public void testCitizenNoRights() throws Exception {
        educationalRepresentative.setSeats(1, 50, 10);
        assertTrue(institution1.getFreeSeats(1) == 40);
        institution2.setSeats(1, 20, 5);
        assertTrue(institution2.getFreeSeats(1) == 15);

        Date birthDate = new Date(2015, 0, 14);
        String birthCertificate = "IJ1229394844";
        citizen.createChildInfo("F I O", birthCertificate, birthDate);
        child = citizen.getChild(birthCertificate);
        assertTrue(child != null && child.getBirthCertificate().equals(birthCertificate));
        
        request1 = citizen.createEduRequest(child, institution1, 1);
        assertTrue(request1.isOpened());
        request2 = citizen.createEduRequest(child, institution2, 1);
        assertTrue(request2.isOpened());
        Set<EduRequest> set = citizen.getEduRequests();
        assertTrue(set.contains(request1));
        assertTrue(set.contains(request2));
        set = institution1.getEduRequests();
        assertTrue(set.contains(request1));
        set = institution2.getEduRequests();
        assertTrue(set.contains(request2));

        educationalRepresentative.acceptEduRequest(request1);
        assertTrue(request1.isAcceptedByInstitution());

        try {
            otherCitizen.acceptEduRequest(request2);
            fail("Wrong citizen accepted educational request!");
        } catch (NoRightsException e) {
            assertTrue(e.getMessage(), true);
        }
    }

    @Test
    public void testInvalidAppointmentDate() throws Exception {
        educationalRepresentative.setSeats(1, 50, 10);
        assertTrue(institution1.getFreeSeats(1) == 40);
        institution2.setSeats(1, 20, 5);
        assertTrue(institution2.getFreeSeats(1) == 15);

        Date birthDate = new Date(2015, 0, 14);
        String birthCertificate = "IJ1229394844";
        citizen.createChildInfo("F I O", birthCertificate, birthDate);
        child = citizen.getChild(birthCertificate);
        assertTrue(child != null && child.getBirthCertificate().equals(birthCertificate));
        
        request1 = citizen.createEduRequest(child, institution1, 1);
        assertTrue(request1.isOpened());
        request2 = citizen.createEduRequest(child, institution2, 1);
        assertTrue(request2.isOpened());
        Set<EduRequest> set = citizen.getEduRequests();
        assertTrue(set.contains(request1));
        assertTrue(set.contains(request2));
        set = institution1.getEduRequests();
        assertTrue(set.contains(request1));
        set = institution2.getEduRequests();
        assertTrue(set.contains(request2));

        educationalRepresentative.acceptEduRequest(request1);
        assertTrue(request1.isAcceptedByInstitution());

        citizen.acceptEduRequest(request1);
        assertTrue(request1.isAcceptedByParent());

        Date appointmentDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(appointmentDate);
        cal.add(Calendar.DATE, -7); //minus number would decrement the days
        appointmentDate = cal.getTime();
        try {
            educationalRepresentative.makeAppointment(request1, appointmentDate);
            fail("Invalid appointment date was set for educational request!");
        } catch (InvalidAppointmentDateException e) {
            assertTrue(e.getMessage(), true);
        }
    }
    
    @Test
    public void testNoFreeSeats() throws Exception {
        educationalRepresentative.setSeats(1, 0, 0);
        assertTrue(institution1.getFreeSeats(1) == 0);
        institution2.setSeats(1, 0, 5);
        assertTrue(institution2.getFreeSeats(1) == -5);

        Date birthDate = new Date(2015, 0, 14);
        String birthCertificate = "IJ1229394844";
        citizen.createChildInfo("F I O", birthCertificate, birthDate);
        child = citizen.getChild(birthCertificate);
        assertTrue(child != null && child.getBirthCertificate().equals(birthCertificate));

        try {
            request1 = citizen.createEduRequest(child, institution1, 1);      
            fail("Created educational request whet there no free seats!");
        } catch (NoFreeSeatsException e) {
            assertTrue(e.getMessage(), true);
        }
        Set<EduRequest> set = citizen.getEduRequests();
        assertFalse(set.contains(request1));
        set = institution1.getEduRequests();
        assertFalse(set.contains(request1));
        
        try {
            request2 = citizen.createEduRequest(child, institution2, 1);      
            fail("Created educational request whet there no free seats!");
        } catch (NoFreeSeatsException e) {
            assertTrue(e.getMessage(), true);
        }
        set = citizen.getEduRequests();
        assertFalse(set.contains(request2));
        set = institution2.getEduRequests();
        assertFalse(set.contains(request2));
        
        try {
            request2 = citizen.createEduRequest(child, institution2, 5);      
            fail("Created educational request whet there no free seats!");
        } catch (NoFreeSeatsException e) {
            assertTrue(e.getMessage(), true);
        }
        set = citizen.getEduRequests();
        assertFalse(set.contains(request2));
        set = institution2.getEduRequests();
        assertFalse(set.contains(request2));
    }
}
