package talentos.pidev.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import talentos.pidev.models.Application;
import talentos.pidev.models.Offer;
import talentos.pidev.services.ApplicationService;
import talentos.pidev.services.OfferService;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

public class CandidateApplicationCardController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> contractFilter;
    @FXML private FlowPane offersCardsContainer;
    @FXML private Label totalOffersLabel;

    private final OfferService offerService;
    private final ApplicationService applicationService;
    private final ObservableList<Offer> offersList;
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter displayFormatter;

    public CandidateApplicationCardController() {
        this.offerService = new OfferService();
        this.applicationService = new ApplicationService();
        this.offersList = FXCollections.observableArrayList();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        this.displayFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        loadOffers();
        styleComponents();
    }

    // ========== STYLES MODE SOMBRE ==========
    private void styleComponents() {
        // Style des filtres
        String filterStyle = "-fx-background-color: #1E293B; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8;";
        searchField.setStyle(filterStyle);
        departmentFilter.setStyle(filterStyle);
        contractFilter.setStyle(filterStyle);

        // Style du label total
        totalOffersLabel.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-text-fill: #94A3B8;" +
                        "-fx-padding: 8 16;" +
                        "-fx-background-radius: 20;" +
                        "-fx-font-size: 13px;" +
                        "-fx-font-weight: bold;"
        );
    }

    private void setupFilters() {
        departmentFilter.getItems().addAll(
                "Tous", "IT", "RH", "Finance", "Marketing", "Production", "Logistique", "Commerce"
        );
        departmentFilter.setValue("Tous");

        contractFilter.getItems().addAll(
                "Tous", "CDI", "CDD", "Stage", "Alternance", "Freelance"
        );
        contractFilter.setValue("Tous");

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

        if ("Tous".equals(department)) department = null;
        if ("Tous".equals(contract)) contract = null;

        ObservableList<Offer> filtered = FXCollections.observableArrayList();

        for (Offer offer : offersList) {
            boolean matches = true;

            if (keyword != null && !keyword.isEmpty()) {
                matches = offer.getTitle().toLowerCase().contains(keyword) ||
                        offer.getDescription().toLowerCase().contains(keyword) ||
                        offer.getDepartment().toLowerCase().contains(keyword);
            }

            if (matches && department != null) {
                matches = offer.getDepartment().equals(department);
            }

            if (matches && contract != null) {
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

    // ========== CARTE OFFRE EN MODE SOMBRE ==========
    private VBox createOfferCard(Offer offer) {
        VBox card = new VBox(15);
        String defaultStyle = "-fx-background-color: #1E293B; -fx-background-radius: 16; -fx-padding: 20; " +
                "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);";
        String hoverStyle = "-fx-background-color: #1E293B; -fx-background-radius: 16; -fx-padding: 20; " +
                "-fx-border-color: rgba(99,102,241,0.5); -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.25), 18, 0, 0, 5); -fx-translate-y: -2;";

        card.setStyle(defaultStyle);
        card.setPrefWidth(320);
        card.setMinHeight(360);
        card.setMaxHeight(400);

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));

        // En-tête avec titre et badge
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(200);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        // Badge jours restants
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), offer.getClosingDate());
        Label daysBadge = new Label(daysLeft + "j");
        if (daysLeft <= 7) {
            daysBadge.setStyle(
                    "-fx-background-color: rgba(239,68,68,0.2);" +
                            "-fx-text-fill: #F87171;" +
                            "-fx-padding: 4 8;" +
                            "-fx-background-radius: 20;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;"
            );
        } else {
            daysBadge.setStyle(
                    "-fx-background-color: rgba(16,185,129,0.2);" +
                            "-fx-text-fill: #34D399;" +
                            "-fx-padding: 4 8;" +
                            "-fx-background-radius: 20;" +
                            "-fx-font-size: 12px;" +
                            "-fx-font-weight: bold;"
            );
        }

        headerBox.getChildren().addAll(titleLabel, daysBadge);

        // Département et type de contrat
        HBox deptBox = new HBox(8);
        deptBox.setAlignment(Pos.CENTER_LEFT);

        Label deptIcon = new Label("🏢");
        deptIcon.setStyle("-fx-font-size: 14px;");

        Label deptLabel = new Label(offer.getDepartment() + " • " + offer.getContractType());
        deptLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

        deptBox.getChildren().addAll(deptIcon, deptLabel);

        // Localisation
        HBox locationBox = new HBox(8);
        locationBox.setAlignment(Pos.CENTER_LEFT);

        Label locIcon = new Label("📍");
        locIcon.setStyle("-fx-font-size: 14px;");

        Label locationLabel = new Label(offer.getLocation());
        locationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

        locationBox.getChildren().addAll(locIcon, locationLabel);

        // Salaire
        HBox salaryBox = new HBox(8);
        salaryBox.setAlignment(Pos.CENTER_LEFT);

        Label salaryIcon = new Label("💰");
        salaryIcon.setStyle("-fx-font-size: 14px;");

        Label salaryLabel = new Label(String.format("%.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        salaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34D399; -fx-font-weight: 600;");

        salaryBox.getChildren().addAll(salaryIcon, salaryLabel);

        // Expérience
        HBox expBox = new HBox(8);
        expBox.setAlignment(Pos.CENTER_LEFT);

        Label expIcon = new Label("📊");
        expIcon.setStyle("-fx-font-size: 14px;");

        Label expLabel = new Label(offer.getExperienceLevel());
        expLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

        expBox.getChildren().addAll(expIcon, expLabel);

        // Date limite
        HBox dateBox = new HBox(8);
        dateBox.setAlignment(Pos.CENTER_LEFT);

        Label dateIcon = new Label("⏰");
        dateIcon.setStyle("-fx-font-size: 14px;");

        String daysText = daysLeft < 0 ? "Expirée" :
                daysLeft == 0 ? "Dernier jour !" :
                        "Limite: " + offer.getClosingDate().format(displayFormatter);

        Label dateLabel = new Label(daysText);
        dateLabel.setStyle(daysLeft < 0 ? "-fx-font-size: 13px; -fx-text-fill: #F87171;" :
                daysLeft <= 7 ? "-fx-font-size: 13px; -fx-text-fill: #FBBF24;" :
                        "-fx-font-size: 13px; -fx-text-fill: #94A3B8;");

        dateBox.getChildren().addAll(dateIcon, dateLabel);

        // Séparateur
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.06);");

        // Bouton Postuler
        Button applyButton = createDarkButton("📝 Postuler", "rgba(99,102,241,0.2)", "#A5B4FC");
        applyButton.setMaxWidth(Double.MAX_VALUE);
        applyButton.setOnAction(e -> showApplicationForm(offer));

        card.getChildren().addAll(
                headerBox,
                deptBox,
                locationBox,
                salaryBox,
                expBox,
                dateBox,
                sep,
                applyButton
        );

        return card;
    }

    private Button createDarkButton(String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        String normal = "-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor +
                "; -fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        String hover = "-fx-background-color: derive(" + bgColor + ", 30%); -fx-text-fill: " + textColor +
                "; -fx-padding: 10 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    private void showNoOffersMessage() {
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(Pos.CENTER);
        messageBox.setPrefWidth(600);
        messageBox.setPrefHeight(300);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 56px; -fx-text-fill: #475569;");

        Label title = new Label("Aucune offre disponible");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #94A3B8;");

        Label subtitle = new Label("Revenez plus tard, de nouvelles offres seront publiées");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        messageBox.getChildren().addAll(icon, title, subtitle);
        offersCardsContainer.getChildren().add(messageBox);
    }

    private void showApplicationForm(Offer offer) {
        // Vider le conteneur
        offersCardsContainer.getChildren().clear();

        // Créer et afficher le formulaire
        VBox formContainer = createApplicationForm(offer);
        offersCardsContainer.getChildren().add(formContainer);
    }

    // ========== FORMULAIRE EN MODE SOMBRE ==========
    private VBox createApplicationForm(Offer offer) {
        VBox formContainer = new VBox(25);
        formContainer.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 30;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);" +
                        "-fx-border-color: rgba(255,255,255,0.06);" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        );
        formContainer.setMaxWidth(800);
        formContainer.setAlignment(Pos.TOP_CENTER);

        // ========== EN-TÊTE ==========
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));

        Button backBtn = new Button("← Retour aux offres");
        backBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #94A3B8;" +
                        "-fx-font-size: 14px;" +
                        "-fx-cursor: hand;" +
                        "-fx-padding: 5 10;"
        );
        backBtn.setOnAction(e -> backToOffers());

        Label formTitle = new Label("📝 Postuler à l'offre");
        formTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backBtn, spacer, formTitle);

        // ========== CARTE DE L'OFFRE ==========
        VBox offerCard = new VBox(15);
        offerCard.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: rgba(99,102,241,0.2);" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        );

        Label offerTitle = new Label(offer.getTitle());
        offerTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        GridPane offerDetails = new GridPane();
        offerDetails.setHgap(20);
        offerDetails.setVgap(10);

        ColumnConstraints iconCol = new ColumnConstraints();
        iconCol.setPercentWidth(10);
        ColumnConstraints textCol = new ColumnConstraints();
        textCol.setPercentWidth(90);
        offerDetails.getColumnConstraints().addAll(iconCol, textCol);

        int row = 0;

        // Département
        Label deptIcon = new Label("🏢");
        deptIcon.setStyle("-fx-font-size: 16px;");
        Label deptText = new Label(offer.getDepartment() + " • " + offer.getContractType());
        deptText.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");
        offerDetails.add(deptIcon, 0, row);
        offerDetails.add(deptText, 1, row++);

        // Localisation
        Label locIcon = new Label("📍");
        locIcon.setStyle("-fx-font-size: 16px;");
        Label locText = new Label(offer.getLocation());
        locText.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");
        offerDetails.add(locIcon, 0, row);
        offerDetails.add(locText, 1, row++);

        // Salaire
        Label salaryIcon = new Label("💰");
        salaryIcon.setStyle("-fx-font-size: 16px;");
        Label salaryText = new Label(String.format("%.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        salaryText.setStyle("-fx-font-size: 14px; -fx-text-fill: #34D399; -fx-font-weight: 600;");
        offerDetails.add(salaryIcon, 0, row);
        offerDetails.add(salaryText, 1, row++);

        // Expérience
        Label expIcon = new Label("📊");
        expIcon.setStyle("-fx-font-size: 16px;");
        Label expText = new Label(offer.getExperienceLevel());
        expText.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");
        offerDetails.add(expIcon, 0, row);
        offerDetails.add(expText, 1, row++);

        // Date limite
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), offer.getClosingDate());
        String daysText = daysLeft < 0 ? "Expirée" :
                daysLeft == 0 ? "Dernier jour !" :
                        daysLeft + " jour" + (daysLeft > 1 ? "s" : "") + " restant" + (daysLeft > 1 ? "s" : "");

        Label dateIcon = new Label("⏰");
        dateIcon.setStyle("-fx-font-size: 16px;");

        VBox dateBox = new VBox(2);
        Label dateLine1 = new Label("Limite: " + offer.getClosingDate().format(dateFormatter));
        dateLine1.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

        Label dateLine2 = new Label(daysText);
        dateLine2.setStyle(daysLeft < 0 ? "-fx-font-size: 12px; -fx-text-fill: #F87171; -fx-font-weight: bold;" :
                daysLeft <= 7 ? "-fx-font-size: 12px; -fx-text-fill: #FBBF24; -fx-font-weight: bold;" :
                        "-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        dateBox.getChildren().addAll(dateLine1, dateLine2);

        offerDetails.add(dateIcon, 0, row);
        offerDetails.add(dateBox, 1, row);

        offerCard.getChildren().addAll(offerTitle, offerDetails);

        // ========== FORMULAIRE ==========
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setPadding(new Insets(20, 0, 0, 0));

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        formGrid.getColumnConstraints().addAll(col1, col2);

        row = 0;

        // Nom complet
        Label nameLabel = new Label("Nom complet *");
        nameLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField nameField = new TextField();
        nameField.setPromptText("Votre nom et prénom");
        nameField.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");
        formGrid.add(nameLabel, 0, row);
        formGrid.add(nameField, 1, row++);

        // Email
        Label emailLabel = new Label("Email *");
        emailLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField emailField = new TextField();
        emailField.setPromptText("exemple@email.com");
        emailField.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");
        formGrid.add(emailLabel, 0, row);
        formGrid.add(emailField, 1, row++);

        // Téléphone
        Label phoneLabel = new Label("Téléphone");
        phoneLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField phoneField = new TextField();
        phoneField.setPromptText("+216 XX XXX XXX");
        phoneField.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");
        formGrid.add(phoneLabel, 0, row);
        formGrid.add(phoneField, 1, row++);

        // CV
        Label cvLabel = new Label("CV *");
        cvLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        HBox cvBox = new HBox(10);
        cvBox.setAlignment(Pos.CENTER_LEFT);

        Button chooseCVBtn = new Button("📎 Choisir un fichier");
        chooseCVBtn.setStyle(
                "-fx-background-color: #334155;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 10 16;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 13px;" +
                        "-fx-cursor: hand;"
        );

        Label cvFileName = new Label("Aucun fichier sélectionné");
        cvFileName.setStyle("-fx-text-fill: #64748B; -fx-font-size: 13px;");

        final String[] cvPath = {null};

        chooseCVBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Choisir votre CV");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Documents PDF", "*.pdf"),
                    new FileChooser.ExtensionFilter("Documents Word", "*.doc", "*.docx")
            );

            File file = fileChooser.showOpenDialog(null);
            if (file != null) {
                cvPath[0] = file.getAbsolutePath();
                cvFileName.setText("📄 " + file.getName());
                cvFileName.setStyle("-fx-text-fill: #34D399; -fx-font-size: 13px; -fx-font-weight: bold;");
                chooseCVBtn.setStyle(
                        "-fx-background-color: #10B981;" +
                                "-fx-text-fill: white;" +
                                "-fx-padding: 10 16;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 13px;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        cvBox.getChildren().addAll(chooseCVBtn, cvFileName);
        formGrid.add(cvLabel, 0, row);
        formGrid.add(cvBox, 1, row++);

        // Lettre de motivation
        Label motivationLabel = new Label("Lettre de motivation");
        motivationLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextArea motivationArea = new TextArea();
        motivationArea.setPromptText("Parlez-nous de votre motivation...");
        motivationArea.setPrefRowCount(4);
        motivationArea.setWrapText(true);
        motivationArea.setStyle("-fx-background-color: #1E293B; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B; -fx-border-color: #334155; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10;");
        formGrid.add(motivationLabel, 0, row);
        formGrid.add(motivationArea, 1, row++);

        // Message champs obligatoires
        Label requiredLabel = new Label("* Champs obligatoires");
        requiredLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        // ========== BOUTONS ==========
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle(
                "-fx-background-color: #475569;" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> backToOffers());

        Button submitBtn = new Button("📤 Envoyer ma candidature");
        submitBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366F1, #06B6D4);" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 12 24;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        // ========== VALIDATION ==========
        Runnable validateForm = () -> {
            boolean isValid = true;

            // ✅ Validation nom
            if (nameField.getText().trim().isEmpty() || nameField.getText().length() < 3) {
                nameField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: #1E293B; -fx-text-fill: #E2E8F0;");
                isValid = false;
            } else {
                nameField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: #1E293B; -fx-text-fill: #E2E8F0;");
            }

            // ✅ Validation email
            String email = emailField.getText().trim();
            if (email.isEmpty() || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                emailField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: #1E293B; -fx-text-fill: #E2E8F0;");
                isValid = false;
            } else {
                emailField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: #1E293B; -fx-text-fill: #E2E8F0;");
            }

            // ✅ Validation CV
            if (cvPath[0] == null) {
                chooseCVBtn.setStyle(
                        "-fx-background-color: #EF4444;" +
                                "-fx-text-fill: white;" +
                                "-fx-padding: 10 16;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 13px;" +
                                "-fx-cursor: hand;"
                );
                isValid = false;
            } else {
                chooseCVBtn.setStyle(
                        "-fx-background-color: #10B981;" +
                                "-fx-text-fill: white;" +
                                "-fx-padding: 10 16;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 13px;" +
                                "-fx-cursor: hand;"
                );
            }

            // ✅ Activer/désactiver le bouton
            submitBtn.setDisable(!isValid);
            submitBtn.setOpacity(isValid ? 1.0 : 0.5);
        };

// ✅ Écouteurs en temps réel
        nameField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        emailField.textProperty().addListener((obs, old, newVal) -> validateForm.run());

// ✅ Validation initiale
        validateForm.run();

        // Ajouter les écouteurs

        // Action du bouton submit
        submitBtn.setOnAction(e -> {
            Application application = new Application();
            application.setOfferId(offer.getId());
            application.setCandidateName(nameField.getText().trim());
            application.setCandidateEmail(emailField.getText().trim());
            application.setCandidatePhone(phoneField.getText().trim());
            application.setCvFilePath(cvPath[0]);
            application.setMotivationLetter(motivationArea.getText().trim());
            application.setStatus("Nouvelle");
            application.setApplicationDate(LocalDate.now());
            application.setScore(0.0);

            if (applicationService.createApplication(application)) {
                showAlert("✅ Succès",
                        "Votre candidature pour **" + offer.getTitle() + "** a été envoyée avec succès !",
                        Alert.AlertType.INFORMATION);
                backToOffers();
                loadOffers();
            } else {
                showAlert("❌ Erreur",
                        "Une erreur est survenue lors de l'envoi de votre candidature.",
                        Alert.AlertType.ERROR);
            }
        });

        buttonBox.getChildren().addAll(cancelBtn, submitBtn);

        // Assemblage final
        formContainer.getChildren().addAll(
                headerBox,
                offerCard,
                formGrid,
                requiredLabel,
                buttonBox
        );

        return formContainer;
    }

    private void backToOffers() {
        offersCardsContainer.getChildren().clear();
        displayOffersCards(offersList);
    }

    private void updateTotalLabel(int count) {
        totalOffersLabel.setText(count + " offre" + (count > 1 ? "s" : ""));
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
}