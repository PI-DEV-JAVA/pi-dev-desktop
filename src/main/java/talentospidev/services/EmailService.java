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
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", SMTP_HOST);
            props.put("mail.smtp.port", String.valueOf(SMTP_PORT));
            props.put("mail.smtp.ssl.trust", SMTP_HOST);

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD);
                }
            });

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
     * Generates a 6-digit numeric code.
     */
    private static String generateCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public static String getLastGeneratedCode() {
        return lastGeneratedCode;
    }
}
