package talentospidev.controllers;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import talentospidev.dao.ProfileDao;
import talentospidev.models.*;
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
    private final ProfileDao profileDao = new ProfileDao();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final String INDIGO = "#6366f1";
    private static final String GREEN = "#22c55e";
    private static final String RED = "#ef4444";
    private static final String AMBER = "#f59e0b";

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

        // ──── Back Button ────
        Button backBtn = new Button("← Retour aux offres");
        backBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + INDIGO + "; -fx-font-size: 14px; " +
                "-fx-font-weight: 600; -fx-cursor: hand; -fx-padding: 0;");
        backBtn.setOnAction(e -> handleJobOffers());

        // ──── Header Card ────
        VBox headerCard = createSectionCard();
        String accentColor = getDepartmentColor(offer.getDepartment());

        HBox accentBar = new HBox();
        accentBar.setPrefHeight(5);
        accentBar.setMinHeight(5);
        accentBar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 16 16 0 0;");

        VBox headerContent = new VBox(12);
        headerContent.setPadding(new Insets(24, 28, 24, 28));

        HBox titleRow = new HBox(12);
        titleRow.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label(offer.getTitle());
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        title.setWrapText(true);
        HBox.setHgrow(title, Priority.ALWAYS);
        Label statusBadge = new Label(offer.getStatus());
        statusBadge.setStyle(getStatusPillStyle(offer.getStatus()));
        titleRow.getChildren().addAll(title, statusBadge);

        // Bookmark for candidates
        if (isCandidate) {
            boolean bm = bookmarkService.isBookmarked(currentUser.getId(), offer.getId());
            Button bmkBtn = new Button(bm ? "★ Saved" : "☆ Save");
            String bmkA = "-fx-background-color: " + AMBER + "; -fx-text-fill: white; -fx-padding: 8 18; " +
                    "-fx-background-radius: 10; -fx-font-size: 13px; -fx-font-weight: 700; -fx-cursor: hand;";
            String bmkI = "-fx-background-color: #fef3c7; -fx-text-fill: " + AMBER + "; -fx-padding: 8 18; " +
                    "-fx-background-radius: 10; -fx-font-size: 13px; -fx-font-weight: 700; -fx-cursor: hand;";
            bmkBtn.setStyle(bm ? bmkA : bmkI);
            bmkBtn.setOnAction(e -> {
                boolean now = bookmarkService.toggleBookmark(currentUser.getId(), offer.getId());
                bmkBtn.setText(now ? "★ Saved" : "☆ Save");
                bmkBtn.setStyle(now ? bmkA : bmkI);
            });
            titleRow.getChildren().add(bmkBtn);
        }

        // Subtitle
        HBox subtitleRow = new HBox(16);
        subtitleRow.setAlignment(Pos.CENTER_LEFT);
        subtitleRow.getChildren().addAll(
                createTag(getDeptIcon(offer.getDepartment()) + " " + offer.getDepartment(), accentColor),
                createTag("📝 " + offer.getContractType(), "#3b82f6"),
                createTag("◎ " + offer.getLocation(), "#8b5cf6"));

        headerContent.getChildren().addAll(titleRow, subtitleRow);
        headerCard.getChildren().addAll(accentBar, headerContent);

        // ──── Info Grid Card ────
        VBox infoCard = createSectionCard();
        VBox infoContent = new VBox(16);
        infoContent.setPadding(new Insets(24, 28, 24, 28));

        Label infoTitle = new Label("Offer Details");
        infoTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #111827;");

        // Two-column grid layout
        GridPane grid = new GridPane();
        grid.setHgap(60);
        grid.setVgap(14);

        addDetailRow(grid, "Salary", String.format("%.0f – %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()), 0,
                GREEN);
        addDetailRow(grid, "Experience", offer.getExperienceLevel(), 1, INDIGO);
        addDetailRow(grid, "Published", offer.getPublishDate().format(dateFormatter), 2, "#3b82f6");
        addDetailRow(grid, "Closes", offer.getClosingDate().format(dateFormatter), 3, RED);
        long daysLeft = ChronoUnit.DAYS.between(java.time.LocalDate.now(), offer.getClosingDate());
        addDetailRow(grid, "Remaining", daysLeft > 0 ? daysLeft + " days" : "Expired", 4, daysLeft > 0 ? AMBER : RED);
        addDetailRow(grid, "Positions", String.valueOf(offer.getPositionsAvailable()), 5, "#8b5cf6");
        addDetailRow(grid, "Applications", String.valueOf(offer.getApplicationsReceived()), 6, "#06b6d4");

        infoContent.getChildren().addAll(infoTitle, grid);
        infoCard.getChildren().add(infoContent);

        // ──── Description Card ────
        VBox descCard = createSectionCard();
        VBox descContent = new VBox(12);
        descContent.setPadding(new Insets(24, 28, 24, 28));
        Label descTitle = new Label("Job Description");
        descTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Text descText = new Text(offer.getDescription() != null ? offer.getDescription() : "No description available.");
        descText.setStyle("-fx-font-size: 14px; -fx-fill: #374151;");
        TextFlow descFlow = new TextFlow(descText);
        descFlow.setLineSpacing(6);
        descContent.getChildren().addAll(descTitle, descFlow);
        descCard.getChildren().add(descContent);

        // ──── Action Buttons ────
        HBox actions = new HBox(14);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(8, 0, 0, 0));

        if (isCandidate && "Ouverte".equals(offer.getStatus())) {
            boolean alreadyApplied = applicationService.hasUserApplied(currentUser.getId(), offer.getId());
            if (alreadyApplied) {
                Label appliedTag = new Label("✓ Already Applied");
                appliedTag.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 12 28; " +
                        "-fx-background-radius: 12; -fx-font-size: 14px; -fx-font-weight: 700;");
                actions.getChildren().add(appliedTag);
            } else {
                Button applyBtn = new Button("Apply Now →");
                applyBtn.setStyle("-fx-background-color: linear-gradient(to right, " + INDIGO + ", #8b5cf6); " +
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

        // Recruiter: AI Analyze button
        if (isRecruiter) {
            Button analyzeBtn = new Button("⚡ Analyze Candidates");
            analyzeBtn.setStyle("-fx-background-color: linear-gradient(to right, " + AMBER + ", #f97316); " +
                    "-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: 700; " +
                    "-fx-padding: 14 32; -fx-background-radius: 12; -fx-cursor: hand; " +
                    "-fx-effect: dropshadow(gaussian, rgba(245,158,11,0.35), 12, 0, 0, 4);");
            analyzeBtn.setOnAction(e -> runAIAnalysis(offer, analyzeBtn));
            actions.getChildren().add(analyzeBtn);

            // Show applicant count hint
            int appCount = applicationService.getApplicationsCountByOffer(offer.getId());
            Label hint = new Label(appCount + " applicant" + (appCount != 1 ? "s" : "") + " will be scored");
            hint.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af; -fx-padding: 0 0 0 8;");
            actions.getChildren().add(hint);
        }

        detailsContainer.getChildren().addAll(backBtn, headerCard, infoCard, descCard, actions);
    }

    // ===== AI Analysis for Recruiter =====

    private void runAIAnalysis(Offer offer, Button analyzeBtn) {
        analyzeBtn.setDisable(true);
        analyzeBtn.setText("⏳ Analyzing...");

        VBox loadingCard = createSectionCard();
        VBox loadingContent = new VBox(16);
        loadingContent.setAlignment(Pos.CENTER);
        loadingContent.setPadding(new Insets(32));

        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setPrefSize(48, 48);
        Label loadingLabel = new Label("⚡ AI is scoring applicants for this offer...");
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: " + AMBER + ";");
        Label subLabel = new Label("Only applicants who submitted a CV to this offer will be scored.");
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9ca3af;");
        loadingContent.getChildren().addAll(spinner, loadingLabel, subLabel);
        loadingCard.getChildren().add(loadingContent);
        detailsContainer.getChildren().add(loadingCard);

        Task<List<CandidateMatch>> task = new Task<>() {
            @Override
            protected List<CandidateMatch> call() throws Exception {
                return matchingService.findBestCandidatesForOffer(offer, 20);
            }
        };

        task.setOnSucceeded(e -> {
            detailsContainer.getChildren().remove(loadingCard);
            analyzeBtn.setDisable(false);
            analyzeBtn.setText("⚡ Re-Analyze");
            displayAnalysisResults(task.getValue(), offer);
        });

        task.setOnFailed(e -> {
            detailsContainer.getChildren().remove(loadingCard);
            analyzeBtn.setDisable(false);
            analyzeBtn.setText("⚡ Retry Analysis");
            VBox errorCard = createSectionCard();
            VBox errContent = new VBox(8);
            errContent.setPadding(new Insets(20));
            Label errLabel = new Label("✗ Analysis failed: " + task.getException().getMessage());
            errLabel.setStyle("-fx-text-fill: " + RED + "; -fx-font-size: 13px;");
            errLabel.setWrapText(true);
            errContent.getChildren().add(errLabel);
            errorCard.getChildren().add(errContent);
            detailsContainer.getChildren().add(errorCard);
        });

        new Thread(task).start();
    }

    private void displayAnalysisResults(List<CandidateMatch> matches, Offer offer) {
        VBox resultsCard = createSectionCard();
        VBox resultsContent = new VBox(16);
        resultsContent.setPadding(new Insets(24, 28, 24, 28));

        // Header
        HBox headerRow = new HBox(12);
        headerRow.setAlignment(Pos.CENTER_LEFT);
        Label rTitle = new Label("⚡ AI Candidate Ranking");
        rTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label countBadge = new Label(matches.size() + " scored");
        countBadge.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: " + INDIGO + "; -fx-padding: 4 12; " +
                "-fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700;");
        headerRow.getChildren().addAll(rTitle, sp, countBadge);
        resultsContent.getChildren().add(headerRow);

        if (matches.isEmpty()) {
            Label noRes = new Label(
                    "No applicants with CVs found for this offer. Candidates need to upload a CV when applying.");
            noRes.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-padding: 20;");
            noRes.setWrapText(true);
            resultsContent.getChildren().add(noRes);
        }

        int rank = 1;
        for (CandidateMatch match : matches) {
            resultsContent.getChildren().add(buildCandidateRow(match, rank));
            rank++;
        }

        resultsCard.getChildren().add(resultsContent);
        detailsContainer.getChildren().add(resultsCard);
    }

    private VBox buildCandidateRow(CandidateMatch match, int rank) {
        AIScoreResult score = match.getScore();
        Application app = match.getApplication();
        VBox row = new VBox(10);

        String borderColor = rank == 1 ? AMBER : "#e5e7eb";
        String bgColor = rank == 1 ? "#fffbeb" : "#f9fafb";
        row.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-padding: 16; " +
                "-fx-border-color: " + borderColor + "; -fx-border-radius: 12; -fx-border-width: "
                + (rank <= 3 ? "2" : "1") + ";");

        // Top row: Rank + Name + Score
        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "";
        String rankText = medal.isEmpty() ? "#" + rank : medal;
        Label rankLabel = new Label(rankText);
        rankLabel.setStyle("-fx-font-size: " + (medal.isEmpty() ? "14" : "20")
                + "px; -fx-font-weight: 800; -fx-text-fill: #374151;");

        // Fetch candidate profile name
        Profile profile = profileDao.findByUserId(app.getUserId());
        String candidateName = "Candidate #" + app.getUserId();
        String candidateTitle = "";
        if (profile != null) {
            if (profile.getFirstName() != null && profile.getLastName() != null) {
                candidateName = profile.getFirstName() + " " + profile.getLastName();
            }
            if (profile.getProfessionalTitle() != null) {
                candidateTitle = profile.getProfessionalTitle();
            }
        }

        VBox nameBox = new VBox(2);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label nameLabel = new Label(candidateName);
        nameLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        nameBox.getChildren().add(nameLabel);
        if (!candidateTitle.isEmpty()) {
            Label titleLabel = new Label(candidateTitle);
            titleLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
            nameBox.getChildren().add(titleLabel);
        }

        // Status badge
        Label statusTag = new Label(app.getStatus());
        statusTag.setStyle(getAppStatusStyle(app.getStatus()));

        // Overall score
        String matchColor = score.getOverallScore() >= 70 ? GREEN : score.getOverallScore() >= 40 ? AMBER : RED;
        VBox scoreBox = new VBox(0);
        scoreBox.setAlignment(Pos.CENTER);
        Label overallLabel = new Label(String.format("%.0f", score.getOverallScore()));
        overallLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: " + matchColor + ";");
        Label pctLabel = new Label("match");
        pctLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
        scoreBox.getChildren().addAll(overallLabel, pctLabel);

        topRow.getChildren().addAll(rankLabel, nameBox, statusTag, scoreBox);

        // Score breakdown bars
        HBox barsRow = new HBox(24);
        barsRow.setAlignment(Pos.CENTER_LEFT);
        barsRow.setPadding(new Insets(4, 0, 0, 0));
        barsRow.getChildren().addAll(
                createScoreBar("Skills", score.getSkillsScore(), INDIGO),
                createScoreBar("Experience", score.getExperienceScore(), "#8b5cf6"),
                createScoreBar("Education", score.getEducationScore(), "#06b6d4"));

        // Application info
        HBox metaRow = new HBox(16);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        if (app.getApplicationDate() != null) {
            Label dateLabel = new Label("Applied: " + app.getApplicationDate().format(dateFormatter));
            dateLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #d1d5db;");
            metaRow.getChildren().add(dateLabel);
        }
        if (app.getCvFilePath() != null) {
            Label cvLabel = new Label("CV: ✓");
            cvLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: " + GREEN + "; -fx-font-weight: 600;");
            metaRow.getChildren().add(cvLabel);
        }

        row.getChildren().addAll(topRow, barsRow);
        if (!metaRow.getChildren().isEmpty())
            row.getChildren().add(metaRow);
        return row;
    }

    // ===== UI Helpers =====

    private VBox createSectionCard() {
        VBox card = new VBox();
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3);");
        return card;
    }

    private Label createTag(String text, String color) {
        Label tag = new Label(text);
        tag.setStyle(
                "-fx-background-color: derive(" + color + ", 90%); -fx-text-fill: " + color + "; -fx-padding: 4 12; " +
                        "-fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 600;");
        return tag;
    }

    private void addDetailRow(GridPane grid, String label, String value, int row, String color) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 13px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-text-fill: " + color + "; -fx-font-weight: 700;");
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private HBox createScoreBar(String label, double score, String color) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af; -fx-min-width: 65;");
        ProgressBar bar = new ProgressBar(score / 100.0);
        bar.setPrefWidth(90);
        bar.setPrefHeight(6);
        bar.setStyle("-fx-accent: " + color + ";");
        Label val = new Label(String.format("%.0f%%", score));
        val.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        row.getChildren().addAll(lbl, bar, val);
        return row;
    }

    private String getStatusPillStyle(String status) {
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

    private String getAppStatusStyle(String status) {
        if (status == null)
            status = "";
        switch (status) {
            case "Acceptée":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 3 10; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: 700;";
            case "Refusée":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 3 10; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: 700;";
            default:
                return "-fx-background-color: #eef2ff; -fx-text-fill: " + INDIGO
                        + "; -fx-padding: 3 10; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: 700;";
        }
    }

    private String getDepartmentColor(String dept) {
        if (dept == null)
            return INDIGO;
        switch (dept) {
            case "IT":
                return "#6366f1";
            case "Finance":
                return "#22c55e";
            case "Marketing":
                return "#f97316";
            case "RH":
                return "#ec4899";
            case "Commercial":
                return "#3b82f6";
            case "Logistique":
                return "#8b5cf6";
            default:
                return "#6b7280";
        }
    }

    private String getDeptIcon(String dept) {
        if (dept == null)
            return "●";
        switch (dept) {
            case "IT":
                return "⟨/⟩";
            case "Finance":
                return "◆";
            case "Marketing":
                return "◉";
            case "RH":
                return "◈";
            case "Commercial":
                return "▲";
            case "Logistique":
                return "■";
            default:
                return "●";
        }
    }

    private void showError(String msg) {
        Label err = new Label("⚠ " + msg);
        err.setStyle("-fx-font-size: 16px; -fx-text-fill: " + RED + "; -fx-padding: 40;");
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
    @FXML private void handleTrends() { talentospidev.utils.SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews() { talentospidev.utils.SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @javafx.fxml.FXML
    private void handleEvents() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Events/EventsFeed.fxml" : "Events/EventsBrowse.fxml");
    }

    @javafx.fxml.FXML
    private void handleCourses() {
        talentospidev.models.User u = talentospidev.services.AuthService.getCurrentUser();
        boolean isRecruiter = u != null && (u.getRole() == talentospidev.models.User.Role.HR || u.getRole() == talentospidev.models.User.Role.ADMIN);
        talentospidev.utils.SceneUtil.switchScene(isRecruiter ? "Courses/CoursesRH.fxml" : "Courses/CoursesBrowse.fxml");
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
