package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentospidev.dao.UserDao;
import talentospidev.models.User;
import talentospidev.utils.FormValidator;
import talentospidev.utils.PasswordUtil;
import talentospidev.utils.SceneUtil;
import javafx.scene.paint.Color;

import java.util.Random;

public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<User.Role> roleBox;

    @FXML private Label emailError;
    @FXML private Label passwordError;
    @FXML private Label confirmError;

    // Password toggle fields
    @FXML private TextField passwordVisible;
    @FXML private TextField confirmVisible;
    @FXML private Button togglePasswordBtn;
    @FXML private Button toggleConfirmBtn;
    private boolean passwordShown = false;
    private boolean confirmShown = false;

    // CAPTCHA fields
    @FXML private Label captchaQuestion;
    @FXML private TextField captchaField;
    @FXML private Label captchaError;
    @FXML private Button refreshCaptchaBtn;
    private int captchaAnswer;
    private final Random random = new Random();

    private final UserDao userDao = new UserDao();
    private final talentospidev.dao.ProfileDao profileDao = new talentospidev.dao.ProfileDao();

    @FXML
    public void initialize() {
        roleBox.getItems().addAll(User.Role.CANDIDATE, User.Role.HR);
        roleBox.setValue(User.Role.CANDIDATE);

        // Attach onBlur (focus-lost) validators
        FormValidator.requireEmail(emailField, emailError);
        FormValidator.requirePassword(passwordField, passwordError);
        FormValidator.requireConfirmPassword(confirmPasswordField, passwordField, confirmError);

        // Eye icons for password toggles
        setupEyeIcon(togglePasswordBtn);
        setupEyeIcon(toggleConfirmBtn);

        // Sync text between hidden/visible fields
        passwordVisible.textProperty().bindBidirectional(passwordField.textProperty());
        confirmVisible.textProperty().bindBidirectional(confirmPasswordField.textProperty());

        // Generate initial CAPTCHA
        generateCaptcha();
    }

    private void generateCaptcha() {
        int a = random.nextInt(20) + 1;
        int b = random.nextInt(10) + 1;
        int op = random.nextInt(3); // 0=add, 1=subtract, 2=multiply
        String symbol;
        switch (op) {
            case 0: captchaAnswer = a + b; symbol = "+"; break;
            case 1: captchaAnswer = a - b; symbol = "−"; break;
            default: a = random.nextInt(10) + 1; b = random.nextInt(10) + 1; captchaAnswer = a * b; symbol = "×"; break;
        }
        captchaQuestion.setText("What is " + a + " " + symbol + " " + b + " ?");
        captchaField.clear();
        if (captchaError != null) {
            captchaError.setVisible(false);
            captchaError.setManaged(false);
        }
    }

    @FXML
    private void refreshCaptcha() {
        generateCaptcha();
    }

    private void setupEyeIcon(Button btn) {
        org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon(
                org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EYE);
        icon.setIconSize(14);
        icon.setIconColor(Color.web("#9ca3af"));
        btn.setGraphic(icon);
        btn.setText("");
    }

    @FXML
    private void handleRegister() {
        // Trigger validation on all fields manually
        triggerValidation();

        // Validate CAPTCHA
        boolean captchaValid = validateCaptcha();

        // Check if any field is in error state or not yet validated
        boolean hasErrors = FormValidator.hasError(emailField) || !FormValidator.isValid(emailField)
                || FormValidator.hasError(passwordField) || !FormValidator.isValid(passwordField)
                || FormValidator.hasError(confirmPasswordField) || !FormValidator.isValid(confirmPasswordField)
                || !captchaValid;

        if (hasErrors) {
            return;
        }

        String email = emailField.getText().trim();

        if (userDao.findByEmail(email) != null) {
            FormValidator.markError(emailField, emailError, "An account with this email already exists.");
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setRole(roleBox.getValue());
        user.setAuthProvider(User.AuthProvider.LOCAL);
        user.setPasswordHash(PasswordUtil.hashPassword(passwordField.getText()));

        userDao.saveLocal(user);
        profileDao.createInitialProfile(user.getId());

        new Alert(Alert.AlertType.INFORMATION, "Account created successfully! You can now login.").showAndWait();
        SceneUtil.switchScene("login.fxml");
    }

    private boolean validateCaptcha() {
        String answer = captchaField.getText();
        if (answer == null || answer.trim().isEmpty()) {
            captchaError.setText("Please solve the security check.");
            captchaError.setVisible(true);
            captchaError.setManaged(true);
            return false;
        }
        try {
            int parsed = Integer.parseInt(answer.trim());
            if (parsed != captchaAnswer) {
                captchaError.setText("Wrong answer. Try again!");
                captchaError.setVisible(true);
                captchaError.setManaged(true);
                generateCaptcha();
                return false;
            }
        } catch (NumberFormatException e) {
            captchaError.setText("Please enter a valid number.");
            captchaError.setVisible(true);
            captchaError.setManaged(true);
            return false;
        }
        captchaError.setVisible(false);
        captchaError.setManaged(false);
        return true;
    }

    /**
     * Manually trigger validation on all fields (for when user clicks Register
     * without tabbing).
     */
    private void triggerValidation() {
        String email = emailField.getText();
        if (email == null || email.trim().isEmpty()) {
            FormValidator.markError(emailField, emailError, "Email is required.");
        } else if (!email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")) {
            FormValidator.markError(emailField, emailError, "Please enter a valid email.");
        } else {
            FormValidator.markValid(emailField, emailError);
        }

        String pw = passwordField.getText();
        if (pw == null || pw.isEmpty()) {
            FormValidator.markError(passwordField, passwordError, "Password is required.");
        } else if (pw.length() < 6) {
            FormValidator.markError(passwordField, passwordError, "Minimum 6 characters.");
        } else if (!pw.matches(".*[A-Z].*")) {
            FormValidator.markError(passwordField, passwordError, "Must contain an uppercase letter.");
        } else if (!pw.matches(".*[0-9].*")) {
            FormValidator.markError(passwordField, passwordError, "Must contain a digit.");
        } else {
            FormValidator.markValid(passwordField, passwordError);
        }

        String confirm = confirmPasswordField.getText();
        if (confirm == null || confirm.isEmpty()) {
            FormValidator.markError(confirmPasswordField, confirmError, "Please confirm your password.");
        } else if (!confirm.equals(pw)) {
            FormValidator.markError(confirmPasswordField, confirmError, "Passwords do not match.");
        } else {
            FormValidator.markValid(confirmPasswordField, confirmError);
        }
    }

    @FXML
    private void goToLogin() {
        SceneUtil.switchScene("login.fxml");
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordShown = !passwordShown;
        passwordField.setVisible(!passwordShown);
        passwordField.setManaged(!passwordShown);
        passwordVisible.setVisible(passwordShown);
        passwordVisible.setManaged(passwordShown);
        swapEyeIcon(togglePasswordBtn, passwordShown);
    }

    @FXML
    private void toggleConfirmVisibility() {
        confirmShown = !confirmShown;
        confirmPasswordField.setVisible(!confirmShown);
        confirmPasswordField.setManaged(!confirmShown);
        confirmVisible.setVisible(confirmShown);
        confirmVisible.setManaged(confirmShown);
        swapEyeIcon(toggleConfirmBtn, confirmShown);
    }

    private void swapEyeIcon(Button btn, boolean shown) {
        org.kordamp.ikonli.javafx.FontIcon icon = new org.kordamp.ikonli.javafx.FontIcon(
                shown ? org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EYE_SLASH
                        : org.kordamp.ikonli.fontawesome5.FontAwesomeSolid.EYE);
        icon.setIconSize(14);
        icon.setIconColor(Color.web("#9ca3af"));
        btn.setGraphic(icon);
    }
}
