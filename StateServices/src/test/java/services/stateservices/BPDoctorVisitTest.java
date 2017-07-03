package services.stateservices;

import java.util.Calendar;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

import static org.junit.Assert.*;
import services.stateservices.entities.Child;
import services.stateservices.entities.Ticket;
import services.stateservices.entities.Feedback;
import services.stateservices.errors.NoRightsException;
import services.stateservices.errors.InvalidTicketsDatesException;
import services.stateservices.institutions.MedicalInstitution;
import services.stateservices.storage.StorageRepository;
import services.stateservices.user.Administrator;
import services.stateservices.user.Citizen;
import services.stateservices.user.MedicalRepresentative;
import services.stateservices.user.Doctor;

public class BPDoctorVisitTest extends TestCase {
    Administrator admin;
    MedicalInstitution institution1, institution2;
    MedicalRepresentative representative1, representative2;
    Citizen citizen;
    Ticket ticket;
    Doctor doctor1, doctor2;
    StorageRepository repository;
    boolean result;

    @Before
    public void setUp() throws Exception {
        /// Adding institution and users to repository
        repository = StorageRepository.getInstance();
        
        admin = repository.getAdministrator("admin");
        admin.signIn("admin");
        institution1 = repository.getMedicalInstitution(6);
        institution2 = repository.getMedicalInstitution(7);
        
        representative1 = repository.getMedicalRepresentative("medr");
        representative1.signIn("pass");
        doctor1 = repository.getDoctor("doctor");
        
        representative2 = repository.getMedicalRepresentative("medr1");
        representative2.signIn("pass");
        doctor2 = repository.getDoctor("doctor1");
        
        citizen = repository.getCitizen("citizen");
        citizen.signIn("pass");
        
        representative1.deleteTickets(doctor1, null);
        representative2.deleteTickets(doctor2, null);
    }

    @After
    public void tearDown() throws Exception {
        admin = null;
        institution1 = null;
        institution2 = null;
        representative1.removeDoctor(doctor1);
        representative1 = null;
        representative2.removeDoctor(doctor2);
        representative2 = null;
        citizen = null;
        ticket = null;
        doctor1 = null;
        doctor2 = null;
        citizen = null;
        repository.clear();
        repository = null;
    }

    @Test
    public void testDoctorVisitBP() throws Exception {
        Date start = new Date();
        start.setHours(10);
        start.setDate(start.getDate() + 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        cal.add(Calendar.HOUR, 2); //minus number would decrement the hours
        Date end = cal.getTime();
        representative1.addTickets(doctor1, start, end, 15);
        assertTrue(institution1.getTickets(doctor1).size() == 8);
        
        Iterator<Ticket> i = institution1.getTickets(doctor1).iterator();
        if (i.hasNext()) {
            ticket = i.next();
        }
        assertFalse(ticket == null);

        citizen.acceptTicket(ticket);
        assertEquals(ticket.getUser(), citizen);
        
        representative1.confirmVisit(ticket, "Healthy!");
        assertTrue(ticket.isVisited());
        
        String text = "The bad work!";
        result = citizen.addFeedback(institution1, text);
        Feedback feedback = null, temp;
        if (result) {
            Iterator<Feedback> k = institution1.getFeedbacks().iterator();
            while (k.hasNext()) {
                temp = k.next();
                if (temp.getUser().equals(citizen) && temp.getText().equals(text)) {
                    feedback = temp;
                }
            }
        }
        assertFalse(feedback == null);

        result = representative1.addFeedbackTo(text, citizen);
        feedback = null;
        if (result) {
            Iterator<Feedback> k = institution1.getFeedbacks().iterator();
            while (k.hasNext()) {
                temp = k.next();
                if (temp.getUser().equals(representative1) && temp.getText().equals(text) && temp.getToUser().equals(citizen)) {
                    feedback = temp;
                }
            }
        }
        assertFalse(feedback == null);
    }

    @Test
    public void testExceptions() {
        Date start = new Date();
        start.setHours(10);
        start.setDate(start.getDate() + 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        cal.add(Calendar.HOUR, -2); //minus number would decrement the hours
        Date end = cal.getTime();
        try {
            representative1.addTickets(doctor1, start, end, 15);
            fail("Start date less then end date!");
        } catch (InvalidTicketsDatesException | NoRightsException e) {
            assertTrue(e.getMessage(), true);
            System.out.println(e.getMessage());
        }
        
        cal.add(Calendar.HOUR, 5); //minus number would decrement the hours
        end = cal.getTime();
        try {
            representative1.addTickets(doctor1, start, end, -15);
            fail("Invalid interval!");
        } catch (InvalidTicketsDatesException | NoRightsException e) {
            assertTrue(e.getMessage(), true);
            System.out.println(e.getMessage());
        }
        
        cal.add(Calendar.HOUR, -100);
        start = cal.getTime();
        try {
            representative1.addTickets(doctor1, start, end, 15);
            fail("Start date in past time!");
        } catch (InvalidTicketsDatesException | NoRightsException e) {
            assertTrue(e.getMessage(), true);
            System.out.println(e.getMessage());
        }
        
        try {
            representative1.addTicket(doctor1, start);
            fail("Date in past timel!");
        } catch (InvalidTicketsDatesException | NoRightsException e) {
            assertTrue(e.getMessage(), true);
            System.out.println(e.getMessage());
        }
        
        try {
            representative1.addTicket(doctor2, start);
            fail("Added ticket of other institution!");
        } catch (InvalidTicketsDatesException | NoRightsException e) {
            assertTrue(e.getMessage(), true);
            System.out.println(e.getMessage());
        }
    }
}
