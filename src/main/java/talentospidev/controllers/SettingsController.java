package talentospidev.controllers;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import talentospidev.dao.SupportTicketDao;
import talentospidev.models.SupportTicket;
import talentospidev.models.User;
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

    // Contact support fields
    @FXML
    private ComboBox<String> categoryBox;
    @FXML
    private TextField subjectField;
    @FXML
    private TextArea messageArea;
    @FXML
    private Label ticketStatusLabel;

    private final SupportTicketDao ticketDao = new SupportTicketDao();

    @FXML
    public void initialize() {
        SidebarUtil.applySidebarIcons(sidebar);

        // Dark mode toggle
        darkModeToggle.setSelected(ThemeManager.isDarkMode());
        updateToggleVisual();

        // Category ComboBox
        categoryBox.setItems(FXCollections.observableArrayList(
                "Bug Report", "Question", "Feature Request", "Other"));
        categoryBox.setValue("Other");
    }

    // ═══ DARK MODE ═══

    @FXML
    private void handleDarkModeToggle() {
        ThemeManager.toggle();
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

    // ═══ CONTACT SUPPORT ═══

    @FXML
    private void handleSubmitTicket() {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;

        String subject = subjectField.getText();
        String message = messageArea.getText();
        String categoryDisplay = categoryBox.getValue();

        // Validation
        if (subject == null || subject.trim().isEmpty()) {
            showTicketStatus("⚠ Please enter a subject.", true);
            return;
        }
        if (message == null || message.trim().isEmpty()) {
            showTicketStatus("⚠ Please describe your issue.", true);
            return;
        }

        // Map display name to DB enum
        String category = switch (categoryDisplay) {
            case "Bug Report" -> "BUG";
            case "Question" -> "QUESTION";
            case "Feature Request" -> "FEATURE";
            default -> "OTHER";
        };

        SupportTicket ticket = new SupportTicket(user.getId(), subject.trim(), message.trim(), category);
        ticketDao.createTicket(ticket);

        // Success feedback
        subjectField.clear();
        messageArea.clear();
        categoryBox.setValue("Other");
        showTicketStatus("✅ Ticket submitted successfully! Our team will get back to you.", false);
    }

    private void showTicketStatus(String msg, boolean isError) {
        ticketStatusLabel.setText(msg);
        ticketStatusLabel.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: " +
                (isError ? "#dc2626" : "#16a34a") + ";");
        ticketStatusLabel.setVisible(true);
        ticketStatusLabel.setManaged(true);
    }

    // ═══ SIDEBAR NAVIGATION ═══

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
    private void handleNotifications() {
        SceneUtil.switchScene("notifications.fxml");
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
