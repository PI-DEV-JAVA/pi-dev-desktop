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
import talentospidev.models.UserViewModel;
import talentospidev.services.AuthService;
import talentospidev.utils.ProfilePopup;
import talentospidev.utils.SceneUtil;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminDashboardController {

    @FXML
    private ListView<UserViewModel> userListView;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> sortByBox;
    @FXML
    private ComboBox<String> sortOrderBox;
    @FXML
    private ComboBox<String> roleFilterBox;
    @FXML
    private Label totalUsersLabel;
    @FXML
    private Label activeUsersLabel;
    @FXML
    private Label candidatesLabel;
    @FXML
    private Label recruitersLabel;

    private final UserDao userDao = new UserDao();
    private ObservableList<UserViewModel> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        // Sort controls
        sortByBox.setItems(FXCollections.observableArrayList("Name", "Job", "Age", "Location"));
        sortByBox.setValue("Name");
        sortOrderBox.setItems(FXCollections.observableArrayList("ASC", "DESC"));
        sortOrderBox.setValue("ASC");
        roleFilterBox.setItems(FXCollections.observableArrayList("All", "CANDIDATE", "HR", "ADMIN"));
        roleFilterBox.setValue("All");

        setupListView();
        loadData();

        // Listeners
        searchField.textProperty().addListener((obs, o, n) -> applyFilterAndSort());
        sortByBox.valueProperty().addListener((obs, o, n) -> applyFilterAndSort());
        sortOrderBox.valueProperty().addListener((obs, o, n) -> applyFilterAndSort());
        roleFilterBox.valueProperty().addListener((obs, o, n) -> applyFilterAndSort());
    }

    private void setupListView() {
        userListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(UserViewModel item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0;");
                } else {
                    setGraphic(createUserCard(item));
                    setText(null);
                    setStyle("-fx-background-color: transparent; -fx-padding: 0 0 8 0;");
                }
            }
        });
    }

    private void loadData() {
        masterData.clear();
        masterData.addAll(userDao.findAllWithDetails());
        updateStats();
        applyFilterAndSort();
    }

    private void updateStats() {
        totalUsersLabel.setText(String.valueOf(masterData.size()));
        activeUsersLabel
                .setText(String.valueOf(masterData.stream().filter(u -> "active".equals(u.getStatus())).count()));
        candidatesLabel.setText(
                String.valueOf(masterData.stream().filter(u -> "CANDIDATE".equalsIgnoreCase(u.getRole())).count()));
        recruitersLabel
                .setText(String.valueOf(masterData.stream().filter(u -> "HR".equalsIgnoreCase(u.getRole())).count()));
    }

    private void applyFilterAndSort() {
        String query = searchField.getText();
        String sortBy = sortByBox.getValue();
        String sortOrder = sortOrderBox.getValue();
        String roleFilter = roleFilterBox.getValue();

        List<UserViewModel> filtered = masterData.stream()
                .filter(p -> {
                    // Role filter
                    if (roleFilter != null && !"All".equals(roleFilter) && !p.getRole().equalsIgnoreCase(roleFilter))
                        return false;
                    // Search filter
                    if (query == null || query.trim().isEmpty())
                        return true;
                    String lower = query.toLowerCase();
                    return p.getFullName().toLowerCase().contains(lower)
                            || p.getProfessionalTitle().toLowerCase().contains(lower)
                            || p.getLocation().toLowerCase().contains(lower)
                            || p.getEmail().toLowerCase().contains(lower);
                })
                .collect(Collectors.toList());

        Comparator<UserViewModel> comparator = getComparator(sortBy);
        if ("DESC".equals(sortOrder)) {
            comparator = comparator.reversed();
        }
        filtered.sort(comparator);

        userListView.setItems(FXCollections.observableArrayList(filtered));
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

    private VBox createUserCard(UserViewModel user) {
        VBox card = new VBox(10);
        card.getStyleClass().add("card");

        // ---- Header: Avatar + Info ----
        HBox header = new HBox(14);
        header.setAlignment(Pos.CENTER_LEFT);

        String initial = user.getFullName().equals("N/A") ? "?" : user.getFullName().substring(0, 1).toUpperCase();
        Label avatar = new Label(initial);
        avatar.getStyleClass().add("avatar-small");

        VBox info = new VBox(2);
        Label name = new Label(user.getFullName());
        name.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Label email = new Label(user.getEmail());
        email.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");

        info.getChildren().addAll(name, email);

        // Meta — right side
        VBox meta = new VBox(2);
        meta.setAlignment(Pos.CENTER_RIGHT);

        String titleStr = user.getProfessionalTitle().isEmpty() ? "No title" : user.getProfessionalTitle();
        Label title = new Label(titleStr);
        title.setStyle("-fx-font-size: 11px; -fx-text-fill: #6b7280;");

        if (!user.getLocation().isEmpty()) {
            Label loc = new Label("📍 " + user.getLocation());
            loc.setStyle("-fx-font-size: 11px; -fx-text-fill: #9ca3af;");
            meta.getChildren().add(loc);
        }
        meta.getChildren().add(title);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(avatar, info, spacer, meta);

        // ---- Footer: Badges + Actions ----
        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);

        // Role badge
        Label roleBadge = new Label(user.getRole());
        String badgeColor = switch (user.getRole().toUpperCase()) {
            case "ADMIN" -> "badge-green";
            case "HR" -> "badge-purple";
            default -> "badge-blue";
        };
        roleBadge.getStyleClass().addAll("badge", badgeColor);

        // Status badge
        Label statusBadge = new Label(user.getStatus().toUpperCase());
        statusBadge.getStyleClass().addAll("badge",
                "active".equals(user.getStatus()) ? "badge-green" : "badge-gray");

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);

        // Action buttons
        Button viewBtn = new Button("View Profile");
        viewBtn.getStyleClass().add("action-button");
        viewBtn.setOnAction(e -> ProfilePopup.show(user));

        Button toggleBtn = new Button("active".equals(user.getStatus()) ? "Deactivate" : "Activate");
        toggleBtn.getStyleClass().add("active".equals(user.getStatus()) ? "action-button-danger" : "action-button");
        toggleBtn.setOnAction(e -> {
            boolean currentlyActive = "active".equals(user.getStatus());
            userDao.setActive(user.getId(), !currentlyActive);
            loadData();
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("action-button-danger");
        deleteBtn.setOnAction(e -> {
            Optional<ButtonType> result = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete user \"" + user.getFullName() + "\"? This cannot be undone.")
                    .showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                userDao.delete(user.getId());
                loadData();
            }
        });

        footer.getChildren().addAll(roleBadge, statusBadge, footerSpacer, viewBtn, toggleBtn, deleteBtn);

        card.getChildren().addAll(header, footer);
        return card;
    }

    @FXML
    private void handleDashboard() {
        SceneUtil.switchScene("admin_dashboard.fxml");
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
