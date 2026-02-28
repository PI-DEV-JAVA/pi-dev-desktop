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
import talentos.pidev.services.AIScoringService;
import talentos.pidev.models.AIScoreResult;
import com.fasterxml.jackson.databind.JsonNode;
import javafx.geometry.Pos;
import javafx.application.Platform;
import javafx.animation.*;
import javafx.util.Duration;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.File;
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
    private VBox aiDetailsPanel;
    private boolean isAIPanelVisible = false;

    @FXML
    private void showStatistics() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistiques");
        alert.setHeaderText("Statistiques des candidatures");
        alert.setContentText("Fonctionnalité à implémenter");
        alert.showAndWait();
    }
    // Variables pour le panneau latéral IA


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
        Button aiScoreBtn = createActionButton("🤖 Score IA", "rgba(139,92,246,0.2)", "#C4B5FD");
        viewBtn.setOnAction(e -> showApplicationDetails(app, offer));
        evaluateBtn.setOnAction(e -> showEvaluationDialog(app));
        deleteBtn.setOnAction(e -> deleteApplication(app));
        // Dans la section des boutons d'action, après les autres boutons

        aiScoreBtn.setOnAction(e -> showAIScore(app, offer));


// Ajoute-le au HBox actions
        actionsBox.getChildren().addAll(viewBtn, evaluateBtn, aiScoreBtn, deleteBtn);



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
    private void showAIScore(Application app, Offer offer) {
        // Vérifier que le fichier CV existe
        if (app.getCvFilePath() == null || app.getCvFilePath().isEmpty()) {
            showAlert("Erreur", "Aucun CV trouvé pour cette candidature", Alert.AlertType.ERROR);
            return;
        }

        File cvFile = new File(app.getCvFilePath());
        if (!cvFile.exists()) {
            showAlert("Erreur", "Le fichier CV n'existe pas: " + app.getCvFilePath(), Alert.AlertType.ERROR);
            return;
        }

        // Dialogue de chargement
        Dialog<Void> loadingDialog = new Dialog<>();
        loadingDialog.setTitle("Analyse IA en cours");
        loadingDialog.setHeaderText("Notre IA analyse la compatibilité...");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));

        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(50, 50);

        Label statusLabel = new Label("Analyse du CV et de l'offre...");
        statusLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748B;");

        content.getChildren().addAll(progressIndicator, statusLabel);
        loadingDialog.getDialogPane().setContent(content);
        loadingDialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);

        // Style du dialogue
        loadingDialog.getDialogPane().setStyle("-fx-background-color: #0F172A;");
        statusLabel.setStyle("-fx-text-fill: #E2E8F0; -fx-font-size: 14px;");

        loadingDialog.show();

        // Lancer l'analyse dans un thread séparé
        new Thread(() -> {
            try {
                AIScoringService aiService = new AIScoringService();

                Platform.runLater(() -> statusLabel.setText("📤 Soumission à l'API..."));
                String jobId = aiService.submitScoringJob(cvFile, offer.getDescription(), "French");

                Platform.runLater(() -> statusLabel.setText("⏳ Analyse en cours..."));

                // ✅ POLLING OPTIMISÉ - Maximum 6 tentatives (30 secondes)
                JsonNode result = null;
                int attempts = 0;
                int maxAttempts = 6;

                while (attempts < maxAttempts) {
                    Thread.sleep(5000); // 5 secondes entre chaque tentative
                    attempts++;

                    result = aiService.getScoringResult(jobId);

                    // ✅ SORTIR DÈS QU'ON A UN RÉSULTAT
                    if (result != null) {
                        System.out.println("✅ Résultat obtenu en " + attempts + " tentatives");
                        break;
                    }

                    final int currentAttempt = attempts;
                    Platform.runLater(() ->
                            statusLabel.setText("⏳ Analyse en cours... (tentative " + currentAttempt + "/" + maxAttempts + ")"));
                }

                // ✅ TRAITER LE RÉSULTAT
                if (result != null) {
                    AIScoreResult score = parseResult(result);
                    Platform.runLater(() -> {
                        loadingDialog.close();
                        showScoreResult(app,score);
                    });
                } else {
                    Platform.runLater(() -> {
                        loadingDialog.close();
                        showAlert("Information",
                                "L'analyse prend plus de temps que prévu. " +
                                        "Vous pourrez réessayer plus tard (5 appels/jour)",
                                Alert.AlertType.INFORMATION);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    loadingDialog.close();
                    showAlert("Erreur", "Échec de l'analyse IA: " + e.getMessage(),
                            Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    private AIScoreResult parseResult(JsonNode responseNode) {
        AIScoreResult score = new AIScoreResult();

        // ✅ Extraire le noeud result de la structure
        JsonNode resultNode = responseNode.path("data")
                .path("attributes")
                .path("result");

        // Extraire les scores
        JsonNode matchScores = resultNode.path("match_scores");
        score.setOverallScore(matchScores.path("overall_match").asDouble(0));
        score.setSkillsScore(matchScores.path("skills_match").asDouble(0));
        score.setExperienceScore(matchScores.path("experience_match").asDouble(0));
        score.setEducationScore(matchScores.path("education_match").asDouble(0));

        // Construire les explications
        JsonNode explanations = resultNode.path("explanations");
        StringBuilder explanationText = new StringBuilder();
        explanations.fields().forEachRemaining(entry ->
                explanationText.append(entry.getKey()).append(": ").append(entry.getValue().asText()).append("\n\n")
        );
        score.setExplanation(explanationText.toString());

        return score;
    }
    private void showScoreResult(Application app, AIScoreResult score) {
        // Créer ou récupérer le panneau latéral
        if (aiDetailsPanel == null) {
            aiDetailsPanel = createAIDetailsPanel();
            // Ajouter le panneau au BorderPane principal
            BorderPane mainPane = (BorderPane) cardsContainer.getScene().getRoot();
            mainPane.setRight(aiDetailsPanel);
        }

        // Mettre à jour le contenu avec les nouveaux résultats
        updateAIPanelContent(app, score);

        // Afficher le panneau avec animation
        aiDetailsPanel.setVisible(true);
        aiDetailsPanel.setTranslateX(0);
        isAIPanelVisible = true;

        // Animation d'apparition
        animatePanelIn();
    }

    /**
     * Crée le panneau de détails IA
     */
    /**
     * Crée le panneau de détails IA avec ScrollPane
     */
    private VBox createAIDetailsPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(380);
        panel.setMaxWidth(380);
        panel.setStyle(
                "-fx-background-color: #0F172A;" +
                        "-fx-background-radius: 24 0 0 24;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-width: 1 0 0 1;" +
                        "-fx-border-radius: 24 0 0 24;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, -5, 0);" +
                        "-fx-padding: 25 20 25 20;"
        );
        panel.setVisible(false);

        // En-tête avec bouton de fermeture (FIXE)
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));

        Label titleLabel = new Label("🔬 Analyse IA détaillée");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle(
                "-fx-background-color: #334155;" +
                        "-fx-text-fill: #94A3B8;" +
                        "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 5 12;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;"
        );
        closeBtn.setOnAction(e -> hideAIPanel());

        // Effets de survol
        closeBtn.setOnMouseEntered(e ->
                closeBtn.setStyle(
                        "-fx-background-color: #475569;" +
                                "-fx-text-fill: #F1F5F9;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 5 12;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                )
        );
        closeBtn.setOnMouseExited(e ->
                closeBtn.setStyle(
                        "-fx-background-color: #334155;" +
                                "-fx-text-fill: #94A3B8;" +
                                "-fx-font-size: 16px;" +
                                "-fx-font-weight: bold;" +
                                "-fx-padding: 5 12;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                )
        );

        header.getChildren().addAll(titleLabel, spacer, closeBtn);

        // Conteneur pour le contenu dynamique (SCROLLABLE)
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        // Style de la barre de défilement
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-border-color: transparent;"
        );

        // Conteneur du contenu (sera mis à jour dynamiquement)
        VBox contentContainer = new VBox(20);
        contentContainer.setId("aiContentContainer");
        contentContainer.setStyle("-fx-padding: 0 0 20 0;");

        scrollPane.setContent(contentContainer);

        // Assemblage : HEADER fixe + SCROLLPANE
        panel.getChildren().addAll(header, new Separator(), scrollPane);

        // Ajuster la croissance du ScrollPane
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        return panel;
    }
    /**
     * Met à jour le contenu du panneau IA
     */
    private void updateAIPanelContent(Application app, AIScoreResult score) {
        VBox contentContainer = (VBox) aiDetailsPanel.lookup("#aiContentContainer");
        if (contentContainer == null) return;

        contentContainer.getChildren().clear();

        // ===== 1. SCORE GLOBAL AVEC JAUGE =====
        VBox globalScoreBox = new VBox(15);
        globalScoreBox.setAlignment(Pos.CENTER);
        globalScoreBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #1E293B, #0F172A);" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 25 20;" +
                        "-fx-border-color: rgba(99,102,241,0.3);" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 1;"
        );

        // Score avec cercle de progression
        StackPane scoreCircle = new StackPane();
        scoreCircle.setPrefSize(140, 140);

        // Cercle extérieur
        Circle outerCircle = new Circle(70);
        outerCircle.setFill(null);
        outerCircle.setStroke(Color.rgb(51, 65, 85));
        outerCircle.setStrokeWidth(8);

        // Cercle de progression
        Circle progressCircle = new Circle(70);
        progressCircle.setFill(null);
        outerCircle.setStroke(Color.web(getScoreColor(score.getOverallScore())));
        progressCircle.setStrokeWidth(8);
        progressCircle.setStrokeLineCap(StrokeLineCap.ROUND);

        // Calcul de l'angle pour le cercle de progression
        double percentage = score.getOverallScore() / 100.0;
        double angle = 360 * percentage;

        // Créer un arc pour représenter la progression
        Arc progressArc = new Arc(70, 70, 65, 65, 90, -angle);
        progressArc.setFill(null);
        progressArc.setStroke(Color.web(getScoreColor(score.getOverallScore())));
        progressArc.setStrokeWidth(8);
        progressArc.setStrokeLineCap(StrokeLineCap.ROUND);
        progressArc.setType(ArcType.OPEN);

        // Texte du score
        VBox scoreText = new VBox(0);
        scoreText.setAlignment(Pos.CENTER);

        Label scoreValue = new Label(String.format("%.0f", score.getOverallScore()));
        scoreValue.setStyle(
                "-fx-font-size: 42px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + getScoreColor(score.getOverallScore()) + ";"
        );

        Label scoreMax = new Label("/100");
        scoreMax.setStyle("-fx-font-size: 16px; -fx-text-fill: #94A3B8;");

        scoreText.getChildren().addAll(scoreValue, scoreMax);

        scoreCircle.getChildren().addAll(outerCircle, progressArc, scoreText);

        // Label de niveau
        Label levelLabel = new Label(getScoreLevel(score.getOverallScore()));
        levelLabel.setStyle(
                "-fx-font-size: 16px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: " + getScoreColor(score.getOverallScore()) + ";"
        );

        globalScoreBox.getChildren().addAll(scoreCircle, levelLabel);

        // ===== 2. SCORES DÉTAILLÉS AVEC BARRES DE PROGRESSION =====
        VBox detailedScoresBox = new VBox(15);
        detailedScoresBox.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;"
        );

        Label detailsTitle = new Label("📊 Scores détaillés");
        detailsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        detailedScoresBox.getChildren().add(detailsTitle);
        detailedScoresBox.getChildren().add(createProgressBarWithIcon("🎯 Compétences", score.getSkillsScore(), "💪"));
        detailedScoresBox.getChildren().add(createProgressBarWithIcon("⏱️ Expérience", score.getExperienceScore(), "📅"));
        detailedScoresBox.getChildren().add(createProgressBarWithIcon("🎓 Formation", score.getEducationScore(), "📚"));

        // ===== 3. ANALYSE DÉTAILLÉE =====
        VBox analysisBox = new VBox(15);
        analysisBox.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;"
        );

        Label analysisTitle = new Label("📝 Analyse détaillée");
        analysisTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        // Zone de texte stylisée pour l'analyse
        TextFlow analysisText = new TextFlow();
        analysisText.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 12; -fx-padding: 15;");

        String[] explanations = score.getExplanation().split("\n\n");
        for (String exp : explanations) {
            if (!exp.trim().isEmpty()) {
                Text text = new Text(exp + "\n\n");
                text.setStyle("-fx-fill: #E2E8F0; -fx-font-size: 13px;");
                analysisText.getChildren().add(text);
            }
        }

        // Ajouter un ScrollPane pour l'analyse si trop longue
        ScrollPane analysisScroll = new ScrollPane(analysisText);
        analysisScroll.setFitToWidth(true);
        analysisScroll.setPrefHeight(200);
        analysisScroll.setStyle(
                "-fx-background: #0F172A;" +
                        "-fx-background-color: #0F172A;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 12;"
        );

        analysisBox.getChildren().addAll(analysisTitle, analysisScroll);

        // ===== 4. INFORMATIONS CANDIDAT =====
        VBox candidateBox = new VBox(10);
        candidateBox.setStyle(
                "-fx-background-color: rgba(99,102,241,0.1);" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 15;" +
                        "-fx-border-color: rgba(99,102,241,0.3);" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        );

        Label candidateTitle = new Label("👤 Candidat");
        candidateTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #A5B4FC;");

        Label candidateName = new Label(app.getCandidateName());
        candidateName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        Label candidateEmail = new Label("✉ " + app.getCandidateEmail());
        candidateEmail.setStyle("-fx-font-size: 12px; -fx-text-fill: #94A3B8;");

        HBox actionsBox = new HBox(10);
        actionsBox.setAlignment(Pos.CENTER_RIGHT);
        actionsBox.setPadding(new Insets(10, 0, 0, 0));

        Button viewCVBtn = new Button("📄 Voir CV");
        viewCVBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #94A3B8;" +
                        "-fx-border-color: #475569;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 6 12;" +
                        "-fx-cursor: hand;"
        );
        viewCVBtn.setOnAction(e -> openCVFile(app.getCvFilePath()));

        Button recalculateBtn = new Button("⟳ Recalculer");
        recalculateBtn.setStyle(
                "-fx-background-color: #6366F1;" +
                        "-fx-text-fill: white;" +
                        "-fx-border-radius: 8;" +
                        "-fx-padding: 6 12;" +
                        "-fx-cursor: hand;" +
                        "-fx-font-weight: bold;"
        );
        recalculateBtn.setOnAction(e -> {
            hideAIPanel();
            // Déclencher un nouveau calcul
            Offer offer = offersList.stream()
                    .filter(o -> o.getId() == app.getOfferId())
                    .findFirst()
                    .orElse(null);
            if (offer != null) {
                showAIScore(app, offer);
            }
        });

        actionsBox.getChildren().addAll(viewCVBtn, recalculateBtn);

        candidateBox.getChildren().addAll(candidateTitle, candidateName, candidateEmail, actionsBox);

        // Assemblage final
        contentContainer.getChildren().addAll(
                globalScoreBox,
                detailedScoresBox,
                analysisBox,
                candidateBox
        );
    }

    /**
     * Crée une barre de progression avec icône et label
     */
    private VBox createProgressBarWithIcon(String label, double score, String icon) {
        VBox container = new VBox(8);

        HBox labelRow = new HBox();
        labelRow.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px; -fx-min-width: 30;");

        Label nameLabel = new Label(label);
        nameLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #94A3B8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label scoreLabel = new Label(String.format("%.0f%%", score));
        scoreLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " + getScoreColor(score) + ";");

        labelRow.getChildren().addAll(iconLabel, nameLabel, spacer, scoreLabel);

        // Barre de progression personnalisée
        StackPane progressBar = new StackPane();
        progressBar.setPrefHeight(8);
        progressBar.setStyle("-fx-background-color: #334155; -fx-background-radius: 4;");

        Region progress = new Region();
        progress.setStyle("-fx-background-color: " + getScoreColor(score) + "; -fx-background-radius: 4;");
        progress.setPrefWidth(score * 2.8); // 280px max * pourcentage
        progress.setMaxWidth(280);
        progress.setMinWidth(0);

        progressBar.getChildren().add(progress);
        StackPane.setAlignment(progress, Pos.CENTER_LEFT);

        container.getChildren().addAll(labelRow, progressBar);

        return container;
    }

    /**
     * Cache le panneau IA avec animation
     */
    private void hideAIPanel() {
        if (aiDetailsPanel != null && isAIPanelVisible) {
            animatePanelOut();
            isAIPanelVisible = false;
        }
    }

    /**
     * Animation d'entrée du panneau
     */
    private void animatePanelIn() {
        if (aiDetailsPanel == null) return;

        aiDetailsPanel.setTranslateX(400);

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(aiDetailsPanel.translateXProperty(), 0, Interpolator.EASE_BOTH)
                )
        );
        timeline.play();
    }

    /**
     * Animation de sortie du panneau
     */
    private void animatePanelOut() {
        if (aiDetailsPanel == null) return;

        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(aiDetailsPanel.translateXProperty(), 400, Interpolator.EASE_BOTH)
                )
        );
        timeline.setOnFinished(e -> aiDetailsPanel.setVisible(false));
        timeline.play();
    }

    /**
     * Obtient le niveau de score
     */
    private String getScoreLevel(double score) {
        if (score >= 85) return "🌟 Excellent";
        if (score >= 70) return "✅ Très bon";
        if (score >= 50) return "📊 Moyen";
        if (score >= 30) return "⚠️ Faible";
        return "❌ Insuffisant";
    }
    private void addScoreRow(GridPane grid, String label, double score, int row) {
        Label nameLabel = new Label(label + ":");
        nameLabel.setStyle("-fx-text-fill: #94A3B8;");

        Label scoreLabel = new Label(String.format("%.1f/100", score));
        scoreLabel.setStyle("-fx-text-fill: " + getScoreColor(score) + "; -fx-font-weight: bold;");

        grid.add(nameLabel, 0, row);
        grid.add(scoreLabel, 1, row);
    }

    private String getScoreColor(double score) {
        if (score >= 70) return "#10B981";
        if (score >= 50) return "#F59E0B";
        return "#EF4444";
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
        // Créer un dialogue personnalisé
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de la candidature");
        dialog.setHeaderText(null);

        // Style du dialogue
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.setPrefWidth(600);
        dialogPane.setPrefHeight(700);
        dialogPane.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 24;");

        dialogPane.getButtonTypes().add(ButtonType.CLOSE);

        // Conteneur principal avec ScrollPane
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #0F172A; -fx-background-color: #0F172A; -fx-border-color: transparent;");

        VBox container = new VBox(25);
        container.setStyle("-fx-padding: 30; -fx-background-color: #0F172A;");

        // ========== 1. EN-TÊTE AVEC BANNIÈRE GRADIENT ==========
        VBox headerBox = new VBox(20);
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, #1E293B, #0F172A);" +
                        "-fx-background-radius: 20;" +
                        "-fx-padding: 25;" +
                        "-fx-border-color: rgba(99,102,241,0.3);" +
                        "-fx-border-radius: 20;" +
                        "-fx-border-width: 1;"
        );

        // Avatar et infos principales
        HBox mainInfoRow = new HBox(20);
        mainInfoRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Grand avatar
        StackPane avatar = new StackPane();
        avatar.setStyle(
                "-fx-background-color: " + getAvatarColor(app.getStatus()) + ";" +
                        "-fx-background-radius: 50;" +
                        "-fx-min-width: 80;" +
                        "-fx-min-height: 80;" +
                        "-fx-max-width: 80;" +
                        "-fx-max-height: 80;" +
                        "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.5), 15, 0, 0, 5);"
        );

        String initials = getInitials(app.getCandidateName());
        Label initialsLabel = new Label(initials);
        initialsLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        avatar.getChildren().add(initialsLabel);

        // Infos candidat
        VBox candidateMainInfo = new VBox(8);

        Label nameLabel = new Label(app.getCandidateName());
        nameLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox statusRow = new HBox(15);
        statusRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label statusBadge = new Label(app.getStatus());
        statusBadge.setStyle(getStatusStyle(app.getStatus()) + "-fx-font-size: 13px; -fx-padding: 6 16;");

        Label dateLabel = new Label("Candidature du " + app.getApplicationDate().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");

        statusRow.getChildren().addAll(statusBadge, dateLabel);

        candidateMainInfo.getChildren().addAll(nameLabel, statusRow);

        mainInfoRow.getChildren().addAll(avatar, candidateMainInfo);

        headerBox.getChildren().add(mainInfoRow);

        // ========== 2. CARTE DE CONTACT ==========
        VBox contactCard = createInfoCard(
                "📞 Informations de contact",
                new String[][]{
                        {"✉ Email", app.getCandidateEmail()},
                        {"📞 Téléphone", app.getCandidatePhone() != null ? app.getCandidatePhone() : "Non renseigné"},
                        {"📅 Date de candidature", app.getApplicationDate().format(dateFormatter)}
                }
        );

        // ========== 3. CARTE DE L'OFFRE ==========
        VBox offerCard;
        if (offer != null) {
            offerCard = createInfoCard(
                    "📋 Offre postulée",
                    new String[][]{
                            {"🏢 Poste", offer.getTitle()},
                            {"📄 Département", offer.getDepartment()},
                            {"📑 Type de contrat", offer.getContractType()},
                            {"📊 Niveau", offer.getExperienceLevel()},
                            {"📍 Localisation", offer.getLocation()},
                            {"💰 Salaire", String.format("%.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax())}
                    }
            );
        } else {
            offerCard = createInfoCard(
                    "⚠️ Offre indisponible",
                    new String[][]{
                            {"❌ Statut", "Cette offre a été supprimée"},
                            {"🆔 ID", String.valueOf(app.getOfferId())}
                    }
            );
        }

        // ========== 4. CARTE D'ÉVALUATION ==========
        VBox evaluationCard = createInfoCard(
                "📊 Évaluation",
                new String[][]{
                        {"⭐ Score", app.getScore() > 0 ? String.format("%.1f/100", app.getScore()) : "Non évalué"},
                        {"👤 Interviewer", app.getInterviewer() != null ? app.getInterviewer() : "Non assigné"},
                        {"📅 Date interview", app.getInterviewDate() != null ? app.getInterviewDate().format(dateFormatter) : "Non planifiée"},
                        {"📝 Résultat", app.getInterviewResult() != null ? app.getInterviewResult() : "En attente"}
                }
        );

        // ========== 5. NOTES ET LETTRE DE MOTIVATION ==========
        VBox notesCard = new VBox(15);
        notesCard.setStyle(
                "-fx-background-color: rgba(255,255,255,0.03);" +
                        "-fx-background-radius: 16;" +
                        "-fx-padding: 20;" +
                        "-fx-border-color: rgba(99,102,241,0.15);" +
                        "-fx-border-radius: 16;" +
                        "-fx-border-width: 1;"
        );

        // Titre de la section
        Label notesTitle = new Label("📝 Notes d'évaluation");
        notesTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

        TextArea notesArea = new TextArea(app.getNotes() != null ? app.getNotes() : "Aucune note");
        notesArea.setWrapText(true);
        notesArea.setEditable(false);
        notesArea.setPrefRowCount(4);
        notesArea.setStyle(
                "-fx-background-color: #1E293B;" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #334155;" +
                        "-fx-border-radius: 12;" +
                        "-fx-text-fill: #1E293B;" +
                        "-fx-font-size: 13px;"
        );

        notesCard.getChildren().addAll(notesTitle, notesArea);

        // ========== 6. LETTRE DE MOTIVATION ==========
        if (app.getMotivationLetter() != null && !app.getMotivationLetter().isEmpty()) {
            VBox motivationCard = new VBox(15);
            motivationCard.setStyle(
                    "-fx-background-color: rgba(255,255,255,0.03);" +
                            "-fx-background-radius: 16;" +
                            "-fx-padding: 20;" +
                            "-fx-border-color: rgba(99,102,241,0.15);" +
                            "-fx-border-radius: 16;" +
                            "-fx-border-width: 1;"
            );

            Label motivationTitle = new Label("💌 Lettre de motivation");
            motivationTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #F1F5F9;");

            TextArea motivationArea = new TextArea(app.getMotivationLetter());
            motivationArea.setWrapText(true);
            motivationArea.setEditable(false);
            motivationArea.setPrefRowCount(6);
            motivationArea.setStyle(
                    "-fx-background-color: #1E293B;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-color: #334155;" +
                            "-fx-border-radius: 12;" +
                            "-fx-text-fill: #1E293B;" +
                            "-fx-font-size: 13px;"
            );

            motivationCard.getChildren().addAll(motivationTitle, motivationArea);
            container.getChildren().add(motivationCard);
        }

        // ========== 7. BOUTON VOIR CV ==========
        if (app.getCvFilePath() != null && !app.getCvFilePath().isEmpty()) {
            Button viewCVBtn = new Button("📄 Voir le CV");
            viewCVBtn.setStyle(
                    "-fx-background-color: #6366F1;" +
                            "-fx-text-fill: white;" +
                            "-fx-font-size: 14px;" +
                            "-fx-font-weight: bold;" +
                            "-fx-padding: 12 20;" +
                            "-fx-background-radius: 12;" +
                            "-fx-cursor: hand;" +
                            "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.3), 10, 0, 0, 2);"
            );
            viewCVBtn.setMaxWidth(Double.MAX_VALUE);

            viewCVBtn.setOnMouseEntered(e ->
                    viewCVBtn.setStyle(
                            "-fx-background-color: #4F46E5;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-background-radius: 12;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.5), 15, 0, 0, 4);"
                    )
            );

            viewCVBtn.setOnMouseExited(e ->
                    viewCVBtn.setStyle(
                            "-fx-background-color: #6366F1;" +
                                    "-fx-text-fill: white;" +
                                    "-fx-font-size: 14px;" +
                                    "-fx-font-weight: bold;" +
                                    "-fx-padding: 12 20;" +
                                    "-fx-background-radius: 12;" +
                                    "-fx-cursor: hand;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.3), 10, 0, 0, 2);"
                    )
            );

            viewCVBtn.setOnAction(e -> openCVFile(app.getCvFilePath()));
            container.getChildren().add(viewCVBtn);
        }

        // Assemblage
        container.getChildren().addAll(
                headerBox,
                contactCard,
                offerCard,
                evaluationCard,
                notesCard
        );

        // Si lettre de motivation existe, elle est déjà ajoutée plus haut

        scrollPane.setContent(container);
        dialogPane.setContent(scrollPane);

        // Style du bouton Fermer
        Button closeButton = (Button) dialogPane.lookupButton(ButtonType.CLOSE);
        closeButton.setStyle(
                "-fx-background-color: #475569;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 8 20;" +
                        "-fx-background-radius: 8;"
        );

        dialog.showAndWait();
    }

    /**
     * Crée une carte d'information élégante
     */
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
            keyLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #94A3B8; -fx-min-width: 100;");

            // Valeur (colonne 1)
            Label valueLabel = new Label(row[1]);
            valueLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #F1F5F9; -fx-wrap-text: true;");

            infoGrid.add(keyLabel, 0, i);
            infoGrid.add(valueLabel, 1, i);
        }

        card.getChildren().addAll(titleLabel, infoGrid);

        return card;
    }

    /**
     * Ouvre le fichier CV
     */
    private void openCVFile(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                java.awt.Desktop.getDesktop().open(file);
            } else {
                showAlert("Erreur", "Fichier CV non trouvé", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Erreur", "Impossible d'ouvrir le fichier: " + e.getMessage(), Alert.AlertType.ERROR);
        }
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
