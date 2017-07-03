package services.stateservices;

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.stateservices.entities.Child;
import services.stateservices.entities.Feedback;
import services.stateservices.entities.Ticket;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.storage.StorageRepository;
import services.stateservices.user.Administrator;
import services.stateservices.user.Citizen;
import services.stateservices.user.Doctor;
import services.stateservices.user.MedicalRepresentative;

public class BPChildDoctorVisitTest extends TestCase {
    Administrator admin;
    MedicalInstitution institution;
    MedicalRepresentative representative;
    Citizen citizen;
    Child child;
    Ticket ticket;
    Doctor doctor;
    StorageRepository repository;
    boolean result;

    @Before
    public void setUp() throws Exception {
        /// Adding institution and users to repository
        repository = StorageRepository.getInstance();
        
        admin = repository.getAdministrator("admin");
        admin.signIn("admin");
        institution = repository.getMedicalInstitution(6);
        
        representative = repository.getMedicalRepresentative("medr");
        representative.signIn("pass");
        doctor = repository.getDoctor("doctor");
        
        citizen = repository.getCitizen("citizen");
        citizen.signIn("pass");
        
        representative.deleteTickets(doctor, null);
    }

    @After
    public void tearDown() throws Exception {
        admin = null;
        institution = null;
        representative.removeDoctor(doctor);
        representative = null;
        citizen = null;
        child = null;
        ticket = null;
        doctor = null;
        citizen = null;
        child = null;
        repository.clear();
        repository = null;
    }

    @Test
    public void testChildDoctorVisitBP() throws Exception {
        Date birthDate = new Date(115, 0, 14);
        String birthCertificate = "IJ12293949";
        citizen.createChildInfo("F I O", birthCertificate, birthDate);
        child = citizen.getChild(birthCertificate);
        assertTrue(child != null && child.getBirthCertificate().equals(birthCertificate));
        
        Date start = new Date();
        start.setHours(10);
        start.setDate(start.getDate() + 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        cal.add(Calendar.HOUR, 2); //minus number would decrement the hours
        Date end = cal.getTime();
        representative.addTickets(doctor, start, end, 15);
        assertTrue(institution.getTickets(doctor).size() == 8);
        
        Iterator<Ticket> i = institution.getTickets(doctor).iterator();
        if (i.hasNext()) {
            ticket = i.next();
        }
        assertFalse(ticket == null);

        citizen.acceptTicketForChild(ticket, child);
        assertEquals(ticket.getUser(), citizen);
        assertTrue(ticket.isTicketForChild());
        assertEquals(ticket.getChild(), child);
        
        representative.confirmVisit(ticket, "Healthy!");
        assertTrue(ticket.isVisited());
        
        String text = "The bad work!";
        result = citizen.addFeedback(institution, text);
        Feedback feedback = null, temp;
        if (result) {
            Iterator<Feedback> k = institution.getFeedbacks().iterator();
            while (k.hasNext()) {
                temp = k.next();
                if (temp.getUser().equals(citizen) && temp.getText().equals(text)) {
                    feedback = temp;
                }
            }
        }
        assertFalse(feedback == null);

        result = representative.addFeedbackTo(text, citizen);
        feedback = null;
        if (result) {
            Iterator<Feedback> k = institution.getFeedbacks().iterator();
            while (k.hasNext()) {
                temp = k.next();
                if (temp.getUser().equals(representative) && temp.getText().equals(text) && temp.getToUser().equals(citizen)) {
                    feedback = temp;
                }
            }
        }
        assertFalse(feedback == null);
    }
}
