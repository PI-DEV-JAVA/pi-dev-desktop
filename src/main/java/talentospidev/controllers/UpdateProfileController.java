package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import talentospidev.dao.ProfileDao;
import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.utils.FormValidator;
import talentospidev.utils.SceneUtil;

import java.time.LocalDate;
import java.time.Period;

public class UpdateProfileController {

    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private DatePicker birthDatePicker;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField locationField;
    @FXML
    private TextField titleField;
    @FXML
    private ComboBox<Integer> experienceBox;
    @FXML
    private TextArea summaryArea;

    // Error labels
    @FXML
    private Label firstNameError;
    @FXML
    private Label lastNameError;
    @FXML
    private Label birthDateError;
    @FXML
    private Label phoneError;
    @FXML
    private Label titleError;

    private final ProfileDao profileDao = new ProfileDao();

    @FXML
    public void initialize() {
        experienceBox.getItems().addAll(0, 1, 2, 3, 4, 5, 7, 10, 15, 20);

        // Attach onBlur validators
        FormValidator.requireNotEmpty(firstNameField, firstNameError, "First Name");
        FormValidator.requireNotEmpty(lastNameField, lastNameError, "Last Name");
        FormValidator.requireAge18(birthDatePicker, birthDateError);
        FormValidator.requireNotEmpty(phoneField, phoneError, "Phone Number");
        FormValidator.requireNotEmpty(titleField, titleError, "Professional Title");

        // Pre-fill
        User user = AuthService.getCurrentUser();
        if (user != null) {
            Profile p = profileDao.findByUserId(user.getId());
            if (p != null) {
                firstNameField.setText(p.getFirstName());
                lastNameField.setText(p.getLastName());
                birthDatePicker.setValue(p.getBirthDate());
                phoneField.setText(p.getPhoneNumber());
                locationField.setText(p.getLocation());
                titleField.setText(p.getProfessionalTitle());
                experienceBox.setValue(p.getYearsOfExperience());
                summaryArea.setText(p.getSummary());
            }
        }
    }

    @FXML
    private void handleSave() {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;

        // Trigger validation
        triggerValidation();

        boolean hasErrors = FormValidator.hasError(firstNameField) || !FormValidator.isValid(firstNameField)
                || FormValidator.hasError(lastNameField) || !FormValidator.isValid(lastNameField)
                || FormValidator.hasError(birthDatePicker) || !FormValidator.isValid(birthDatePicker)
                || FormValidator.hasError(phoneField) || !FormValidator.isValid(phoneField)
                || FormValidator.hasError(titleField) || !FormValidator.isValid(titleField);

        if (hasErrors) {
            return;
        }

        Profile profile = new Profile();
        profile.setFirstName(firstNameField.getText().trim());
        profile.setLastName(lastNameField.getText().trim());
        profile.setBirthDate(birthDatePicker.getValue());
        profile.setPhoneNumber(phoneField.getText().trim());
        profile.setLocation(locationField.getText() != null ? locationField.getText().trim() : "");
        profile.setProfessionalTitle(titleField.getText().trim());
        Integer exp = experienceBox.getValue();
        profile.setYearsOfExperience(exp != null ? exp : 0);
        profile.setSummary(summaryArea.getText() != null ? summaryArea.getText().trim() : "");

        try {
            profileDao.save(profile, user);
            new Alert(Alert.AlertType.INFORMATION, "Profile updated successfully!").showAndWait();
            SceneUtil.switchScene("profile-view.fxml");
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Error updating profile: " + e.getMessage()).show();
        }
    }

    private void triggerValidation() {
        validateTextField(firstNameField, firstNameError, "First Name");
        validateTextField(lastNameField, lastNameError, "Last Name");
        validateTextField(phoneField, phoneError, "Phone Number");
        validateTextField(titleField, titleError, "Professional Title");

        LocalDate date = birthDatePicker.getValue();
        if (date == null) {
            FormValidator.markError(birthDatePicker, birthDateError, "Birth date is required.");
        } else {
            int age = Period.between(date, LocalDate.now()).getYears();
            if (age < 18) {
                FormValidator.markError(birthDatePicker, birthDateError, "You must be at least 18.");
            } else {
                FormValidator.markValid(birthDatePicker, birthDateError);
            }
        }
    }

    private void validateTextField(TextField field, Label errorLabel, String name) {
        if (field.getText() == null || field.getText().trim().isEmpty()) {
            FormValidator.markError(field, errorLabel, name + " is required.");
        } else {
            FormValidator.markValid(field, errorLabel);
        }
    }

    @FXML
    private void handleCancel() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleDashboard() {
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
