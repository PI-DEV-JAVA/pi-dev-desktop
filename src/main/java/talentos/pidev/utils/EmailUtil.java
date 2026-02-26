package talentos.pidev.utils;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.time.format.DateTimeFormatter;

public class EmailUtil {
    
    // Email configuration - update with your email credentials
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final String SMTP_PORT = "587";
    private static final String SENDER_EMAIL = "unimeet7@gmail.com"; // Replace with your email
    private static final String SENDER_PASSWORD = "lxgo cgjm jcrw uevd "; // Replace with your app password
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    
    public static void sendActivityAssignmentEmail(String recipientEmail, String employeeName, 
                                                   String projectName, String activityDate, 
                                                   double hours, String description) {
        
        String subject = "New Activity Assigned - " + projectName;
        
        String body = buildEmailBody(employeeName, projectName, activityDate, hours, description);
        
        sendEmail(recipientEmail, subject, body);
    }
    
    private static String buildEmailBody(String employeeName, String projectName, 
                                         String activityDate, double hours, String description) {
        
        return "<!DOCTYPE html>" +
               "<html>" +
               "<head>" +
               "<style>" +
               "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
               ".container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; }" +
               ".header { background-color: #0D203B; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0; }" +
               ".content { padding: 30px; }" +
               ".activity-details { background-color: #F1F5F9; padding: 20px; border-radius: 8px; margin: 20px 0; }" +
               ".detail-item { margin-bottom: 15px; }" +
               ".label { font-weight: bold; color: #0D203B; display: inline-block; width: 100px; }" +
               ".value { color: #4A5568; }" +
               ".footer { text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #e0e0e0; color: #718096; font-size: 12px; }" +
               ".button { background-color: #0D203B; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; display: inline-block; }" +
               "</style>" +
               "</head>" +
               "<body>" +
               "<div class='container'>" +
               "<div class='header'>" +
               "<h2>New Activity Assignment</h2>" +
               "</div>" +
               "<div class='content'>" +
               "<p>Hello <strong>" + employeeName + "</strong>,</p>" +
               "<p>A new activity has been assigned to you. Please log in to your dashboard to view and start working on it.</p>" +
               "<div class='activity-details'>" +
               "<h3 style='color: #0D203B; margin-top: 0;'>Activity Details</h3>" +
               "<div class='detail-item'><span class='label'>Project:</span> <span class='value'>" + projectName + "</span></div>" +
               "<div class='detail-item'><span class='label'>Date:</span> <span class='value'>" + activityDate + "</span></div>" +
               "<div class='detail-item'><span class='label'>Hours:</span> <span class='value'>" + hours + " hours</span></div>" +
               "<div class='detail-item'><span class='label'>Description:</span> <span class='value'>" + description + "</span></div>" +
               "</div>" +
               "<p style='text-align: center; margin-top: 30px;'>" +
               "<a href='#' class='button'>View in Dashboard</a>" +
               "</p>" +
               "</div>" +
               "<div class='footer'>" +
               "<p>This is an automated message. Please do not reply to this email.</p>" +
               "<p>&copy; 2024 Talentos PI. All rights reserved.</p>" +
               "</div>" +
               "</div>" +
               "</body>" +
               "</html>";
    }
    
    private static void sendEmail(String recipient, String subject, String htmlBody) {
        
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
        
        // Enable debugging to see SMTP communication (optional)
        session.setDebug(true);
        
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            message.setSubject(subject);
            
            // Set HTML content
            MimeBodyPart mimeBodyPart = new MimeBodyPart();
            mimeBodyPart.setContent(htmlBody, "text/html; charset=utf-8");
            
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPart);
            
            message.setContent(multipart);
            
            Transport.send(message);
            System.out.println("Email sent successfully to: " + recipient);
            
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
    
    // For testing email configuration
    public static boolean testConnection() {
        try {
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", SMTP_PORT);
            
            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });
            
            Transport transport = session.getTransport("smtp");
            transport.connect(SMTP_HOST, SENDER_EMAIL, SENDER_PASSWORD);
            transport.close();
            
            System.out.println("Email configuration is valid!");
            return true;
            
        } catch (Exception e) {
            System.err.println("Email configuration error: " + e.getMessage());
            return false;
        }
    }
}