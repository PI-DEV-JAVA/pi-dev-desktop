package talentospidev.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import talentospidev.models.Offer;
import talentospidev.models.User;
import talentospidev.services.ApplicationService;
import talentospidev.services.AuthService;
import talentospidev.services.BookmarkService;
import talentospidev.services.OfferService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Controller for browsing job offers — includes bookmarks, multi-select
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
    private ObservableList<Offer> offersList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // Multi-select state
    private final Set<Integer> selectedOfferIds = new HashSet<>();
    private boolean showingBookmarks = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);

        // Hide candidate-only buttons for non-candidates
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
            if (u != null) {
                offersList.setAll(bookmarkService.getBookmarkedOffers(u.getId()));
            }
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
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPadding(new Insets(60));
            Label icon = new Label(showingBookmarks ? "🔖" : "📭");
            icon.setStyle("-fx-font-size: 48px;");
            Label text = new Label(showingBookmarks ? "No bookmarked offers yet" : "Aucune offre trouvée");
            text.setStyle("-fx-font-size: 16px; -fx-text-fill: #9ca3af; -fx-font-weight: 600;");
            empty.getChildren().addAll(icon, text);
            cardsContainer.getChildren().add(empty);
        }
    }

    private VBox createCard(Offer offer) {
        VBox card = new VBox(10);
        String base = "-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 20; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);";
        String hover = "-fx-background-color: white; -fx-background-radius: 14; -fx-padding: 20; " +
                "-fx-border-color: #6366f1; -fx-border-radius: 14; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.18), 12, 0, 0, 4); -fx-translate-y: -2;";
        String selected = "-fx-background-color: #eef2ff; -fx-background-radius: 14; -fx-padding: 20; " +
                "-fx-border-color: #6366f1; -fx-border-radius: 14; -fx-border-width: 2; " +
                "-fx-effect: dropshadow(gaussian, rgba(99,102,241,0.25), 10, 0, 0, 3);";

        boolean isSelected = selectedOfferIds.contains(offer.getId());
        card.setStyle(isSelected ? selected : base);
        card.setPrefWidth(320);
        card.setMinHeight(240);
        card.setMaxHeight(300);

        if (!isSelected) {
            card.setOnMouseEntered(e -> {
                if (!selectedOfferIds.contains(offer.getId()))
                    card.setStyle(hover);
            });
            card.setOnMouseExited(e -> {
                if (!selectedOfferIds.contains(offer.getId()))
                    card.setStyle(base);
            });
        }

        User currentUser = AuthService.getCurrentUser();
        boolean isCandidate = currentUser != null && currentUser.getRole() == User.Role.CANDIDATE;

        // Header: Title + Status + Bookmark
        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label statusLabel = new Label(offer.getStatus());
        statusLabel.setStyle(getStatusStyle(offer.getStatus()));

        header.getChildren().addAll(titleLabel, statusLabel);

        // Bookmark button (candidates only)
        if (isCandidate) {
            boolean isBookmarked = bookmarkService.isBookmarked(currentUser.getId(), offer.getId());
            Button bmkBtn = new Button(isBookmarked ? "🔖" : "🏷");
            String bmkNormal = "-fx-background-color: " + (isBookmarked ? "#eef2ff" : "transparent") +
                    "; -fx-font-size: 16px; -fx-padding: 4 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: transparent;";
            bmkBtn.setStyle(bmkNormal);
            bmkBtn.setTooltip(new Tooltip(isBookmarked ? "Remove bookmark" : "Bookmark this offer"));
            bmkBtn.setOnAction(e -> {
                boolean nowBookmarked = bookmarkService.toggleBookmark(currentUser.getId(), offer.getId());
                bmkBtn.setText(nowBookmarked ? "🔖" : "🏷");
                bmkBtn.setStyle("-fx-background-color: " + (nowBookmarked ? "#eef2ff" : "transparent") +
                        "; -fx-font-size: 16px; -fx-padding: 4 8; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: transparent;");
                bmkBtn.setTooltip(new Tooltip(nowBookmarked ? "Remove bookmark" : "Bookmark this offer"));
            });
            header.getChildren().add(bmkBtn);
        }

        // Info
        Label deptLabel = new Label("🏢 " + offer.getDepartment() + " • " + offer.getContractType());
        deptLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Label locationLabel = new Label("📍 " + offer.getLocation());
        locationLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        Label salaryLabel = new Label(String.format("💰 %.0f - %.0f DT", offer.getSalaryMin(), offer.getSalaryMax()));
        salaryLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #22c55e; -fx-font-weight: 700;");

        // Footer
        HBox footer = new HBox(12);
        footer.setAlignment(Pos.CENTER_LEFT);
        Label dateLabel = new Label("📅 " + offer.getClosingDate().format(dateFormatter));
        dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
        long daysLeft = ChronoUnit.DAYS.between(java.time.LocalDate.now(), offer.getClosingDate());
        Label daysLabel = new Label(daysLeft > 0 ? "⏳ " + daysLeft + "j" : "⏳ Expiré");
        daysLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (daysLeft > 7 ? "#22c55e" : "#ef4444")
                + "; -fx-font-weight: 600;");
        footer.getChildren().addAll(dateLabel, daysLabel);

        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e5e7eb;");

        // Actions row
        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        // Select checkbox (candidates only)
        if (isCandidate) {
            CheckBox selectCb = new CheckBox();
            selectCb.setSelected(selectedOfferIds.contains(offer.getId()));
            selectCb.setTooltip(new Tooltip("Select to compare"));
            selectCb.setOnAction(e -> {
                if (selectCb.isSelected()) {
                    selectedOfferIds.add(offer.getId());
                    card.setStyle(selected);
                    card.setOnMouseEntered(null);
                    card.setOnMouseExited(null);
                } else {
                    selectedOfferIds.remove(offer.getId());
                    card.setStyle(base);
                    card.setOnMouseEntered(ev -> card.setStyle(hover));
                    card.setOnMouseExited(ev -> card.setStyle(base));
                }
                updateCompareButton();
            });
            actions.getChildren().add(selectCb);
        }

        Button viewBtn = createBtn("👁 Détails", "#eef2ff", "#6366f1");
        viewBtn.setOnAction(e -> {
            ViewContext.setSelectedOfferId(offer.getId());
            SceneUtil.switchScene("offer_details.fxml");
        });
        actions.getChildren().add(viewBtn);

        // Candidate: Apply button
        if (isCandidate && "Ouverte".equals(offer.getStatus())) {
            boolean alreadyApplied = applicationService.hasUserApplied(currentUser.getId(), offer.getId());
            if (alreadyApplied) {
                Button appliedBtn = createBtn("✅ Déjà postulé", "#f0fdf4", "#16a34a");
                appliedBtn.setDisable(true);
                appliedBtn.setOpacity(0.8);
                actions.getChildren().add(appliedBtn);
            } else {
                Button applyBtn = createBtn("📤 Postuler", "#dcfce7", "#16a34a");
                applyBtn.setOnAction(e -> {
                    ViewContext.setSelectedOfferId(offer.getId());
                    SceneUtil.switchScene("apply_offer.fxml");
                });
                actions.getChildren().add(applyBtn);
            }
        }

        card.getChildren().addAll(header, deptLabel, locationLabel, salaryLabel, footer, sep, actions);
        return card;
    }

    // ===== Bookmark Toggle =====

    @FXML
    private void handleBookmarksToggle() {
        showingBookmarks = !showingBookmarks;
        if (showingBookmarks) {
            bookmarksToggle.setText("📋 All Offers");
            bookmarksToggle.setStyle(
                    "-fx-background-color: #6366f1; -fx-text-fill: white; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
        } else {
            bookmarksToggle.setText("🔖 My Bookmarks");
            bookmarksToggle.setStyle(
                    "-fx-background-color: #eef2ff; -fx-text-fill: #6366f1; -fx-padding: 8 16; -fx-background-radius: 8; -fx-font-size: 12px; -fx-font-weight: 700; -fx-cursor: hand;");
        }
        loadOffers();
    }

    // ===== Compare =====

    private void updateCompareButton() {
        boolean show = selectedOfferIds.size() >= 2;
        if (compareBtn != null) {
            compareBtn.setVisible(show);
            compareBtn.setManaged(show);
            if (show) {
                compareBtn.setText("⚖ Compare (" + selectedOfferIds.size() + ")");
            }
        }
    }

    @FXML
    private void handleCompare() {
        if (selectedOfferIds.size() < 2)
            return;

        List<Offer> toCompare = new ArrayList<>();
        for (int id : selectedOfferIds) {
            Offer o = offerService.getOfferById(id);
            if (o != null)
                toCompare.add(o);
        }
        if (toCompare.size() < 2)
            return;

        // Build comparison dialog
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Compare Offers");
        dialog.setHeaderText("Side-by-side comparison of " + toCompare.size() + " offers");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setPrefWidth(Math.min(toCompare.size() * 280 + 40, 900));
        dialog.getDialogPane().setPrefHeight(550);

        HBox columns = new HBox(16);
        columns.setPadding(new Insets(16));
        columns.setAlignment(Pos.TOP_CENTER);

        for (Offer o : toCompare) {
            VBox col = new VBox(12);
            col.setPadding(new Insets(16));
            col.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #e5e7eb; " +
                    "-fx-border-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 4, 0, 0, 2);");
            col.setPrefWidth(250);
            col.setAlignment(Pos.TOP_LEFT);

            Label title = new Label(o.getTitle());
            title.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #111827;");
            title.setWrapText(true);

            Label dept = new Label("🏢 " + o.getDepartment());
            dept.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            Label contract = new Label("📝 " + o.getContractType());
            contract.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            Label exp = new Label("⭐ " + o.getExperienceLevel());
            exp.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            Label loc = new Label("📍 " + o.getLocation());
            loc.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            Label salary = new Label(String.format("💰 %.0f – %.0f DT", o.getSalaryMin(), o.getSalaryMax()));
            salary.setStyle("-fx-font-size: 14px; -fx-font-weight: 700; -fx-text-fill: #22c55e;");

            long daysLeft = ChronoUnit.DAYS.between(java.time.LocalDate.now(), o.getClosingDate());
            Label deadline = new Label("📅 Closes: " + o.getClosingDate().format(dateFormatter) +
                    (daysLeft > 0 ? " (" + daysLeft + " days left)" : " (Expired)"));
            deadline.setStyle("-fx-font-size: 11px; -fx-text-fill: " + (daysLeft > 7 ? "#6b7280" : "#ef4444") + ";");
            deadline.setWrapText(true);

            Label positions = new Label("👥 " + o.getPositionsAvailable() + " position(s)");
            positions.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            Label apps = new Label("📩 " + o.getApplicationsReceived() + " application(s)");
            apps.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

            Label statusLbl = new Label(o.getStatus());
            statusLbl.setStyle(getStatusStyle(o.getStatus()));

            Separator s = new Separator();
            s.setStyle("-fx-background-color: #e5e7eb;");

            // Description preview
            String desc = o.getDescription() != null ? o.getDescription() : "";
            if (desc.length() > 120)
                desc = desc.substring(0, 120) + "…";
            Label descLabel = new Label(desc);
            descLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
            descLabel.setWrapText(true);

            col.getChildren().addAll(title, statusLbl, s, dept, contract, exp, loc, salary, positions, apps, deadline,
                    descLabel);
            columns.getChildren().add(col);
        }

        ScrollPane sp = new ScrollPane(columns);
        sp.setFitToHeight(true);
        sp.setStyle("-fx-background: #f0f2f5; -fx-background-color: #f0f2f5; -fx-border-color: transparent;");
        dialog.getDialogPane().setContent(sp);

        dialog.showAndWait();

        // Clear selection after dialog closes
        selectedOfferIds.clear();
        updateCompareButton();
        loadOffers();
    }

    // ===== Helpers =====

    private Button createBtn(String text, String bg, String fg) {
        Button btn = new Button(text);
        String normal = "-fx-background-color: " + bg + "; -fx-text-fill: " + fg +
                "; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;";
        String hovered = "-fx-background-color: derive(" + bg + ", -10%); -fx-text-fill: " + fg +
                "; -fx-padding: 6 14; -fx-background-radius: 8; -fx-font-size: 11px; -fx-font-weight: 700; -fx-cursor: hand;";
        btn.setStyle(normal);
        btn.setOnMouseEntered(e -> btn.setStyle(hovered));
        btn.setOnMouseExited(e -> btn.setStyle(normal));
        return btn;
    }

    private String getStatusStyle(String status) {
        switch (status) {
            case "Ouverte":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #16a34a; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "Fermée":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            case "En attente":
                return "-fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 4 12; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold;";
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
