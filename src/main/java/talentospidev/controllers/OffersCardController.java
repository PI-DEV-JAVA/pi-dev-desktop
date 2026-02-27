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
import talentospidev.services.OfferService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ResourceBundle;

/**
 * View-only controller for browsing available job offers.
 * CRUD operations are only available from RecruiterDashboardController.
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

    private final OfferService offerService = new OfferService();
    private final ApplicationService applicationService = new ApplicationService();
    private ObservableList<Offer> offersList = FXCollections.observableArrayList();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
        offersList.setAll(offerService.getOpenOffers());
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

        java.util.List<Offer> sorted = new java.util.ArrayList<>(offers);
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
            Label icon = new Label("📭");
            icon.setStyle("-fx-font-size: 48px;");
            Label text = new Label("Aucune offre trouvée");
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
        card.setStyle(base);
        card.setPrefWidth(320);
        card.setMinHeight(240);
        card.setMaxHeight(280);

        card.setOnMouseEntered(e -> card.setStyle(hover));
        card.setOnMouseExited(e -> card.setStyle(base));

        // Header
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(offer.getTitle());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(200);
        HBox.setHgrow(titleLabel, Priority.ALWAYS);

        Label statusLabel = new Label(offer.getStatus());
        statusLabel.setStyle(getStatusStyle(offer.getStatus()));

        header.getChildren().addAll(titleLabel, statusLabel);

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

        // Separator + Actions
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #e5e7eb;");

        HBox actions = new HBox(8);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(4, 0, 0, 0));

        Button viewBtn = createBtn("👁 Détails", "#eef2ff", "#6366f1");
        viewBtn.setOnAction(e -> {
            ViewContext.setSelectedOfferId(offer.getId());
            SceneUtil.switchScene("offer_details.fxml");
        });
        actions.getChildren().add(viewBtn);

        // Candidate: add Apply button (or "Already applied" indicator)
        User currentUser = AuthService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == User.Role.CANDIDATE
                && "Ouverte".equals(offer.getStatus())) {
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
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "Fonctionnalité bientôt disponible !").showAndWait();
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
