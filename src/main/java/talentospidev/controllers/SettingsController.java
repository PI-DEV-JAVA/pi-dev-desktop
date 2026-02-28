package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.SidebarUtil;
import talentospidev.utils.ThemeManager;

public class SettingsController {

    @FXML
    private VBox sidebar;
    @FXML
    private ToggleButton darkModeToggle;
    @FXML
    private Label themeStatusLabel;
    @FXML
    private Label themeIcon;

    @FXML
    public void initialize() {
        SidebarUtil.applySidebarIcons(sidebar);

        // Restore toggle state
        darkModeToggle.setSelected(ThemeManager.isDarkMode());
        updateToggleVisual();
    }

    @FXML
    private void handleDarkModeToggle() {
        ThemeManager.toggle();
        // Reload the settings page to reflect the new theme immediately
        SceneUtil.switchScene("settings.fxml");
    }

    private void updateToggleVisual() {
        boolean dark = ThemeManager.isDarkMode();

        if (dark) {
            darkModeToggle.setText("ON");
            darkModeToggle.setStyle(
                    "-fx-background-color: #6366f1; -fx-background-radius: 14; " +
                            "-fx-padding: 4 8 4 20; -fx-min-width: 52; -fx-min-height: 28; " +
                            "-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 10px; -fx-cursor: hand;");
            themeStatusLabel.setText("Dark (Primer Dark)");
            themeIcon.setText("🌙");
        } else {
            darkModeToggle.setText("OFF");
            darkModeToggle.setStyle(
                    "-fx-background-color: #e5e7eb; -fx-background-radius: 14; " +
                            "-fx-padding: 4 20 4 8; -fx-min-width: 52; -fx-min-height: 28; " +
                            "-fx-text-fill: #6b7280; -fx-font-weight: 700; -fx-font-size: 10px; -fx-cursor: hand;");
            themeStatusLabel.setText("Light (Primer Light)");
            themeIcon.setText("☀");
        }
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        var user = AuthService.getCurrentUser();
        if (user == null)
            return;
        switch (user.getRole()) {
            case ADMIN -> SceneUtil.switchScene("admin_dashboard.fxml");
            case HR -> SceneUtil.switchScene("recruiter_dashboard.fxml");
            default -> SceneUtil.switchScene("dashboard.fxml");
        }
    }

    @FXML
    private void handleJobOffers() {
        SceneUtil.switchScene("OffersCardView.fxml");
    }

    @FXML
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleSettings() {
        /* Already here */ }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
