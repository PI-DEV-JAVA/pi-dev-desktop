package talentospidev.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import talentospidev.dao.ProfileDao;
import talentospidev.models.AIScoreResult;
import talentospidev.models.Offer;
import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.services.*;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Controller for browsing job offers — includes bookmarks, multi-select AI
 * compare.
 */
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
    @FXML
    private Button bookmarksToggle;
    @FXML
    private Button compareBtn;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;

    private final OfferService offerService = new OfferService();
    private final ApplicationService applicationService = new ApplicationService();
    private final BookmarkService bookmarkService = new BookmarkService();
    private final AIScoringService aiScoringService = new AIScoringService();
    private final ProfileDao profileDao = new ProfileDao();
    private ObservableList<Offer> offersList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final Set<Integer> selectedOfferIds = new LinkedHashSet<>();
    private boolean showingBookmarks = false;

    // Colors palette
    private static final String INDIGO = "#6366f1";
    private static final String GREEN = "#22c55e";
    private static final String RED = "#ef4444";
    private static final String AMBER = "#f59e0b";
    private static final String GRAY = "#6b7280";
    private static final String LIGHT_GRAY = "#9ca3af";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        User currentUser = AuthService.getCurrentUser();
        boolean isCandidate = currentUser != null && currentUser.getRole() == User.Role.CANDIDATE;
        if (bookmarksToggle != null) {
            bookmarksToggle.setVisible(isCandidate);
            bookmarksToggle.setManaged(isCandidate);
        }
        loadFilters();
        loadSortOptions();
        loadOffers();
    }

    // ===== Data Loading =====

    private void loadFilters() {
        departmentFilter.getItems().clear();
        departmentFilter.getItems().add("Tous");
        departmentFilter.getItems().addAll("IT", "Finance", "Marketing", "RH", "Commercial", "Logistique");
        departmentFilter.setValue("Tous");
        departmentFilter.setOnAction(e -> filterOffers());

        statusFilter.getItems().clear();
        statusFilter.getItems().addAll("Tous", "Ouverte", "Fermée", "En attente");
        statusFilter.setValue("Tous");
        statusFilter.setOnAction(e -> filterOffers());

        searchField.textProperty().addListener((obs, old, val) -> filterOffers());
    }

    private void loadSortOptions() {
        sortComboBox.getItems().clear();
        sortComboBox.getItems().addAll("Plus récentes", "Plus anciennes", "Salaire ↑", "Salaire ↓");
        sortComboBox.setValue("Plus récentes");
        sortComboBox.setOnAction(e -> sortOffers());
    }

    private void loadOffers() {
        if (showingBookmarks) {
            User u = AuthService.getCurrentUser();
            if (u != null)
                offersList.setAll(bookmarkService.getBookmarkedOffers(u.getId()));
        } else {
            offersList.setAll(offerService.getOpenOffers());
        }
        displayCards(offersList);
        updateTotalLabel(offersList.size());
    }

    // ===== Filter & Sort =====

    private void filterOffers() {
        String query = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String dept = departmentFilter.getValue();
        String status = statusFilter.getValue();
        ObservableList<Offer> filtered = offersList.filtered(o -> {
            boolean matchSearch = query.isEmpty() ||
                    o.getTitle().toLowerCase().contains(query) ||
                    o.getDepartment().toLowerCase().contains(query) ||
                    o.getLocation().toLowerCase().contains(query);
            boolean matchDept = "Tous".equals(dept) || o.getDepartment().equals(dept);
            boolean matchStatus = "Tous".equals(status) || o.getStatus().equals(status);
            return matchSearch && matchDept && matchStatus;
        });
        sortAndDisplay(filtered);
    }

    private void sortOffers() {
        filterOffers();
    }

    private void sortAndDisplay(ObservableList<Offer> offers) {
        String sort = sortComboBox.getValue();
        if (sort == null)
            sort = "Plus récentes";
        List<Offer> sorted = new ArrayList<>(offers);
        switch (sort) {
            case "Plus récentes":
                sorted.sort((a, b) -> b.getPublishDate().compareTo(a.getPublishDate()));
                break;
            case "Plus anciennes":
                sorted.sort((a, b) -> a.getPublishDate().compareTo(b.getPublishDate()));
                break;
            case "Salaire ↑":
                sorted.sort((a, b) -> Double.compare(a.getSalaryMax(), b.getSalaryMax()));
                break;
            case "Salaire ↓":
                sorted.sort((a, b) -> Double.compare(b.getSalaryMax(), a.getSalaryMax()));
                break;
        }
        displayCards(FXCollections.observableArrayList(sorted));
    }

    // ===== Display =====

    private void displayCards(ObservableList<Offer> offers) {
        cardsContainer.getChildren().clear();
        updateTotalLabel(offers.size());
        for (Offer offer : offers) {
            cardsContainer.getChildren().add(createCard(offer));
        }
        if (offers.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(80));
            Label icon = new Label(showingBookmarks ? "🔖" : "📭");
            icon.setStyle("-fx-font-size: 52px;");
            Label text = new Label(showingBookmarks ? "No bookmarked offers yet" : "Aucune offre trouvée");
            text.setStyle("-fx-font-size: 16px; -fx-text-fill: " + LIGHT_GRAY + "; -fx-font-weight: 600;");
            Label sub = new Label(
                    showingBookmarks ? "Bookmark offers you like to find them here" : "Try adjusting your filters");
            sub.setStyle("-fx-font-size: 12px; -fx-text-fill: #d1d5db;");
            empty.getChildren().addAll(icon, text, sub);
            cardsContainer.getChildren().add(empty);
        }
    }

    private VBox createCard(Offer offer) {
        VBox card = new VBox(0);
        String base = "-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 0; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3);";
        String hover = "-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 0; " +
                "-fx-border-color: " + INDIGO + "; -fx-border-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.2), 16, 0, 0, 4); -fx-translate-y: -3;";
        String selected = "-fx-background-color: #f5f3ff; -fx-background-radius: 16; -fx-padding: 0; " +
                "-fx-border-color: " + INDIGO + "; -fx-border-radius: 16; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.25), 12, 0, 0, 4);";

        boolean isSelected = selectedOfferIds.contains(offer.getId());
        card.setStyle(isSelected ? selected : base);
        card.setPrefWidth(330);
        card.setMinHeight(260);

        card.setOnMouseEntered(e -> {
            if (!selectedOfferIds.contains(offer.getId()))
                card.setStyle(hover);
        });
        card.setOnMouseExited(e -> {
            if (!selectedOfferIds.contains(offer.getId()))
                card.setStyle(base);
        });

        User currentUser = AuthService.getCurrentUser();
        boolean isCandidate = currentUser != null && currentUser.getRole() == User.Role.CANDIDATE;

        // ──── Top colored accent bar ────
        String accentColor = getDepartmentColor(offer.getDepartment());
        HBox accentBar = new HBox();
        accentBar.setPrefHeight(6);
        accentBar.setMinHeight(6);
        accentBar.setStyle("-fx-background-color: " + accentColor + "; -fx-background-radius: 16 16 0 0;");

        VBox content = new VBox(10);
        content.setPadding(new Insets(18, 20, 18, 20));

        // ──── Header: Title + Bookmark ────
        HBox header = new HBox(8);
        header.setAlignment(Pos.TOP_LEFT);

        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(220);

        Label statusLabel = new Label(offer.getStatus());
        statusLabel.setStyle(getStatusStyle(offer.getStatus()));

        titleBox.getChildren().addAll(titleLabel, statusLabel);
        header.getChildren().add(titleBox);

        // Bookmark button
        if (isCandidate) {
            boolean isBookmarked = bookmarkService.isBookmarked(currentUser.getId(), offer.getId());
            Button bmkBtn = new Button(isBookmarked ? "★" : "☆");
            bmkBtn.setTooltip(new Tooltip(isBookmarked ? "Remove bookmark" : "Save for later"));
            String bmkActive = "-fx-background-color: " + AMBER + "; -fx-text-fill: white; -fx-font-size: 16px; " +
                    "-fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; " +
                    "-fx-background-radius: 50; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;";
            String bmkInactive = "-fx-background-color: #f3f4f6; -fx-text-fill: #d1d5db; -fx-font-size: 16px; " +
                    "-fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; " +
                    "-fx-background-radius: 50; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;";
            bmkBtn.setStyle(isBookmarked ? bmkActive : bmkInactive);
            bmkBtn.setOnAction(e -> {
                boolean now = bookmarkService.toggleBookmark(currentUser.getId(), offer.getId());
                bmkBtn.setText(now ? "★" : "☆");
                bmkBtn.setStyle(now ? bmkActive : bmkInactive);
                bmkBtn.setTooltip(new Tooltip(now ? "Remove bookmark" : "Save for later"));
            });
            header.getChildren().add(bmkBtn);
        }

        // ──── Info rows with styled icons ────
        VBox infoBox = new VBox(6);
        infoBox.getChildren().addAll(
                createInfoRow(getDeptIcon(offer.getDepartment()),
                        offer.getDepartment() + " · " + offer.getContractType(), accentColor),
                createInfoRow("◎", offer.getLocation(), "#3b82f6"),
                createInfoRow("◈", offer.getExperienceLevel(), "#8b5cf6"));

        // Salary badge
        Label salaryLabel = new Label(String.format("%.0f – %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        salaryLabel.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #15803d; -fx-padding: 6 14; " +
                "-fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: 800;");

        // ──── Footer: deadline + applicants ────
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        long daysLeft = ChronoUnit.DAYS.between(java.time.LocalDate.now(), offer.getClosingDate());
        String daysColor = daysLeft > 14 ? GREEN : daysLeft > 7 ? AMBER : RED;
        Label daysLabel = new Label(daysLeft > 0 ? daysLeft + "d left" : "Expired");
        daysLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + daysColor + "; -fx-font-weight: 700;");

        Label appsLabel = new Label(
                offer.getApplicationsReceived() + " applicant" + (offer.getApplicationsReceived() != 1 ? "s" : ""));
        appsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + LIGHT_GRAY + ";");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        footer.getChildren().addAll(daysLabel, new Label("·"), appsLabel, footerSpacer);

        // Positions badge
        if (offer.getPositionsAvailable() > 1) {
            Label posLabel = new Label(offer.getPositionsAvailable() + " posts");
            posLabel.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: " + INDIGO + "; -fx-padding: 2 8; " +
                    "-fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: 700;");
            footer.getChildren().add(posLabel);
        }

        Separator sep = new Separator();

        // ──── Actions Row ────
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(2, 0, 0, 0));

        // Select checkbox (candidate only)
        if (isCandidate) {
            CheckBox selectCb = new CheckBox();
            selectCb.setSelected(selectedOfferIds.contains(offer.getId()));
            selectCb.setTooltip(new Tooltip("Select to compare with AI"));
            selectCb.setOnAction(e -> {
                if (selectCb.isSelected()) {
                    selectedOfferIds.add(offer.getId());
                    card.setStyle(selected);
                } else {
                    selectedOfferIds.remove(offer.getId());
                    card.setStyle(base);
                }
                updateCompareButton();
            });
            actions.getChildren().add(selectCb);
        }

        Button viewBtn = createStyledBtn("View Details", INDIGO, "#eef2ff");
        viewBtn.setOnAction(e -> {
            ViewContext.setSelectedOfferId(offer.getId());
            SceneUtil.switchScene("offer_details.fxml");
        });
        actions.getChildren().add(viewBtn);

        if (isCandidate && "Ouverte".equals(offer.getStatus())) {
            boolean alreadyApplied = applicationService.hasUserApplied(currentUser.getId(), offer.getId());
            if (alreadyApplied) {
                Label appliedTag = new Label("✓ Applied");
                appliedTag.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 5 12; " +
                        "-fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700;");
                actions.getChildren().add(appliedTag);
            } else {
                Button applyBtn = createStyledBtn("Apply", "#16a34a", "#dcfce7");
                applyBtn.setOnAction(e -> {
                    ViewContext.setSelectedOfferId(offer.getId());
                    SceneUtil.switchScene("apply_offer.fxml");
                });
                actions.getChildren().add(applyBtn);
            }
        }

        content.getChildren().addAll(header, infoBox, salaryLabel, footer, sep, actions);
        card.getChildren().addAll(accentBar, content);
        return card;
    }

    // ===== Bookmark Toggle =====

    @FXML
    private void handleBookmarksToggle() {
        showingBookmarks = !showingBookmarks;
        if (showingBookmarks) {
            bookmarksToggle.setText("◀  All Offers");
            bookmarksToggle.setStyle("-fx-background-color: " + INDIGO + "; -fx-text-fill: white; -fx-padding: 8 16; " +
                    "-fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
        } else {
            bookmarksToggle.setText("★  My Bookmarks");
            bookmarksToggle
                    .setStyle("-fx-background-color: #eef2ff; -fx-text-fill: " + INDIGO + "; -fx-padding: 8 16; " +
                            "-fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
        }
        loadOffers();
    }

    // ===== Compare with AI =====

    private void updateCompareButton() {
        boolean show = selectedOfferIds.size() >= 2;
        if (compareBtn != null) {
            compareBtn.setVisible(show);
            compareBtn.setManaged(show);
            if (show)
                compareBtn.setText("⚡ AI Compare (" + selectedOfferIds.size() + ")");
        }
    }

    @FXML
    private void handleCompare() {
        if (selectedOfferIds.size() < 2)
            return;

        User currentUser = AuthService.getCurrentUser();
        if (currentUser == null)
            return;

        // Get the candidate's CV
        Profile profile = profileDao.findByUserId(currentUser.getId());
        String cvPath = null;
        if (profile != null && profile.getCvPath() != null && !profile.getCvPath().isEmpty()) {
            cvPath = profile.getCvPath();
        }

        // Gather offers
        List<Offer> toCompare = new ArrayList<>();
        for (int id : selectedOfferIds) {
            Offer o = offerService.getOfferById(id);
            if (o != null)
                toCompare.add(o);
        }
        if (toCompare.size() < 2)
            return;

        // Build dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("AI Offer Comparison");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(Math.min(toCompare.size() * 300 + 60, 960));
        dialog.getDialogPane().setPrefHeight(620);
        dialog.setResizable(true);

        VBox root = new VBox(16);
        root.setPadding(new Insets(0));

        // Title
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 12, 20));
        titleBar.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); -fx-background-radius: 12 12 0 0;");
        Label dialogTitle = new Label("⚡ AI-Powered Comparison");
        dialogTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label countLbl = new Label(toCompare.size() + " offers");
        countLbl.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px; -fx-font-weight: 600;");
        titleBar.getChildren().addAll(dialogTitle, spacer, countLbl);

        // Columns container
        HBox columns = new HBox(16);
        columns.setPadding(new Insets(16, 20, 20, 20));
        columns.setAlignment(Pos.TOP_CENTER);

        for (Offer o : toCompare) {
            VBox col = buildCompareColumn(o, null, false);
            columns.getChildren().add(col);
        }

        ScrollPane sp = new ScrollPane(columns);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc; -fx-border-color: transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);

        root.getChildren().addAll(titleBar, sp);
        dialog.getDialogPane().setContent(root);

        // If candidate has a CV, run AI scoring in background
        final String finalCvPath = cvPath;
        if (finalCvPath != null) {
            File cvFile = new File(finalCvPath);
            if (cvFile.exists()) {
                // Show loading on each column
                for (int i = 0; i < columns.getChildren().size(); i++) {
                    VBox col = (VBox) columns.getChildren().get(i);
                    ProgressIndicator pi = new ProgressIndicator();
                    pi.setPrefSize(28, 28);
                    Label loadLbl = new Label("Scoring...");
                    loadLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + LIGHT_GRAY + ";");
                    HBox loadBox = new HBox(8, pi, loadLbl);
                    loadBox.setAlignment(Pos.CENTER);
                    loadBox.setId("loading-" + i);
                    col.getChildren().add(loadBox);
                }

                // Background task to score each offer
                Task<Map<Integer, AIScoreResult>> task = new Task<>() {
                    @Override
                    protected Map<Integer, AIScoreResult> call() throws Exception {
                        Map<Integer, AIScoreResult> results = new LinkedHashMap<>();
                        for (int i = 0; i < toCompare.size(); i++) {
                            Offer o = toCompare.get(i);
                            try {
                                if (i > 0)
                                    Thread.sleep(1200);
                                String jobId = aiScoringService.submitScoringJob(cvFile, o.getDescription(), "English");
                                com.fasterxml.jackson.databind.JsonNode result = aiScoringService
                                        .getScoringResultSync(jobId, 8);
                                if (result != null) {
                                    results.put(o.getId(), parseAIResult(result));
                                }
                            } catch (Exception e) {
                                System.err.println("AI score error for offer " + o.getId() + ": " + e.getMessage());
                            }
                        }
                        return results;
                    }
                };

                task.setOnSucceeded(e -> {
                    Map<Integer, AIScoreResult> results = task.getValue();
                    columns.getChildren().clear();
                    // Find best offer
                    int bestOfferId = -1;
                    double bestScore = -1;
                    for (Map.Entry<Integer, AIScoreResult> entry : results.entrySet()) {
                        if (entry.getValue().getOverallScore() > bestScore) {
                            bestScore = entry.getValue().getOverallScore();
                            bestOfferId = entry.getKey();
                        }
                    }
                    for (Offer o : toCompare) {
                        AIScoreResult score = results.get(o.getId());
                        boolean isBest = o.getId() == bestOfferId && score != null;
                        columns.getChildren().add(buildCompareColumn(o, score, isBest));
                    }
                });

                task.setOnFailed(e -> {
                    // Just remove loading indicators
                    for (int i = 0; i < columns.getChildren().size(); i++) {
                        VBox col = (VBox) columns.getChildren().get(i);
                        col.getChildren().removeIf(n -> n.getId() != null && n.getId().startsWith("loading-"));
                    }
                });

                new Thread(task).start();
            }
        }

        dialog.showAndWait();
        selectedOfferIds.clear();
        updateCompareButton();
        loadOffers();
    }

    private VBox buildCompareColumn(Offer o, AIScoreResult score, boolean isBest) {
        VBox col = new VBox(10);
        col.setPadding(new Insets(18));
        col.setPrefWidth(260);
        col.setAlignment(Pos.TOP_LEFT);

        String borderColor = isBest ? AMBER : "#e5e7eb";
        String bgColor = isBest ? "#fffbeb" : "white";
        col.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 14; -fx-border-color: "
                + borderColor + "; " +
                "-fx-border-radius: 14; -fx-border-width: " + (isBest ? "2" : "1") + "; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        // Best match badge
        if (isBest) {
            Label bestBadge = new Label("🏆 Best Match");
            bestBadge.setStyle("-fx-background-color: " + AMBER + "; -fx-text-fill: white; -fx-padding: 4 12; " +
                    "-fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: 800;");
            col.getChildren().add(bestBadge);
        }

        Label title = new Label(o.getTitle());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        title.setWrapText(true);

        Label statusLbl = new Label(o.getStatus());
        statusLbl.setStyle(getStatusStyle(o.getStatus()));

        Separator s1 = new Separator();

        col.getChildren().addAll(title, statusLbl, s1);

        // Info rows
        col.getChildren().addAll(
                createCompareRow("Department", o.getDepartment()),
                createCompareRow("Contract", o.getContractType()),
                createCompareRow("Experience", o.getExperienceLevel()),
                createCompareRow("Location", o.getLocation()),
                createCompareRow("Positions", String.valueOf(o.getPositionsAvailable())),
                createCompareRow("Applicants", String.valueOf(o.getApplicationsReceived())));

        // Salary
        Label salary = new Label(String.format("%.0f – %.0f DT", o.getSalaryMin(), o.getSalaryMax()));
        salary.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #15803d; -fx-padding: 6 14; " +
                "-fx-background-radius: 8; -fx-font-size: 14px; -fx-font-weight: 800;");
        col.getChildren().add(salary);

        // Deadline
        long daysLeft = ChronoUnit.DAYS.between(java.time.LocalDate.now(), o.getClosingDate());
        Label deadline = new Label((daysLeft > 0 ? daysLeft + " days left" : "Expired") + " · "
                + o.getClosingDate().format(dateFormatter));
        String daysColor = daysLeft > 14 ? GRAY : daysLeft > 7 ? AMBER : RED;
        deadline.setStyle("-fx-font-size: 11px; -fx-text-fill: " + daysColor + "; -fx-font-weight: 600;");
        col.getChildren().add(deadline);

        // AI Score section
        if (score != null) {
            Separator s2 = new Separator();
            col.getChildren().add(s2);

            Label aiTitle = new Label("⚡ Your AI Match");
            aiTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: " + INDIGO + ";");

            String matchColor = score.getOverallScore() >= 70 ? GREEN : score.getOverallScore() >= 40 ? AMBER : RED;
            Label overallScore = new Label(String.format("%.0f%%", score.getOverallScore()));
            overallScore.setStyle("-fx-font-size: 28px; -fx-font-weight: 900; -fx-text-fill: " + matchColor + ";");

            VBox scoresBox = new VBox(4);
            scoresBox.getChildren().addAll(
                    createScoreBar("Skills", score.getSkillsScore(), INDIGO),
                    createScoreBar("Experience", score.getExperienceScore(), "#8b5cf6"),
                    createScoreBar("Education", score.getEducationScore(), "#06b6d4"));

            col.getChildren().addAll(aiTitle, overallScore, scoresBox);
        }

        // Description snippet
        String desc = o.getDescription() != null ? o.getDescription() : "";
        if (desc.length() > 100)
            desc = desc.substring(0, 100) + "…";
        Label descLabel = new Label(desc);
        descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #d1d5db;");
        descLabel.setWrapText(true);
        col.getChildren().add(descLabel);

        return col;
    }

    private AIScoreResult parseAIResult(com.fasterxml.jackson.databind.JsonNode responseNode) {
        AIScoreResult score = new AIScoreResult();
        com.fasterxml.jackson.databind.JsonNode resultNode = responseNode.path("data").path("attributes")
                .path("result");
        com.fasterxml.jackson.databind.JsonNode matchScores = resultNode.path("match_scores");
        score.setOverallScore(matchScores.path("overall_match").asDouble(0));
        score.setSkillsScore(matchScores.path("skills_match").asDouble(0));
        score.setExperienceScore(matchScores.path("experience_match").asDouble(0));
        score.setEducationScore(matchScores.path("education_match").asDouble(0));
        return score;
    }

    // ===== UI Helper Methods =====

    private HBox createInfoRow(String icon, String text, String iconColor) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + iconColor + "; -fx-font-weight: 800;");
        iconLabel.setMinWidth(16);
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GRAY + ";");
        row.getChildren().addAll(iconLabel, textLabel);
        return row;
    }

    private HBox createCompareRow(String label, String value) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + LIGHT_GRAY + "; -fx-min-width: 70;");
        Label val = new Label(value);
        val.setStyle("-fx-font-size: 12px; -fx-text-fill: #374151; -fx-font-weight: 600;");
        row.getChildren().addAll(lbl, val);
        return row;
    }

    private HBox createScoreBar(String label, double score, String color) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + LIGHT_GRAY + "; -fx-min-width: 65;");
        ProgressBar bar = new ProgressBar(score / 100.0);
        bar.setPrefWidth(80);
        bar.setPrefHeight(6);
        bar.setStyle("-fx-accent: " + color + ";");
        Label val = new Label(String.format("%.0f", score));
        val.setStyle("-fx-font-size: 10px; -fx-font-weight: 700; -fx-text-fill: " + color + ";");
        row.getChildren().addAll(lbl, bar, val);
        return row;
    }

    private Button createStyledBtn(String text, String fg, String bg) {
        Button btn = new Button(text);
        String normal = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg +
                "; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;";
        String hovered = "-fx-background-color: derive(" + bg + ", -8%); -fx-text-fill: " + fg +
                "; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hovered));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "Ouverte":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;";
            case "Fermée":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #f3f4f6; -fx-text-fill: " + GRAY
                        + "; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;";
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

    private void updateTotalLabel(int count) {
        totalOffersLabel.setText(count + " offre" + (count > 1 ? "s" : ""));
    }

    // ===== Sidebar Navigation =====
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
        /* Already here */ }

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
    private void handleMyCircle() {
        SceneUtil.switchScene("my_circle.fxml");
    }

    @FXML
    private void handleNotifications() {
        SceneUtil.switchScene("notifications.fxml");
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
