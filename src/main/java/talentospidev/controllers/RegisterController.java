package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentospidev.dao.UserDao;
import talentospidev.models.User;
import talentospidev.utils.FormValidator;
import talentospidev.utils.PasswordUtil;
import talentospidev.utils.SceneUtil;

public class RegisterController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private ComboBox<User.Role> roleBox;

    @FXML
    private Label emailError;
    @FXML
    private Label passwordError;
    @FXML
    private Label confirmError;

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
    }

    @FXML
    private void handleRegister() {
        // Trigger validation on all fields manually
        triggerValidation();

        // Check if any field is in error state or not yet validated
        boolean hasErrors = FormValidator.hasError(emailField) || !FormValidator.isValid(emailField)
                || FormValidator.hasError(passwordField) || !FormValidator.isValid(passwordField)
                || FormValidator.hasError(confirmPasswordField) || !FormValidator.isValid(confirmPasswordField);

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
}
