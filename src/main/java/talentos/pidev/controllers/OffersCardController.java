package talentos.pidev.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.time.temporal.ChronoUnit;
import javafx.util.Callback;
import talentos.pidev.models.Offer;
import talentos.pidev.services.OfferService;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class OffersCardController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> departmentFilter;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Label totalOffersLabel;
    @FXML
    private ComboBox<String> sortComboBox;

    private final OfferService offerService;
    private final ObservableList<Offer> offersList;
    private final DateTimeFormatter dateFormatter;

    public OffersCardController() {
        this.offerService = new OfferService();
        this.offersList = FXCollections.observableArrayList();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadFilters();
        loadSortOptions();
        loadOffers();
    }

    private void loadFilters() {
        departmentFilter.getItems().addAll(
                "Tous", "IT", "RH", "Finance", "Marketing", "Production", "Logistique", "Commerce");
        departmentFilter.setValue("Tous");

        statusFilter.getItems().addAll("Tous", "Ouverte", "Fermée", "En attente", "Pourvue");
        statusFilter.setValue("Tous");

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterOffers());
        departmentFilter.setOnAction(e -> filterOffers());
        statusFilter.setOnAction(e -> filterOffers());
        sortComboBox.setOnAction(e -> sortOffers());
    }

    private void loadSortOptions() {
        sortComboBox.getItems().addAll(
                "Date (récent → ancien)",
                "Date (ancien → récent)",
                "Titre (A → Z)",
                "Titre (Z → A)",
                "Salaire (↑)",
                "Salaire (↓)");
        sortComboBox.setValue("Date (récent → ancien)");
    }

    @FXML
    private void loadOffers() {
        offersList.clear();
        offersList.addAll(offerService.getAllOffers());
        updateTotalLabel();
        displayCards(offersList);
    }

    private void filterOffers() {
        String keyword = searchField.getText().toLowerCase();
        String department = departmentFilter.getValue();
        String status = statusFilter.getValue();

        ObservableList<Offer> filtered = FXCollections.observableArrayList();

        for (Offer offer : offersList) {
            boolean matches = true;

            if (!keyword.isEmpty()) {
                matches = offer.getTitle().toLowerCase().contains(keyword) ||
                        offer.getDescription().toLowerCase().contains(keyword) ||
                        offer.getDepartment().toLowerCase().contains(keyword);
            }

            if (matches && !"Tous".equals(department)) {
                matches = offer.getDepartment().equals(department);
            }

            if (matches && !"Tous".equals(status)) {
                matches = offer.getStatus().equals(status);
            }

            if (matches) {
                filtered.add(offer);
            }
        }

        displayCards(filtered);
        updateTotalLabel(filtered.size());
    }

    private void sortOffers() {
        String sortOption = sortComboBox.getValue();

        switch (sortOption) {
            case "Date (récent → ancien)":
                offersList.sort((o1, o2) -> o2.getPublishDate().compareTo(o1.getPublishDate()));
                break;
            case "Date (ancien → récent)":
                offersList.sort((o1, o2) -> o1.getPublishDate().compareTo(o2.getPublishDate()));
                break;
            case "Titre (A → Z)":
                offersList.sort((o1, o2) -> o1.getTitle().compareTo(o2.getTitle()));
                break;
            case "Titre (Z → A)":
                offersList.sort((o1, o2) -> o2.getTitle().compareTo(o1.getTitle()));
                break;
            case "Salaire (↑)":
                offersList.sort((o1, o2) -> Double.compare(o1.getSalaryMin(), o2.getSalaryMin()));
                break;
            case "Salaire (↓)":
                offersList.sort((o1, o2) -> Double.compare(o2.getSalaryMax(), o1.getSalaryMax()));
                break;
        }

        filterOffers();
    }

    private void displayCards(ObservableList<Offer> offers) {
        cardsContainer.getChildren().clear();

        for (Offer offer : offers) {
            VBox card = createCard(offer);
            cardsContainer.getChildren().add(card);
        }

        if (offers.isEmpty()) {
            showNoResultsMessage();
        }
    }

    private VBox createCard(Offer offer) {
        VBox card = new VBox(10);
        String defaultStyle = "-fx-background-color: #1E293B; -fx-background-radius: 16; -fx-padding: 18; " +
                "-fx-border-color: rgba(255,255,255,0.06); -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);";
        String hoverStyle = "-fx-background-color: #1E293B; -fx-background-radius: 16; -fx-padding: 18; " +
                "-fx-border-color: rgba(99,102,241,0.5); -fx-border-radius: 16; -fx-border-width: 1; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.25), 18, 0, 0, 5); -fx-translate-y: -2;";
        card.setStyle(defaultStyle);
        card.setPrefWidth(340);
        card.setMinHeight(260);
        card.setMaxHeight(300);

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));

        // En-tête
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(210);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label statusLabel = new Label(offer.getStatus());
        statusLabel.setStyle(getStatusStyle(offer.getStatus()));

        header.getChildren().addAll(titleLabel, statusLabel);

        // Département
        Label deptLabel = new Label("🏢 " + offer.getDepartment() + " • " + offer.getContractType());
        deptLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");

        // Localisation
        Label locationLabel = new Label("📍 " + offer.getLocation());
        locationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");

        // Salaire
        Label salaryLabel = new Label(String.format("💰 %.0f - %.0f DT",
                offer.getSalaryMin(), offer.getSalaryMax()));
        salaryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34D399;");

        // Expérience
        Label expLabel = new Label("📊 " + offer.getExperienceLevel());
        expLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");

        // Footer
        HBox footer = new HBox(14);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        footer.setPadding(new Insets(6, 0, 0, 0));

        Label dateLabel = new Label("📅 " + offer.getClosingDate().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        Label appLabel = new Label("👥 " + offer.getApplicationsReceived() + " candidatures");
        appLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");

        footer.getChildren().addAll(dateLabel, appLabel);

        // Separator
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.06);");

        // Boutons
        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        Button viewBtn = createDarkButton("👁 Voir", "rgba(99,102,241,0.2)", "#A5B4FC");
        viewBtn.setOnAction(e -> showOfferDetails(offer));

        Button editBtn = createDarkButton("✏ Modifier", "rgba(245,158,11,0.2)", "#FBBF24");
        editBtn.setOnAction(e -> showEditForm(offer));

        Button deleteBtn = createDarkButton("🗑 Supprimer", "rgba(239,68,68,0.2)", "#F87171");
        deleteBtn.setOnAction(e -> deleteOffer(offer));

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        card.getChildren().addAll(header, deptLabel, locationLabel, salaryLabel, expLabel, footer, sep, actions);

        return card;
    }

    private Button createDarkButton(String text, String bgColor, String textColor) {
        Button btn = new Button(text);
        String normal = "-fx-background-color: " + bgColor + "; -fx-text-fill: " + textColor +
                "; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;";
        String hover = "-fx-background-color: derive(" + bgColor + ", 30%); -fx-text-fill: " + textColor +
                "; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hover));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "Ouverte":
                return "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #34D399; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "Fermée":
                return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #F87171; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: rgba(245,158,11,0.15); -fx-text-fill: #FBBF24; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #94A3B8; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
        }
    }

    private void showNoResultsMessage() {
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(javafx.geometry.Pos.CENTER);
        messageBox.setPrefWidth(600);
        messageBox.setPrefHeight(300);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 56px;");

        Label title = new Label("Aucune offre trouvée");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #94A3B8;");

        Label subtitle = new Label("Cliquez sur 'Nouvelle offre' pour créer votre première offre");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        Button newOfferBtn = new Button("+ Créer une offre");
        newOfferBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366F1, #06B6D4); -fx-text-fill: white; -fx-padding: 10 24; -fx-background-radius: 12; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        newOfferBtn.setOnAction(e -> addNewOffer());

        messageBox.getChildren().addAll(icon, title, subtitle, newOfferBtn);
        cardsContainer.getChildren().add(messageBox);
    }

    private void updateTotalLabel() {
        updateTotalLabel(offersList.size());
    }

    private void updateTotalLabel(int count) {
        totalOffersLabel.setText(count + " offre" + (count > 1 ? "s" : ""));
    }

    // Remplacer la méthode addNewOffer() par celle-ci :

    @FXML
    private void addNewOffer() {
        // Masquer le conteneur des cartes
        cardsContainer.setVisible(false);

        // Créer et afficher le formulaire de création
        VBox createForm = createOfferForm();
        cardsContainer.getChildren().clear();
        cardsContainer.getChildren().add(createForm);
        cardsContainer.setVisible(true);
    }

    /**
     * Crée le formulaire de création d'offre
     */
    /**
     * Crée le formulaire de création d'offre avec validation
     */
    private VBox createOfferForm() {
        VBox formContainer = new VBox(20);
        formContainer.setStyle("-fx-background-color: #1E293B; -fx-padding: 30; -fx-background-radius: 16; -fx-max-width: 800;");
        formContainer.setMaxWidth(800);
        formContainer.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // En-tête avec bouton retour
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button backBtn = new Button("← Retour aux offres");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 5 10;");
        backBtn.setOnAction(e -> backToOffers());

        Label titleLabel = new Label("➕ Créer une nouvelle offre");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backBtn, spacer, titleLabel);

        // Formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(javafx.geometry.Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        formGrid.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        // ========== TITRE ==========
        Label titleFieldLabel = new Label("Titre *");
        titleFieldLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField titleField = new TextField();
        titleField.setPromptText("Ex: Développeur Java Senior");
        titleField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B;");

        Label titleError = new Label();
        titleError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox titleBox = new VBox(3);
        titleBox.getChildren().addAll(titleField, titleError);
        formGrid.add(titleFieldLabel, 0, row);
        formGrid.add(titleBox, 1, row++);

        // ========== DESCRIPTION ==========
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Décrivez les missions, responsabilités, etc.");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B;");

        formGrid.add(descLabel, 0, row);
        formGrid.add(descriptionArea, 1, row++);

        // ========== DÉPARTEMENT ==========
        Label deptLabel = new Label("Département *");
        deptLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> departmentCombo = new ComboBox<>();
        departmentCombo.getItems().addAll("IT", "RH", "Finance", "Marketing", "Production", "Logistique", "Commerce");
        departmentCombo.setPromptText("Sélectionnez un département");
        departmentCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        Label deptError = new Label();
        deptError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox deptBox = new VBox(3);
        deptBox.getChildren().addAll(departmentCombo, deptError);
        formGrid.add(deptLabel, 0, row);
        formGrid.add(deptBox, 1, row++);

        // ========== TYPE DE CONTRAT ==========
        Label contractLabel = new Label("Type de contrat *");
        contractLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> contractCombo = new ComboBox<>();
        contractCombo.getItems().addAll("CDI", "CDD", "Stage", "Alternance", "Freelance");
        contractCombo.setPromptText("Sélectionnez un type");
        contractCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        Label contractError = new Label();
        contractError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox contractBox = new VBox(3);
        contractBox.getChildren().addAll(contractCombo, contractError);
        formGrid.add(contractLabel, 0, row);
        formGrid.add(contractBox, 1, row++);

        // ========== NIVEAU D'EXPÉRIENCE ==========
        Label expLabel = new Label("Expérience *");
        expLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> expCombo = new ComboBox<>();
        expCombo.getItems().addAll("Débutant", "Junior", "Intermédiaire", "Senior", "Expert");
        expCombo.setPromptText("Sélectionnez un niveau");
        expCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        Label expError = new Label();
        expError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox expBox = new VBox(3);
        expBox.getChildren().addAll(expCombo, expError);
        formGrid.add(expLabel, 0, row);
        formGrid.add(expBox, 1, row++);

        // ========== SALAIRE MIN ==========
        Label salaryMinLabel = new Label("Salaire min *");
        salaryMinLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField salaryMinField = new TextField();
        salaryMinField.setPromptText("Ex: 45000");
        salaryMinField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B;");

        // Validation pour n'accepter que les nombres
        salaryMinField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                salaryMinField.setText(newVal.replaceAll("[^\\d.]", ""));
            }
        });

        Label salaryMinError = new Label();
        salaryMinError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox salaryMinBox = new VBox(3);
        salaryMinBox.getChildren().addAll(salaryMinField, salaryMinError);
        formGrid.add(salaryMinLabel, 0, row);
        formGrid.add(salaryMinBox, 1, row);

        // ========== SALAIRE MAX ==========
        Label salaryMaxLabel = new Label("Salaire max *");
        salaryMaxLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField salaryMaxField = new TextField();
        salaryMaxField.setPromptText("Ex: 65000");
        salaryMaxField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B;");

        // Validation pour n'accepter que les nombres
        salaryMaxField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                salaryMaxField.setText(newVal.replaceAll("[^\\d.]", ""));
            }
        });

        Label salaryMaxError = new Label();
        salaryMaxError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox salaryMaxBox = new VBox(3);
        salaryMaxBox.getChildren().addAll(salaryMaxField, salaryMaxError);
        formGrid.add(salaryMaxLabel, 2, row);
        formGrid.add(salaryMaxBox, 3, row++);

        // ========== LOCALISATION ==========
        Label locationLabel = new Label("Localisation *");
        locationLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> locationCombo = new ComboBox<>();
        locationCombo.getItems().addAll("Tunis", "Sfax", "Sousse", "Gabès", "Bizerte", "Ariana", "Ben Arous", "Nabeul");
        locationCombo.setPromptText("Sélectionnez une ville");
        locationCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");
        locationCombo.setEditable(true);

        Label locationError = new Label();
        locationError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox locationBox = new VBox(3);
        locationBox.getChildren().addAll(locationCombo, locationError);
        formGrid.add(locationLabel, 0, row);
        formGrid.add(locationBox, 1, row++);

        // ========== DATE PUBLICATION ==========
        Label publishLabel = new Label("Date publication");
        publishLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        DatePicker publishDatePicker = new DatePicker(LocalDate.now());
        publishDatePicker.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        formGrid.add(publishLabel, 0, row);
        formGrid.add(publishDatePicker, 1, row++);

        // ========== DATE CLÔTURE ==========
        Label closingLabel = new Label("Date clôture *");
        closingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        DatePicker closingDatePicker = new DatePicker(LocalDate.now().plusMonths(1));
        closingDatePicker.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        Label closingError = new Label();
        closingError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox closingBox = new VBox(3);
        closingBox.getChildren().addAll(closingDatePicker, closingError);
        formGrid.add(closingLabel, 0, row);
        formGrid.add(closingBox, 1, row++);

        // ========== POSTES DISPONIBLES ==========
        Label positionsLabel = new Label("Postes disponibles");
        positionsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        Spinner<Integer> positionsSpinner = new Spinner<>(1, 50, 1);
        positionsSpinner.setEditable(true);
        positionsSpinner.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: #E2E8F0;");

        formGrid.add(positionsLabel, 0, row);
        formGrid.add(positionsSpinner, 1, row++);

        // Bouton de sauvegarde (désactivé par défaut)
        Button saveBtn = new Button("Créer l'offre");
        saveBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: #94A3B8; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        saveBtn.setDisable(true);

        // ========== VALIDATION EN TEMPS RÉEL ==========
        Runnable validateForm = () -> {
            boolean isValid = true;

            // Validation titre
            String title = titleField.getText();
            if (title.isEmpty()) {
                titleError.setText("❌ Le titre est obligatoire");
                titleField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else if (title.length() < 3) {
                titleError.setText("❌ Minimum 3 caractères");
                titleField.setStyle("-fx-border-color: #F59E0B; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                titleError.setText("✅ Valide");
                titleError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                titleField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation département
            if (departmentCombo.getValue() == null) {
                deptError.setText("❌ Sélectionnez un département");
                departmentCombo.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                deptError.setText("✅ Valide");
                deptError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                departmentCombo.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation type contrat
            if (contractCombo.getValue() == null) {
                contractError.setText("❌ Sélectionnez un type de contrat");
                contractCombo.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                contractError.setText("✅ Valide");
                contractError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                contractCombo.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation expérience
            if (expCombo.getValue() == null) {
                expError.setText("❌ Sélectionnez un niveau");
                expCombo.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                expError.setText("✅ Valide");
                expError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                expCombo.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation salaire min
            String salMin = salaryMinField.getText();
            if (salMin.isEmpty()) {
                salaryMinError.setText("❌ Salaire minimum obligatoire");
                salaryMinField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                try {
                    double min = Double.parseDouble(salMin);
                    if (min <= 0) {
                        salaryMinError.setText("❌ Doit être > 0");
                        salaryMinField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                        isValid = false;
                    } else {
                        salaryMinError.setText("✅ Valide");
                        salaryMinError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                        salaryMinField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                    }
                } catch (NumberFormatException e) {
                    salaryMinError.setText("❌ Format invalide");
                    salaryMinField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                    isValid = false;
                }
            }

            // Validation salaire max
            String salMax = salaryMaxField.getText();
            if (salMax.isEmpty()) {
                salaryMaxError.setText("❌ Salaire maximum obligatoire");
                salaryMaxField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                try {
                    double max = Double.parseDouble(salMax);
                    if (max <= 0) {
                        salaryMaxError.setText("❌ Doit être > 0");
                        salaryMaxField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                        isValid = false;
                    } else {
                        salaryMaxError.setText("✅ Valide");
                        salaryMaxError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                        salaryMaxField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                    }
                } catch (NumberFormatException e) {
                    salaryMaxError.setText("❌ Format invalide");
                    salaryMaxField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                    isValid = false;
                }
            }

            // Validation croisée des salaires
            if (!salMin.isEmpty() && !salMax.isEmpty()) {
                try {
                    double min = Double.parseDouble(salMin);
                    double max = Double.parseDouble(salMax);
                    if (max < min) {
                        salaryMinError.setText("❌ Min > Max");
                        salaryMaxError.setText("❌ Max < Min");
                        salaryMinField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                        salaryMaxField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    // Déjà géré
                }
            }

            // Validation localisation
            if (locationCombo.getValue() == null || locationCombo.getValue().trim().isEmpty()) {
                locationError.setText("❌ Localisation obligatoire");
                locationCombo.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                locationError.setText("✅ Valide");
                locationError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                locationCombo.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation date clôture
            LocalDate closingDate = closingDatePicker.getValue();
            if (closingDate == null) {
                closingError.setText("❌ Date de clôture obligatoire");
                closingDatePicker.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else if (closingDate.isBefore(LocalDate.now())) {
                closingError.setText("❌ Ne peut pas être dans le passé");
                closingDatePicker.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                closingError.setText("✅ Valide");
                closingError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                closingDatePicker.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Activer/désactiver le bouton
            if (isValid) {
                saveBtn.setDisable(false);
                saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #6366F1, #06B6D4); -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                saveBtn.setDisable(true);
                saveBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: #94A3B8; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            }
        };

        // Ajouter les écouteurs de validation
        titleField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        departmentCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        contractCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        expCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        salaryMinField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        salaryMaxField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        locationCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        closingDatePicker.valueProperty().addListener((obs, old, newVal) -> validateForm.run());

        // Validation initiale
        validateForm.run();

        // ========== BOUTON ANNULER ==========
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> backToOffers());

        // Action du bouton sauvegarder
        saveBtn.setOnAction(e -> {
            try {
                Offer newOffer = new Offer();
                newOffer.setTitle(titleField.getText());
                newOffer.setDescription(descriptionArea.getText());
                newOffer.setDepartment(departmentCombo.getValue());
                newOffer.setContractType(contractCombo.getValue());
                newOffer.setExperienceLevel(expCombo.getValue());
                newOffer.setSalaryMin(Double.parseDouble(salaryMinField.getText()));
                newOffer.setSalaryMax(Double.parseDouble(salaryMaxField.getText()));
                newOffer.setLocation(locationCombo.getValue());
                newOffer.setStatus("Ouverte");
                newOffer.setPublishDate(publishDatePicker.getValue());
                newOffer.setClosingDate(closingDatePicker.getValue());
                newOffer.setPositionsAvailable(positionsSpinner.getValue());
                newOffer.setApplicationsReceived(0);

                if (offerService.createOffer(newOffer)) {
                    showAlert("Succès", "Offre créée avec succès!", Alert.AlertType.INFORMATION);
                    backToOffers();
                    loadOffers();
                } else {
                    showAlert("Erreur", "Erreur lors de la création", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException ex) {
                showAlert("Erreur", "Vérifiez les champs numériques", Alert.AlertType.ERROR);
            }
        });

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        // Message champs obligatoires
        Label requiredLabel = new Label("* Champs obligatoires");
        requiredLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        // Assemblage final
        formContainer.getChildren().addAll(headerBox, formGrid, buttonBox, requiredLabel);

        return formContainer;
    }

    /**
     * Retourne à la liste des offres
     */
    private void backToOffers() {
        cardsContainer.getChildren().clear();
        displayCards(offersList);
    }
    private VBox createValidatedField(String label, String placeholder,
            java.util.function.Function<String, String> validator) {
        VBox box = new VBox(5);

        Label labelField = new Label(label);
        labelField.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; " +
                "-fx-font-size: 14px; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px; -fx-padding: 2 0 0 5;");

        // Validation en temps réel
        textField.textProperty().addListener((obs, old, newVal) -> {
            String error = validator.apply(newVal);
            if (error == null) {
                errorLabel.setText("✅ Valide");
                errorLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px; -fx-padding: 2 0 0 5;");
                textField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: #F0FDF4;");
            } else {
                errorLabel.setText("❌ " + error);
                errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px; -fx-padding: 2 0 0 5;");
                textField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: #FEF2F2;");
            }
        });

        box.getChildren().addAll(labelField, textField, errorLabel);

        return box;
    }

    /**
     * Crée un ComboBox avec validation
     */
    private VBox createValidatedCombo(String label, java.util.function.Function<String, String> validator,
            String... items) {
        VBox box = new VBox(5);

        Label labelField = new Label(label);
        labelField.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(items);
        comboBox.setPromptText("Sélectionner...");
        comboBox.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; " +
                "-fx-font-size: 14px;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px; -fx-padding: 2 0 0 5;");

        comboBox.valueProperty().addListener((obs, old, newVal) -> {
            String error = validator.apply(newVal);
            if (error == null) {
                errorLabel.setText("✅ Valide");
                errorLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px; -fx-padding: 2 0 0 5;");
                comboBox.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: #F0FDF4;");
            } else {
                errorLabel.setText("❌ " + error);
                errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px; -fx-padding: 2 0 0 5;");
                comboBox.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: #FEF2F2;");
            }
        });

        box.getChildren().addAll(labelField, comboBox, errorLabel);

        return box;
    }

    /**
     * Crée un DatePicker avec validation
     */
    private VBox createValidatedDateField(String label, LocalDate defaultValue,
            java.util.function.Function<LocalDate, String> validator) {
        VBox box = new VBox(5);

        Label labelField = new Label(label);
        labelField.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        DatePicker datePicker = new DatePicker(defaultValue);
        datePicker.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); " +
                "-fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5;");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px; -fx-padding: 2 0 0 5;");

        datePicker.valueProperty().addListener((obs, old, newVal) -> {
            String error = validator.apply(newVal);
            if (error == null) {
                errorLabel.setText("✅ Valide");
                errorLabel.setStyle("-fx-text-fill: #10B981; -fx-font-size: 12px; -fx-padding: 2 0 0 5;");
                datePicker.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: #F0FDF4;");
            } else {
                errorLabel.setText("❌ " + error);
                errorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px; -fx-padding: 2 0 0 5;");
                datePicker.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: #FEF2F2;");
            }
        });

        box.getChildren().addAll(labelField, datePicker, errorLabel);

        return box;
    }

    /**
     * Valide un champ et retourne true si valide
     */
    private boolean validateField(VBox fieldBox) {
        if (fieldBox.getChildren().size() < 3)
            return true;

        Label errorLabel = (Label) fieldBox.getChildren().get(2);
        return errorLabel.getText().startsWith("✅");
    }

    /**
     * Ajoute un écouteur de validation
     */
    private void addValidationListener(VBox fieldBox, Runnable validator) {
        if (fieldBox.getChildren().size() < 2)
            return;

        Control control = (Control) fieldBox.getChildren().get(1);

        if (control instanceof TextField) {
            ((TextField) control).textProperty().addListener((obs, old, newVal) -> validator.run());
        } else if (control instanceof ComboBox) {
            ((ComboBox<?>) control).valueProperty().addListener((obs, old, newVal) -> validator.run());
        } else if (control instanceof DatePicker) {
            ((DatePicker) control).valueProperty().addListener((obs, old, newVal) -> validator.run());
        }
    }

    private void showOfferDetails(Offer offer) {
        // Vider le conteneur des cartes
        cardsContainer.getChildren().clear();

        // Créer et afficher la vue détails
        VBox detailsView = createDetailsView(offer);
        cardsContainer.getChildren().add(detailsView);
    }
    /**
     * Crée la vue détaillée d'une offre
     /**
     * Crée la vue détaillée d'une offre - Version corrigée
     */
    private VBox createDetailsView(Offer offer) {
        VBox container = new VBox(25);
        container.setStyle("-fx-background-color: #0F172A; -fx-padding: 30; -fx-background-radius: 16; -fx-max-width: 900;");
        container.setMaxWidth(900);
        container.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // ========== EN-TÊTE AVEC BOUTON RETOUR ==========
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button backBtn = new Button("← Retour aux offres");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 5 10;");
        backBtn.setOnAction(e -> backToOffers());

        Label titleLabel = new Label("📋 Détails de l'offre");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Label offerTitle = new Label(" • " + offer.getTitle());
        offerTitle.setStyle("-fx-font-size: 20px; -fx-text-fill: #A5B4FC; -fx-font-weight: 600;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Boutons d'action rapides
        Button editBtn = new Button("✏ Modifier");
        editBtn.setStyle("-fx-background-color: rgba(245,158,11,0.2); -fx-text-fill: #FBBF24; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        editBtn.setOnAction(e -> showEditForm(offer));

        Button deleteBtn = new Button("🗑 Supprimer");
        deleteBtn.setStyle("-fx-background-color: rgba(239,68,68,0.2); -fx-text-fill: #F87171; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: bold; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> deleteOffer(offer));

        headerBox.getChildren().addAll(backBtn, spacer, titleLabel, offerTitle, editBtn, deleteBtn);

        // ========== BANNIÈRE GRADIENT ==========
        VBox bannerBox = new VBox(15);
        bannerBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #6366F1, #06B6D4);" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 25;" +
                        "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.3), 15, 0, 0, 5);"
        );

        // Titre dans la bannière
        Label bannerTitle = new Label(offer.getTitle());
        bannerTitle.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox badgesRow = new HBox(15);
        badgesRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statusBadge = new Label(offer.getStatus());
        statusBadge.setStyle(getStatusStyle(offer.getStatus()) + "-fx-font-size: 14px; -fx-padding: 6 16;");

        Label departmentBadge = new Label(offer.getDepartment());
        departmentBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 13px; -fx-font-weight: 600;");

        Label contractBadge = new Label(offer.getContractType());
        contractBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 13px; -fx-font-weight: 600;");

        Label expBadge = new Label(offer.getExperienceLevel());
        expBadge.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 13px; -fx-font-weight: 600;");

        badgesRow.getChildren().addAll(statusBadge, departmentBadge, contractBadge, expBadge);

        bannerBox.getChildren().addAll(bannerTitle, badgesRow);

        // ========== CARTE INFORMATIONS ==========
        VBox infoCard = new VBox(20);
        infoCard.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 25;" +
                        "-fx-border-color: rgba(99,102,241,0.2);" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        );

        // Grille d'informations (2 colonnes)
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(30);
        infoGrid.setVgap(20);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(15);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(35);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(15);
        ColumnConstraints col4 = new ColumnConstraints();
        col4.setPercentWidth(35);
        infoGrid.getColumnConstraints().addAll(col1, col2, col3, col4);

        // Ligne 1: Localisation
        Label locIcon = new Label("📍");
        locIcon.setStyle("-fx-font-size: 18px;");
        Label locLabel = new Label("Localisation");
        locLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
        HBox locTitleBox = new HBox(8, locIcon, locLabel);

        Label locValue = new Label(offer.getLocation());
        locValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #F1F5F9; -fx-font-weight: 600;");

        // Ligne 1 (colonne 2): Salaire
        Label salaryIcon = new Label("💰");
        salaryIcon.setStyle("-fx-font-size: 18px;");
        Label salaryLabel = new Label("Salaire");
        salaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
        HBox salaryTitleBox = new HBox(8, salaryIcon, salaryLabel);

        Label salaryValue = new Label(String.format("%.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        salaryValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #F1F5F9; -fx-font-weight: 600;");

        infoGrid.add(locTitleBox, 0, 0);
        infoGrid.add(locValue, 1, 0);
        infoGrid.add(salaryTitleBox, 2, 0);
        infoGrid.add(salaryValue, 3, 0);

        // Ligne 2: Postes disponibles
        Label positionsIcon = new Label("👥");
        positionsIcon.setStyle("-fx-font-size: 18px;");
        Label positionsLabel = new Label("Postes disponibles");
        positionsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
        HBox positionsTitleBox = new HBox(8, positionsIcon, positionsLabel);

        Label positionsValue = new Label(String.valueOf(offer.getPositionsAvailable()));
        positionsValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #F1F5F9; -fx-font-weight: 600;");

        // Ligne 2 (colonne 2): Candidatures reçues
        Label appsIcon = new Label("📋");
        appsIcon.setStyle("-fx-font-size: 18px;");
        Label appsLabel = new Label("Candidatures reçues");
        appsLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
        HBox appsTitleBox = new HBox(8, appsIcon, appsLabel);

        Label appsValue = new Label(String.valueOf(offer.getApplicationsReceived()));
        appsValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #F1F5F9; -fx-font-weight: 600;");

        infoGrid.add(positionsTitleBox, 0, 1);
        infoGrid.add(positionsValue, 1, 1);
        infoGrid.add(appsTitleBox, 2, 1);
        infoGrid.add(appsValue, 3, 1);

        // Ligne 3: Date publication
        Label publishIcon = new Label("📅");
        publishIcon.setStyle("-fx-font-size: 18px;");
        Label publishLabel = new Label("Date publication");
        publishLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
        HBox publishTitleBox = new HBox(8, publishIcon, publishLabel);

        Label publishValue = new Label(offer.getPublishDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        publishValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #F1F5F9; -fx-font-weight: 600;");

        // Ligne 3 (colonne 2): Date clôture
        Label closingIcon = new Label("⏰");
        closingIcon.setStyle("-fx-font-size: 18px;");
        Label closingLabel = new Label("Date clôture");
        closingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
        HBox closingTitleBox = new HBox(8, closingIcon, closingLabel);

        // Calcul des jours restants
        long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), offer.getClosingDate());
        String daysText;
        String daysColor;

        if (daysLeft < 0) {
            daysText = "Expirée";
            daysColor = "#EF4444";
        } else if (daysLeft == 0) {
            daysText = "Dernier jour !";
            daysColor = "#F59E0B";
        } else if (daysLeft <= 7) {
            daysText = daysLeft + " jour" + (daysLeft > 1 ? "s" : "") + " restant" + (daysLeft > 1 ? "s" : "");
            daysColor = "#F59E0B";
        } else {
            daysText = daysLeft + " jour" + (daysLeft > 1 ? "s" : "") + " restant" + (daysLeft > 1 ? "s" : "");
            daysColor = "#10B981";
        }

        VBox closingValueBox = new VBox(3);
        Label closingDateValue = new Label(offer.getClosingDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        closingDateValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #F1F5F9; -fx-font-weight: 600;");

        Label daysLeftValue = new Label(daysText);
        daysLeftValue.setStyle("-fx-font-size: 13px; -fx-text-fill: " + daysColor + "; -fx-font-weight: 600;");

        closingValueBox.getChildren().addAll(closingDateValue, daysLeftValue);

        infoGrid.add(publishTitleBox, 0, 2);
        infoGrid.add(publishValue, 1, 2);
        infoGrid.add(closingTitleBox, 2, 2);
        infoGrid.add(closingValueBox, 3, 2);

        infoCard.getChildren().add(infoGrid);

        // ========== DESCRIPTION ==========
        VBox descBox = new VBox(15);
        descBox.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 25;" +
                        "-fx-border-color: rgba(99,102,241,0.2);" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        );

        HBox descHeader = new HBox(10);
        descHeader.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label descIcon = new Label("📝");
        descIcon.setStyle("-fx-font-size: 20px;");

        Label descTitle = new Label("Description du poste");
        descTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        descHeader.getChildren().addAll(descIcon, descTitle);

        TextArea descriptionArea = new TextArea(offer.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(6);
        descriptionArea.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 12;" +
                        "-fx-text-fill: #E2E8F0;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Segoe UI';" +
                        "-fx-padding: 15;"
        );

        descBox.getChildren().addAll(descHeader, descriptionArea);

        // ========== STATISTIQUES ==========
        HBox statsBox = new HBox(15);
        statsBox.setAlignment(javafx.geometry.Pos.CENTER);
        statsBox.setPadding(new Insets(10, 0, 0, 0));

        // Taux de remplissage
        double fillRate = offer.getPositionsAvailable() > 0 ?
                (double) offer.getApplicationsReceived() / offer.getPositionsAvailable() * 100 : 0;

        VBox fillRateBox = createStatBox(
                "Taux de remplissage",
                String.format("%.1f%%", fillRate),
                fillRate >= 100 ? "#EF4444" : (fillRate >= 50 ? "#10B981" : "#F59E0B")
        );

        // Ratio candidatures/postes
        double ratio = offer.getPositionsAvailable() > 0 ?
                (double) offer.getApplicationsReceived() / offer.getPositionsAvailable() : 0;

        VBox ratioBox = createStatBox(
                "Ratio candidatures/postes",
                String.format("%.1f", ratio),
                ratio >= 5 ? "#EF4444" : (ratio >= 2 ? "#F59E0B" : "#10B981")
        );

        // Compétitivité
        String competitivite;
        String compColor;
        if (offer.getApplicationsReceived() == 0) {
            competitivite = "Faible";
            compColor = "#94A3B8";
        } else if (offer.getApplicationsReceived() < 5) {
            competitivite = "Modérée";
            compColor = "#F59E0B";
        } else if (offer.getApplicationsReceived() < 15) {
            competitivite = "Élevée";
            compColor = "#10B981";
        } else {
            competitivite = "Très élevée";
            compColor = "#EF4444";
        }

        VBox compBox = createStatBox(
                "Compétitivité",
                competitivite,
                compColor
        );

        statsBox.getChildren().addAll(fillRateBox, ratioBox, compBox);
        HBox.setHgrow(fillRateBox, Priority.ALWAYS);
        HBox.setHgrow(ratioBox, Priority.ALWAYS);
        HBox.setHgrow(compBox, Priority.ALWAYS);

        // ========== INFORMATIONS COMPLÉMENTAIRES ==========
        HBox footerBox = new HBox(15);
        footerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        footerBox.setPadding(new Insets(15, 0, 0, 0));

        Label createdLabel = new Label("📅 Créée le: " + (offer.getPublishDate() != null ?
                offer.getPublishDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A"));
        createdLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Label idLabel = new Label("🆔 ID: " + offer.getId());
        idLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        footerBox.getChildren().addAll(createdLabel, spacer2, idLabel);

        // ========== ASSEMBLAGE ==========
        container.getChildren().addAll(
                headerBox,
                bannerBox,
                infoCard,
                descBox,
                statsBox,
                footerBox
        );

        return container;
    }

    /**
     * Crée une boîte de statistique (version corrigée)
     */
    private VBox createStatBox(String title, String value, String color) {
        VBox box = new VBox(5);
        box.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-padding: 15;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: rgba(99,102,241,0.2);" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;" +
                        "-fx-alignment: center;"
        );
        box.setPrefWidth(200);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
        titleLabel.setAlignment(javafx.geometry.Pos.CENTER);
        titleLabel.setMaxWidth(Double.MAX_VALUE);

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
        valueLabel.setAlignment(javafx.geometry.Pos.CENTER);
        valueLabel.setMaxWidth(Double.MAX_VALUE);

        box.getChildren().addAll(titleLabel, valueLabel);

        return box;
    }
    /**
     * Ajoute une ligne d'information dans la grille
     */
    private void addDetailRow(GridPane grid, String label, String value, int col, int row) {
        Label labelField = createDetailLabel(label);
        Label valueField = new Label(value);
        valueField.setStyle("-fx-font-size: 15px; -fx-text-fill: #F1F5F9; -fx-font-weight: 600;");

        grid.add(labelField, col, row);
        grid.add(valueField, col + 1, row);
    }

    /**
     * Crée un label pour les détails
     */
    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8; -fx-font-weight: 600;");
        return label;
    }
    private VBox createInfoCard(String title, String[][] infoRows) {
        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: rgba(99,102,241,0.15);" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        );

        // Titre de la carte
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        // Grille d'informations
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(12);

        for (int i = 0; i < infoRows.length; i++) {
            String[] row = infoRows[i];

            // Label (colonne 0)
            Label keyLabel = new Label(row[0]);
            keyLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8; -fx-min-width: 150;");

            // Valeur (colonne 1)
            Label valueLabel = new Label(row[1]);
            valueLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #F1F5F9; -fx-wrap-text: true;");

            infoGrid.add(keyLabel, 0, i);
            infoGrid.add(valueLabel, 1, i);
        }

        card.getChildren().addAll(titleLabel, infoGrid);

        return card;
    }

    /**
     * Crée une boîte de statistique
     */

    /**
     * Crée une boîte de statistique
     */

    /**
     * Affiche le formulaire de modification d'offre
     */
    private void showEditForm(Offer offer) {
        // Vider le conteneur des cartes
        cardsContainer.getChildren().clear();

        // Créer et afficher le formulaire de modification
        VBox editForm = createEditForm(offer);
        cardsContainer.getChildren().add(editForm);
    }

    /**
     * Crée le formulaire de modification d'offre avec validation
     */
    private VBox createEditForm(Offer offer) {
        VBox formContainer = new VBox(20);
        formContainer.setStyle("-fx-background-color: #1E293B; -fx-padding: 30; -fx-background-radius: 16; -fx-max-width: 800;");
        formContainer.setMaxWidth(800);
        formContainer.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // En-tête avec bouton retour
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Button backBtn = new Button("← Retour aux offres");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94A3B8; -fx-font-size: 14px; -fx-cursor: hand; -fx-padding: 5 10;");
        backBtn.setOnAction(e -> backToOffers());

        Label titleLabel = new Label("✏️ Modifier l'offre");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Label offerTitle = new Label(" • " + offer.getTitle());
        offerTitle.setStyle("-fx-font-size: 18px; -fx-text-fill: #94A3B8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(backBtn, spacer, titleLabel, offerTitle);

        // Formulaire
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(javafx.geometry.Pos.CENTER);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        formGrid.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        // ========== TITRE ==========
        Label titleFieldLabel = new Label("Titre *");
        titleFieldLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField titleField = new TextField(offer.getTitle());
        titleField.setPromptText("Ex: Développeur Java Senior");
        titleField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B;");

        Label titleError = new Label();
        titleError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox titleBox = new VBox(3);
        titleBox.getChildren().addAll(titleField, titleError);
        formGrid.add(titleFieldLabel, 0, row);
        formGrid.add(titleBox, 1, row++);

        // ========== DESCRIPTION ==========
        Label descLabel = new Label("Description");
        descLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextArea descriptionArea = new TextArea(offer.getDescription());
        descriptionArea.setPromptText("Décrivez les missions, responsabilités, etc.");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B;");

        formGrid.add(descLabel, 0, row);
        formGrid.add(descriptionArea, 1, row++);

        // ========== DÉPARTEMENT ==========
        Label deptLabel = new Label("Département *");
        deptLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> departmentCombo = new ComboBox<>();
        departmentCombo.getItems().addAll("IT", "RH", "Finance", "Marketing", "Production", "Logistique", "Commerce");
        departmentCombo.setValue(offer.getDepartment());
        departmentCombo.setPromptText("Sélectionnez un département");
        departmentCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        Label deptError = new Label();
        deptError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox deptBox = new VBox(3);
        deptBox.getChildren().addAll(departmentCombo, deptError);
        formGrid.add(deptLabel, 0, row);
        formGrid.add(deptBox, 1, row++);

        // ========== TYPE DE CONTRAT ==========
        Label contractLabel = new Label("Type de contrat *");
        contractLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> contractCombo = new ComboBox<>();
        contractCombo.getItems().addAll("CDI", "CDD", "Stage", "Alternance", "Freelance");
        contractCombo.setValue(offer.getContractType());
        contractCombo.setPromptText("Sélectionnez un type");
        contractCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        Label contractError = new Label();
        contractError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox contractBox = new VBox(3);
        contractBox.getChildren().addAll(contractCombo, contractError);
        formGrid.add(contractLabel, 0, row);
        formGrid.add(contractBox, 1, row++);

        // ========== NIVEAU D'EXPÉRIENCE ==========
        Label expLabel = new Label("Expérience *");
        expLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> expCombo = new ComboBox<>();
        expCombo.getItems().addAll("Débutant", "Junior", "Intermédiaire", "Senior", "Expert");
        expCombo.setValue(offer.getExperienceLevel());
        expCombo.setPromptText("Sélectionnez un niveau");
        expCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        Label expError = new Label();
        expError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox expBox = new VBox(3);
        expBox.getChildren().addAll(expCombo, expError);
        formGrid.add(expLabel, 0, row);
        formGrid.add(expBox, 1, row++);

        // ========== SALAIRE MIN ==========
        Label salaryMinLabel = new Label("Salaire min *");
        salaryMinLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField salaryMinField = new TextField(String.valueOf(offer.getSalaryMin()));
        salaryMinField.setPromptText("Ex: 45000");
        salaryMinField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B;");

        // Validation pour n'accepter que les nombres
        salaryMinField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                salaryMinField.setText(newVal.replaceAll("[^\\d.]", ""));
            }
        });

        Label salaryMinError = new Label();
        salaryMinError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox salaryMinBox = new VBox(3);
        salaryMinBox.getChildren().addAll(salaryMinField, salaryMinError);
        formGrid.add(salaryMinLabel, 0, row);
        formGrid.add(salaryMinBox, 1, row);

        // ========== SALAIRE MAX ==========
        Label salaryMaxLabel = new Label("Salaire max *");
        salaryMaxLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        TextField salaryMaxField = new TextField(String.valueOf(offer.getSalaryMax()));
        salaryMaxField.setPromptText("Ex: 65000");
        salaryMaxField.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 10; -fx-text-fill: #E2E8F0; -fx-prompt-text-fill: #64748B;");

        // Validation pour n'accepter que les nombres
        salaryMaxField.textProperty().addListener((obs, old, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                salaryMaxField.setText(newVal.replaceAll("[^\\d.]", ""));
            }
        });

        Label salaryMaxError = new Label();
        salaryMaxError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox salaryMaxBox = new VBox(3);
        salaryMaxBox.getChildren().addAll(salaryMaxField, salaryMaxError);
        formGrid.add(salaryMaxLabel, 2, row);
        formGrid.add(salaryMaxBox, 3, row++);

        // ========== LOCALISATION ==========
        Label locationLabel = new Label("Localisation *");
        locationLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> locationCombo = new ComboBox<>();
        locationCombo.getItems().addAll("Tunis", "Sfax", "Sousse", "Gabès", "Bizerte", "Ariana", "Ben Arous", "Nabeul");
        locationCombo.setValue(offer.getLocation());
        locationCombo.setPromptText("Sélectionnez une ville");
        locationCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");
        locationCombo.setEditable(true);

        Label locationError = new Label();
        locationError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox locationBox = new VBox(3);
        locationBox.getChildren().addAll(locationCombo, locationError);
        formGrid.add(locationLabel, 0, row);
        formGrid.add(locationBox, 1, row++);

        // ========== STATUT ==========
        Label statusLabel = new Label("Statut");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Ouverte", "Fermée", "En attente", "Pourvue");
        statusCombo.setValue(offer.getStatus());
        statusCombo.setPromptText("Sélectionnez un statut");
        statusCombo.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        formGrid.add(statusLabel, 0, row);
        formGrid.add(statusCombo, 1, row++);

        // ========== DATE PUBLICATION ==========
        Label publishLabel = new Label("Date publication");
        publishLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        DatePicker publishDatePicker = new DatePicker(offer.getPublishDate());
        publishDatePicker.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        formGrid.add(publishLabel, 0, row);
        formGrid.add(publishDatePicker, 1, row++);

        // ========== DATE CLÔTURE ==========
        Label closingLabel = new Label("Date clôture *");
        closingLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        DatePicker closingDatePicker = new DatePicker(offer.getClosingDate());
        closingDatePicker.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-padding: 5; -fx-text-fill: #E2E8F0;");

        Label closingError = new Label();
        closingError.setStyle("-fx-text-fill: #F87171; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");

        VBox closingBox = new VBox(3);
        closingBox.getChildren().addAll(closingDatePicker, closingError);
        formGrid.add(closingLabel, 0, row);
        formGrid.add(closingBox, 1, row++);

        // ========== POSTES DISPONIBLES ==========
        Label positionsLabel = new Label("Postes disponibles");
        positionsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        Spinner<Integer> positionsSpinner = new Spinner<>(1, 50, offer.getPositionsAvailable());
        positionsSpinner.setEditable(true);
        positionsSpinner.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-border-color: rgba(255,255,255,0.08); -fx-border-radius: 10; -fx-background-radius: 10; -fx-text-fill: #E2E8F0;");

        formGrid.add(positionsLabel, 0, row);
        formGrid.add(positionsSpinner, 1, row++);

        // Bouton de sauvegarde (désactivé par défaut)
        Button saveBtn = new Button("Mettre à jour");
        saveBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: #94A3B8; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        saveBtn.setDisable(true);

        // ========== VALIDATION EN TEMPS RÉEL ==========
        Runnable validateForm = () -> {
            boolean isValid = true;

            // Validation titre
            String title = titleField.getText();
            if (title.isEmpty()) {
                titleError.setText("❌ Le titre est obligatoire");
                titleField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else if (title.length() < 3) {
                titleError.setText("❌ Minimum 3 caractères");
                titleField.setStyle("-fx-border-color: #F59E0B; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                titleError.setText("✅ Valide");
                titleError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                titleField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation département
            if (departmentCombo.getValue() == null) {
                deptError.setText("❌ Sélectionnez un département");
                departmentCombo.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                deptError.setText("✅ Valide");
                deptError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                departmentCombo.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation type contrat
            if (contractCombo.getValue() == null) {
                contractError.setText("❌ Sélectionnez un type de contrat");
                contractCombo.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                contractError.setText("✅ Valide");
                contractError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                contractCombo.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation expérience
            if (expCombo.getValue() == null) {
                expError.setText("❌ Sélectionnez un niveau");
                expCombo.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                expError.setText("✅ Valide");
                expError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                expCombo.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation salaire min
            String salMin = salaryMinField.getText();
            if (salMin.isEmpty()) {
                salaryMinError.setText("❌ Salaire minimum obligatoire");
                salaryMinField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                try {
                    double min = Double.parseDouble(salMin);
                    if (min <= 0) {
                        salaryMinError.setText("❌ Doit être > 0");
                        salaryMinField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                        isValid = false;
                    } else {
                        salaryMinError.setText("✅ Valide");
                        salaryMinError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                        salaryMinField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                    }
                } catch (NumberFormatException e) {
                    salaryMinError.setText("❌ Format invalide");
                    salaryMinField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                    isValid = false;
                }
            }

            // Validation salaire max
            String salMax = salaryMaxField.getText();
            if (salMax.isEmpty()) {
                salaryMaxError.setText("❌ Salaire maximum obligatoire");
                salaryMaxField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                try {
                    double max = Double.parseDouble(salMax);
                    if (max <= 0) {
                        salaryMaxError.setText("❌ Doit être > 0");
                        salaryMaxField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                        isValid = false;
                    } else {
                        salaryMaxError.setText("✅ Valide");
                        salaryMaxError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                        salaryMaxField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                    }
                } catch (NumberFormatException e) {
                    salaryMaxError.setText("❌ Format invalide");
                    salaryMaxField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                    isValid = false;
                }
            }

            // Validation croisée des salaires
            if (!salMin.isEmpty() && !salMax.isEmpty()) {
                try {
                    double min = Double.parseDouble(salMin);
                    double max = Double.parseDouble(salMax);
                    if (max < min) {
                        salaryMinError.setText("❌ Min > Max");
                        salaryMaxError.setText("❌ Max < Min");
                        salaryMinField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                        salaryMaxField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    // Déjà géré
                }
            }

            // Validation localisation
            if (locationCombo.getValue() == null || locationCombo.getValue().trim().isEmpty()) {
                locationError.setText("❌ Localisation obligatoire");
                locationCombo.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                locationError.setText("✅ Valide");
                locationError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                locationCombo.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Validation date clôture
            LocalDate closingDate = closingDatePicker.getValue();
            if (closingDate == null) {
                closingError.setText("❌ Date de clôture obligatoire");
                closingDatePicker.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else if (closingDate.isBefore(LocalDate.now())) {
                closingError.setText("❌ Ne peut pas être dans le passé");
                closingDatePicker.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
                isValid = false;
            } else {
                closingError.setText("✅ Valide");
                closingError.setStyle("-fx-text-fill: #10B981; -fx-font-size: 11px; -fx-padding: 2 0 0 5;");
                closingDatePicker.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: rgba(255,255,255,0.05);");
            }

            // Activer/désactiver le bouton
            if (isValid) {
                saveBtn.setDisable(false);
                saveBtn.setStyle("-fx-background-color: linear-gradient(to right, #6366F1, #06B6D4); -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            } else {
                saveBtn.setDisable(true);
                saveBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: #94A3B8; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
            }
        };

        // Ajouter les écouteurs de validation
        titleField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        departmentCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        contractCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        expCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        salaryMinField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        salaryMaxField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        locationCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        closingDatePicker.valueProperty().addListener((obs, old, newVal) -> validateForm.run());

        // Validation initiale
        validateForm.run();

        // ========== BOUTON ANNULER ==========
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle("-fx-background-color: #475569; -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;");
        cancelBtn.setOnAction(e -> backToOffers());

        // Action du bouton sauvegarder
        saveBtn.setOnAction(e -> {
            try {
                // Mettre à jour l'offre existante
                offer.setTitle(titleField.getText());
                offer.setDescription(descriptionArea.getText());
                offer.setDepartment(departmentCombo.getValue());
                offer.setContractType(contractCombo.getValue());
                offer.setExperienceLevel(expCombo.getValue());
                offer.setSalaryMin(Double.parseDouble(salaryMinField.getText()));
                offer.setSalaryMax(Double.parseDouble(salaryMaxField.getText()));
                offer.setLocation(locationCombo.getValue());
                offer.setStatus(statusCombo.getValue());
                offer.setPublishDate(publishDatePicker.getValue());
                offer.setClosingDate(closingDatePicker.getValue());
                offer.setPositionsAvailable(positionsSpinner.getValue());

                if (offerService.updateOffer(offer)) {
                    showAlert("Succès", "Offre modifiée avec succès!", Alert.AlertType.INFORMATION);
                    backToOffers();
                    loadOffers();
                } else {
                    showAlert("Erreur", "Erreur lors de la modification", Alert.AlertType.ERROR);
                }
            } catch (NumberFormatException ex) {
                showAlert("Erreur", "Vérifiez les champs numériques", Alert.AlertType.ERROR);
            }
        });

        // Boutons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        buttonBox.getChildren().addAll(cancelBtn, saveBtn);

        // Message champs obligatoires
        Label requiredLabel = new Label("* Champs obligatoires");
        requiredLabel.setStyle("-fx-text-fill: #64748B; -fx-font-size: 12px;");

        // Assemblage final
        formContainer.getChildren().addAll(headerBox, formGrid, buttonBox, requiredLabel);

        return formContainer;
    }
    // ✅ AJOUT DE LA MÉTHODE deleteOffer
    private void deleteOffer(Offer offer) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation de suppression");
        confirm.setHeaderText("Supprimer l'offre");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer l'offre : " + offer.getTitle() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (offerService.deleteOffer(offer.getId())) {
                    offersList.remove(offer);
                    loadOffers();
                    showAlert("Succès", "Offre supprimée avec succès", Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Erreur", "Erreur lors de la suppression", Alert.AlertType.ERROR);
                }
            }
        });
    }

    // ✅ AJOUT DE LA MÉTHODE showAlert
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
