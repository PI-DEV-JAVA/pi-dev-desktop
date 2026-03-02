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
import talentospidev.dao.ProfileDao;
import talentospidev.models.Application;
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
    private final ProfileDao profileDao = new ProfileDao();
    private ObservableList<Offer> offersList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final Set<Integer> selectedOfferIds = new LinkedHashSet<>();
    private boolean showingBookmarks = false;

    private static final String IND = "#6366f1";
    private static final String GRN = "#22c55e";
    private static final String RED = "#ef4444";
    private static final String AMB = "#f59e0b";
    private static final String GRY = "#6b7280";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        User u = AuthService.getCurrentUser();
        boolean isCandidate = u != null && u.getRole() == User.Role.CANDIDATE;
        if (bookmarksToggle != null) {
            bookmarksToggle.setVisible(isCandidate);
            bookmarksToggle.setManaged(isCandidate);
        }
        loadFilters();
        loadSortOptions();
        loadOffers();
    }

    private void loadFilters() {
        departmentFilter.getItems().setAll("Tous", "IT", "Finance", "Marketing", "RH", "Commercial", "Logistique");
        departmentFilter.setValue("Tous");
        departmentFilter.setOnAction(e -> filterOffers());
        statusFilter.getItems().setAll("Tous", "Ouverte", "Fermée", "En attente");
        statusFilter.setValue("Tous");
        statusFilter.setOnAction(e -> filterOffers());
        searchField.textProperty().addListener((o, a, b) -> filterOffers());
    }

    private void loadSortOptions() {
        sortComboBox.getItems().setAll("Plus récentes", "Plus anciennes", "Salaire ↑", "Salaire ↓");
        sortComboBox.setValue("Plus récentes");
        sortComboBox.setOnAction(e -> filterOffers());
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
    }

    private void filterOffers() {
        String q = searchField.getText() != null ? searchField.getText().toLowerCase().trim() : "";
        String dept = departmentFilter.getValue();
        String status = statusFilter.getValue();
        List<Offer> filtered = new ArrayList<>();
        for (Offer o : offersList) {
            boolean ms = q.isEmpty() || o.getTitle().toLowerCase().contains(q)
                    || o.getDepartment().toLowerCase().contains(q) || o.getLocation().toLowerCase().contains(q);
            boolean md = "Tous".equals(dept) || o.getDepartment().equals(dept);
            boolean mt = "Tous".equals(status) || o.getStatus().equals(status);
            if (ms && md && mt)
                filtered.add(o);
        }
        String sort = sortComboBox.getValue();
        if (sort == null)
            sort = "Plus récentes";
        switch (sort) {
            case "Plus récentes":
                filtered.sort((a, b) -> b.getPublishDate().compareTo(a.getPublishDate()));
                break;
            case "Plus anciennes":
                filtered.sort((a, b) -> a.getPublishDate().compareTo(b.getPublishDate()));
                break;
            case "Salaire ↑":
                filtered.sort((a, b) -> Double.compare(a.getSalaryMax(), b.getSalaryMax()));
                break;
            case "Salaire ↓":
                filtered.sort((a, b) -> Double.compare(b.getSalaryMax(), a.getSalaryMax()));
                break;
        }
        displayCards(FXCollections.observableArrayList(filtered));
    }

    private void displayCards(ObservableList<Offer> offers) {
        cardsContainer.getChildren().clear();
        totalOffersLabel.setText(offers.size() + " offre" + (offers.size() > 1 ? "s" : ""));
        for (Offer o : offers)
            cardsContainer.getChildren().add(createCard(o));
        if (offers.isEmpty()) {
            VBox empty = new VBox(12);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(80));
            Label ic = new Label(showingBookmarks ? "★" : "📭");
            ic.setStyle("-fx-font-size: 52px;");
            Label tx = new Label(showingBookmarks ? "No bookmarked offers yet" : "Aucune offre trouvée");
            tx.setStyle("-fx-font-size: 16px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
            empty.getChildren().addAll(ic, tx);
            cardsContainer.getChildren().add(empty);
        }
    }

    // ===== Card Builder =====

    private VBox createCard(Offer offer) {
        VBox card = new VBox(0);
        String base = "-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 0; -fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 8, 0, 0, 3);";
        String hover = "-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 0; -fx-border-color: "
                + IND
                + "; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(99,102,241,0.2), 16, 0, 0, 4); -fx-translate-y: -3;";
        String sel = "-fx-background-color: #f5f3ff; -fx-background-radius: 16; -fx-padding: 0; -fx-border-color: "
                + IND
                + "; -fx-border-radius: 16; -fx-border-width: 2; -fx-effect: dropshadow(gaussian, rgba(99,102,241,0.25), 12, 0, 0, 4);";

        boolean isSel = selectedOfferIds.contains(offer.getId());
        card.setStyle(isSel ? sel : base);
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

        User cu = AuthService.getCurrentUser();
        boolean isCand = cu != null && cu.getRole() == User.Role.CANDIDATE;
        String ac = getDeptColor(offer.getDepartment());

        HBox accent = new HBox();
        accent.setPrefHeight(6);
        accent.setMinHeight(6);
        accent.setStyle("-fx-background-color: " + ac + "; -fx-background-radius: 16 16 0 0;");

        VBox content = new VBox(10);
        content.setPadding(new Insets(18, 20, 18, 20));

        // Header
        HBox header = new HBox(8);
        header.setAlignment(Pos.TOP_LEFT);
        VBox titleBox = new VBox(4);
        HBox.setHgrow(titleBox, Priority.ALWAYS);
        Label tl = new Label(offer.getTitle());
        tl.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        tl.setWrapText(true);
        tl.setMaxWidth(220);
        Label sl = new Label(offer.getStatus());
        sl.setStyle(statusPill(offer.getStatus()));
        titleBox.getChildren().addAll(tl, sl);
        header.getChildren().add(titleBox);

        if (isCand) {
            boolean bm = bookmarkService.isBookmarked(cu.getId(), offer.getId());
            Button bmk = new Button(bm ? "★" : "☆");
            String bmA = "-fx-background-color: " + AMB
                    + "; -fx-text-fill: white; -fx-font-size: 16px; -fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; -fx-background-radius: 50; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;";
            String bmI = "-fx-background-color: #f3f4f6; -fx-text-fill: #d1d5db; -fx-font-size: 16px; -fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; -fx-background-radius: 50; -fx-padding: 0; -fx-cursor: hand; -fx-border-color: transparent;";
            bmk.setStyle(bm ? bmA : bmI);
            bmk.setTooltip(new Tooltip(bm ? "Remove bookmark" : "Save for later"));
            bmk.setOnAction(e -> {
                boolean n = bookmarkService.toggleBookmark(cu.getId(), offer.getId());
                bmk.setText(n ? "★" : "☆");
                bmk.setStyle(n ? bmA : bmI);
            });
            header.getChildren().add(bmk);
        }

        VBox info = new VBox(6);
        info.getChildren().addAll(
                infoRow(getDeptIcon(offer.getDepartment()), offer.getDepartment() + " · " + offer.getContractType(),
                        ac),
                infoRow("◎", offer.getLocation(), "#3b82f6"),
                infoRow("◈", offer.getExperienceLevel(), "#8b5cf6"));

        Label sal = new Label(String.format("%.0f – %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        sal.setStyle(
                "-fx-background-color: #f0fdf4; -fx-text-fill: #15803d; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 13px; -fx-font-weight: 800;");

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        long dl = ChronoUnit.DAYS.between(java.time.LocalDate.now(), offer.getClosingDate());
        Label dLbl = new Label(dl > 0 ? dl + "d left" : "Expired");
        dLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (dl > 14 ? GRN : dl > 7 ? AMB : RED)
                + "; -fx-font-weight: 700;");
        Label apLbl = new Label(
                offer.getApplicationsReceived() + " applicant" + (offer.getApplicationsReceived() != 1 ? "s" : ""));
        apLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        Region fs = new Region();
        HBox.setHgrow(fs, Priority.ALWAYS);
        footer.getChildren().addAll(dLbl, new Label("·"), apLbl, fs);
        if (offer.getPositionsAvailable() > 1) {
            Label pos = new Label(offer.getPositionsAvailable() + " posts");
            pos.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: " + IND
                    + "; -fx-padding: 2 8; -fx-background-radius: 6; -fx-font-size: 10px; -fx-font-weight: 700;");
            footer.getChildren().add(pos);
        }

        Separator sep = new Separator();
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(2, 0, 0, 0));

        if (isCand) {
            CheckBox cb = new CheckBox();
            cb.setSelected(selectedOfferIds.contains(offer.getId()));
            cb.setTooltip(new Tooltip("Select to compare"));
            cb.setOnAction(e -> {
                if (cb.isSelected()) {
                    selectedOfferIds.add(offer.getId());
                    card.setStyle(sel);
                } else {
                    selectedOfferIds.remove(offer.getId());
                    card.setStyle(base);
                }
                updateCompareButton();
            });
            actions.getChildren().add(cb);
        }

        Button vBtn = styledBtn("View Details", IND, "#eef2ff");
        vBtn.setOnAction(e -> {
            ViewContext.setSelectedOfferId(offer.getId());
            SceneUtil.switchScene("offer_details.fxml");
        });
        actions.getChildren().add(vBtn);

        if (isCand && "Ouverte".equals(offer.getStatus())) {
            boolean applied = applicationService.hasUserApplied(cu.getId(), offer.getId());
            if (applied) {
                Label aTag = new Label("✓ Applied");
                aTag.setStyle(
                        "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 5 12; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700;");
                actions.getChildren().add(aTag);
            } else {
                Button ab = styledBtn("Apply", "#16a34a", "#dcfce7");
                ab.setOnAction(e -> {
                    ViewContext.setSelectedOfferId(offer.getId());
                    SceneUtil.switchScene("apply_offer.fxml");
                });
                actions.getChildren().add(ab);
            }
        }

        content.getChildren().addAll(header, info, sal, footer, sep, actions);
        card.getChildren().addAll(accent, content);
        return card;
    }

    // ===== Bookmarks =====

    @FXML
    private void handleBookmarksToggle() {
        showingBookmarks = !showingBookmarks;
        bookmarksToggle.setText(showingBookmarks ? "◀  All Offers" : "★  My Bookmarks");
        bookmarksToggle.setStyle(showingBookmarks
                ? "-fx-background-color: " + IND
                        + "; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;"
                : "-fx-background-color: #eef2ff; -fx-text-fill: " + IND
                        + "; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
        loadOffers();
    }

    // ===== Compare =====

    private void updateCompareButton() {
        boolean show = selectedOfferIds.size() >= 2;
        if (compareBtn != null) {
            compareBtn.setVisible(show);
            compareBtn.setManaged(show);
            if (show)
                compareBtn.setText("⚡ Compare (" + selectedOfferIds.size() + ")");
        }
    }

    @FXML
    private void handleCompare() {
        if (selectedOfferIds.size() < 2)
            return;
        User cu = AuthService.getCurrentUser();
        if (cu == null)
            return;

        Profile profile = profileDao.findByUserId(cu.getId());
        List<Offer> offers = new ArrayList<>();
        for (int id : selectedOfferIds) {
            Offer o = offerService.getOfferById(id);
            if (o != null)
                offers.add(o);
        }
        if (offers.size() < 2)
            return;

        // Score each offer using smart local algorithm
        Map<Integer, Integer> scores = new LinkedHashMap<>();
        Map<Integer, Map<String, Integer>> breakdowns = new LinkedHashMap<>();
        for (Offer o : offers) {
            Map<String, Integer> bd = computeMatchScore(o, profile);
            int total = bd.values().stream().mapToInt(Integer::intValue).sum();
            scores.put(o.getId(), total);
            breakdowns.put(o.getId(), bd);
        }

        // Sort by score descending
        offers.sort((a, b) -> scores.get(b.getId()) - scores.get(a.getId()));
        int bestId = offers.get(0).getId();

        // Build dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Smart Offer Comparison");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(Math.min(offers.size() * 300 + 60, 980));
        dialog.getDialogPane().setPrefHeight(650);
        dialog.setResizable(true);

        VBox root = new VBox(0);

        // Gradient title bar
        HBox titleBar = new HBox(12);
        titleBar.setAlignment(Pos.CENTER_LEFT);
        titleBar.setPadding(new Insets(16, 20, 12, 20));
        titleBar.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); -fx-background-radius: 0;");
        Label dt = new Label("⚡ Smart Comparison");
        dt.setStyle("-fx-font-size: 18px; -fx-font-weight: 800; -fx-text-fill: white;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Label ct = new Label(offers.size() + " offers · Scored by your profile match");
        ct.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 11px;");
        titleBar.getChildren().addAll(dt, sp, ct);

        HBox columns = new HBox(16);
        columns.setPadding(new Insets(16, 20, 20, 20));
        columns.setAlignment(Pos.TOP_CENTER);
        for (Offer o : offers) {
            boolean best = o.getId() == bestId;
            columns.getChildren()
                    .add(buildCompareCol(o, scores.get(o.getId()), breakdowns.get(o.getId()), best, cu, dialog));
        }

        ScrollPane scrollPane = new ScrollPane(columns);
        scrollPane.setFitToHeight(true);
        scrollPane.setStyle("-fx-background: #f8fafc; -fx-background-color: #f8fafc; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        root.getChildren().addAll(titleBar, scrollPane);
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();

        selectedOfferIds.clear();
        updateCompareButton();
        loadOffers();
    }

    /**
     * Smart local scoring: rates how well an offer matches the candidate's profile.
     * No external API needed — uses profile fields vs offer fields.
     */
    private Map<String, Integer> computeMatchScore(Offer offer, Profile profile) {
        Map<String, Integer> bd = new LinkedHashMap<>();

        // 1. Experience Fit (0-30 pts)
        int expScore = 15; // baseline
        if (profile != null && profile.getYearsOfExperience() > 0) {
            String lvl = offer.getExperienceLevel() != null ? offer.getExperienceLevel().toLowerCase() : "";
            int yrs = profile.getYearsOfExperience();
            if (lvl.contains("junior") || lvl.contains("débutant") || lvl.contains("entry")) {
                expScore = yrs <= 3 ? 30 : yrs <= 5 ? 20 : 10;
            } else if (lvl.contains("mid") || lvl.contains("intermédiaire") || lvl.contains("confirmé")) {
                expScore = (yrs >= 2 && yrs <= 7) ? 30 : yrs < 2 ? 12 : 18;
            } else if (lvl.contains("senior") || lvl.contains("expert") || lvl.contains("lead")) {
                expScore = yrs >= 5 ? 30 : yrs >= 3 ? 20 : 8;
            }
        }
        bd.put("Experience", expScore);

        // 2. Location Match (0-20 pts)
        int locScore = 10;
        if (profile != null && profile.getLocation() != null && offer.getLocation() != null) {
            String pLoc = profile.getLocation().toLowerCase().trim();
            String oLoc = offer.getLocation().toLowerCase().trim();
            if (pLoc.equals(oLoc) || oLoc.contains(pLoc) || pLoc.contains(oLoc))
                locScore = 20;
            else if (oLoc.contains("remote") || oLoc.contains("distance") || oLoc.contains("télétravail"))
                locScore = 18;
            else
                locScore = 6;
        }
        bd.put("Location", locScore);

        // 3. Salary Attractiveness (0-15 pts) — higher salary = better score
        double maxSalary = 15000; // normalize against reasonable max
        double salNorm = Math.min(offer.getSalaryMax() / maxSalary, 1.0);
        bd.put("Salary", (int) Math.round(salNorm * 15));

        // 4. Competition (0-15 pts) — fewer applicants per position = easier
        int apps = Math.max(offer.getApplicationsReceived(), 1);
        int positions = Math.max(offer.getPositionsAvailable(), 1);
        double ratio = (double) positions / apps;
        int compScore = ratio >= 1.0 ? 15 : ratio >= 0.5 ? 12 : ratio >= 0.2 ? 8 : 4;
        bd.put("Competition", compScore);

        // 5. Freshness (0-10 pts) — more days left = better
        long daysLeft = ChronoUnit.DAYS.between(java.time.LocalDate.now(), offer.getClosingDate());
        int freshScore = daysLeft > 30 ? 10 : daysLeft > 14 ? 8 : daysLeft > 7 ? 5 : daysLeft > 0 ? 3 : 0;
        bd.put("Freshness", freshScore);

        // 6. Profile Keyword Match (0-10 pts) — title/summary keywords found in
        // description
        int kwScore = 0;
        if (profile != null && offer.getDescription() != null) {
            String desc = offer.getDescription().toLowerCase();
            String title = profile.getProfessionalTitle() != null ? profile.getProfessionalTitle().toLowerCase() : "";
            String summary = profile.getSummary() != null ? profile.getSummary().toLowerCase() : "";
            Set<String> keywords = new HashSet<>();
            for (String w : (title + " " + summary).split("\\s+")) {
                if (w.length() > 3)
                    keywords.add(w);
            }
            int hits = 0;
            for (String kw : keywords) {
                if (desc.contains(kw))
                    hits++;
            }
            kwScore = keywords.isEmpty() ? 5 : Math.min(10, (int) (10.0 * hits / Math.min(keywords.size(), 10)));
        }
        bd.put("Keywords", kwScore);

        return bd;
    }

    private VBox buildCompareCol(Offer o, int totalScore, Map<String, Integer> bd, boolean best, User currentUser,
            Dialog<?> dialog) {
        VBox col = new VBox(10);
        col.setPadding(new Insets(18));
        col.setPrefWidth(260);
        col.setAlignment(Pos.TOP_LEFT);
        String border = best ? AMB : "#e5e7eb";
        String bg = best ? "#fffbeb" : "white";
        col.setStyle("-fx-background-color: " + bg + "; -fx-background-radius: 14; -fx-border-color: " + border
                + "; -fx-border-radius: 14; -fx-border-width: " + (best ? "2" : "1")
                + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 2);");

        if (best) {
            Label badge = new Label("🏆 Best Match");
            badge.setStyle("-fx-background-color: " + AMB
                    + "; -fx-text-fill: white; -fx-padding: 4 12; -fx-background-radius: 6; -fx-font-size: 11px; -fx-font-weight: 800;");
            col.getChildren().add(badge);
        }

        // Overall score ring
        String scoreColor = totalScore >= 70 ? GRN : totalScore >= 45 ? AMB : RED;
        Label scoreLabel = new Label(totalScore + "/100");
        scoreLabel.setStyle("-fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: " + scoreColor + ";");
        Label matchLbl = new Label("profile match");
        matchLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");

        Label title = new Label(o.getTitle());
        title.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #111827;");
        title.setWrapText(true);
        Label statusLbl = new Label(o.getStatus());
        statusLbl.setStyle(statusPill(o.getStatus()));
        Separator s1 = new Separator();

        col.getChildren().addAll(scoreLabel, matchLbl, title, statusLbl, s1);

        // Score breakdown bars
        String[] colors = { IND, "#3b82f6", GRN, "#8b5cf6", AMB, "#06b6d4" };
        int[] maxes = { 30, 20, 15, 15, 10, 10 };
        int ci = 0;
        for (Map.Entry<String, Integer> entry : bd.entrySet()) {
            HBox bar = new HBox(6);
            bar.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(entry.getKey());
            lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af; -fx-min-width: 70;");
            ProgressBar pb = new ProgressBar((double) entry.getValue() / maxes[ci]);
            pb.setPrefWidth(80);
            pb.setPrefHeight(6);
            pb.setStyle("-fx-accent: " + colors[ci % colors.length] + ";");
            Label val = new Label(entry.getValue() + "/" + maxes[ci]);
            val.setStyle(
                    "-fx-font-size: 9px; -fx-font-weight: 700; -fx-text-fill: " + colors[ci % colors.length] + ";");
            bar.getChildren().addAll(lbl, pb, val);
            col.getChildren().add(bar);
            ci++;
        }

        Separator s2 = new Separator();
        col.getChildren().add(s2);

        // Quick info
        col.getChildren().addAll(
                compRow("Dept", o.getDepartment()),
                compRow("Contract", o.getContractType()),
                compRow("Location", o.getLocation()));
        Label salLbl = new Label(String.format("%.0f – %.0f DT", o.getSalaryMin(), o.getSalaryMax()));
        salLbl.setStyle(
                "-fx-background-color: #f0fdf4; -fx-text-fill: #15803d; -fx-padding: 4 12; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 800;");
        col.getChildren().add(salLbl);

        long dl = ChronoUnit.DAYS.between(java.time.LocalDate.now(), o.getClosingDate());
        Label deadLbl = new Label((dl > 0 ? dl + "d left" : "Expired") + " · " + o.getApplicationsReceived() + " apps");
        deadLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: " + (dl > 7 ? GRY : RED) + ";");
        col.getChildren().add(deadLbl);

        // Apply button
        boolean alreadyApplied = applicationService.hasUserApplied(currentUser.getId(), o.getId());
        if ("Ouverte".equals(o.getStatus())) {
            if (alreadyApplied) {
                Label appTag = new Label("✓ Already Applied");
                appTag.setStyle(
                        "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700;");
                col.getChildren().add(appTag);
            } else {
                Button applyBtn = new Button("Apply Now →");
                String abN = "-fx-background-color: " + IND
                        + "; -fx-text-fill: white; -fx-padding: 8 18; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;";
                String abH = "-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-padding: 8 18; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;";
                applyBtn.setStyle(abN);
                applyBtn.setOnMouseEntered(e -> applyBtn.setStyle(abH));
                applyBtn.setOnMouseExited(e -> applyBtn.setStyle(abN));
                applyBtn.setMaxWidth(Double.MAX_VALUE);
                applyBtn.setOnAction(e -> showQuickApplyPopup(o, currentUser, dialog, applyBtn));
                col.getChildren().add(applyBtn);
            }
        }

        return col;
    }

    // ===== Quick Apply Popup (from compare dialog) =====

    private void showQuickApplyPopup(Offer offer, User user, Dialog<?> parentDialog, Button triggerBtn) {
        Profile profile = profileDao.findByUserId(user.getId());

        Dialog<Boolean> applyDlg = new Dialog<>();
        applyDlg.setTitle("Quick Apply — " + offer.getTitle());
        applyDlg.getDialogPane().setPrefWidth(500);
        applyDlg.getDialogPane().setPrefHeight(520);

        VBox form = new VBox(16);
        form.setPadding(new Insets(20));

        // Header
        VBox headerBox = new VBox(4);
        headerBox.setStyle(
                "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); -fx-background-radius: 12; -fx-padding: 16;");
        Label formTitle = new Label("📤 Apply to: " + offer.getTitle());
        formTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: white;");
        formTitle.setWrapText(true);
        Label formSub = new Label(offer.getDepartment() + " · " + offer.getLocation() + " · "
                + String.format("%.0f–%.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        formSub.setStyle("-fx-font-size: 11px; -fx-text-fill: rgba(255,255,255,0.8);");
        headerBox.getChildren().addAll(formTitle, formSub);

        // Profile preview
        VBox profileBox = new VBox(4);
        profileBox.setStyle("-fx-background-color: #f9fafb; -fx-background-radius: 10; -fx-padding: 12;");
        String name = "—";
        String pTitle = "—";
        if (profile != null) {
            if (profile.getFirstName() != null)
                name = profile.getFirstName() + " " + (profile.getLastName() != null ? profile.getLastName() : "");
            if (profile.getProfessionalTitle() != null)
                pTitle = profile.getProfessionalTitle();
        }
        Label nameLbl = new Label("👤 " + name);
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        Label titleLbl = new Label(pTitle);
        titleLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        profileBox.getChildren().addAll(nameLbl, titleLbl);

        // CV picker
        final String[] cvPath = { profile != null && profile.getCvPath() != null ? profile.getCvPath() : null };
        HBox cvRow = new HBox(10);
        cvRow.setAlignment(Pos.CENTER_LEFT);
        Label cvLabel = new Label("📎 CV:");
        cvLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        Label cvFile = new Label(cvPath[0] != null ? new File(cvPath[0]).getName() : "No CV attached");
        cvFile.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (cvPath[0] != null ? GRN : RED) + ";");
        Button pickCv = new Button("Browse");
        pickCv.setStyle(
                "-fx-background-color: #f3f4f6; -fx-text-fill: #374151; -fx-padding: 4 12; -fx-background-radius: 6; -fx-font-size: 11px; -fx-cursor: hand;");
        pickCv.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Select CV");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
            File f = fc.showOpenDialog(applyDlg.getDialogPane().getScene().getWindow());
            if (f != null) {
                cvPath[0] = f.getAbsolutePath();
                cvFile.setText(f.getName());
                cvFile.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GRN + ";");
            }
        });
        cvRow.getChildren().addAll(cvLabel, cvFile, pickCv);

        // Motivation letter
        Label motLabel = new Label("✍ Cover Letter");
        motLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: 600; -fx-text-fill: #374151;");
        TextArea motText = new TextArea();
        motText.setPromptText("Write a brief motivation letter (optional)...");
        motText.setPrefRowCount(5);
        motText.setStyle(
                "-fx-font-size: 12px; -fx-border-color: #e5e7eb; -fx-border-radius: 8; -fx-background-radius: 8;");

        // Submit button
        Button submitBtn = new Button("Submit Application →");
        submitBtn.setMaxWidth(Double.MAX_VALUE);
        String sbN = "-fx-background-color: linear-gradient(to right, #6366f1, #8b5cf6); -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: 700; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(99,102,241,0.3), 8, 0, 0, 3);";
        submitBtn.setStyle(sbN);

        Label statusLabel = new Label();
        statusLabel.setStyle("-fx-font-size: 12px;");

        submitBtn.setOnAction(e -> {
            Application app = new Application(user.getId(), offer.getId(), cvPath[0], motText.getText());
            boolean success = applicationService.createApplication(app);
            if (success) {
                statusLabel.setText("✓ Application submitted successfully!");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GRN + "; -fx-font-weight: 700;");
                submitBtn.setDisable(true);
                submitBtn.setText("✓ Submitted");
                submitBtn.setStyle(
                        "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 12 24; -fx-background-radius: 10; -fx-font-size: 14px; -fx-font-weight: 700;");
                // Update the trigger button in compare dialog
                triggerBtn.setText("✓ Applied");
                triggerBtn.setDisable(true);
                triggerBtn.setStyle(
                        "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 8 18; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700;");
            } else {
                statusLabel.setText("✗ You may have already applied to this offer.");
                statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + RED + "; -fx-font-weight: 700;");
            }
        });

        form.getChildren().addAll(headerBox, profileBox, cvRow, motLabel, motText, submitBtn, statusLabel);

        applyDlg.getDialogPane().setContent(form);
        applyDlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        applyDlg.showAndWait();
    }

    // ===== Helpers =====

    private HBox infoRow(String icon, String text, String color) {
        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);
        Label il = new Label(icon);
        il.setStyle("-fx-font-size: 12px; -fx-text-fill: " + color + "; -fx-font-weight: 800;");
        il.setMinWidth(16);
        Label tl = new Label(text);
        tl.setStyle("-fx-font-size: 12px; -fx-text-fill: " + GRY + ";");
        r.getChildren().addAll(il, tl);
        return r;
    }

    private HBox compRow(String label, String value) {
        HBox r = new HBox(8);
        r.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label(label);
        l.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af; -fx-min-width: 60;");
        Label v = new Label(value);
        v.setStyle("-fx-font-size: 11px; -fx-text-fill: #374151; -fx-font-weight: 600;");
        r.getChildren().addAll(l, v);
        return r;
    }

    private Button styledBtn(String text, String fg, String bg) {
        Button b = new Button(text);
        String n = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg
                + "; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;";
        String h = "-fx-background-color: derive(" + bg + ", -8%); -fx-text-fill: " + fg
                + "; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;";
        b.setStyle(n);
        b.setOnMouseEntered(e -> b.setStyle(h));
        b.setOnMouseExited(e -> b.setStyle(n));
        return b;
    }

    private String statusPill(String s) {
        switch (s) {
            case "Ouverte":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;";
            case "Fermée":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #f3f4f6; -fx-text-fill: " + GRY
                        + "; -fx-padding: 3 10; -fx-background-radius: 20; -fx-font-size: 10px; -fx-font-weight: bold;";
        }
    }

    private String getDeptColor(String d) {
        if (d == null)
            return IND;
        switch (d) {
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

    private String getDeptIcon(String d) {
        if (d == null)
            return "●";
        switch (d) {
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

    // ===== Sidebar =====
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
    }

    @FXML
    private void handleTrends() {
        SceneUtil.switchScene("MarketTrendsView.fxml");
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
        new Alert(Alert.AlertType.INFORMATION, "Coming soon!").showAndWait();
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
