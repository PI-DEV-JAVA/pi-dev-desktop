package talentospidev.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import talentospidev.models.AIScoreResult;
import talentospidev.models.CandidateMatch;
import talentospidev.models.Offer;
import talentospidev.models.User;
import talentospidev.services.*;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class OfferDetailsController {

    @FXML
    private VBox detailsContainer;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final OfferService offerService = new OfferService();
    private final ApplicationService applicationService = new ApplicationService();
    private final BookmarkService bookmarkService = new BookmarkService();
    private final CandidateMatchingService matchingService = new CandidateMatchingService();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        int offerId = ViewContext.getSelectedOfferId();
        if (offerId <= 0) {
            showError("Aucune offre sélectionnée.");
            return;
        }
        Offer offer = offerService.getOfferById(offerId);
        if (offer == null) {
            showError("Offre introuvable.");
            return;
        }
        buildDetailsView(offer);
    }

    private void buildDetailsView(Offer offer) {
        detailsContainer.getChildren().clear();

        User currentUser = AuthService.getCurrentUser();
        boolean isCandidate = currentUser != null && currentUser.getRole() == User.Role.CANDIDATE;
        boolean isRecruiter = currentUser != null
                && (currentUser.getRole() == User.Role.HR || currentUser.getRole() == User.Role.ADMIN);

        // === Back Button ===
        Button backBtn = new Button("← Retour aux offres");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #6366f1; -fx-font-size: 14px; " +
                "-fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 0;");
        backBtn.setOnAction(e -> handleJobOffers());

        // === Title Card ===
        VBox headerCard = new VBox(12);
        headerCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        HBox titleRow = new HBox(16);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label(offer.getTitle());
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        title.setWrapText(true);
        HBox.setHgrow(title, Priority.ALWAYS);

        Label statusBadge = new Label(offer.getStatus());
        statusBadge.setStyle(getStatusStyle(offer.getStatus()));

        titleRow.getChildren().addAll(title, statusBadge);

        // Bookmark button for candidates
        if (isCandidate) {
            boolean isBookmarked = bookmarkService.isBookmarked(currentUser.getId(), offer.getId());
            Button bmkBtn = new Button(isBookmarked ? "🔖 Bookmarked" : "🏷 Bookmark");
            String bmkActive = "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-padding: 8 20; " +
                    "-fx-background-radius: 10; -fx-font-size: 13px; -fx-font-weight: 700; -fx-cursor: hand;";
            String bmkInactive = "-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-padding: 8 20; " +
                    "-fx-background-radius: 10; -fx-font-size: 13px; -fx-font-weight: 700; -fx-cursor: hand;";
            bmkBtn.setStyle(isBookmarked ? bmkActive : bmkInactive);
            bmkBtn.setOnAction(e -> {
                boolean now = bookmarkService.toggleBookmark(currentUser.getId(), offer.getId());
                bmkBtn.setText(now ? "🔖 Bookmarked" : "🏷 Bookmark");
                bmkBtn.setStyle(now ? bmkActive : bmkInactive);
            });
            titleRow.getChildren().add(bmkBtn);
        }

        Label subtitle = new Label(
                "🏢 " + offer.getDepartment() + "  •  " + offer.getContractType() + "  •  📍 " + offer.getLocation());
        subtitle.setStyle("-fx-font-size: 15px; -fx-text-fill: #6b7280;");

        headerCard.getChildren().addAll(titleRow, subtitle);

        // === Info Grid ===
        VBox infoCard = new VBox(18);
        infoCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        Label infoTitle = new Label("Détails de l'offre");
        infoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        GridPane grid = new GridPane();
        grid.setHgap(40);
        grid.setVgap(16);
        addInfoRow(grid, "💰 Salaire", String.format("%.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()), 0,
                "#22c55e");
        addInfoRow(grid, "📊 Expérience", offer.getExperienceLevel(), 1, "#6366f1");
        addInfoRow(grid, "📅 Date de publication", offer.getPublishDate().format(dateFormatter), 2, "#3b82f6");
        addInfoRow(grid, "📅 Date de clôture", offer.getClosingDate().format(dateFormatter), 3, "#ef4444");
        long daysLeft = ChronoUnit.DAYS.between(java.time.LocalDate.now(), offer.getClosingDate());
        addInfoRow(grid, "⏳ Délai", daysLeft > 0 ? daysLeft + " jours restants" : "Expiré", 4,
                daysLeft > 0 ? "#f59e0b" : "#ef4444");
        addInfoRow(grid, "👥 Postes disponibles", String.valueOf(offer.getPositionsAvailable()), 5, "#8b5cf6");
        addInfoRow(grid, "📬 Candidatures reçues", String.valueOf(offer.getApplicationsReceived()), 6, "#06b6d4");
        infoCard.getChildren().addAll(infoTitle, grid);

        // === Description ===
        VBox descCard = new VBox(12);
        descCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");
        Label descTitle = new Label("Description du poste");
        descTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        Text descText = new Text(
                offer.getDescription() != null ? offer.getDescription() : "Aucune description disponible.");
        descText.setStyle("-fx-font-size: 14px; -fx-fill: #374151;");
        TextFlow descFlow = new TextFlow(descText);
        descFlow.setLineSpacing(4);
        descCard.getChildren().addAll(descTitle, descFlow);

        // === Action Buttons ===
        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        // Candidate: Apply button
        if (isCandidate && "Ouverte".equals(offer.getStatus())) {
            boolean alreadyApplied = applicationService.hasUserApplied(currentUser.getId(), offer.getId());
            if (alreadyApplied) {
                Button appliedBtn = new Button("✅  Déjà postulé");
                appliedBtn.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #16a34a; -fx-font-size: 15px; " +
                        "-fx-font-weight: 700; -fx-padding: 14 36; -fx-background-radius: 12; -fx-border-color: #86efac; -fx-border-radius: 12;");
                appliedBtn.setDisable(true);
                appliedBtn.setOpacity(0.85);
                actions.getChildren().add(appliedBtn);
            } else {
                Button applyBtn = new Button("📤  Postuler maintenant");
                applyBtn.setStyle("-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); " +
                        "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; " +
                        "-fx-padding: 14 36; -fx-background-radius: 12; -fx-cursor: hand; " +
                        "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.35), 12, 0, 0, 4);");
                applyBtn.setOnAction(e -> {
                    ViewContext.setSelectedOfferId(offer.getId());
                    SceneUtil.switchScene("apply_offer.fxml");
                });
                actions.getChildren().add(applyBtn);
            }
        }

        // Recruiter: AI Analyze Candidates button
        if (isRecruiter) {
            Button analyzeBtn = new Button("🤖  Analyze Candidates");
            analyzeBtn.setStyle("-fx-background-color: linear-gradient(to right, #f59e0b, #f97316); " +
                    "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; " +
                    "-fx-padding: 14 36; -fx-background-radius: 12; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.35), 12, 0, 0, 4);");
            analyzeBtn.setOnAction(e -> runAIAnalysis(offer));
            actions.getChildren().add(analyzeBtn);
        }

        detailsContainer.getChildren().addAll(backBtn, headerCard, infoCard, descCard, actions);
    }

    // ===== AI Analysis =====

    private void runAIAnalysis(Offer offer) {
        // Add loading indicator
        VBox loadingCard = new VBox(16);
        loadingCard.setAlignment(Pos.CENTER);
        loadingCard.setPadding(new Insets(32));
        loadingCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 32; " +
                "-fx-border-color: #fbbf24; -fx-border-radius: 16; -fx-border-width: 2;");

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(48, 48);

        Label loadingLabel = new Label("🤖 AI is analyzing candidates...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #f59e0b;");

        Label subLabel = new Label("This may take up to 30 seconds. Scoring CVs against the job description.");
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");

        loadingCard.getChildren().addAll(spinner, loadingLabel, subLabel);
        detailsContainer.getChildren().add(loadingCard);

        // Run analysis in background thread
        Task<List<CandidateMatch>> task = new Task<>() {
            @Override
            protected List<CandidateMatch> call() throws Exception {
                return matchingService.findBestCandidatesForOffer(offer, 10);
            }
        };

        task.setOnSucceeded(e -> {
            detailsContainer.getChildren().remove(loadingCard);
            List<CandidateMatch> matches = task.getValue();
            displayAnalysisResults(matches, offer);
        });

        task.setOnFailed(e -> {
            detailsContainer.getChildren().remove(loadingCard);
            VBox errorCard = new VBox(8);
            errorCard.setPadding(new Insets(20));
            errorCard.setStyle("-fx-background-color: #fef2f2; -fx-background-radius: 12; -fx-padding: 20;");
            Label errLabel = new Label("❌ Analysis failed: " + task.getException().getMessage());
            errLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 13px;");
            errLabel.setWrapText(true);
            errorCard.getChildren().add(errLabel);
            detailsContainer.getChildren().add(errorCard);
        });

        new Thread(task).start();
    }

    private void displayAnalysisResults(List<CandidateMatch> matches, Offer offer) {
        VBox resultsCard = new VBox(16);
        resultsCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 28; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 2);");

        HBox resultsHeader = new HBox(12);
        resultsHeader.setAlignment(Pos.CENTER_LEFT);
        Label rTitle = new Label("🎯 AI Candidate Ranking");
        rTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Label rCount = new Label(matches.size() + " candidate(s) scored");
        rCount.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        resultsHeader.getChildren().addAll(rTitle, spacer, rCount);

        resultsCard.getChildren().add(resultsHeader);

        if (matches.isEmpty()) {
            Label noResults = new Label("No candidates with CV files found for this offer.");
            noResults.setStyle("-fx-font-size: 14px; -fx-text-fill: #9ca3af; -fx-padding: 20;");
            resultsCard.getChildren().add(noResults);
        }

        int rank = 1;
        for (CandidateMatch match : matches) {
            AIScoreResult score = match.getScore();
            VBox row = new VBox(8);
            row.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 12; -fx-padding: 16; " +
                    "-fx-border-color: #e5e7eb; -fx-border-radius: 12;");

            // Rank + Name row
            HBox nameRow = new HBox(12);
            nameRow.setAlignment(Pos.CENTER_LEFT);

            String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "#" + rank;
            Label rankLabel = new Label(medal);
            rankLabel.setStyle("-fx-font-size: 20px;");

            Label nameLabel = new Label("Application #" + match.getApplication().getId());
            nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #111827;");
            HBox.setHgrow(nameLabel, Priority.ALWAYS);

            Label overallLabel = new Label(String.format("%.0f%%", score.getOverallScore()));
            String scoreColor = score.getOverallScore() >= 70 ? "#22c55e"
                    : score.getOverallScore() >= 40 ? "#f59e0b" : "#ef4444";
            overallLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: " + scoreColor + ";");

            nameRow.getChildren().addAll(rankLabel, nameLabel, overallLabel);

            // Score breakdown
            HBox scores = new HBox(20);
            scores.setAlignment(Pos.CENTER_LEFT);
            scores.getChildren().addAll(
                    createScorePill("Skills", score.getSkillsScore(), "#6366f1"),
                    createScorePill("Experience", score.getExperienceScore(), "#8b5cf6"),
                    createScorePill("Education", score.getEducationScore(), "#06b6d4"));

            row.getChildren().addAll(nameRow, scores);
            resultsCard.getChildren().add(row);
            rank++;
        }

        detailsContainer.getChildren().add(resultsCard);
    }

    private VBox createScorePill(String label, double score, String color) {
        VBox pill = new VBox(2);
        pill.setAlignment(Pos.CENTER);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");

        ProgressBar bar = new ProgressBar(score / 100.0);
        bar.setPrefWidth(80);
        bar.setPrefHeight(6);
        bar.setStyle("-fx-accent: " + color + ";");

        Label val = new Label(String.format("%.0f%%", score));
        val.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");

        pill.getChildren().addAll(lbl, bar, val);
        return pill;
    }

    // ===== Helpers =====

    private void addInfoRow(GridPane grid, String label, String value, int row, String color) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-text-fill: " + color + "; -fx-font-weight: 700;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "Ouverte":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "Fermée":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 6 16; -fx-background-radius: 20; -fx-font-size: 12px; -fx-font-weight: bold;";
        }
    }

    private void showError(String msg) {
        Label err = new Label("⚠ " + msg);
        err.setStyle("-fx-font-size: 16px; -fx-text-fill: #ef4444; -fx-padding: 40;");
        detailsContainer.getChildren().add(err);
    }

    // === Sidebar Navigation ===
    @FXML
    private void handleDashboard() {
        User u = AuthService.getCurrentUser();
        if (u != null && u.getRole() == User.Role.HR)
            SceneUtil.switchScene("recruiter_dashboard.fxml");
        else if (u != null && u.getRole() == User.Role.ADMIN)
            SceneUtil.switchScene("admin_dashboard.fxml");
        else
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
        new Alert(Alert.AlertType.INFORMATION, "Fonctionnalité bientôt disponible !").showAndWait();
    }

    @FXML
    private void handleSettings() {
        SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handleMyCircle() {
        SceneUtil.switchScene("my_circle.fxml");
    }

    @FXML
    private void handleNotifications() {
        SceneUtil.switchScene("notifications.fxml");
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
