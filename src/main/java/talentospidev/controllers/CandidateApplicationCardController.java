package talentospidev.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import talentospidev.models.Application;
import talentospidev.models.Offer;
import talentospidev.services.ApplicationService;
import talentospidev.services.AuthService;
import talentospidev.services.OfferService;
import talentospidev.utils.SceneUtil;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class CandidateApplicationCardController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> departmentFilter;
    @FXML
    private ComboBox<String> contractFilter;
    @FXML
    private FlowPane offersCardsContainer;
    @FXML
    private VBox applicationForm;
    @FXML
    private Label selectedOfferTitle;
    @FXML
    private Label selectedOfferDept;
    @FXML
    private Label selectedOfferLocation;
    @FXML
    private Label selectedOfferSalary;
    @FXML
    private Label selectedOfferClosing;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private TextArea motivationArea;
    @FXML
    private Label cvFileNameLabel;
    @FXML
    private Button submitButton;
    @FXML
    private Button backToOffersButton;
    @FXML
    private Label totalOffersLabel;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final OfferService offerService;
    private final ApplicationService applicationService;
    private final ObservableList<Offer> offersList;
    private final DateTimeFormatter dateFormatter;
    private Offer selectedOffer;
    private String cvFilePath;

    public CandidateApplicationCardController() {
        this.offerService = new OfferService();
        this.applicationService = new ApplicationService();
        this.offersList = FXCollections.observableArrayList();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        setupFilters();
        loadOffers();
        setupApplicationForm();
    }

    private void setupFilters() {
        departmentFilter.getItems().addAll(
                "Tous les départements", "IT", "RH", "Finance", "Marketing", "Production", "Logistique", "Commerce");
        departmentFilter.setValue("Tous les départements");

        contractFilter.getItems().addAll(
                "Tous les contrats", "CDI", "CDD", "Stage", "Alternance", "Freelance");
        contractFilter.setValue("Tous les contrats");

        searchField.textProperty().addListener((obs, old, newVal) -> filterOffers());
        departmentFilter.setOnAction(e -> filterOffers());
        contractFilter.setOnAction(e -> filterOffers());
    }

    private void loadOffers() {
        offersList.clear();
        offersList.addAll(offerService.searchOffers("", "", "Ouverte"));
        displayOffersCards(offersList);
        updateTotalLabel(offersList.size());
    }

    private void filterOffers() {
        String keyword = searchField.getText().toLowerCase();
        String department = departmentFilter.getValue();
        String contract = contractFilter.getValue();

        ObservableList<Offer> filtered = FXCollections.observableArrayList();

        for (Offer offer : offersList) {
            boolean matches = true;

            if (!keyword.isEmpty()) {
                matches = offer.getTitle().toLowerCase().contains(keyword) ||
                        offer.getDescription().toLowerCase().contains(keyword) ||
                        offer.getDepartment().toLowerCase().contains(keyword);
            }

            if (matches && !"Tous les départements".equals(department)) {
                matches = offer.getDepartment().equals(department);
            }

            if (matches && !"Tous les contrats".equals(contract)) {
                matches = offer.getContractType().equals(contract);
            }

            if (matches) {
                filtered.add(offer);
            }
        }

        displayOffersCards(filtered);
        updateTotalLabel(filtered.size());
    }

    private void displayOffersCards(ObservableList<Offer> offers) {
        offersCardsContainer.getChildren().clear();

        for (Offer offer : offers) {
            VBox card = createOfferCard(offer);
            offersCardsContainer.getChildren().add(card);
        }

        if (offers.isEmpty()) {
            showNoOffersMessage();
        }
    }

    private VBox createOfferCard(Offer offer) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 1;");
        card.setPrefWidth(320);
        card.setPrefHeight(380);
        card.setMaxWidth(320);
        card.setMaxHeight(380);

        // Animation au survol
        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 25, 0, 0, 10);" +
                            "-fx-border-color: #3B82F6;" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-width: 2;" +
                            "-fx-scale-x: 1.02;" +
                            "-fx-scale-y: 1.02;");
        });

        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                            "-fx-background-radius: 20;" +
                            "-fx-padding: 20;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);" +
                            "-fx-border-color: #E5E7EB;" +
                            "-fx-border-radius: 20;" +
                            "-fx-border-width: 1;" +
                            "-fx-scale-x: 1;" +
                            "-fx-scale-y: 1;");
        });

        // En-tête avec titre et badge
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(200);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Badge jours restants
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), offer.getClosingDate());
        Label daysBadge = new Label(daysLeft + "j");
        if (daysLeft <= 7) {
            daysBadge.setStyle(
                    "-fx-background-color: #FEE2E2;" +
                            "-fx-text-fill: #DC2626;" +
                            "-fx-padding: 4 8;" +
                            "-fx-background-radius: 20;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;");
        } else {
            daysBadge.setStyle(
                    "-fx-background-color: #DBEAFE;" +
                            "-fx-text-fill: #2563EB;" +
                            "-fx-padding: 4 8;" +
                            "-fx-background-radius: 20;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;");
        }

        headerBox.getChildren().addAll(titleLabel, daysBadge);

        // Département et type de contrat
        HBox deptBox = new HBox(8);
        deptBox.setAlignment(Pos.CENTER_LEFT);

        Label deptIcon = new Label("🏢");
        deptIcon.setStyle("-fx-font-size: 14px;");

        Label deptLabel = new Label(offer.getDepartment() + " • " + offer.getContractType());
        deptLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563;");

        deptBox.getChildren().addAll(deptIcon, deptLabel);

        // Localisation
        HBox locationBox = new HBox(8);
        locationBox.setAlignment(Pos.CENTER_LEFT);

        Label locIcon = new Label("📍");
        locIcon.setStyle("-fx-font-size: 14px;");

        Label locationLabel = new Label(offer.getLocation());
        locationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563;");

        locationBox.getChildren().addAll(locIcon, locationLabel);

        // Salaire
        HBox salaryBox = new HBox(8);
        salaryBox.setAlignment(Pos.CENTER_LEFT);

        Label salaryIcon = new Label("💰");
        salaryIcon.setStyle("-fx-font-size: 14px;");

        Label salaryLabel = new Label(String.format("%.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        salaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563;");

        salaryBox.getChildren().addAll(salaryIcon, salaryLabel);

        // Expérience
        HBox expBox = new HBox(8);
        expBox.setAlignment(Pos.CENTER_LEFT);

        Label expIcon = new Label("📊");
        expIcon.setStyle("-fx-font-size: 14px;");

        Label expLabel = new Label(offer.getExperienceLevel());
        expLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563;");

        expBox.getChildren().addAll(expIcon, expLabel);

        // Date limite
        HBox dateBox = new HBox(8);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        Label dateIcon = new Label("⏰");
        dateIcon.setStyle("-fx-font-size: 14px;");

        Label dateLabel = new Label("Limite: " + offer.getClosingDate().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280;");

        dateBox.getChildren().addAll(dateIcon, dateLabel);

        // Bouton Postuler
        Button applyButton = new Button("📝 Postuler à cette offre");
        applyButton.setStyle(
                "-fx-background-color: #3B82F6;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 10, 0, 0, 2);");
        applyButton.setMaxWidth(Double.MAX_VALUE);

        applyButton.setOnMouseEntered(e -> applyButton.setStyle(
                "-fx-background-color: #2563EB;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(37,99,235,0.4), 15, 0, 0, 4);"));

        applyButton.setOnMouseExited(e -> applyButton.setStyle(
                "-fx-background-color: #3B82F6;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12;" +
                        "-fx-background-radius: 12;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;" +
                        "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 10, 0, 0, 2);"));

        applyButton.setOnAction(e -> selectOffer(offer));

        card.getChildren().addAll(
                headerBox,
                new Separator(),
                deptBox,
                locationBox,
                salaryBox,
                expBox,
                dateBox,
                new Region() {
                    {
                        VBox.setVgrow(this, Priority.ALWAYS);
                    }
                },
                applyButton);

        return card;
    }

    private void showNoOffersMessage() {
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPrefWidth(600);
        messageBox.setPrefHeight(300);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Aucune offre disponible");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");

        Label subtitle = new Label("Revenez plus tard, de nouvelles offres seront publiées");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #9CA3AF;");

        messageBox.getChildren().addAll(icon, title, subtitle);
        offersCardsContainer.getChildren().add(messageBox);
    }

    private void setupApplicationForm() {
        applicationForm.setVisible(false);

        // Validation email
        emailField.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal && !emailField.getText().isEmpty()) {
                validateEmail();
            }
        });

        // Validation nom
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.length() < 3 && newVal.length() > 0) {
                nameField.setStyle("-fx-border-color: #F59E0B; -fx-border-width: 2;");
            } else if (newVal.length() >= 3) {
                nameField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2;");
            } else {
                nameField.setStyle("");
            }
        });
    }

    private void selectOffer(Offer offer) {
        this.selectedOffer = offer;

        // Afficher les détails de l'offre sélectionnée
        selectedOfferTitle.setText(offer.getTitle());
        selectedOfferDept.setText(offer.getDepartment() + " • " + offer.getContractType());
        selectedOfferLocation.setText(offer.getLocation());
        selectedOfferSalary.setText(String.format("%.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        selectedOfferClosing.setText("Limite: " + offer.getClosingDate().format(dateFormatter));

        // Afficher le formulaire
        applicationForm.setVisible(true);

        // Scroll en haut de la page
        applicationForm.requestFocus();
    }

    @FXML
    private void backToOffers() {
        applicationForm.setVisible(false);
        selectedOffer = null;
        clearForm();
    }

    @FXML
    private void chooseCV() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir votre CV");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"),
                new FileChooser.ExtensionFilter("Documents Word", "*.doc", "*.docx"),
                new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));

        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            this.cvFilePath = file.getAbsolutePath();
            cvFileNameLabel.setText("📄 " + file.getName());
            cvFileNameLabel.setStyle("-fx-text-fill: #10B981; -fx-font-weight: bold;");
        }
    }

    private void validateEmail() {
        String email = emailField.getText();
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            emailField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2;");
            showAlert("Email invalide", "Veuillez entrer un email valide (ex: nom@email.com)", Alert.AlertType.WARNING);
        } else {
            emailField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2;");
        }
    }

    @FXML
    private void submitApplication() {
        // Validation
        if (selectedOffer == null) {
            showAlert("Erreur", "Veuillez sélectionner une offre", Alert.AlertType.ERROR);
            return;
        }

        if (nameField.getText().trim().isEmpty() || nameField.getText().length() < 3) {
            showAlert("Erreur", "Veuillez entrer votre nom complet (min. 3 caractères)", Alert.AlertType.ERROR);
            nameField.requestFocus();
            return;
        }

        String email = emailField.getText().trim();
        if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showAlert("Erreur", "Veuillez entrer un email valide", Alert.AlertType.ERROR);
            emailField.requestFocus();
            return;
        }

        if (cvFilePath == null || cvFilePath.isEmpty()) {
            showAlert("Erreur", "Veuillez choisir votre CV", Alert.AlertType.ERROR);
            return;
        }

        // Créer la candidature via userId
        talentospidev.models.User currentUser = AuthService.getCurrentUser();
        if (currentUser == null) {
            showAlert("Erreur", "Vous devez être connecté pour postuler", Alert.AlertType.ERROR);
            return;
        }

        Application application = new Application(
                currentUser.getId(),
                selectedOffer.getId(),
                cvFilePath,
                motivationArea.getText().trim());

        // Sauvegarder
        if (applicationService.createApplication(application)) {
            // Animation de succès
            showSuccessAnimation();

            showAlert("✅ Félicitations !",
                    "Votre candidature a été envoyée avec succès !\n\n" +
                            "Nous vous contacterons très prochainement.",
                    Alert.AlertType.INFORMATION);

            clearForm();
            backToOffers();
            loadOffers(); // Rafraîchir la liste
        } else {
            showAlert("❌ Erreur",
                    "Une erreur est survenue lors de l'envoi de votre candidature.\n" +
                            "Veuillez réessayer plus tard ou contacter le support.",
                    Alert.AlertType.ERROR);
        }
    }

    private void showSuccessAnimation() {
        submitButton.setText("✅ Envoyé !");
        submitButton.setStyle(
                "-fx-background-color: #10B981;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12;" +
                        "-fx-background-radius: 12;");

        // Remettre le texte après 2 secondes
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                javafx.application.Platform.runLater(() -> {
                    submitButton.setText("📤 Envoyer ma candidature");
                    submitButton.setStyle(
                            "-fx-background-color: #3B82F6;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-padding: 12;" +
                                    "-fx-background-radius: 12;");
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void clearForm() {
        nameField.clear();
        emailField.clear();
        phoneField.clear();
        motivationArea.clear();
        cvFilePath = null;
        cvFileNameLabel.setText("Aucun fichier sélectionné");
        cvFileNameLabel.setStyle("-fx-text-fill: #6B7280;");

        // Reset styles
        nameField.setStyle("");
        emailField.setStyle("");
    }

    private void updateTotalLabel(int count) {
        totalOffersLabel.setText(count + " offre" + (count > 1 ? "s" : "") + " disponible" + (count > 1 ? "s" : ""));
    }

    @FXML
    private void refreshOffers() {
        loadOffers();
        showAlert("Actualisé", "La liste des offres a été mise à jour", Alert.AlertType.INFORMATION);
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ========== SIDEBAR NAVIGATION ==========

    @FXML
    private void handleDashboard() {
        SceneUtil.switchScene("dashboard.fxml");
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
    private void handleToDo() {
        SceneUtil.switchScene("activities/activity_employee.fxml");
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
        new Alert(Alert.AlertType.INFORMATION, "This feature is coming soon!").show();
    }

    @FXML
    private void handleSettings() {
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
