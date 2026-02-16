package talentospidev.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import talentospidev.dao.UserDao;
import talentospidev.models.User;
import talentospidev.models.UserViewModel;
import talentospidev.services.AuthService;
import talentospidev.utils.ProfilePopup;
import talentospidev.utils.SceneUtil;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DashboardController {

    @FXML
    private ListView<UserViewModel> feedListView;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortByBox;
    @FXML
    private ComboBox<String> sortOrderBox;
    @FXML
    private Label countLabel;

    private final UserDao userDao = new UserDao();
    private ObservableList<UserViewModel> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        User currentUser = AuthService.getCurrentUser();
        if (currentUser == null)
            return;

        // Sort controls setup
        sortByBox.setItems(FXCollections.observableArrayList("Name", "Job", "Age", "Location"));
        sortByBox.setValue("Name");
        sortOrderBox.setItems(FXCollections.observableArrayList("ASC", "DESC"));
        sortOrderBox.setValue("ASC");

        setupListView();
        loadFeed(currentUser.getId());

        // Re-filter + re-sort on any change
        searchField.textProperty().addListener((obs, o, n) -> applyFilterAndSort());
        sortByBox.valueProperty().addListener((obs, o, n) -> applyFilterAndSort());
        sortOrderBox.valueProperty().addListener((obs, o, n) -> applyFilterAndSort());
    }

    private void setupListView() {
        feedListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(UserViewModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                } else {
                    setGraphic(createProfileCard(item));
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 8 0;");
                }
            }
        });
    }

    private void loadFeed(int currentUserId) {
        masterData.clear();
        masterData.addAll(userDao.findAllActiveProfiles(currentUserId));
        applyFilterAndSort();
    }

    private void applyFilterAndSort() {
        String query = searchField.getText();
        String sortBy = sortByBox.getValue();
        String sortOrder = sortOrderBox.getValue();

        // 1) Filter
        List<UserViewModel> filtered = masterData.stream()
                .filter(p -> {
                    if (query == null || query.trim().isEmpty())
                        return true;
                    String lower = query.toLowerCase();
                    return p.getFullName().toLowerCase().contains(lower)
                            || p.getProfessionalTitle().toLowerCase().contains(lower)
                            || p.getLocation().toLowerCase().contains(lower)
                            || p.getRole().toLowerCase().contains(lower);
                })
                .collect(Collectors.toList());

        // 2) Sort
        Comparator<UserViewModel> comparator = getComparator(sortBy);
        if ("DESC".equals(sortOrder)) {
            comparator = comparator.reversed();
        }
        filtered.sort(comparator);

        // 3) Update ListView
        feedListView.setItems(FXCollections.observableArrayList(filtered));
        countLabel.setText(filtered.size() + " profile" + (filtered.size() != 1 ? "s" : ""));
    }

    private Comparator<UserViewModel> getComparator(String sortBy) {
        if (sortBy == null)
            return Comparator.comparing(UserViewModel::getFullName, String.CASE_INSENSITIVE_ORDER);
        return switch (sortBy) {
            case "Job" -> Comparator.comparing(UserViewModel::getProfessionalTitle, String.CASE_INSENSITIVE_ORDER);
            case "Age" -> Comparator.comparingInt(UserViewModel::getAge);
            case "Location" -> Comparator.comparing(UserViewModel::getLocation, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(UserViewModel::getFullName, String.CASE_INSENSITIVE_ORDER);
        };
    }

    private VBox createProfileCard(UserViewModel profile) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        // ---- Header: Avatar + Info + Badges ----
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(profile.getFullName().substring(0, 1).toUpperCase());
        avatar.getStyleClass().add("avatar-small");

        VBox info = new VBox(2);
        Label name = new Label(profile.getFullName());
        name.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        String subtitleText = profile.getProfessionalTitle().isEmpty() ? profile.getRole()
                : profile.getProfessionalTitle();
        Label subtitle = new Label(subtitleText);
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        info.getChildren().addAll(name, subtitle);

        // Location + Age meta
        VBox meta = new VBox(2);
        meta.setAlignment(Pos.CENTER_RIGHT);

        if (!profile.getLocation().isEmpty()) {
            Label loc = new Label("📍 " + profile.getLocation());
            loc.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
            meta.getChildren().add(loc);
        }
        if (profile.getAge() > 0) {
            Label age = new Label(profile.getAge() + " yrs");
            age.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
            meta.getChildren().add(age);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, info, spacer, meta);

        // ---- Footer: View + Connect buttons ----
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_RIGHT);

        Button viewBtn = new Button("View Profile");
        viewBtn.getStyleClass().add("primary-button");
        viewBtn.setOnAction(e -> ProfilePopup.show(profile));

        Button connectBtn = new Button("Connect");
        connectBtn.getStyleClass().add("secondary-button");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Role badge
        Label roleBadge = new Label(profile.getRole());
        roleBadge.getStyleClass().addAll("badge",
                profile.getRole().equalsIgnoreCase("CANDIDATE") ? "badge-blue" : "badge-purple");

        footer.getChildren().addAll(roleBadge, footerSpacer, connectBtn, viewBtn);

        card.getChildren().addAll(header, footer);
        return card;
    }

    @FXML
    private void handleDashboard() {
        SceneUtil.switchScene("dashboard.fxml");
    }

    @FXML
    private void handleMyProfile() {
        SceneUtil.switchScene("profile-view.fxml");
    }

    @FXML
    private void handlePlaceholder() {
        new Alert(Alert.AlertType.INFORMATION, "This feature is coming soon!").show();
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
