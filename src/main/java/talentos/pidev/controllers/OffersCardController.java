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

    @FXML private TextField searchField;
    @FXML private ComboBox<String> departmentFilter;
    @FXML private ComboBox<String> statusFilter;
    @FXML private FlowPane cardsContainer;
    @FXML private Label totalOffersLabel;
    @FXML private ComboBox<String> sortComboBox;

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
                "Tous", "IT", "RH", "Finance", "Marketing", "Production", "Logistique", "Commerce"
        );
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
                "Salaire (↓)"
        );
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
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-padding: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        card.setPrefWidth(320);
        card.setPrefHeight(280);

        // En-tête
        HBox header = new HBox(10);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(200);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label statusLabel = new Label(offer.getStatus());
        statusLabel.setStyle(getStatusStyle(offer.getStatus()));

        header.getChildren().addAll(titleLabel, statusLabel);

        // Département
        Label deptLabel = new Label("🏢 " + offer.getDepartment() + " • " + offer.getContractType());
        deptLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563;");

        // Localisation
        Label locationLabel = new Label("📍 " + offer.getLocation());
        locationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563;");

        // Salaire
        Label salaryLabel = new Label(String.format("💰 %.0f - %.0f DT",
                offer.getSalaryMin(), offer.getSalaryMax()));
        salaryLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563;");

        // Expérience
        Label expLabel = new Label("📊 " + offer.getExperienceLevel());
        expLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #4B5563;");

        // Footer
        HBox footer = new HBox(16);
        footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 0, 0, 0));

        Label dateLabel = new Label("📅 " + offer.getClosingDate().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Label appLabel = new Label("👥 " + offer.getApplicationsReceived() + " candidatures");
        appLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        footer.getChildren().addAll(dateLabel, appLabel);

        // Boutons
        HBox actions = new HBox(8);
        actions.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(12, 0, 0, 0));

        Button viewBtn = new Button("Voir");
        viewBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
        viewBtn.setOnAction(e -> showOfferDetails(offer));

        Button editBtn = new Button("Modifier");
        editBtn.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
        editBtn.setOnAction(e -> editOffer(offer));

        Button deleteBtn = new Button("Supprimer");
        deleteBtn.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-padding: 6 12; -fx-background-radius: 6;");
        deleteBtn.setOnAction(e -> deleteOffer(offer));

        actions.getChildren().addAll(viewBtn, editBtn, deleteBtn);

        card.getChildren().addAll(header, deptLabel, locationLabel, salaryLabel, expLabel, footer, actions);

        return card;
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "Ouverte":
                return "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "Fermée":
                return "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
        }
    }

    private void showNoResultsMessage() {
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(javafx.geometry.Pos.CENTER);
        messageBox.setPrefWidth(600);
        messageBox.setPrefHeight(300);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Aucune offre trouvée");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");

        Label subtitle = new Label("Cliquez sur 'Nouvelle offre' pour créer votre première offre");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #9CA3AF;");

        Button newOfferBtn = new Button("+ Créer une offre");
        newOfferBtn.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 6; -fx-font-size: 14px;");
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
    @FXML
    private void addNewOffer() {
        Dialog<Offer> dialog = new Dialog<>();
        dialog.setTitle("Nouvelle offre d'emploi");
        dialog.setHeaderText(null);

        // Style du dialogue
        dialog.getDialogPane().setPrefWidth(500);  // ← Change cette valeur
        dialog.getDialogPane().setPrefHeight(600);

        ButtonType saveButtonType = new ButtonType("Créer l'offre", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Conteneur principal
        VBox mainContainer = new VBox(20);
        mainContainer.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 12;");

        // En-tête
        VBox headerBox = new VBox(5);
        Label titleLabel = new Label("➕ Nouvelle offre d'emploi");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label subtitleLabel = new Label("Remplissez les informations ci-dessous pour créer une nouvelle offre");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");

        headerBox.getChildren().addAll(titleLabel, subtitleLabel);

        // ========== FORMULAIRE AVEC VALIDATION SOUS CHAQUE CHAMP ==========
        VBox formBox = new VBox(15);

        // Titre
        VBox titleFieldBox = createValidatedField(
                "Titre du poste *",
                "Ex: Développeur Java Senior",
                value -> {
                    if (value.isEmpty()) return "Le titre est obligatoire";
                    if (value.length() < 3) return "Minimum 3 caractères";
                    if (value.length() > 100) return "Maximum 100 caractères";
                    return null;
                }
        );

        // Description
        VBox descFieldBox = new VBox(5);
        Label descLabel = new Label("Description du poste");
        descLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("Décrivez les missions, responsabilités, etc.");
        descriptionArea.setPrefRowCount(4);
        descriptionArea.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; " +
                "-fx-font-size: 14px;");

        Label descErrorLabel = new Label();
        descErrorLabel.setStyle("-fx-text-fill: #EF4444; -fx-font-size: 12px;");

        descFieldBox.getChildren().addAll(descLabel, descriptionArea, descErrorLabel);

        // Département
        VBox deptFieldBox = createValidatedCombo(
                "Département *",
                value -> value == null ? "Sélectionnez un département" : null,
                "IT", "RH", "Finance", "Marketing", "Production", "Logistique", "Commerce"
        );

        // Type de contrat
        VBox contractFieldBox = createValidatedCombo(
                "Type de contrat *",
                value -> value == null ? "Sélectionnez un type de contrat" : null,
                "CDI", "CDD", "Stage", "Alternance", "Freelance"
        );

        // Niveau d'expérience
        VBox expFieldBox = createValidatedCombo(
                "Niveau d'expérience *",
                value -> value == null ? "Sélectionnez un niveau" : null,
                "Débutant", "Junior", "Intermédiaire", "Senior", "Expert"
        );

        // Salaires (grille 2 colonnes)
        GridPane salaryGrid = new GridPane();
        salaryGrid.setHgap(15);
        salaryGrid.setVgap(10);

        // Salaire min
        VBox salaryMinBox = createValidatedField(
                "Salaire minimum (DT) *",
                "Ex: 45000",
                value -> {
                    if (value.isEmpty()) return "Salaire minimum obligatoire";
                    try {
                        double salaire = Double.parseDouble(value);
                        if (salaire <= 0) return "Doit être > 0";
                        if (salaire > 1000000) return "Trop élevé";
                        return null;
                    } catch (NumberFormatException e) {
                        return "Format invalide (chiffres uniquement)";
                    }
                }
        );

        // Salaire max
        VBox salaryMaxBox = createValidatedField(
                "Salaire maximum (DT) *",
                "Ex: 65000",
                value -> {
                    if (value.isEmpty()) return "Salaire maximum obligatoire";
                    try {
                        double salaire = Double.parseDouble(value);
                        if (salaire <= 0) return "Doit être > 0";
                        if (salaire > 1000000) return "Trop élevé";
                        return null;
                    } catch (NumberFormatException e) {
                        return "Format invalide (chiffres uniquement)";
                    }
                }
        );

        salaryGrid.add(salaryMinBox, 0, 0);
        salaryGrid.add(salaryMaxBox, 1, 0);

        // Localisation
        VBox locationFieldBox = createValidatedCombo(
                "Localisation *",
                value -> value == null || value.trim().isEmpty() ? "Sélectionnez une ville" : null,
                "Tunis", "Sfax", "Sousse", "Gabès", "Bizerte", "Ariana", "Ben Arous", "Nabeul"
        );

        // Dates (grille 2 colonnes)
        GridPane dateGrid = new GridPane();
        dateGrid.setHgap(15);
        dateGrid.setVgap(10);

        // Date publication
        VBox publishDateBox = createValidatedDateField(
                "Date de publication",
                LocalDate.now(),
                date -> null // Toujours valide
        );

        // Date clôture
        VBox closingDateBox = createValidatedDateField(
                "Date de clôture *",
                LocalDate.now().plusMonths(1),
                date -> {
                    if (date == null) return "Date de clôture obligatoire";
                    if (date.isBefore(LocalDate.now())) return "Ne peut pas être dans le passé";
                    return null;
                }
        );

        dateGrid.add(publishDateBox, 0, 0);
        dateGrid.add(closingDateBox, 1, 0);

        // Postes disponibles
        VBox positionsBox = new VBox(5);
        Label positionsLabel = new Label("Postes disponibles");
        positionsLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        Spinner<Integer> positionsSpinner = new Spinner<>(1, 50, 1);
        positionsSpinner.setEditable(true);
        positionsSpinner.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        positionsBox.getChildren().addAll(positionsLabel, positionsSpinner);

        // Assemblage du formulaire
        formBox.getChildren().addAll(
                titleFieldBox,
                descFieldBox,
                deptFieldBox,
                contractFieldBox,
                expFieldBox,
                salaryGrid,
                locationFieldBox,
                dateGrid,
                positionsBox
        );

        mainContainer.getChildren().addAll(headerBox, new Separator(), formBox);

        // ScrollPane si nécessaire
        ScrollPane scrollPane = new ScrollPane(mainContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: white; -fx-border-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);

        // Récupérer le bouton "Créer"
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setStyle("-fx-background-color: #10B981; -fx-text-fill: white; -fx-padding: 10 20; " +
                "-fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: bold;");
        saveButton.setDisable(true);

        // Validation croisée des salaires
        TextField salaryMinField = (TextField) ((VBox) salaryGrid.getChildren().get(0)).getChildren().get(1);
        TextField salaryMaxField = (TextField) ((VBox) salaryGrid.getChildren().get(1)).getChildren().get(1);
        Label salaryMinError = (Label) ((VBox) salaryGrid.getChildren().get(0)).getChildren().get(2);
        Label salaryMaxError = (Label) ((VBox) salaryGrid.getChildren().get(1)).getChildren().get(2);

        // Validation en temps réel
        Runnable validateAll = () -> {
            boolean allValid = true;

            // Valider chaque champ
            allValid &= validateField((VBox) titleFieldBox);
            allValid &= validateField((VBox) deptFieldBox);
            allValid &= validateField((VBox) contractFieldBox);
            allValid &= validateField((VBox) expFieldBox);
            allValid &= validateField((VBox) locationFieldBox);
            allValid &= validateField((VBox) closingDateBox);

            // Validation spéciale pour les salaires
            boolean salaryMinValid = validateField((VBox) salaryMinBox);
            boolean salaryMaxValid = validateField((VBox) salaryMaxBox);
            allValid &= salaryMinValid && salaryMaxValid;

            // Validation croisée des salaires
            if (salaryMinValid && salaryMaxValid) {
                try {
                    double min = Double.parseDouble(salaryMinField.getText());
                    double max = Double.parseDouble(salaryMaxField.getText());

                    if (max < min) {
                        salaryMinError.setText("❌ Le salaire min ne peut pas être > au max");
                        salaryMaxError.setText("❌ Le salaire max doit être ≥ au min");
                        salaryMinField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: #FEF2F2;");
                        salaryMaxField.setStyle("-fx-border-color: #EF4444; -fx-border-width: 2; -fx-background-color: #FEF2F2;");
                        allValid = false;
                    } else {
                        if (salaryMinError.getText().startsWith("✅")) {
                            salaryMinError.setText("✅ Valide");
                            salaryMinField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: #F0FDF4;");
                        }
                        if (salaryMaxError.getText().startsWith("✅")) {
                            salaryMaxError.setText("✅ Valide");
                            salaryMaxField.setStyle("-fx-border-color: #10B981; -fx-border-width: 2; -fx-background-color: #F0FDF4;");
                        }
                    }
                } catch (NumberFormatException e) {
                    // Déjà géré
                }
            }

            saveButton.setDisable(!allValid);
        };

        // Ajouter les écouteurs
        addValidationListener((VBox) titleFieldBox, validateAll);
        addValidationListener((VBox) deptFieldBox, validateAll);
        addValidationListener((VBox) contractFieldBox, validateAll);
        addValidationListener((VBox) expFieldBox, validateAll);
        addValidationListener((VBox) salaryMinBox, validateAll);
        addValidationListener((VBox) salaryMaxBox, validateAll);
        addValidationListener((VBox) locationFieldBox, validateAll);
        addValidationListener((VBox) closingDateBox, validateAll);

        // Résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    Offer newOffer = new Offer();
                    newOffer.setTitle(((TextField) ((VBox) titleFieldBox).getChildren().get(1)).getText());
                    newOffer.setDescription(descriptionArea.getText());
                    newOffer.setDepartment((String) ((ComboBox) ((VBox) deptFieldBox).getChildren().get(1)).getValue());
                    newOffer.setContractType((String) ((ComboBox) ((VBox) contractFieldBox).getChildren().get(1)).getValue());
                    newOffer.setExperienceLevel((String) ((ComboBox) ((VBox) expFieldBox).getChildren().get(1)).getValue());
                    newOffer.setSalaryMin(Double.parseDouble(salaryMinField.getText()));
                    newOffer.setSalaryMax(Double.parseDouble(salaryMaxField.getText()));
                    newOffer.setLocation((String) ((ComboBox) ((VBox) locationFieldBox).getChildren().get(1)).getValue());
                    newOffer.setStatus("Ouverte");
                    newOffer.setPublishDate(((DatePicker) ((VBox) publishDateBox).getChildren().get(1)).getValue());
                    newOffer.setClosingDate(((DatePicker) ((VBox) closingDateBox).getChildren().get(1)).getValue());
                    newOffer.setPositionsAvailable(positionsSpinner.getValue());
                    newOffer.setApplicationsReceived(0);
                    return newOffer;
                } catch (Exception e) {
                    showAlert("Erreur", "Erreur de saisie: " + e.getMessage(), Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        dialog.showAndWait().ifPresent(offer -> {
            if (offerService.createOffer(offer)) {
                loadOffers();
                showAlert("Succès", "Offre créée avec succès!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Erreur lors de la création", Alert.AlertType.ERROR);
            }
        });
    }

// ========== MÉTHODES UTILITAIRES ==========

    /**
     * Crée un champ de texte avec validation
     */
    private VBox createValidatedField(String label, String placeholder,
                                      java.util.function.Function<String, String> validator) {
        VBox box = new VBox(5);

        Label labelField = new Label(label);
        labelField.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        TextField textField = new TextField();
        textField.setPromptText(placeholder);
        textField.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10; " +
                "-fx-font-size: 14px;");

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
        labelField.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(items);
        comboBox.setPromptText("Sélectionner...");
        comboBox.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 5; " +
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
        labelField.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #374151;");

        DatePicker datePicker = new DatePicker(defaultValue);
        datePicker.setStyle("-fx-background-color: #F9FAFB; -fx-border-color: #E5E7EB; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 5;");

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
        if (fieldBox.getChildren().size() < 3) return true;

        Label errorLabel = (Label) fieldBox.getChildren().get(2);
        return errorLabel.getText().startsWith("✅");
    }

    /**
     * Ajoute un écouteur de validation
     */
    private void addValidationListener(VBox fieldBox, Runnable validator) {
        if (fieldBox.getChildren().size() < 2) return;

        Control control = (Control) fieldBox.getChildren().get(1);

        if (control instanceof TextField) {
            ((TextField) control).textProperty().addListener((obs, old, newVal) -> validator.run());
        } else if (control instanceof ComboBox) {
            ((ComboBox<?>) control).valueProperty().addListener((obs, old, newVal) -> validator.run());
        } else if (control instanceof DatePicker) {
            ((DatePicker) control).valueProperty().addListener((obs, old, newVal) -> validator.run());
        }
    }
    // ✅ AJOUT DE LA MÉTHODE showOfferDetails
   private void showOfferDetails(Offer offer) {
        // Créer un dialogue personnalisé
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'offre");
        dialog.setHeaderText(null);

        // Style du dialogue
        dialog.getDialogPane().setPrefWidth(600);
        dialog.getDialogPane().setPrefHeight(200);
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/style/app.css").toExternalForm()
        );

        // Bouton de fermeture
        ButtonType closeButtonType = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().add(closeButtonType);

        // Conteneur principal
        BorderPane mainContainer = new BorderPane();
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 12;");

        // ========== EN-TÊTE AVEC BANNIÈRE ==========
        VBox headerBox = new VBox();
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #2563EB, #1E40AF);" +
                "-fx-background-radius: 12 12 0 0; -fx-padding: 25;");

        // Titre et statut
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");
        titleLabel.setWrapText(true);

        Label statusLabel = new Label(offer.getStatus());
        statusLabel.setStyle(getModernStatusStyle(offer.getStatus()));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleRow.getChildren().addAll(titleLabel, spacer, statusLabel);

        // Sous-titre avec département
        Label subtitleLabel = new Label(offer.getDepartment() + " • " + offer.getContractType());
        subtitleLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: rgba(255,255,255,0.9);");

        headerBox.getChildren().addAll(titleRow, subtitleLabel);

        // ========== CONTENU PRINCIPAL ==========
        VBox contentBox = new VBox(20);
        contentBox.setStyle("-fx-padding: 25; -fx-background-color: white;");

        // Grille d'informations avec icônes
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(20);
        infoGrid.setVgap(15);
        infoGrid.setPadding(new Insets(0, 0, 20, 0));

        // Ligne 1: Localisation
        addInfoRow(infoGrid, "📍 Localisation", offer.getLocation(), 0, "#3B82F6");

        // Ligne 2: Salaire
        String salaireText = String.format("%.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax());
        addInfoRow(infoGrid, "💰 Salaire", salaireText, 1, "#10B981");

        // Ligne 3: Expérience
        addInfoRow(infoGrid, "📊 Niveau d'expérience", offer.getExperienceLevel(), 2, "#8B5CF6");

        // Ligne 4: Postes disponibles
        String postesText = offer.getPositionsAvailable() + " poste" + (offer.getPositionsAvailable() > 1 ? "s" : "");
        addInfoRow(infoGrid, "👥 Postes disponibles", postesText, 3, "#F59E0B");

        // Ligne 5: Candidatures reçues
        String candidaturesText = offer.getApplicationsReceived() + " candidature" + (offer.getApplicationsReceived() > 1 ? "s" : "");
        addInfoRow(infoGrid, "📋 Candidatures reçues", candidaturesText, 4, "#EC4899");

        // ========== DATES ==========
        HBox datesBox = new HBox(20);
        datesBox.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 15; -fx-background-radius: 12;");

        VBox publishBox = createDateBox(
                "📅 Date de publication",
                offer.getPublishDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                "#3B82F6"
        );

        VBox closingBox = createDateBox(
                "⏰ Date de clôture",
                offer.getClosingDate().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                offer.getClosingDate().isBefore(LocalDate.now()) ? "#EF4444" : "#10B981"
        );

        // Ajouter un indicateur de jours restants
        if (!offer.getClosingDate().isBefore(LocalDate.now())) {
            long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), offer.getClosingDate());
            Label daysLeftLabel = new Label(daysLeft + " jour" + (daysLeft > 1 ? "s" : "") + " restant" + (daysLeft > 1 ? "s" : ""));
            daysLeftLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280; -fx-padding: 5 0 0 0;");
            closingBox.getChildren().add(daysLeftLabel);
        }

        datesBox.getChildren().addAll(publishBox, closingBox);
        HBox.setHgrow(publishBox, Priority.ALWAYS);
        HBox.setHgrow(closingBox, Priority.ALWAYS);

        // ========== DESCRIPTION ==========
        VBox descriptionBox = new VBox(10);
        descriptionBox.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 20; -fx-background-radius: 12;");

        Label descTitle = new Label("📝 Description du poste");
        descTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        TextArea descriptionArea = new TextArea(offer.getDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(8);
        descriptionArea.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; " +
                "-fx-border-radius: 8; -fx-background-radius: 8; -fx-font-size: 14px;");

        descriptionBox.getChildren().addAll(descTitle, descriptionArea);

        // ========== STATISTIQUES ==========
       HBox statsBox = new HBox(15);
        statsBox.setStyle("-fx-padding: 10 0 0 0;");

        // Taux de remplissage
        double fillRate = offer.getPositionsAvailable() > 0 ?
                (double) offer.getApplicationsReceived() / offer.getPositionsAvailable() * 100 : 0;

        VBox rateBox = createStatBox(
                "📊 Taux de remplissage",
                String.format("%.1f%%", fillRate),
                fillRate > 100 ? "#EF4444" : (fillRate > 50 ? "#10B981" : "#F59E0B")
        );

        // Ratio candidatures/postes
        VBox ratioBox = createStatBox(
                "📈 Ratio candidatures/postes",
                String.format("%.1f", (double) offer.getApplicationsReceived() / offer.getPositionsAvailable()),
                "#8B5CF6"
        );

        statsBox.getChildren().addAll(rateBox, ratioBox);
        HBox.setHgrow(rateBox, Priority.ALWAYS);
        HBox.setHgrow(ratioBox, Priority.ALWAYS);

        // ========== ASSEMBLAGE ==========
        contentBox.getChildren().addAll(
                infoGrid,
                new Separator(),
                datesBox,
                new Separator(),
                descriptionBox,
                statsBox
        );

        mainContainer.setTop(headerBox);
        mainContainer.setCenter(contentBox);

        dialog.getDialogPane().setContent(mainContainer);

        // Style du bouton fermer
        Button closeButton = (Button) dialog.getDialogPane().lookupButton(closeButtonType);
        closeButton.setStyle("-fx-background-color: #6B7280; -fx-text-fill: white; -fx-padding: 8 20; " +
                "-fx-background-radius: 6; -fx-font-size: 14px; -fx-font-weight: bold;");
        closeButton.setOnMouseEntered(e ->
                closeButton.setStyle("-fx-background-color: #4B5563; -fx-text-fill: white; -fx-padding: 8 20; " +
                        "-fx-background-radius: 6; -fx-font-size: 14px; -fx-font-weight: bold;")
        );
        closeButton.setOnMouseExited(e ->
                closeButton.setStyle("-fx-background-color: #6B7280; -fx-text-fill: white; -fx-padding: 8 20; " +
                        "-fx-background-radius: 6; -fx-font-size: 14px; -fx-font-weight: bold;")
        );

        // Afficher le dialogue
        dialog.showAndWait();
    }

    /**
     * Ajoute une ligne d'information dans la grille
     */
    private void addInfoRow(GridPane grid, String label, String value, int row, String color) {
        // Label
        Label labelField = new Label(label);
        labelField.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280; -fx-font-weight: 600;");

        // Valeur avec icône
        HBox valueBox = new HBox(8);
        valueBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label iconLabel = new Label("●");
        iconLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 16px;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #111827; -fx-font-weight: 500;");

        valueBox.getChildren().addAll(iconLabel, valueLabel);

        grid.add(labelField, 0, row);
        grid.add(valueBox, 1, row);
    }

    /**
     * Crée une boîte de date stylisée
     */
    private VBox createDateBox(String title, String date, String color) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 8; " +
                "-fx-border-color: #E5E7EB; -fx-border-radius: 8; -fx-border-width: 1;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #6B7280; -fx-font-weight: 600;");

        HBox dateBox = new HBox(8);
        dateBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label dateValue = new Label(date);
        dateValue.setStyle("-fx-font-size: 16px; -fx-text-fill: #111827; -fx-font-weight: bold;");

        // Petit indicateur coloré
        Label indicator = new Label("●");
        indicator.setStyle("-fx-text-fill: " + color + "; -fx-font-size: 12px;");

        dateBox.getChildren().addAll(indicator, dateValue);

        box.getChildren().addAll(titleLabel, dateBox);

        return box;
    }

    /**
     * Crée une boîte de statistique
     */
    private VBox createStatBox(String title, String value, String color) {
        VBox box = new VBox(5);
        box.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 15; -fx-background-radius: 8;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");

        box.getChildren().addAll(titleLabel, valueLabel);

        return box;
    }

    /**
     * Style moderne pour les badges de statut
     */
    private String getModernStatusStyle(String status) {
        switch (status) {
            case "Ouverte":
                return "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 6 15; " +
                        "-fx-background-radius: 30; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-border-color: #A7F3D0; -fx-border-radius: 30; -fx-border-width: 1;";
            case "Fermée":
                return "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-padding: 6 15; " +
                        "-fx-background-radius: 30; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-border-color: #FECACA; -fx-border-radius: 30; -fx-border-width: 1;";
            case "En attente":
                return "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 6 15; " +
                        "-fx-background-radius: 30; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-border-color: #FDE68A; -fx-border-radius: 30; -fx-border-width: 1;";
            case "Pourvue":
                return "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-padding: 6 15; " +
                        "-fx-background-radius: 30; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-border-color: #BFDBFE; -fx-border-radius: 30; -fx-border-width: 1;";
            default:
                return "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-padding: 6 15; " +
                        "-fx-background-radius: 30; -fx-font-size: 14px; -fx-font-weight: bold; " +
                        "-fx-border-color: #E5E7EB; -fx-border-radius: 30; -fx-border-width: 1;";
        }
    }

    // ✅ AJOUT DE LA MÉTHODE editOffer
    private void editOffer(Offer offer) {
        // Créer le dialogue
        Dialog<Offer> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'offre");
        dialog.setHeaderText("Modifier l'offre : " + offer.getTitle());

        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Grille pour le formulaire
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Champs du formulaire pré-remplis avec les données de l'offre
        TextField titleField = new TextField(offer.getTitle());
        titleField.setPromptText("Titre du poste");

        TextArea descriptionArea = new TextArea(offer.getDescription());
        descriptionArea.setPromptText("Description du poste");
        descriptionArea.setPrefRowCount(5);

        ComboBox<String> departmentCombo = new ComboBox<>();
        departmentCombo.getItems().addAll("IT", "RH", "Finance", "Marketing", "Production", "Logistique", "Commerce");
        departmentCombo.setValue(offer.getDepartment());
        departmentCombo.setPromptText("Département");

        ComboBox<String> contractTypeCombo = new ComboBox<>();
        contractTypeCombo.getItems().addAll("CDI", "CDD", "Stage", "Alternance", "Freelance");
        contractTypeCombo.setValue(offer.getContractType());
        contractTypeCombo.setPromptText("Type de contrat");

        ComboBox<String> experienceCombo = new ComboBox<>();
        experienceCombo.getItems().addAll("Débutant", "Junior", "Intermédiaire", "Senior", "Expert");
        experienceCombo.setValue(offer.getExperienceLevel());
        experienceCombo.setPromptText("Niveau d'expérience");

        TextField salaryMinField = new TextField(String.valueOf(offer.getSalaryMin()));
        salaryMinField.setPromptText("Salaire min");

        TextField salaryMaxField = new TextField(String.valueOf(offer.getSalaryMax()));
        salaryMaxField.setPromptText("Salaire max");

        ComboBox<String> locationCombo = new ComboBox<>();
        locationCombo.getItems().addAll("Tunis", "Sfax", "Sousse", "Gabès", "Bizerte", "Ariana", "Ben Arous", "Nabeul");
        locationCombo.setValue(offer.getLocation());
        locationCombo.setPromptText("Localisation");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Ouverte", "Fermée", "En attente", "Pourvue");
        statusCombo.setValue(offer.getStatus());
        statusCombo.setPromptText("Statut");

        DatePicker publishDatePicker = new DatePicker(offer.getPublishDate());
        DatePicker closingDatePicker = new DatePicker(offer.getClosingDate());

        Spinner<Integer> positionsSpinner = new Spinner<>(1, 100, offer.getPositionsAvailable());
        positionsSpinner.setEditable(true);

        // Ajout des champs à la grille
        int row = 0;
        grid.add(new Label("Titre*:"), 0, row);
        grid.add(titleField, 1, row++);

        grid.add(new Label("Description:"), 0, row);
        grid.add(descriptionArea, 1, row++);

        grid.add(new Label("Département*:"), 0, row);
        grid.add(departmentCombo, 1, row++);

        grid.add(new Label("Type de contrat*:"), 0, row);
        grid.add(contractTypeCombo, 1, row++);

        grid.add(new Label("Expérience*:"), 0, row);
        grid.add(experienceCombo, 1, row++);

        grid.add(new Label("Salaire min*:"), 0, row);
        grid.add(salaryMinField, 1, row);
        grid.add(new Label("Salaire max*:"), 2, row);
        grid.add(salaryMaxField, 3, row++);

        grid.add(new Label("Localisation*:"), 0, row);
        grid.add(locationCombo, 1, row++);

        grid.add(new Label("Statut:"), 0, row);
        grid.add(statusCombo, 1, row++);

        grid.add(new Label("Date publication:"), 0, row);
        grid.add(publishDatePicker, 1, row);
        grid.add(new Label("Date clôture:"), 2, row);
        grid.add(closingDatePicker, 3, row++);

        grid.add(new Label("Postes disponibles:"), 0, row);
        grid.add(positionsSpinner, 1, row);

        dialog.getDialogPane().setContent(grid);

        // Récupérer le bouton "Enregistrer"
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);

        // Validation du formulaire
        Runnable validateForm = () -> {
            boolean isValid = !titleField.getText().trim().isEmpty() &&
                    departmentCombo.getValue() != null &&
                    contractTypeCombo.getValue() != null &&
                    experienceCombo.getValue() != null &&
                    !salaryMinField.getText().trim().isEmpty() &&
                    !salaryMaxField.getText().trim().isEmpty() &&
                    locationCombo.getValue() != null;

            // Vérifier que les salaires sont des nombres valides
            if (isValid) {
                try {
                    Double.parseDouble(salaryMinField.getText());
                    Double.parseDouble(salaryMaxField.getText());
                } catch (NumberFormatException e) {
                    isValid = false;
                }
            }

            saveButton.setDisable(!isValid);
        };

        // Ajouter des écouteurs
        titleField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        departmentCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        contractTypeCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        experienceCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());
        salaryMinField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        salaryMaxField.textProperty().addListener((obs, old, newVal) -> validateForm.run());
        locationCombo.valueProperty().addListener((obs, old, newVal) -> validateForm.run());

        // Valider initialement
        validateForm.run();

        // Conversion résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    // Mettre à jour l'offre existante
                    offer.setTitle(titleField.getText());
                    offer.setDescription(descriptionArea.getText());
                    offer.setDepartment(departmentCombo.getValue());
                    offer.setContractType(contractTypeCombo.getValue());
                    offer.setExperienceLevel(experienceCombo.getValue());
                    offer.setSalaryMin(Double.parseDouble(salaryMinField.getText()));
                    offer.setSalaryMax(Double.parseDouble(salaryMaxField.getText()));
                    offer.setLocation(locationCombo.getValue());
                    offer.setStatus(statusCombo.getValue());
                    offer.setPublishDate(publishDatePicker.getValue());
                    offer.setClosingDate(closingDatePicker.getValue());
                    offer.setPositionsAvailable(positionsSpinner.getValue());
                    return offer;
                } catch (NumberFormatException e) {
                    showAlert("Erreur", "Les salaires doivent être des nombres valides", Alert.AlertType.ERROR);
                    return null;
                }
            }
            return null;
        });

        // Afficher le dialogue et traiter le résultat
        dialog.showAndWait().ifPresent(updatedOffer -> {
            if (offerService.updateOffer(updatedOffer)) {
                // Rafraîchir l'affichage
                int index = offersList.indexOf(offer);
                if (index != -1) {
                    offersList.set(index, updatedOffer);
                }
                loadOffers(); // Recharger toutes les offres
                showAlert("Succès", "Offre modifiée avec succès!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Erreur lors de la modification de l'offre", Alert.AlertType.ERROR);
            }
        });
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
