package talentospidev.controllers;

import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import talentospidev.config.GoogleOAuthConfig;
import talentospidev.dao.ProfileDao;
import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.services.GoogleOAuthService;
import talentospidev.utils.SceneUtil;

public class LoginController {

    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label loginError;

    private final ProfileDao profileDao = new ProfileDao();

    // ───────────────────────────────────────────────
    // LOCAL LOGIN
    // ───────────────────────────────────────────────

    @FXML
    private void handleLogin() {
        hideError();

        String email = emailField.getText();
        String password = passwordField.getText();

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

        emailField.getStyleClass().removeAll("input-error");
        passwordField.getStyleClass().removeAll("input-error");

        redirectAfterLogin(user);
    }

    // ───────────────────────────────────────────────
    // GOOGLE LOGIN
    // ───────────────────────────────────────────────

    @FXML
    private void handleGoogleLogin() {
        // Create a popup with a WebView
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Sign in with Google");

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        // Listen for URL changes (waiting for the redirect with ?code=...)
        engine.locationProperty().addListener((obs, oldUrl, newUrl) -> {
            if (newUrl != null && newUrl.startsWith(GoogleOAuthConfig.REDIRECT_URI)) {
                // Extract the authorization code from the URL
                String code = extractParam(newUrl, "code");

                if (code != null) {
                    popup.close();
                    processGoogleCode(code);
                } else {
                    popup.close();
                    showError("Google sign-in was cancelled or failed.");
                }
            }
        });

        // Load Google's consent page
        engine.load(GoogleOAuthService.getAuthUrl());

        Scene scene = new Scene(webView, 500, 600);
        popup.setScene(scene);
        popup.showAndWait();
    }

    /**
     * After getting the auth code, exchange it for tokens and log in.
     */
    private void processGoogleCode(String code) {
        try {
            // 1. Exchange code → access token
            String accessToken = GoogleOAuthService.exchangeCodeForToken(code);

            // 2. Fetch user info from Google
            JsonObject userInfo = GoogleOAuthService.getUserInfo(accessToken);
            String email = userInfo.get("email").getAsString();
            String googleId = userInfo.get("id").getAsString();

            // 3. Login or register via AuthService
            User user = AuthService.loginOAuth(email, googleId);

            if (user == null) {
                showError("Google login failed. Please try again.");
                return;
            }

            // 4. Redirect to dashboard
            Platform.runLater(() -> redirectAfterLogin(user));

        } catch (Exception e) {
            e.printStackTrace();
            Platform.runLater(() -> showError("Google login error: " + e.getMessage()));
        }
    }

    // ───────────────────────────────────────────────
    // SHARED HELPERS
    // ───────────────────────────────────────────────

    /**
     * Redirects based on role and profile status (shared by local + Google login).
     */
    private void redirectAfterLogin(User user) {
        if (user.getRole() == User.Role.ADMIN) {
            SceneUtil.switchScene("admin_dashboard.fxml");
        } else if (user.getRole() == User.Role.HR) {
            SceneUtil.switchScene("recruiter_dashboard.fxml");
        } else {
            SceneUtil.switchScene("dashboard.fxml");
        }
    }

    /**
     * Extracts a query parameter from a URL string.
     */
    private String extractParam(String url, String param) {
        String search = param + "=";
        int start = url.indexOf(search);
        if (start == -1)
            return null;
        start += search.length();
        int end = url.indexOf("&", start);
        return end == -1 ? url.substring(start) : url.substring(start, end);
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
