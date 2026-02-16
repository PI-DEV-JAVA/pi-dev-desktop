package talentos.pidev.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentos.pidev.models.Application;
import talentos.pidev.models.Offer;
import talentos.pidev.services.ApplicationService;
import talentos.pidev.services.OfferService;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ApplicationsCardController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> statusFilter;
    @FXML
    private ComboBox<Offer> offerFilter;
    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;
    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Label totalApplicationsLabel;
    @FXML
    private void refreshTable() {
        loadApplications();
        showAlert("Info", "Liste des candidatures actualisée", Alert.AlertType.INFORMATION);
    }

    private final ApplicationService applicationService;
    private final OfferService offerService;
    private final ObservableList<Application> applicationsList;
    private final ObservableList<Offer> offersList;
    private final DateTimeFormatter dateFormatter;

    public ApplicationsCardController() {
        this.applicationService = new ApplicationService();
        this.offerService = new OfferService();
        this.applicationsList = FXCollections.observableArrayList();
        this.offersList = FXCollections.observableArrayList();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupFilters();
        loadOffers();
        loadApplications();

    }

    private void setupFilters() {
        // Statuts
        statusFilter.getItems().addAll(
                "Tous", "Nouvelle", "En cours", "Acceptée", "Rejetée", "En attente");
        statusFilter.setValue("Tous");

        // Dates par défaut (30 derniers jours)
        fromDatePicker.setValue(LocalDate.now().minusDays(30));
        toDatePicker.setValue(LocalDate.now());

        // Configuration du ComboBox des offres
        offerFilter.setCellFactory(param -> new ListCell<Offer>() {
            @Override
            protected void updateItem(Offer offer, boolean empty) {
                super.updateItem(offer, empty);
                if (empty || offer == null) {
                    setText("Toutes les offres");
                } else {
                    setText(offer.getTitle() + " (" + offer.getDepartment() + ")");
                }
            }
        });

        offerFilter.setButtonCell(new ListCell<Offer>() {
            @Override
            protected void updateItem(Offer offer, boolean empty) {
                super.updateItem(offer, empty);
                if (empty || offer == null) {
                    setText("Toutes les offres");
                } else {
                    setText(offer.getTitle());
                }
            }
        });

        // Écouteurs
        searchField.textProperty().addListener((obs, old, newVal) -> filterApplications());
        statusFilter.setOnAction(e -> filterApplications());
        offerFilter.setOnAction(e -> filterApplications());
        fromDatePicker.setOnAction(e -> filterApplications());
        toDatePicker.setOnAction(e -> filterApplications());
    }

    private void loadOffers() {
        offersList.clear();
        offersList.addAll(offerService.getAllOffers());
        offerFilter.getItems().clear();
        offerFilter.getItems().add(null); // Option "Toutes les offres"
        offerFilter.getItems().addAll(offersList);
        offerFilter.setValue(null);
    }

    @FXML
    private void showStatistics() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques");
        alert.setHeaderText("Statistiques des candidatures");
        alert.setContentText("Fonctionnalité à implémenter");
        alert.showAndWait();
    }

    @FXML
    private void loadApplications() {
        applicationsList.clear();
        applicationsList.addAll(applicationService.getAllApplications());
        displayCards(applicationsList);
        updateTotalLabel(applicationsList.size());
    }

    private void filterApplications() {
        String keyword = searchField.getText().toLowerCase();
        String status = statusFilter.getValue();
        Offer selectedOffer = offerFilter.getValue();
        LocalDate fromDate = fromDatePicker.getValue();
        LocalDate toDate = toDatePicker.getValue();

        if ("Tous".equals(status))
            status = null;

        ObservableList<Application> filtered = FXCollections.observableArrayList();

        for (Application app : applicationsList) {
            boolean matches = true;

            // Filtre par mot-clé
            if (!keyword.isEmpty()) {
                matches = app.getCandidateName().toLowerCase().contains(keyword) ||
                        app.getCandidateEmail().toLowerCase().contains(keyword);
            }

            // Filtre par statut
            if (matches && status != null) {
                matches = app.getStatus().equals(status);
            }

            // Filtre par offre
            if (matches && selectedOffer != null) {
                matches = app.getOfferId() == selectedOffer.getId();
            }

            // Filtre par date
            if (matches && fromDate != null) {
                matches = !app.getApplicationDate().isBefore(fromDate);
            }
            if (matches && toDate != null) {
                matches = !app.getApplicationDate().isAfter(toDate);
            }

            if (matches) {
                filtered.add(app);
            }
        }

        displayCards(filtered);
        updateTotalLabel(filtered.size());
    }

    private void displayCards(ObservableList<Application> applications) {
        cardsContainer.getChildren().clear();

        for (Application app : applications) {
            VBox card = createApplicationCard(app);
            cardsContainer.getChildren().add(card);
        }

        if (applications.isEmpty()) {
            showNoResultsMessage();
        }
    }

    private VBox createApplicationCard(Application app) {
        // Trouver l'offre associée
        Offer offer = offersList.stream()
                .filter(o -> o.getId() == app.getOfferId())
                .findFirst()
                .orElse(null);
        if (offer == null) {
            System.out.println("   ❌ Offre NON trouvée pour offerId: " + app.getOfferId());
        } else {
            System.out.println("   ✅ Offre trouvée: " + offer.getTitle());
        }

        VBox card = new VBox(12);
        String defaultStyle = "-fx-background-color: #1E293B;" +
                "-fx-background-radius: 16;" +
                "-fx-padding: 18;" +
                "-fx-border-color: rgba(255,255,255,0.06);" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 3);";
        String hoverStyle = "-fx-background-color: #1E293B;" +
                "-fx-background-radius: 16;" +
                "-fx-padding: 18;" +
                "-fx-border-color: rgba(99,102,241,0.5);" +
                "-fx-border-radius: 16;" +
                "-fx-border-width: 1;" +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.25), 18, 0, 0, 5); -fx-translate-y: -2;";
        card.setStyle(defaultStyle);
        card.setPrefWidth(340);
        card.setMinHeight(280);
        card.setMaxHeight(340);

        card.setOnMouseEntered(e -> card.setStyle(hoverStyle));
        card.setOnMouseExited(e -> card.setStyle(defaultStyle));

        // En-tête avec initiales
        HBox headerBox = new HBox(14);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Avatar avec initiales
        StackPane avatar = new StackPane();
        avatar.setStyle(
                "-fx-background-color: " + getAvatarColor(app.getStatus()) + ";" +
                        "-fx-background-radius: 24;" +
                        "-fx-min-width: 48;" +
                        "-fx-min-height: 48;" +
                        "-fx-max-width: 48;" +
                        "-fx-max-height: 48;");

        String initials = getInitials(app.getCandidateName());
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        avatar.getChildren().add(initialsLabel);

        // Informations candidat
        VBox candidateInfo = new VBox(3);

        Label nameLabel = new Label(app.getCandidateName());
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Label emailLabel = new Label("✉ " + app.getCandidateEmail());
        emailLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");

        candidateInfo.getChildren().addAll(nameLabel, emailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge de statut
        Label statusBadge = new Label(app.getStatus());
        statusBadge.setStyle(getStatusStyle(app.getStatus()));

        headerBox.getChildren().addAll(avatar, candidateInfo, spacer, statusBadge);

        // Candidate details row (phone + date + score)
        HBox detailsRow = new HBox(16);
        detailsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        detailsRow.setStyle("-fx-padding: 4 0 0 0;");

        if (app.getCandidatePhone() != null && !app.getCandidatePhone().isEmpty()) {
            Label phoneLabel = new Label("📞 " + app.getCandidatePhone());
            phoneLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");
            detailsRow.getChildren().add(phoneLabel);
        }

        Label dateValue = new Label("📅 " + app.getApplicationDate().format(dateFormatter));
        dateValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748B;");
        detailsRow.getChildren().add(dateValue);

        if (app.getScore() > 0) {
            Label scoreValue = new Label(String.format("⭐ %.0f/100", app.getScore()));
            scoreValue.setStyle(getScoreStyle(app.getScore()));
            detailsRow.getChildren().add(scoreValue);
        }

        // Informations de l'offre
        VBox offerBox = new VBox(6);
        offerBox.setStyle(
                "-fx-background-color: rgba(99,102,241,0.08);" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 10 14;" +
                        "-fx-border-color: rgba(99,102,241,0.12);" +
                        "-fx-border-radius: 12;" +
                        "-fx-border-width: 1;");
        String offerDisplayText;
        if (offer != null) {
            offerDisplayText = "📋 " + offer.getTitle();
        } else {
            offerDisplayText = "📋 Offre supprimée"; // Message plus clair si l'offre n'existe plus
        }
        Label offerTitle = new Label(offerDisplayText);
        offerTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #A5B4FC;");
        offerTitle.setWrapText(true);

        if (offer != null) {
            Label deptLabel = new Label("🏢 " + offer.getDepartment()
                    + (offer.getLocation() != null ? " • 📍 " + offer.getLocation() : ""));
            deptLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8;");
            offerBox.getChildren().addAll(offerTitle, deptLabel);
        } else {
            offerBox.getChildren().add(offerTitle);
        }

        // Separator
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: rgba(255,255,255,0.06);");

        // Boutons d'action
        HBox actionsBox = new HBox(8);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        actionsBox.setPadding(new Insets(4, 0, 0, 0));

        Button viewBtn = createActionButton("👁 Voir", "rgba(99,102,241,0.2)", "#A5B4FC");
        Button evaluateBtn = createActionButton("📊 Évaluer", "rgba(245,158,11,0.2)", "#FBBF24");
        Button deleteBtn = createActionButton("🗑 Supprimer", "rgba(239,68,68,0.2)", "#F87171");

        viewBtn.setOnAction(e -> showApplicationDetails(app, offer));
        evaluateBtn.setOnAction(e -> showEvaluationDialog(app));
        deleteBtn.setOnAction(e -> deleteApplication(app));

        actionsBox.getChildren().addAll(viewBtn, evaluateBtn, deleteBtn);

        // Assemblage final
        card.getChildren().addAll(headerBox, detailsRow, offerBox, sep, actionsBox);

        return card;
    }

    private Button createActionButton(String text, String bgColor, String textColor) {
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

    private String getInitials(String name) {
        if (name == null || name.isEmpty())
            return "?";
        String[] parts = name.split(" ");
        if (parts.length == 1)
            return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String getAvatarColor(String status) {
        switch (status) {
            case "Nouvelle":
                return "linear-gradient(to bottom right, #6366F1, #06B6D4)";
            case "En cours":
                return "linear-gradient(to bottom right, #F59E0B, #EF4444)";
            case "Acceptée":
                return "linear-gradient(to bottom right, #10B981, #06B6D4)";
            case "Rejetée":
                return "linear-gradient(to bottom right, #EF4444, #EC4899)";
            case "En attente":
                return "linear-gradient(to bottom right, #8B5CF6, #6366F1)";
            default:
                return "#475569";
        }
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "Nouvelle":
                return "-fx-background-color: rgba(99,102,241,0.15); -fx-text-fill: #A5B4FC; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "En cours":
                return "-fx-background-color: rgba(245,158,11,0.15); -fx-text-fill: #FBBF24; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "Acceptée":
                return "-fx-background-color: rgba(16,185,129,0.15); -fx-text-fill: #34D399; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "Rejetée":
                return "-fx-background-color: rgba(239,68,68,0.15); -fx-text-fill: #F87171; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: rgba(139,92,246,0.15); -fx-text-fill: #C4B5FD; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: rgba(255,255,255,0.06); -fx-text-fill: #94A3B8; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
        }
    }

    private String getScoreStyle(double score) {
        if (score >= 70)
            return "-fx-text-fill: #34D399; -fx-font-weight: bold; -fx-font-size: 12px;";
        if (score >= 50)
            return "-fx-text-fill: #FBBF24; -fx-font-weight: bold; -fx-font-size: 12px;";
        if (score > 0)
            return "-fx-text-fill: #F87171; -fx-font-weight: bold; -fx-font-size: 12px;";
        return "-fx-text-fill: #64748B; -fx-font-size: 12px;";
    }

    private void showNoResultsMessage() {
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(javafx.geometry.Pos.CENTER);
        messageBox.setPrefWidth(600);
        messageBox.setPrefHeight(400);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 56px;");

        Label title = new Label("Aucune candidature trouvée");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #94A3B8;");

        Label subtitle = new Label("Essayez de modifier vos filtres de recherche");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        Button resetBtn = new Button("Réinitialiser les filtres");
        resetBtn.setStyle(
                "-fx-background-color: rgba(99,102,241,0.2); -fx-text-fill: #A5B4FC; -fx-padding: 10 24; -fx-background-radius: 12; -fx-font-weight: bold; -fx-cursor: hand;");
        resetBtn.setOnAction(e -> {
            searchField.clear();
            statusFilter.setValue("Tous");
            offerFilter.setValue(null);
            fromDatePicker.setValue(LocalDate.now().minusDays(30));
            toDatePicker.setValue(LocalDate.now());
            filterApplications();
        });

        messageBox.getChildren().addAll(icon, title, subtitle, resetBtn);
        cardsContainer.getChildren().add(messageBox);
    }

    private void updateTotalLabel(int count) {
        totalApplicationsLabel.setText(count + " candidature" + (count > 1 ? "s" : ""));
    }

    private void showApplicationDetails(Application app, Offer offer) {
        // À implémenter - boîte de dialogue détaillée
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de la candidature");
        alert.setHeaderText(app.getCandidateName());

        String content = String.format(
                "📧 Email: %s\n" +
                        "📞 Téléphone: %s\n" +
                        "📋 Offre: %s\n" +
                        "📅 Date candidature: %s\n" +
                        "📊 Statut: %s\n" +
                        "⭐ Score: %.1f/100\n\n" +
                        "📝 Notes:\n%s",
                app.getCandidateEmail(),
                app.getCandidatePhone() != null ? app.getCandidatePhone() : "Non renseigné",
                offer != null ? offer.getTitle() : "Offre #" + app.getOfferId(),
                app.getApplicationDate().format(dateFormatter),
                app.getStatus(),
                app.getScore(),
                app.getNotes() != null ? app.getNotes() : "Aucune note");

        alert.setContentText(content);
        alert.showAndWait();
    }

private void showEvaluationDialog(Application app) {
        // Trouver l'offre associée
        Offer offer = offersList.stream()
                .filter(o -> o.getId() == app.getOfferId())
                .findFirst()
                .orElse(null);

        // Créer le dialogue
        Dialog<Application> dialog = new Dialog<>();
        dialog.setTitle("Évaluation de candidature");
        dialog.setHeaderText("Évaluation de " + app.getCandidateName());

        // Style du dialogue
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setPrefWidth(550);
        dialogPane.setPrefHeight(600);
        dialogPane.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 16;");

        ButtonType saveButtonType = new ButtonType("Enregistrer l'évaluation", ButtonBar.ButtonData.OK_DONE);
        dialogPane.getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Conteneur principal
        VBox content = new VBox(20);
        content.setStyle("-fx-padding: 20; -fx-background-color: #0F172A;");
        content.setAlignment(javafx.geometry.Pos.TOP_CENTER);

        // ========== EN-TÊTE AVATAR ==========
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Avatar avec initiales
        StackPane avatar = new StackPane();
        avatar.setStyle(
                "-fx-background-color: " + getAvatarColor(app.getStatus()) + ";" +
                        "-fx-background-radius: 40;" +
                        "-fx-min-width: 70;" +
                        "-fx-min-height: 70;" +
                        "-fx-max-width: 70;" +
                        "-fx-max-height: 70;");

        String initials = getInitials(app.getCandidateName());
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        avatar.getChildren().add(initialsLabel);

        // Informations candidat
        VBox candidateInfo = new VBox(5);

        Label nameLabel = new Label(app.getCandidateName());
        nameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Label emailLabel = new Label("✉ " + app.getCandidateEmail());
        emailLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");

        if (app.getCandidatePhone() != null && !app.getCandidatePhone().isEmpty()) {
            Label phoneLabel = new Label("📞 " + app.getCandidatePhone());
            phoneLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #94A3B8;");
            candidateInfo.getChildren().addAll(nameLabel, emailLabel, phoneLabel);
        } else {
            candidateInfo.getChildren().addAll(nameLabel, emailLabel);
        }

        headerBox.getChildren().addAll(avatar, candidateInfo);

        // ========== INFORMATIONS OFFRE ==========
        if (offer != null) {
            VBox offerInfoBox = new VBox(8);
            offerInfoBox.setStyle(
                    "-fx-background-color: rgba(99,102,241,0.08);" +
                            "-fx-background-radius: 12;" +
                            "-fx-padding: 15;" +
                            "-fx-border-color: rgba(99,102,241,0.2);" +
                            "-fx-border-radius: 12;" +
                            "-fx-border-width: 1;");

            Label offerTitleLabel = new Label("📋 " + offer.getTitle());
            offerTitleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #A5B4FC;");

            HBox offerDetailsRow = new HBox(20);
            offerDetailsRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label deptLabel = new Label("🏢 " + offer.getDepartment());
            deptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

            Label contractLabel = new Label("📄 " + offer.getContractType());
            contractLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

            Label expLabel = new Label("📊 " + offer.getExperienceLevel());
            expLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

            offerDetailsRow.getChildren().addAll(deptLabel, contractLabel, expLabel);

            offerInfoBox.getChildren().addAll(offerTitleLabel, offerDetailsRow);
            content.getChildren().add(offerInfoBox);
        }

        // ========== FORMULAIRE D'ÉVALUATION ==========
        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-padding: 10 0;");
        grid.setAlignment(javafx.geometry.Pos.CENTER);

        // Largeur des colonnes
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(30);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(70);
        grid.getColumnConstraints().addAll(col1, col2);

        int row = 0;

        // 1. Score avec Slider
        Label scoreLabel = new Label("Score (0-100):");
        scoreLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #F1F5F9;");

        HBox scoreBox = new HBox(15);
        scoreBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Slider scoreSlider = new Slider(0, 100, app.getScore());
        scoreSlider.setShowTickLabels(true);
        scoreSlider.setShowTickMarks(true);
        scoreSlider.setMajorTickUnit(25);
        scoreSlider.setBlockIncrement(5);
        scoreSlider.setStyle(
                "-fx-control-inner-background: #1E293B;" +
                        "-fx-track-background: #334155;");

        Label scoreValue = new Label(String.format("%.0f", app.getScore()));
        scoreValue.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #A5B4FC; -fx-min-width: 40;");

        scoreSlider.valueProperty().addListener((obs, old, newVal) ->
                scoreValue.setText(String.format("%.0f", newVal)));

        scoreBox.getChildren().addAll(scoreSlider, scoreValue);

        grid.add(scoreLabel, 0, row);
        grid.add(scoreBox, 1, row++);

        // 2. Statut
        Label statusLabel = new Label("Statut:");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #F1F5F9;");

        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("Nouvelle", "En cours", "Acceptée", "Rejetée", "En attente");
        statusCombo.setValue(app.getStatus());
        statusCombo.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: #F1F5F9;");

        // Couleur du texte dans la liste déroulante
        statusCombo.setCellFactory(param -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-background-color: #1E293B; -fx-text-fill: #F1F5F9;");
                }
            }
        });

        grid.add(statusLabel, 0, row);
        grid.add(statusCombo, 1, row++);

        // 3. Interviewer
        Label interviewerLabel = new Label("Interviewer:");
        interviewerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #F1F5F9;");

        TextField interviewerField = new TextField(app.getInterviewer());
        interviewerField.setPromptText("Nom de l'interviewer");
        interviewerField.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: #F1F5F9;" +
                        "-fx-prompt-text-fill: #64748B;");

        grid.add(interviewerLabel, 0, row);
        grid.add(interviewerField, 1, row++);

        // 4. Date interview
        Label interviewDateLabel = new Label("Date interview:");
        interviewDateLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #F1F5F9;");

        DatePicker interviewDatePicker = new DatePicker(
                app.getInterviewDate() != null ? app.getInterviewDate() : LocalDate.now()
        );
        interviewDatePicker.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: #F1F5F9;");

        grid.add(interviewDateLabel, 0, row);
        grid.add(interviewDatePicker, 1, row++);

        // 5. Résultat
        Label resultLabel = new Label("Résultat:");
        resultLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #F1F5F9;");

        TextArea resultArea = new TextArea(app.getInterviewResult());
        resultArea.setPromptText("Résultat de l'interview...");
        resultArea.setPrefRowCount(3);
        resultArea.setWrapText(true);
        resultArea.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: #F1F5F9;" +
                        "-fx-prompt-text-fill: #64748B;");

        grid.add(resultLabel, 0, row);
        grid.add(resultArea, 1, row++);

        // 6. Notes d'évaluation
        Label notesLabel = new Label("Notes:");
        notesLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #F1F5F9;");

        TextArea notesArea = new TextArea(app.getNotes());
        notesArea.setPromptText("Notes d'évaluation...");
        notesArea.setPrefRowCount(4);
        notesArea.setWrapText(true);
        notesArea.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-text-fill: #F1F5F9;" +
                        "-fx-prompt-text-fill: #64748B;");

        grid.add(notesLabel, 0, row);
        grid.add(notesArea, 1, row);

        content.getChildren().addAll(headerBox, new Separator(), grid);

        // Ajouter le contenu dans un ScrollPane
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0F172A; -fx-background-color: #0F172A; -fx-border-color: transparent;");
        dialogPane.setContent(scrollPane);

        // Style des boutons
        Button saveButton = (Button) dialogPane.lookupButton(saveButtonType);
        saveButton.setStyle(
                "-fx-background-color: #6366F1;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8;");

        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle(
                "-fx-background-color: #475569;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 8;");

        // Validation de base (optionnelle)
        saveButton.setDisable(false);

        // Résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                app.setScore(scoreSlider.getValue());
                app.setStatus(statusCombo.getValue());
                app.setInterviewer(interviewerField.getText());
                app.setInterviewDate(interviewDatePicker.getValue());
                app.setInterviewResult(resultArea.getText());
                app.setNotes(notesArea.getText());
                return app;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updatedApp -> {
            if (applicationService.updateApplication(updatedApp)) {
                refreshTable();
                showAlert("Succès", "Évaluation enregistrée avec succès!", Alert.AlertType.INFORMATION);
            } else {
                showAlert("Erreur", "Erreur lors de l'enregistrement", Alert.AlertType.ERROR);
            }
        });
    }


    private void deleteApplication(Application app) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la candidature");
        confirm.setContentText("Voulez-vous vraiment supprimer la candidature de " + app.getCandidateName() + " ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                if (applicationService.deleteApplication(app.getId())) {
                    applicationsList.remove(app);
                    filterApplications();
                    showAlert("Succès", "Candidature supprimée", Alert.AlertType.INFORMATION);
                }
            }
        });
    }

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}
