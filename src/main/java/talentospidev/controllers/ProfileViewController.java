package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import talentospidev.dao.ProfileDao;
import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProfileViewController {

    @FXML
    private Label avatarLabel;
    @FXML
    private Text nameText;
    @FXML
    private Text titleText;
    @FXML
    private Label locationLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Text summaryText;
    @FXML
    private Label experienceLabel;
    @FXML
    private Label joinDateLabel;

    private final ProfileDao profileDao = new ProfileDao();

    @FXML
    public void initialize() {
        User user = AuthService.getCurrentUser();
        if (user == null) {
            SceneUtil.switchScene("login.fxml");
            return;
        }

        Profile profile = profileDao.findByUserId(user.getId());

        if (profile != null && profile.getFirstName() != null) {
            String fullName = profile.getFirstName() + " " + profile.getLastName();
            nameText.setText(fullName);
            avatarLabel.setText(profile.getFirstName().substring(0, 1).toUpperCase());

            titleText.setText(profile.getProfessionalTitle() != null ? profile.getProfessionalTitle() : "No Title");
            locationLabel.setText(profile.getLocation() != null ? profile.getLocation() : "Unknown Location");
            phoneLabel.setText(profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "No Phone");
            emailLabel.setText(user.getEmail());

            summaryText.setText(profile.getSummary() != null && !profile.getSummary().isEmpty() ? profile.getSummary()
                    : "No summary provided.");
            experienceLabel.setText(profile.getYearsOfExperience() + " Years");
        } else {
            nameText.setText("Incomplete Profile");
            titleText.setText("Please complete your profile setup.");
        }

        if (user.getCreatedAt() != null) {
            joinDateLabel.setText(user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy")));
        }
    }

    @FXML
    private void handleEdit() {
        SceneUtil.switchScene("update-profile.fxml");
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Profile");
        alert.setHeaderText("Delete your profile?");
        alert.setContentText(
                "This will permanently remove your profile details and set your account to 'Incomplete'. You will be redirected to the setup page.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            User user = AuthService.getCurrentUser();
            if (user != null) {
                profileDao.resetProfile(user.getId());
                SceneUtil.switchScene("complete-profile.fxml");
            }
        }
    }

    /**
     * Role-aware redirect: admins go to admin dashboard, others to candidate
     * dashboard
     */
    @FXML
    private void handleBackToDashboard() {
        User user = AuthService.getCurrentUser();
        if (user != null && user.getRole() == User.Role.ADMIN) {
            SceneUtil.switchScene("admin_dashboard.fxml");
        } else {
            SceneUtil.switchScene("dashboard.fxml");
        }
    }

    @FXML
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "This feature is coming soon!").show();
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
