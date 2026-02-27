package talentospidev.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import talentospidev.models.Offer;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.services.OfferService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.LocalDate;

public class AddOfferController {

    @FXML
    private Label pageTitle;
    @FXML
    private Label pageSubtitle;
    @FXML
    private TextField titleField;
    @FXML
    private TextArea descriptionField;
    @FXML
    private ComboBox<String> departmentBox;
    @FXML
    private ComboBox<String> contractBox;
    @FXML
    private ComboBox<String> experienceBox;
    @FXML
    private TextField locationField;
    @FXML
    private TextField salaryMinField;
    @FXML
    private TextField salaryMaxField;
    @FXML
    private TextField positionsField;
    @FXML
    private DatePicker closingDatePicker;
    @FXML
    private VBox statusSection;
    @FXML
    private ComboBox<String> statusBox;
    @FXML
    private Button submitBtn;

    private final OfferService offerService = new OfferService();
    private Offer editingOffer = null;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        // Populate combo boxes
        departmentBox.getItems().addAll("IT", "Finance", "Marketing", "RH", "Commercial", "Logistique");
        contractBox.getItems().addAll("CDI", "CDD", "Stage", "Freelance");
        experienceBox.getItems().addAll("Junior", "Intermédiaire", "Senior", "Expert");
        statusBox.getItems().addAll("Ouverte", "Fermée", "En attente");

        // Default values
        closingDatePicker.setValue(LocalDate.now().plusMonths(1));
        positionsField.setText("1");

        // Check if editing an existing offer
        int offerId = ViewContext.getSelectedOfferId();
        if (offerId > 0) {
            editingOffer = offerService.getOfferById(offerId);
            if (editingOffer != null) {
                isEditMode = true;
                populateForm(editingOffer);
            }
        }
    }

    private void populateForm(Offer offer) {
        pageTitle.setText("Modifier l'offre");
        pageSubtitle.setText("Mettez à jour les informations de votre offre");
        submitBtn.setText("Enregistrer les modifications");

        titleField.setText(offer.getTitle());
        descriptionField.setText(offer.getDescription());
        departmentBox.setValue(offer.getDepartment());
        contractBox.setValue(offer.getContractType());
        experienceBox.setValue(offer.getExperienceLevel());
        locationField.setText(offer.getLocation());
        salaryMinField.setText(String.valueOf((int) offer.getSalaryMin()));
        salaryMaxField.setText(String.valueOf((int) offer.getSalaryMax()));
        positionsField.setText(String.valueOf(offer.getPositionsAvailable()));
        closingDatePicker.setValue(offer.getClosingDate());

        // Show status selector in edit mode
        statusSection.setVisible(true);
        statusSection.setManaged(true);
        statusBox.setValue(offer.getStatus());
    }

    @FXML
    private void handleSubmit() {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;

        // Validate title
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            showAlert("Erreur", "Le titre est obligatoire.", Alert.AlertType.WARNING);
            return;
        }

        try {
            double salaryMin = Double.parseDouble(salaryMinField.getText().trim());
            double salaryMax = Double.parseDouble(salaryMaxField.getText().trim());
            int positions = Integer.parseInt(positionsField.getText().trim());

            if (isEditMode && editingOffer != null) {
                // UPDATE
                editingOffer.setTitle(title);
                editingOffer.setDescription(descriptionField.getText().trim());
                editingOffer.setDepartment(departmentBox.getValue() != null ? departmentBox.getValue() : "");
                editingOffer.setContractType(contractBox.getValue() != null ? contractBox.getValue() : "");
                editingOffer.setExperienceLevel(experienceBox.getValue() != null ? experienceBox.getValue() : "");
                editingOffer.setSalaryMin(salaryMin);
                editingOffer.setSalaryMax(salaryMax);
                editingOffer.setLocation(locationField.getText().trim());
                editingOffer.setClosingDate(closingDatePicker.getValue());
                editingOffer.setPositionsAvailable(positions);
                if (statusBox.getValue() != null)
                    editingOffer.setStatus(statusBox.getValue());

                if (offerService.updateOffer(editingOffer)) {
                    showAlert("Succès", "Offre mise à jour !", Alert.AlertType.INFORMATION);
                    handleDashboard();
                } else {
                    showAlert("Erreur", "Impossible de mettre à jour.", Alert.AlertType.ERROR);
                }
            } else {
                // CREATE
                Offer offer = new Offer();
                offer.setRecruiterId(user.getId());
                offer.setTitle(title);
                offer.setDescription(descriptionField.getText().trim());
                offer.setDepartment(departmentBox.getValue() != null ? departmentBox.getValue() : "");
                offer.setContractType(contractBox.getValue() != null ? contractBox.getValue() : "");
                offer.setExperienceLevel(experienceBox.getValue() != null ? experienceBox.getValue() : "");
                offer.setSalaryMin(salaryMin);
                offer.setSalaryMax(salaryMax);
                offer.setLocation(locationField.getText().trim());
                offer.setStatus("Ouverte");
                offer.setPublishDate(LocalDate.now());
                offer.setClosingDate(closingDatePicker.getValue());
                offer.setPositionsAvailable(positions);
                offer.setApplicationsReceived(0);

                if (offerService.createOffer(offer)) {
                    showAlert("Succès", "Offre créée avec succès !", Alert.AlertType.INFORMATION);
                    handleDashboard();
                } else {
                    showAlert("Erreur", "Impossible de créer l'offre.", Alert.AlertType.ERROR);
                }
            }
        } catch (NumberFormatException e) {
            showAlert("Erreur", "Les champs numériques (salaire, postes) doivent être des nombres valides.",
                    Alert.AlertType.WARNING);
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        SceneUtil.switchScene("recruiter_dashboard.fxml");
    }

    @FXML
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handleJobOffers() {
        SceneUtil.switchScene("OffersCardView.fxml");
    }

    @FXML
    private void handleActivities() {
        SceneUtil.switchScene("activities/activities.fxml");
    }

    @FXML
    private void handleProjects() {
        SceneUtil.switchScene("projects/projects.fxml");
    }

    @FXML
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Fonctionnalité bientôt disponible !").showAndWait();
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
