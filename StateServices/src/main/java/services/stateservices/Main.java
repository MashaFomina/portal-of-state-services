package services.stateservices;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import services.stateservices.storage.StorageRepository;
import services.stateservices.user.*;
import services.stateservices.entities.*;
import services.stateservices.errors.AlreadyExistsException;
import services.stateservices.errors.NoFreeSeatsException;
import services.stateservices.facade.Facade;
import services.stateservices.errors.NoRightsException;
import services.stateservices.institutions.*;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import services.stateservices.errors.InvalidTicketsDatesException;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.io.IOException;
import services.stateservices.controllers.*;
import services.stateservices.service.NotificationEmaiService;

public class Main extends Application {
    private static Stage mainStage;
    public static Facade facade = new Facade();

    /*private static Map<String, Stage> projectViews = new HashMap<>();
    private static Map<String, Stage> userViews = new HashMap<>();
    private static Map<Integer, Stage> milestoneViews = new HashMap<>();
    private static Map<Integer, Stage> ticketViews = new HashMap<>();
    private static Map<Integer, Stage> reportViews = new HashMap<>();*/
    
    public static void main(String[] args) {
        /*NotificationEmaiService service = new NotificationEmaiService("mariyaaleksandrowna@yandex.ru", "Notification!");
        if (service.sendNotification()) System.out.println("Notification sent!");*/

        launch(args);
        /*StorageRepository repository = StorageRepository.getInstance();
        
        Administrator admin = repository.getAdministrator("admin");
        admin.signIn("admin");
        //institution = admin.addMedicalInstitution("hospital № 1", "Saint-Petersburg", "Kirovskyi", "88127777777", "88127777777", "pr. Veteranov h. 69");
        
        MedicalInstitution institution0 = repository.getMedicalInstitution(3);
        for (Ticket t: institution0.getTickets())
            System.out.println(t.getInstitution().getTitle() + " " + (t.getUser() != null ? t.getUser().getFullName() : "") + " " + (t.getChild() != null ? t.getChild().getFullName() : "") + " " + t.getDoctor().getFullName() + " " + t.getDate() + " " + t.isVisited() + " " + t.getSummary());
        for (Feedback f:  institution0.getFeedbacks())
            System.out.println(f.getText() + " " + f.getDate() + " " + f.getInstitution().getTitle() + " " + f.getUser().getFullName() + " " + ((f.getToUser() != null) ? f.getToUser().getFullName() : ""));
        for (Doctor d:  institution0.getDoctors())
            System.out.println(d.getInstitution().getTitle() + " " + d.getFullName());
        
        
        Citizen citizen = repository.getCitizen("citizen");
        Child child = null;
        EduRequest request = null;
        Ticket ticket = null;
        for (Child c:  citizen.getChilds().values()) {
            child = c;
            System.out.println(c.getFullName() + " " + c.getBirthCertificate() + " " + c.getBirthDate());
        }
        for (Notification n:  citizen.getNotifications())
            System.out.println(n.getNotification());
        for (EduRequest c: citizen.getEduRequests()) {
            request = c;
            System.out.println(c.getInstitution().getTitle() + " " + c.getStatus().getText() + " " + c.getChild().getFullName() + " " + c.getParent().getFullName() + " " + c.getClassNumber() + " " + c.getCreationDate() + " " + c.getAppointment());
        }
        for (Ticket t: citizen.getTickets()) {
            ticket = t;
            System.out.println(t.getInstitution().getTitle() + " " + (t.getUser() != null ? t.getUser().getFullName() : "") + " " + (t.getChild() != null ? t.getChild().getFullName() : "") + " " + t.getDoctor().getFullName() + " " + t.getDate() + " " + t.isVisited() + " " + t.getSummary());
        }
        EducationalRepresentative representative0 = repository.getEducationalRepresentative("edur");
        EducationalInstitution institution = repository.getEducationalInstitution(1);
        for (EduRequest c:  institution.getEduRequests())
            System.out.println(c.getInstitution().getTitle() + " " + c.getStatus().getText() + " " + c.getChild().getFullName() + " " + c.getParent().getFullName() + " " + c.getClassNumber() + " " + c.getCreationDate() + " " + c.getAppointment());
        for (Feedback f:  institution.getFeedbacks())
            System.out.println(f.getText() + " " + f.getDate() + " " + f.getInstitution().getTitle() + " " + f.getUser().getFullName() + " " + ((f.getToUser() != null) ? f.getToUser().getFullName() : ""));
        MedicalRepresentative representative1 = repository.getMedicalRepresentative("medr");
        repository.authenticateUser(representative0, "pass");
        if (representative0.isAuthenticated()) System.out.println("representative0 is authenticated!");
 
        System.out.println(representative0.getInstitution().getTitle());
        System.out.println(representative1.getInstitution().getTitle());
        
        Doctor doctor = repository.getDoctor("doctor");
        System.out.println("Doctor institution: " + doctor.getInstitution().getTitle());
        */

        /*Map<Integer, Integer> seats = new HashMap<>(); // key - class number, value - seats
        Map<Integer, Integer> busySeats = new HashMap<>(); // key - class number, value - busy seats
        seats.put(1, 50);
        busySeats.put(1, 10);
        seats.put(2, 40);
        busySeats.put(2, 30);
        repository.addEducationalInstitution("title", "city", "district", "telephone", "fax", "address", seats, busySeats);*/
        //repository.addMedicalInstitution("title", "city", "district", "telephone", "fax", "address");
        
        /*Doctor newUser = new Doctor("login", "password", "fullName", "email", institution0, "position", "summary", true);
        Administrator newUser1 = new Administrator("login0", "password", "fullName", "email");
        MedicalRepresentative newUser2 = new MedicalRepresentative("login1", "password", "fullName", "email", institution0, true);
        EducationalRepresentative newUser3 = new EducationalRepresentative("login3", "password", "fullName", "email", institution, true);
        Citizen newUser4 = new Citizen("login2", "password", "fullName", "email", "970878", "55757", new Date());
        */
        /*try {
            repository.addUser(newUser);
            repository.addUser(newUser1);
            repository.addUser(newUser2);
            repository.addUser(newUser3);
            repository.addUser(newUser4);
        } catch (AlreadyExistsException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        /*if (child != null && request != null) {
            try {
                System.out.println("Here");
                //EduRequest request1 = citizen.createEduRequest(child, institution, 2);
                request.makeAppointment(new Date());
                request.changeStatus(EduRequest.Status.ACCEPTED_BY_INSTITUTION);
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                System.out.println(dateFormat.format(request.getAppointment())); //2016/11/16 12:08:43
                repository.updateEducationalInstitution(institution);
            } catch (SQLException ex) { //NoRightsException | NoFreeSeatsException |
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        try {
            representative1.addTicket(doctor, new Date());
            repository.updateMedicalInstitution(doctor.getInstitution());
        } catch (NoRightsException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidTicketsDatesException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (SQLException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (ticket != null) {
            ticket.refuseTicket();
            ticket.setVisited(true, "summary");
            try {
                repository.updateMedicalInstitution(ticket.getInstitution());
            } catch (SQLException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }*/
    }

    @Override
    public void start(Stage stage) throws Exception {
        mainStage = stage;
        mainStage.setTitle("Portal of state services");

        showSignInView();

        mainStage.show();
    }

    public static void showSignInView() {
        try {
            String fxmlFile = "/fxml/SignIn.fxml";
            FXMLLoader loader = new FXMLLoader();
            AnchorPane root = null;
            root = (AnchorPane) loader.load(Main.class.getClass().getResourceAsStream(fxmlFile));
            Scene scene = new Scene(root, 384, 275);
            mainStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showMainCitizenView(String user) {
        try {
            String fxmlFile = "/fxml/MainCitizenView.fxml";
            FXMLLoader loader = new FXMLLoader();
            AnchorPane root = (AnchorPane) loader.load(Main.class.getClass().getResourceAsStream(fxmlFile));
            MainCitizenViewController uvc = loader.getController();
            uvc.setup(user);
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.setOnCloseRequest(windowEvent -> Platform.exit());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     
    public static void showEducationalRepresentativeView(String user) {
        try {
            String fxmlFile = "/fxml/MainEducationalRepresentativeView.fxml";
            FXMLLoader loader = new FXMLLoader();
            AnchorPane root = (AnchorPane) loader.load(Main.class.getClass().getResourceAsStream(fxmlFile));
            MainCitizenViewController uvc = loader.getController();
            uvc.setup(user);
            Scene scene = new Scene(root);
            mainStage.setScene(scene);
            mainStage.setOnCloseRequest(windowEvent -> Platform.exit());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /*public static void showRegisterView() {
        try {
            String fxmlFile = "/fxml/RegisterView.fxml";
            FXMLLoader loader = new FXMLLoader();
            AnchorPane root = (AnchorPane) loader.load(Main.class.getClass().getResourceAsStream(fxmlFile));
            Scene scene = new Scene(root, 341, 277);
            mainStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showUserView(String user) {
        if (userViews.containsKey(user)) {
            userViews.get(user).toFront();
            return;
        }
        try {
            Stage stage = new Stage();
            String fxmlFile = "/fxml/UserView.fxml";
            FXMLLoader loader = new FXMLLoader();
            AnchorPane root = (AnchorPane) loader.load(Main.class.getClass().getResourceAsStream(fxmlFile));
            UserViewController uvc = loader.getController();
            uvc.setup(user);
            Scene scene = new Scene(root, 225, 130);
            stage.setScene(scene);
            stage.setTitle("User info");
            stage.show();
            stage.setOnCloseRequest(windowEvent -> userViews.remove(user));
            userViews.put(user, stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showProjectView(String user, String project) {
        if (projectViews.containsKey(project)) {
            projectViews.get(project).toFront();
            return;
        }
        try {
            Stage stage = new Stage();
            String fxmlFile = "/fxml/ProjectView.fxml";
            FXMLLoader loader = new FXMLLoader();
            AnchorPane root = (AnchorPane) loader.load(Main.class.getClass().getResourceAsStream(fxmlFile));
            ProjectViewController uvc = loader.getController();
            uvc.setup(user, project);
            Scene scene = new Scene(root, 600, 600);
            stage.setScene(scene);
            stage.setTitle("Project info");
            stage.show();
            stage.setOnCloseRequest(windowEvent -> projectViews.remove(project));
            projectViews.put(project, stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showMilestoneView(String user, String project, Integer milestone) {
        if (milestoneViews.containsKey(milestone)) {
            milestoneViews.get(milestone).toFront();
            return;
        }
        try {
            Stage stage = new Stage();
            String fxmlFile = "/fxml/MilestoneView.fxml";
            FXMLLoader loader = new FXMLLoader();
            AnchorPane root = (AnchorPane) loader.load(Main.class.getClass().getResourceAsStream(fxmlFile));
            MilestoneViewController uvc = loader.getController();
            uvc.setup(user, project, milestone);
            Scene scene = new Scene(root, 320, 440);
            stage.setScene(scene);
            stage.setTitle("Milestone info");
            stage.show();
            stage.setOnCloseRequest(windowEvent -> milestoneViews.remove(milestone));
            milestoneViews.put(milestone, stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showTicketView(String user, String project, Integer ticket) {
        if (ticketViews.containsKey(ticket)) {
            ticketViews.get(ticket).toFront();
            return;
        }
        try {
            Stage stage = new Stage();
            String fxmlFile = "/fxml/TicketView.fxml";
            FXMLLoader loader = new FXMLLoader();
            AnchorPane root = (AnchorPane) loader.load(Main.class.getClass().getResourceAsStream(fxmlFile));
            TicketViewController uvc = loader.getController();
            uvc.setup(user, project, ticket);
            Scene scene = new Scene(root, 600, 400);
            stage.setScene(scene);
            stage.setTitle("Ticket info");
            stage.show();
            stage.setOnCloseRequest(windowEvent -> ticketViews.remove(ticket));
            ticketViews.put(ticket, stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void showReportView(String user, String project, Integer report) {
        if (reportViews.containsKey(report)) {
            reportViews.get(report).toFront();
            return;
        }
        try {
            Stage stage = new Stage();
            String fxmlFile = "/fxml/BugReportView.fxml";
            FXMLLoader loader = new FXMLLoader();
            AnchorPane root = (AnchorPane) loader.load(Main.class.getClass().getResourceAsStream(fxmlFile));
            BugReportViewController uvc = loader.getController();
            uvc.setup(user, project, report);
            Scene scene = new Scene(root, 600, 250);
            stage.setScene(scene);
            stage.setTitle("Report info");
            stage.show();
            stage.setOnCloseRequest(windowEvent -> reportViews.remove(report));
            reportViews.put(report, stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/
}
