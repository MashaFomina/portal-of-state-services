package services.stateservices.facade;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collection;
import services.stateservices.entities.*;

public interface FacadeInterface {
    boolean authenticate(String login, String password) throws Exception;
    void signOut(String login);
    List<Struct> getAllNotificationsForUser(String login);
    List<Struct> getAllChildsForUser(String login);
    String getCitizenPassport(String login);
    String getCitizenPolicy(String login);
    String getCitizenBirthDate(String login);
    String getUserEmail(String login);
    String getUserFullName(String login);
    boolean addChild(String login, String fullName, String birthCertificate, String birthDate);
    boolean deleteChild(String login, String id);
    List<Struct> getAllTicketsForUser(String login);
    boolean refuseTicket(String login, String ticketId);
    Map<Integer, Integer> getEducationalInstitutionSeats(int id);
    Map<Integer, Integer> getEducationalInstitutionBusySeats(int id);
    String getEducationalInstitutionTitle(int id);
    String getEducationalInstitutionCity(int id);
    String getEducationalInstitutionDistrict(int id);
    String getEducationalInstitutionTelephone(int id);
    String getEducationalInstitutionFax(int id);
    String getEducationalInstitutionAddress(int id);
}