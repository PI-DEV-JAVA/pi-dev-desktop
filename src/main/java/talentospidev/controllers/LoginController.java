package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentospidev.dao.ProfileDao;
import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label loginError;

    private final ProfileDao profileDao = new ProfileDao();

    @FXML
    private void handleLogin() {
        // Hide any previous error
        hideError();

        String email = emailField.getText();
        String password = passwordField.getText();

        // Only basic empty checks — then try auth and show result
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            showError("Please enter your email and password.");
            return;
        }

        User user = AuthService.loginLocal(email.trim(), password);

        if (user == null) {
            showError("Invalid credentials. Email not found or wrong password.");
            emailField.getStyleClass().add("input-error");
            passwordField.getStyleClass().add("input-error");
            return;
        }

        // Clear error styles on success
        emailField.getStyleClass().removeAll("input-error");
        passwordField.getStyleClass().removeAll("input-error");

        Profile profile = profileDao.findByUserId(user.getId());

        if (user.getRole() == User.Role.ADMIN) {
            SceneUtil.switchScene("admin_dashboard.fxml");
            return;
        }

        if (profile != null && profile.isProfileCompleted()) {
            SceneUtil.switchScene("dashboard.fxml");
        } else {
            SceneUtil.switchScene("complete-profile.fxml");
        }
    }

    private void showError(String msg) {
        loginError.setText("⚠  " + msg);
        loginError.setVisible(true);
        loginError.setManaged(true);
    }

    private void hideError() {
        loginError.setText("");
        loginError.setVisible(false);
        loginError.setManaged(false);
        emailField.getStyleClass().removeAll("input-error");
        passwordField.getStyleClass().removeAll("input-error");
    }

    @FXML
    private void goToRegister() {
        SceneUtil.switchScene("register.fxml");
    }
}
