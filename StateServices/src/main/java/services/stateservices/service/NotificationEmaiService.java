package services.stateservices.service;

import java.io.FileNotFoundException;
import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.naming.NamingException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NotificationEmaiService {

    private InputStream inputStream;
    private String fromEmail;
    private String emailPassword;
    private String toEmail;
    private String notification;

    public NotificationEmaiService(String toEmail, String notification) {
        // Read configuration file
        try {
            Properties prop = new Properties();
            String propFileName = "config.ini";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }

            fromEmail = prop.getProperty("fromEmail");
            emailPassword = prop.getProperty("emailPassword");
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(NotificationEmaiService.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.toEmail = toEmail;
        this.notification = notification;
    }

    public boolean sendNotification() {
        if (!isValidEmailAddress()) return false;
        return sendEmail();
    }

    private boolean isValidEmailAddress() {
        try {
            new InternetAddress(toEmail).validate();
        } catch (AddressException ex) {
            return false;
        }
        String hostname = toEmail.split("@")[1];
        try {
            return EmailFunctions.doMailServerLookup(hostname);
        } catch (NamingException e) {
            return false;
        }
    }

    private boolean sendEmail() {

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.trust", "smtp.gmail.com");

        //create Authenticator object to pass in Session.getInstance argument
        Authenticator auth = new Authenticator() {
            //override the getPasswordAuthentication method
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, emailPassword);
            }
        };
        Session session = Session.getInstance(props, auth);

        StringBuilder sb = new StringBuilder();
        sb.append("You are registered at portal of state services.\n");
        sb.append("Important notification: " + notification + "\n");

        EmailFunctions.sendEmail(session, toEmail, "Important notification", sb.toString());
        return true;
    }

    private class SMTPAuthenticator extends javax.mail.Authenticator {
        public PasswordAuthentication getPasswordAuthentication() {
            String username = "pmsbot.spbpu@gmail.com";
            String password = "pmsbotspbpu";
            return new PasswordAuthentication(username, password);
        }
    }
}