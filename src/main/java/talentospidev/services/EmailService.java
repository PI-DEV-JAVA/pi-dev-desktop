package talentospidev.services;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;
import java.util.Random;

/**
 * Email service for sending verification codes via Gmail SMTP.
 * Uses a 6-digit code that the user enters in the app to verify their email.
 */
public class EmailService {

    // ⚠️ For production, move these to a config file or environment variables
    private static final String SMTP_HOST = "smtp.gmail.com";
    private static final int SMTP_PORT = 587;
    private static final String SENDER_EMAIL = "talentos.pidev@gmail.com";
    private static final String SENDER_PASSWORD = "ywsc uivz umlb ekpu"; // App password (16-char, no spaces)

    private static String lastGeneratedCode = null;

    /**
     * Generates a 6-digit verification code and sends it to the given email.
     * Returns the code so the caller can verify it later, or null on failure.
     */
    public static String sendVerificationCode(String recipientEmail) {
        String code = generateCode();
        lastGeneratedCode = code;

        try {
            Session session = getEmailSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Talentos"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("🔐 Talentos — Code de vérification");

            String htmlContent = """
                    <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 480px; margin: 0 auto;
                                padding: 32px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                                border-radius: 16px;">
                        <div style="background: white; border-radius: 12px; padding: 32px; text-align: center;">
                            <h1 style="color: #111827; font-size: 24px; margin: 0 0 8px;">Talentos</h1>
                            <p style="color: #6b7280; font-size: 14px; margin: 0 0 24px;">Vérification de votre email</p>
                            <div style="background: #f3f4f6; border-radius: 12px; padding: 20px; margin: 0 0 24px;">
                                <p style="color: #9ca3af; font-size: 12px; margin: 0 0 8px;">Votre code de vérification</p>
                                <h2 style="color: #6366f1; font-size: 36px; letter-spacing: 8px; margin: 0; font-weight: 800;">%s</h2>
                            </div>
                            <p style="color: #9ca3af; font-size: 12px; margin: 0;">Ce code expire dans 10 minutes.</p>
                        </div>
                    </div>
                    """
                    .formatted(code);

            message.setContent(htmlContent, "text/html; charset=utf-8");
            Transport.send(message);

            System.out.println("✅ Verification email sent to " + recipientEmail);
            return code;

        } catch (Exception e) {
            System.err.println("❌ Failed to send verification email: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends an activity assignment notification email to an employee
     */
    public static void sendActivityAssignmentEmail(String recipientEmail, String employeeName,
            String projectName, String description,
            double hours, String date) {
        String subject = "📋 New Activity Assigned - Talentos";

        String htmlContent = String.format(
                """
                        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 480px; margin: 0 auto;
                                    padding: 32px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%);
                                    border-radius: 16px;">
                            <div style="background: white; border-radius: 12px; padding: 32px;">
                                <h1 style="color: #111827; font-size: 24px; margin: 0 0 8px; text-align: center;">Talentos</h1>
                                <p style="color: #6b7280; font-size: 14px; margin: 0 0 24px; text-align: center;">New Activity Assignment</p>

                                <p style="color: #374151; font-size: 14px; margin: 0 0 16px;">Hello <strong>%s</strong>,</p>
                                <p style="color: #374151; font-size: 14px; margin: 0 0 16px;">A new activity has been assigned to you:</p>

                                <div style="background: #f3f4f6; border-radius: 12px; padding: 20px; margin: 0 0 24px;">
                                    <table style="width: 100%%; border-collapse: collapse;">
                                        <tr>
                                            <td style="padding: 8px 0; color: #6b7280; font-size: 12px;">Project:</td>
                                            <td style="padding: 8px 0; color: #111827; font-weight: 600;">%s</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 8px 0; color: #6b7280; font-size: 12px;">Description:</td>
                                            <td style="padding: 8px 0; color: #111827;">%s</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 8px 0; color: #6b7280; font-size: 12px;">Hours:</td>
                                            <td style="padding: 8px 0; color: #111827; font-weight: 600;">%.1f hours</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 8px 0; color: #6b7280; font-size: 12px;">Date:</td>
                                            <td style="padding: 8px 0; color: #111827;">%s</td>
                                        </tr>
                                    </table>
                                </div>

                                <p style="color: #374151; font-size: 14px; margin: 0 0 8px;">Please log in to your account to view more details.</p>
                                <p style="color: #9ca3af; font-size: 12px; margin: 24px 0 0;">This is an automated message, please do not reply.</p>
                            </div>
                        </div>
                        """,
                employeeName, projectName, description, hours, date);

        sendEmail(recipientEmail, subject, htmlContent);
    }

    /**
     * Sends an activity update notification email to an employee
     */
    public static void sendActivityUpdateEmail(String recipientEmail, String employeeName,
            String projectName, String description,
            double hours, String date, String changes) {
        String subject = "🔄 Activity Updated - Talentos";

        String htmlContent = String.format(
                """
                        <div style="font-family: 'Segoe UI', Arial, sans-serif; max-width: 480px; margin: 0 auto;
                                    padding: 32px; background: linear-gradient(135deg, #f59e0b, #d97706);
                                    border-radius: 16px;">
                            <div style="background: white; border-radius: 12px; padding: 32px;">
                                <h1 style="color: #111827; font-size: 24px; margin: 0 0 8px; text-align: center;">Talentos</h1>
                                <p style="color: #6b7280; font-size: 14px; margin: 0 0 24px; text-align: center;">Activity Update Notification</p>

                                <p style="color: #374151; font-size: 14px; margin: 0 0 16px;">Hello <strong>%s</strong>,</p>
                                <p style="color: #374151; font-size: 14px; margin: 0 0 16px;">Your activity has been updated:</p>

                                <div style="background: #f3f4f6; border-radius: 12px; padding: 20px; margin: 0 0 24px;">
                                    <table style="width: 100%%; border-collapse: collapse;">
                                        <tr>
                                            <td style="padding: 8px 0; color: #6b7280; font-size: 12px;">Project:</td>
                                            <td style="padding: 8px 0; color: #111827; font-weight: 600;">%s</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 8px 0; color: #6b7280; font-size: 12px;">Description:</td>
                                            <td style="padding: 8px 0; color: #111827;">%s</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 8px 0; color: #6b7280; font-size: 12px;">Hours:</td>
                                            <td style="padding: 8px 0; color: #111827; font-weight: 600;">%.1f hours</td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 8px 0; color: #6b7280; font-size: 12px;">Date:</td>
                                            <td style="padding: 8px 0; color: #111827;">%s</td>
                                        </tr>
                                    </table>
                                </div>

                                <div style="background: #fef2f2; border-radius: 8px; padding: 16px; margin: 0 0 24px;">
                                    <p style="color: #ef4444; font-size: 12px; font-weight: 600; margin: 0 0 8px;">Changes Made:</p>
                                    <p style="color: #374151; font-size: 13px; margin: 0; white-space: pre-wrap;">%s</p>
                                </div>

                                <p style="color: #9ca3af; font-size: 12px; margin: 24px 0 0;">This is an automated message, please do not reply.</p>
                            </div>
                        </div>
                        """,
                employeeName, projectName, description, hours, date, changes);

        sendEmail(recipientEmail, subject, htmlContent);
    }

    /**
     * Core method to send emails
     */
    private static void sendEmail(String recipientEmail, String subject, String htmlContent) {
        try {
            Session session = getEmailSession();
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL, "Talentos"));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject(subject);
            message.setContent(htmlContent, "text/html; charset=utf-8");

            Transport.send(message);
            System.out.println("✅ Email sent successfully to " + recipientEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates and returns an email session
     */
    private static Session getEmailSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
        props.put("mail.smtp.ssl.trust", SMTP_HOST);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
            }
        });
    }

    /**
     * Generates a 6-digit numeric code.
     */
    private static String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public static String getLastGeneratedCode() {
        return lastGeneratedCode;
    }
}