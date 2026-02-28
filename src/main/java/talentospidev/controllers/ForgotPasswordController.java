package talentospidev.controllers;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid;
import org.kordamp.ikonli.javafx.FontIcon;
import talentospidev.dao.UserDao;
import talentospidev.models.User;
import talentospidev.services.EmailService;
import talentospidev.utils.FormValidator;
import talentospidev.utils.PasswordUtil;
import talentospidev.utils.SceneUtil;

/**
 * 3-step forgot-password flow:
 * Step 1 — enter email → send OTP
 * Step 2 — enter 6-digit code → verify
 * Step 3 — enter new password → reset
 */
public class ForgotPasswordController {

    // Header
    @FXML
    private Label headerTitle;
    @FXML
    private Label headerSubtitle;
    @FXML
    private Label statusLabel;

    // Step 1 — Email
    @FXML
    private VBox stepEmail;
    @FXML
    private TextField emailField;
    @FXML
    private Label emailError;

    // Step 2 — OTP
    @FXML
    private VBox stepOtp;
    @FXML
    private TextField otpField;
    @FXML
    private Label otpError;
    @FXML
    private Label timerLabel;
    @FXML
    private Hyperlink resendLink;

    // Step 3 — New password
    @FXML
    private VBox stepNewPassword;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private TextField newPasswordVisible;
    @FXML
    private Button toggleNewPwBtn;
    @FXML
    private Label newPasswordError;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private TextField confirmPasswordVisible;
    @FXML
    private Button toggleConfirmPwBtn;
    @FXML
    private Label confirmPasswordError;

    private final UserDao userDao = new UserDao();
    private String generatedOtp;
    private String targetEmail;
    private User targetUser;
    private Timeline countdownTimeline;
    private int secondsRemaining;
    private boolean newPwShown = false;
    private boolean confirmPwShown = false;

    @FXML
    public void initialize() {
        // Email validation
        FormValidator.requireEmail(emailField, emailError);

        // Password eye icons
        setupEyeIcon(toggleNewPwBtn);
        setupEyeIcon(toggleConfirmPwBtn);

        // Bidirectional text sync
        newPasswordVisible.textProperty().bindBidirectional(newPasswordField.textProperty());
        confirmPasswordVisible.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        // Password validators
        FormValidator.requirePassword(newPasswordField, newPasswordError);
        FormValidator.requireConfirmPassword(confirmPasswordField, newPasswordField, confirmPasswordError);

        // Limit OTP field to 6 digits
        otpField.textProperty().addListener((obs, ov, nv) -> {
            if (nv != null && !nv.matches("\\d{0,6}")) {
                otpField.setText(ov);
            }
        });
    }

    // ════════════════════════════════════════════════
    // STEP 1: Send OTP
    // ════════════════════════════════════════════════

    @FXML
    private void handleSendCode() {
        hideStatus();
        String email = emailField.getText();

        if (email == null || email.trim().isEmpty()) {
            FormValidator.markError(emailField, emailError, "Email is required.");
            return;
        }
        if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            FormValidator.markError(emailField, emailError, "Please enter a valid email.");
            return;
        }

        // Check if user exists
        targetUser = userDao.findByEmail(email.trim());
        if (targetUser == null) {
            FormValidator.markError(emailField, emailError, "No account found with this email.");
            return;
        }

        targetEmail = email.trim();
        sendOtpEmail();
    }

    private void sendOtpEmail() {
        showStatus("Sending verification code...", false);

        // Send on a background thread to keep UI responsive
        new Thread(() -> {
            String code = EmailService.sendVerificationCode(targetEmail);
            javafx.application.Platform.runLater(() -> {
                if (code != null) {
                    generatedOtp = code;
                    showStep(2);
                    startCountdown(600); // 10 minutes
                    showStatus("✅ Code sent to " + targetEmail, false);
                } else {
                    showStatus("❌ Failed to send email. Please try again.", true);
                }
            });
        }).start();
    }

    // ════════════════════════════════════════════════
    // STEP 2: Verify OTP
    // ════════════════════════════════════════════════

    @FXML
    private void handleVerifyCode() {
        hideStatus();
        String code = otpField.getText();

        if (code == null || code.trim().isEmpty()) {
            FormValidator.markError(otpField, otpError, "Please enter the 6-digit code.");
            return;
        }

        if (secondsRemaining <= 0) {
            FormValidator.markError(otpField, otpError, "Code has expired. Please request a new one.");
            return;
        }

        if (!code.trim().equals(generatedOtp)) {
            FormValidator.markError(otpField, otpError, "Invalid code. Please check and try again.");
            return;
        }

        // OTP verified!
        if (countdownTimeline != null)
            countdownTimeline.stop();
        FormValidator.markValid(otpField, otpError);
        showStep(3);
        showStatus("✅ Code verified! Set your new password.", false);
    }

    @FXML
    private void handleResendCode() {
        otpField.clear();
        FormValidator.markNeutral(otpField, otpError);
        sendOtpEmail();
    }

    // ════════════════════════════════════════════════
    // STEP 3: Reset Password
    // ════════════════════════════════════════════════

    @FXML
    private void handleResetPassword() {
        hideStatus();

        // Trigger validation
        String pw = newPasswordField.getText();
        String confirm = confirmPasswordField.getText();

        boolean hasError = false;

        if (pw == null || pw.isEmpty()) {
            FormValidator.markError(newPasswordField, newPasswordError, "Password is required.");
            hasError = true;
        } else if (pw.length() < 6) {
            FormValidator.markError(newPasswordField, newPasswordError, "Minimum 6 characters.");
            hasError = true;
        } else if (!pw.matches(".*[A-Z].*")) {
            FormValidator.markError(newPasswordField, newPasswordError, "Must contain an uppercase letter.");
            hasError = true;
        } else if (!pw.matches(".*[0-9].*")) {
            FormValidator.markError(newPasswordField, newPasswordError, "Must contain a digit.");
            hasError = true;
        } else {
            FormValidator.markValid(newPasswordField, newPasswordError);
        }

        if (confirm == null || confirm.isEmpty()) {
            FormValidator.markError(confirmPasswordField, confirmPasswordError, "Please confirm your password.");
            hasError = true;
        } else if (!confirm.equals(pw)) {
            FormValidator.markError(confirmPasswordField, confirmPasswordError, "Passwords do not match.");
            hasError = true;
        } else {
            FormValidator.markValid(confirmPasswordField, confirmPasswordError);
        }

        if (hasError)
            return;

        // Hash and save
        String hash = PasswordUtil.hashPassword(pw);
        boolean success = userDao.updatePassword(targetUser.getId(), hash);

        if (success) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText(null);
            alert.setContentText("Your password has been reset successfully! You can now log in.");
            alert.showAndWait();
            SceneUtil.switchScene("login.fxml");
        } else {
            showStatus("❌ Failed to reset password. Please try again.", true);
        }
    }

    // ════════════════════════════════════════════════
    // Password Toggle
    // ════════════════════════════════════════════════

    @FXML
    private void toggleNewPassword() {
        newPwShown = !newPwShown;
        newPasswordField.setVisible(!newPwShown);
        newPasswordField.setManaged(!newPwShown);
        newPasswordVisible.setVisible(newPwShown);
        newPasswordVisible.setManaged(newPwShown);
        swapEyeIcon(toggleNewPwBtn, newPwShown);
    }

    @FXML
    private void toggleConfirmPassword() {
        confirmPwShown = !confirmPwShown;
        confirmPasswordField.setVisible(!confirmPwShown);
        confirmPasswordField.setManaged(!confirmPwShown);
        confirmPasswordVisible.setVisible(confirmPwShown);
        confirmPasswordVisible.setManaged(confirmPwShown);
        swapEyeIcon(toggleConfirmPwBtn, confirmPwShown);
    }

    // ════════════════════════════════════════════════
    // Helpers
    // ════════════════════════════════════════════════

    private void showStep(int step) {
        stepEmail.setVisible(step == 1);
        stepEmail.setManaged(step == 1);
        stepOtp.setVisible(step == 2);
        stepOtp.setManaged(step == 2);
        stepNewPassword.setVisible(step == 3);
        stepNewPassword.setManaged(step == 3);

        switch (step) {
            case 1 -> {
                headerTitle.setText("RESET PASSWORD");
                headerSubtitle.setText("Enter your email to receive a verification code");
            }
            case 2 -> {
                headerTitle.setText("CHECK YOUR EMAIL");
                headerSubtitle.setText("We've sent a 6-digit code to " + targetEmail);
            }
            case 3 -> {
                headerTitle.setText("NEW PASSWORD");
                headerSubtitle.setText("Create a strong password for your account");
            }
        }
    }

    private void startCountdown(int totalSeconds) {
        secondsRemaining = totalSeconds;
        if (countdownTimeline != null)
            countdownTimeline.stop();

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            secondsRemaining--;
            int mins = secondsRemaining / 60;
            int secs = secondsRemaining % 60;
            timerLabel.setText(String.format("Code expires in %02d:%02d", mins, secs));

            if (secondsRemaining <= 60) {
                timerLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: 700;");
            }

            if (secondsRemaining <= 0) {
                countdownTimeline.stop();
                timerLabel.setText("Code expired");
                timerLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 11px; -fx-font-weight: 700;");
            }
        }));
        countdownTimeline.setCycleCount(totalSeconds);
        countdownTimeline.play();
    }

    private void showStatus(String msg, boolean isError) {
        statusLabel.setText(msg);
        statusLabel.setVisible(true);
        statusLabel.setManaged(true);
        if (isError) {
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-background-color: #fef2f2; " +
                    "-fx-padding: 8 16; -fx-background-radius: 8; -fx-font-size: 12px;");
        } else {
            statusLabel.setStyle("-fx-text-fill: #059669; -fx-background-color: #ecfdf5; " +
                    "-fx-padding: 8 16; -fx-background-radius: 8; -fx-font-size: 12px;");
        }
    }

    private void hideStatus() {
        statusLabel.setVisible(false);
        statusLabel.setManaged(false);
    }

    private void setupEyeIcon(Button btn) {
        FontIcon icon = new FontIcon(FontAwesomeSolid.EYE);
        icon.setIconSize(14);
        icon.setIconColor(Color.web("#9ca3af"));
        btn.setGraphic(icon);
        btn.setText("");
    }

    private void swapEyeIcon(Button btn, boolean shown) {
        FontIcon icon = new FontIcon(shown ? FontAwesomeSolid.EYE_SLASH : FontAwesomeSolid.EYE);
        icon.setIconSize(14);
        icon.setIconColor(Color.web("#9ca3af"));
        btn.setGraphic(icon);
    }

    @FXML
    private void goToLogin() {
        if (countdownTimeline != null)
            countdownTimeline.stop();
        SceneUtil.switchScene("login.fxml");
    }
}
