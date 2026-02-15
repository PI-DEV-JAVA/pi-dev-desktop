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

    @FXML private TextField searchField;
    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<Offer> offerFilter;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private FlowPane cardsContainer;
    @FXML private Label totalApplicationsLabel;

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
        loadApplications();
        loadOffers();
    }

    private void setupFilters() {
        // Statuts
        statusFilter.getItems().addAll(
                "Tous", "Nouvelle", "En cours", "Acceptée", "Rejetée", "En attente"
        );
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

        if ("Tous".equals(status)) status = null;

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

        VBox card = new VBox(15);
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);" +
                        "-fx-border-color: #E5E7EB;" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        );
        card.setPrefWidth(350);
        card.setPrefHeight(380);

        // Animation au survol
        card.setOnMouseEntered(e ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 16;" +
                                "-fx-padding: 20;" +
                                "-fx-effect: dropshadow(gaussian, rgba(59,130,246,0.3), 20, 0, 0, 8);" +
                                "-fx-border-color: #3B82F6;" +
                                "-fx-border-radius: 16;" +
                                "-fx-border-width: 2;"
                )
        );
        card.setOnMouseExited(e ->
                card.setStyle(
                        "-fx-background-color: white;" +
                                "-fx-background-radius: 16;" +
                                "-fx-padding: 20;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 12, 0, 0, 4);" +
                                "-fx-border-color: #E5E7EB;" +
                                "-fx-border-radius: 16;" +
                                "-fx-border-width: 1;"
                )
        );

        // En-tête avec initiales
        HBox headerBox = new HBox(15);
        headerBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Avatar avec initiales
        StackPane avatar = new StackPane();
        avatar.setStyle(
                "-fx-background-color: " + getAvatarColor(app.getStatus()) + ";" +
                        "-fx-background-radius: 30;" +
                        "-fx-min-width: 50;" +
                        "-fx-min-height: 50;" +
                        "-fx-max-width: 50;" +
                        "-fx-max-height: 50;"
        );

        String initials = getInitials(app.getCandidateName());
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
        avatar.getChildren().add(initialsLabel);

        // Informations candidat
        VBox candidateInfo = new VBox(5);

        Label nameLabel = new Label(app.getCandidateName());
        nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label emailLabel = new Label(app.getCandidateEmail());
        emailLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");

        candidateInfo.getChildren().addAll(nameLabel, emailLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge de statut
        Label statusBadge = new Label(app.getStatus());
        statusBadge.setStyle(getStatusStyle(app.getStatus()));

        headerBox.getChildren().addAll(avatar, candidateInfo, spacer, statusBadge);

        // Informations de l'offre
        VBox offerBox = new VBox(8);
        offerBox.setStyle(
                "-fx-background-color: #F8FAFC;" +
                        "-fx-background-radius: 12;" +
                        "-fx-padding: 12;"
        );

        Label offerTitle = new Label("📋 " + (offer != null ? offer.getTitle() : "Offre #" + app.getOfferId()));
        offerTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: 600; -fx-text-fill: #1F2937;");

        HBox deptBox = new HBox(10);
        Label deptIcon = new Label("🏢");
        deptIcon.setStyle("-fx-font-size: 12px;");
        Label deptLabel = new Label(offer != null ? offer.getDepartment() : "N/A");
        deptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6B7280;");
        deptBox.getChildren().addAll(deptIcon, deptLabel);

        offerBox.getChildren().addAll(offerTitle, deptBox);

        // Informations supplémentaires
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(10);
        infoGrid.setVgap(8);
        infoGrid.setPadding(new Insets(5, 0, 5, 0));

        // Date de candidature
        Label dateIcon = new Label("📅");
        dateIcon.setStyle("-fx-font-size: 12px;");
        Label dateValue = new Label(app.getApplicationDate().format(dateFormatter));
        dateValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
        infoGrid.add(dateIcon, 0, 0);
        infoGrid.add(dateValue, 1, 0);

        // Téléphone
        if (app.getCandidatePhone() != null && !app.getCandidatePhone().isEmpty()) {
            Label phoneIcon = new Label("📞");
            phoneIcon.setStyle("-fx-font-size: 12px;");
            Label phoneValue = new Label(app.getCandidatePhone());
            phoneValue.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151;");
            infoGrid.add(phoneIcon, 0, 1);
            infoGrid.add(phoneValue, 1, 1);
        }

        // Score
        if (app.getScore() > 0) {
            HBox scoreBox = new HBox(5);
            scoreBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            Label scoreIcon = new Label("⭐");
            scoreIcon.setStyle("-fx-font-size: 12px;");

            Label scoreValue = new Label(String.format("%.1f/100", app.getScore()));
            scoreValue.setStyle(getScoreStyle(app.getScore()));

            scoreBox.getChildren().addAll(scoreIcon, scoreValue);
            infoGrid.add(scoreBox, 0, 2, 2, 1);
        }

        // Boutons d'action
        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(javafx.geometry.Pos.CENTER);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));

        Button viewBtn = createActionButton("👁️ Voir", "#3B82F6");
        Button evaluateBtn = createActionButton("📊 Évaluer", "#F59E0B");
        Button deleteBtn = createActionButton("🗑️ Supprimer", "#EF4444");

        viewBtn.setOnAction(e -> showApplicationDetails(app, offer));
        evaluateBtn.setOnAction(e -> showEvaluationDialog(app));
        deleteBtn.setOnAction(e -> deleteApplication(app));

        actionsBox.getChildren().addAll(viewBtn, evaluateBtn, deleteBtn);

        // Assemblage final
        card.getChildren().addAll(headerBox, offerBox, infoGrid, actionsBox);

        return card;
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color: " + color + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-padding: 8 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-cursor: hand;"
        );

        // Effet hover
        btn.setOnMouseEntered(e ->
                btn.setStyle(
                        "-fx-background-color: derive(" + color + ", -20%);" +
                                "-fx-text-fill: white;" +
                                "-fx-padding: 8 12;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;" +
                                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);"
                )
        );

        btn.setOnMouseExited(e ->
                btn.setStyle(
                        "-fx-background-color: " + color + ";" +
                                "-fx-text-fill: white;" +
                                "-fx-padding: 8 12;" +
                                "-fx-background-radius: 8;" +
                                "-fx-font-size: 12px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-cursor: hand;"
                )
        );

        return btn;
    }

    private String getInitials(String name) {
        if (name == null || name.isEmpty()) return "?";
        String[] parts = name.split(" ");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private String getAvatarColor(String status) {
        switch (status) {
            case "Nouvelle": return "#3B82F6";
            case "En cours": return "#F59E0B";
            case "Acceptée": return "#10B981";
            case "Rejetée": return "#EF4444";
            case "En attente": return "#8B5CF6";
            default: return "#6B7280";
        }
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "Nouvelle":
                return "-fx-background-color: #DBEAFE; -fx-text-fill: #1E40AF; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "En cours":
                return "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "Acceptée":
                return "-fx-background-color: #D1FAE5; -fx-text-fill: #065F46; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "Rejetée":
                return "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: #EDE9FE; -fx-text-fill: #6D28D9; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #F3F4F6; -fx-text-fill: #374151; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
        }
    }

    private String getScoreStyle(double score) {
        if (score >= 70) return "-fx-text-fill: #10B981; -fx-font-weight: bold; -fx-font-size: 12px;";
        if (score >= 50) return "-fx-text-fill: #F59E0B; -fx-font-weight: bold; -fx-font-size: 12px;";
        if (score > 0) return "-fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-font-size: 12px;";
        return "-fx-text-fill: #6B7280; -fx-font-size: 12px;";
    }

    private void showNoResultsMessage() {
        VBox messageBox = new VBox(20);
        messageBox.setAlignment(javafx.geometry.Pos.CENTER);
        messageBox.setPrefWidth(600);
        messageBox.setPrefHeight(400);

        Label icon = new Label("🔍");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Aucune candidature trouvée");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #6B7280;");

        Label subtitle = new Label("Essayez de modifier vos filtres de recherche");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #9CA3AF;");

        Button resetBtn = new Button("Réinitialiser les filtres");
        resetBtn.setStyle("-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 8;");
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
                app.getNotes() != null ? app.getNotes() : "Aucune note"
        );

        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showEvaluationDialog(Application app) {
        // À implémenter - dialogue d'évaluation
        showAlert("Évaluation", "Fonctionnalité d'évaluation à implémenter", Alert.AlertType.INFORMATION);
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
