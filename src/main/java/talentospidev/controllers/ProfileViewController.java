package talentospidev.controllers;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import talentospidev.dao.ProfileDao;
import talentospidev.dao.UserDao;
import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.services.AuthService;
import talentospidev.services.EmailService;
import talentospidev.utils.SceneUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class ProfileViewController {

    // Avatar directory inside user home — portable, no absolute DB paths
    private static final String AVATARS_DIR = System.getProperty("user.home") + File.separator + ".talentos"
            + File.separator + "avatars";

    @FXML
    private VBox completionBarContainer;
    @FXML
    private ImageView avatarImageView;
    @FXML
    private Label avatarLabel;
    @FXML
    private Text nameText;
    @FXML
    private Text titleText;
    @FXML
    private Label locationLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label verifiedBadge;
    @FXML
    private Text summaryText;
    @FXML
    private Label experienceLabel;
    @FXML
    private Label joinDateLabel;
    @FXML
    private Label birthDateLabel;
    @FXML
    private Button toDoTab;
    @FXML
    private Button activitiesTab;
    @FXML
    private Button projectsTab;
    @FXML
    private FlowPane skillsFlowPane;
    @FXML
    private TextField skillInput;

    private final ProfileDao profileDao = new ProfileDao();
    private final UserDao userDao = new UserDao();
    private final talentospidev.dao.SkillDao skillDao = new talentospidev.dao.SkillDao();

    @FXML
    public void initialize() {
        User user = AuthService.getCurrentUser();
        if (user == null) {
            SceneUtil.switchScene("login.fxml");
            return;
        }

        // Toggle sidebar tabs based on role
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);

        Profile profile = profileDao.findByUserId(user.getId());

        // Build completion bar
        buildCompletionBar(profile, user);

        // Load avatar
        loadAvatar(user.getId(), profile);

        // Email verified badge
        if (user.isEmailVerified()) {
            verifiedBadge.setVisible(true);
            verifiedBadge.setManaged(true);
        }

        if (profile != null && profile.getFirstName() != null && !profile.getFirstName().isEmpty()) {
            String fullName = profile.getFirstName() + " " + profile.getLastName();
            nameText.setText(fullName);
            avatarLabel.setText(profile.getFirstName().substring(0, 1).toUpperCase());
            titleText.setText(profile.getProfessionalTitle() != null ? profile.getProfessionalTitle() : "No Title");
            locationLabel.setText("📍 " + (profile.getLocation() != null ? profile.getLocation() : "—"));
            phoneLabel.setText("📞 " + (profile.getPhoneNumber() != null ? profile.getPhoneNumber() : "—"));
            emailLabel.setText("✉️ " + user.getEmail());
            summaryText.setText(profile.getSummary() != null && !profile.getSummary().isEmpty()
                    ? profile.getSummary()
                    : "No summary provided.");
            experienceLabel.setText(profile.getYearsOfExperience() + " Years");
            if (profile.getBirthDate() != null)
                birthDateLabel.setText(profile.getBirthDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        } else {
            nameText.setText("Incomplete Profile");
            titleText.setText("Complete your profile to stand out!");
            avatarLabel.setText("?");
            locationLabel.setText("📍 —");
            phoneLabel.setText("📞 —");
            emailLabel.setText("✉️ " + user.getEmail());
            summaryText.setText(
                    "Your profile is not yet complete. Click 'Edit Profile' below or use the completion guide above.");
            experienceLabel.setText("—");
        }

        if (user.getCreatedAt() != null) {
            joinDateLabel.setText(user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMM yyyy")));
        }

        // Load skills
        loadSkills(user.getId());
    }

    // ===== AVATAR LOADING =====

    private void loadAvatar(int userId, Profile profile) {
        // Try to load from app-internal avatars directory
        File avatarFile = new File(AVATARS_DIR, userId + ".png");
        if (!avatarFile.exists()) {
            avatarFile = new File(AVATARS_DIR, userId + ".jpg");
        }
        // Fallback: try old absolute path from DB
        if (!avatarFile.exists() && profile != null && profile.getProfilePicturePath() != null) {
            avatarFile = new File(profile.getProfilePicturePath());
        }

        if (avatarFile.exists()) {
            try {
                Image img = new Image(avatarFile.toURI().toString(), 90, 90, true, true);
                avatarImageView.setImage(img);
                // Circular clip
                Circle clip = new Circle(45, 45, 45);
                avatarImageView.setClip(clip);
                avatarImageView.setVisible(true);
                avatarLabel.setVisible(false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // ===== COMPLETION BAR =====

    private void buildCompletionBar(Profile profile, User user) {
        completionBarContainer.getChildren().clear();

        int pct = (profile != null) ? profile.getCompletionPercentage(user.isEmailVerified()) : 20;

        boolean infoFilled = profile != null && profile.getFirstName() != null && !profile.getFirstName().isEmpty()
                && profile.getLastName() != null && !profile.getLastName().isEmpty()
                && profile.getPhoneNumber() != null && !profile.getPhoneNumber().isEmpty()
                && profile.getProfessionalTitle() != null && !profile.getProfessionalTitle().isEmpty()
                && profile.getLocation() != null && !profile.getLocation().isEmpty();
        boolean hasPicture = profile != null && profile.getProfilePicturePath() != null
                && !profile.getProfilePicturePath().isEmpty();
        boolean emailVerified = user.isEmailVerified();
        boolean hasCv = profile != null && profile.getCvPath() != null && !profile.getCvPath().isEmpty();

        // Also check app-internal avatar
        if (!hasPicture) {
            File av = new File(AVATARS_DIR, user.getId() + ".png");
            if (!av.exists())
                av = new File(AVATARS_DIR, user.getId() + ".jpg");
            hasPicture = av.exists();
        }

        VBox card = new VBox(16);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 24; " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);");
        card.setMaxWidth(800);

        // Header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        Label headerLabel = new Label("Profile Completion");
        headerLabel.setStyle("-fx-font-size: 17px; -fx-font-weight: 700; -fx-text-fill: #111827;");
        Region hSpacer = new Region();
        HBox.setHgrow(hSpacer, Priority.ALWAYS);
        String pctColor = pct >= 90 ? "#22c55e" : pct >= 60 ? "#f59e0b" : "#ef4444";
        Label pctLabel = new Label(pct + "%");
        pctLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: " + pctColor + ";");
        header.getChildren().addAll(headerLabel, hSpacer, pctLabel);

        // Progress bar
        StackPane barContainer = new StackPane();
        barContainer.setAlignment(Pos.CENTER_LEFT);
        Region barBg = new Region();
        barBg.setMinHeight(8);
        barBg.setMaxHeight(8);
        barBg.setStyle("-fx-background-color: #f3f4f6; -fx-background-radius: 4;");
        Region barFill = new Region();
        barFill.setMinHeight(8);
        barFill.setMaxHeight(8);
        String barGradient = pct >= 90 ? "#22c55e, #16a34a" : pct >= 60 ? "#f59e0b, #d97706" : "#ef4444, #dc2626";
        barFill.setStyle(
                "-fx-background-color: linear-gradient(to right, " + barGradient + "); -fx-background-radius: 4;");
        barFill.maxWidthProperty().bind(barContainer.widthProperty().multiply(pct / 100.0));
        barContainer.getChildren().addAll(barBg, barFill);

        // Milestone steps
        VBox steps = new VBox(8);
        steps.getChildren().add(createMilestoneRow("👤", "Account Created", "+20%", true, null));
        steps.getChildren().add(createMilestoneRow("📝", "Complete Profile Info", "+40%", infoFilled,
                infoFilled ? null : createActionBtn("Edit Profile", "#6366f1", e -> handleEdit())));
        steps.getChildren().add(createMilestoneRow("📸", "Upload Profile Picture", "+10%", hasPicture,
                hasPicture ? null : createActionBtn("Upload Photo", "#8b5cf6", e -> handleUploadPhoto())));
        steps.getChildren().add(createMilestoneRow("✉️", "Verify Email", "+20%", emailVerified,
                emailVerified ? null : createActionBtn("Verify Now", "#3b82f6", e -> handleVerifyEmail(user))));
        steps.getChildren().add(createMilestoneRow("📄", "Upload CV", "+10%", hasCv,
                hasCv ? null : createActionBtn("Upload CV", "#22c55e", e -> handleUploadCV())));

        card.getChildren().addAll(header, barContainer, steps);
        completionBarContainer.getChildren().add(card);
    }

    private HBox createMilestoneRow(String icon, String label, String pctText, boolean completed, Button actionBtn) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(8, 12, 8, 12));
        row.setStyle("-fx-background-color: " + (completed ? "#f0fdf4" : "#fafbfc") + "; -fx-background-radius: 10;");

        Label iconLbl = new Label(icon);
        iconLbl.setStyle("-fx-font-size: 18px;");
        VBox info = new VBox(1);
        HBox.setHgrow(info, Priority.ALWAYS);
        Label nameLbl = new Label(label);
        nameLbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: "
                + (completed ? "#16a34a" : "#374151") + ";");
        Label pctLbl = new Label(pctText);
        pctLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #9ca3af;");
        info.getChildren().addAll(nameLbl, pctLbl);
        Label statusLbl = new Label(completed ? "✅" : "⬜");
        statusLbl.setStyle("-fx-font-size: 16px;");
        row.getChildren().addAll(iconLbl, info, statusLbl);
        if (actionBtn != null)
            row.getChildren().add(actionBtn);
        return row;
    }

    private Button createActionBtn(String text, String color,
            javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 11px; " +
                "-fx-font-weight: 700; -fx-padding: 6 14; -fx-background-radius: 8; -fx-cursor: hand;");
        btn.setOnAction(handler);
        return btn;
    }

    // ===== PROFILE ACTIONS =====

    @FXML
    private void handleEdit() {
        SceneUtil.switchScene("update-profile.fxml");
    }

    // ═══ SKILLS MANAGEMENT ═══

    private void loadSkills(int userId) {
        skillsFlowPane.getChildren().clear();
        var skills = skillDao.getSkills(userId);
        if (skills.isEmpty()) {
            Label noSkills = new Label("No skills added yet");
            noSkills.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12px; -fx-font-style: italic;");
            skillsFlowPane.getChildren().add(noSkills);
            return;
        }
        for (String skill : skills) {
            skillsFlowPane.getChildren().add(createSkillBadge(skill, userId));
        }
    }

    private HBox createSkillBadge(String skill, int userId) {
        HBox badge = new HBox(4);
        badge.setAlignment(javafx.geometry.Pos.CENTER);
        badge.setStyle("-fx-background-color: #eef2ff; -fx-background-radius: 12; -fx-padding: 4 10;");

        Label label = new Label(skill);
        label.setStyle("-fx-font-size: 11px; -fx-font-weight: 600; -fx-text-fill: #6366f1;");

        Label removeBtn = new Label("✕");
        removeBtn.setStyle("-fx-text-fill: #a5b4fc; -fx-font-size: 10px; -fx-cursor: hand;");
        removeBtn.setOnMouseClicked(e -> {
            skillDao.removeSkill(userId, skill);
            loadSkills(userId);
        });
        removeBtn.setOnMouseEntered(
                e -> removeBtn.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 10px; -fx-cursor: hand;"));
        removeBtn.setOnMouseExited(
                e -> removeBtn.setStyle("-fx-text-fill: #a5b4fc; -fx-font-size: 10px; -fx-cursor: hand;"));

        badge.getChildren().addAll(label, removeBtn);
        return badge;
    }

    @FXML
    private void handleAddSkill() {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;
        String skill = skillInput.getText();
        if (skill == null || skill.trim().isEmpty())
            return;
        skillDao.addSkill(user.getId(), skill.trim());
        skillInput.clear();
        loadSkills(user.getId());
    }

    private void handleUploadPhoto() {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Picture");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = chooser.showOpenDialog(completionBarContainer.getScene().getWindow());

        if (file != null) {
            try {
                // Ensure avatars directory exists
                Path avatarsPath = Paths.get(AVATARS_DIR);
                Files.createDirectories(avatarsPath);

                // Copy to app-internal directory with userId as filename
                String extension = file.getName().contains(".")
                        ? file.getName().substring(file.getName().lastIndexOf("."))
                        : ".png";
                Path dest = avatarsPath.resolve(user.getId() + extension);
                Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

                // Store only the filename in DB (not full path)
                Profile profile = profileDao.findByUserId(user.getId());
                if (profile != null) {
                    profile.setProfilePicturePath(user.getId() + extension);
                    profileDao.save(profile, user);
                }

                showAlert("Success", "Profile picture updated!", Alert.AlertType.INFORMATION);
                SceneUtil.switchScene("profile-view.fxml"); // Refresh
            } catch (IOException e) {
                showAlert("Error", "Failed to save avatar: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void handleUploadCV() {
        User user = AuthService.getCurrentUser();
        if (user == null)
            return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose your CV");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF Documents", "*.pdf"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File file = chooser.showOpenDialog(completionBarContainer.getScene().getWindow());

        if (file != null) {
            Profile profile = profileDao.findByUserId(user.getId());
            if (profile == null) {
                showAlert("Info", "Please complete your basic profile first.", Alert.AlertType.INFORMATION);
                return;
            }
            profile.setCvPath(file.getAbsolutePath());
            profileDao.save(profile, user);
            showAlert("Success", "CV uploaded successfully!", Alert.AlertType.INFORMATION);
            SceneUtil.switchScene("profile-view.fxml");
        }
    }

    private void handleVerifyEmail(User user) {
        String code = EmailService.sendVerificationCode(user.getEmail());
        if (code == null) {
            showAlert("Error", "Failed to send verification email.\n\n" +
                    "⚠️ Make sure the SMTP credentials are configured in EmailService.java.\n" +
                    "You need a Gmail App Password (not your normal password).\n\n" +
                    "Steps:\n1. Go to myaccount.google.com → Security → 2-Step Verification\n" +
                    "2. At the bottom, click 'App passwords'\n" +
                    "3. Generate a password for 'Mail' on 'Other (Talentos)'\n" +
                    "4. Paste the 16-char code into EmailService.SENDER_PASSWORD", Alert.AlertType.ERROR);
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Email Verification");
        dialog.setHeaderText("📧 Verification code sent to " + user.getEmail());
        dialog.setContentText("Enter the 6-digit code:");
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (result.get().trim().equals(code)) {
                userDao.setEmailVerified(user.getId(), true);
                user.setEmailVerified(true);
                showAlert("✅ Verified!", "Your email has been verified successfully!", Alert.AlertType.INFORMATION);
                SceneUtil.switchScene("profile-view.fxml");
            } else {
                showAlert("❌ Invalid Code", "The code you entered doesn't match.", Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleReset() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Reset Profile");
        alert.setHeaderText("Reset your profile?");
        alert.setContentText(
                "This will clear all your profile information (name, phone, summary, etc.) but keep your account.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            User user = AuthService.getCurrentUser();
            if (user != null) {
                profileDao.resetProfile(user.getId());
                // Also delete avatar file
                try {
                    Files.deleteIfExists(Paths.get(AVATARS_DIR, user.getId() + ".png"));
                    Files.deleteIfExists(Paths.get(AVATARS_DIR, user.getId() + ".jpg"));
                } catch (IOException ignored) {
                }
                SceneUtil.switchScene("profile-view.fxml");
            }
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Account");
        alert.setHeaderText("Delete your entire account?");
        alert.setContentText("This will permanently remove your account and all data. This cannot be undone.");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            User user = AuthService.getCurrentUser();
            if (user != null) {
                userDao.delete(user.getId());
                AuthService.logout();
                SceneUtil.switchScene("login.fxml");
            }
        }
    }

    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ===== SIDEBAR NAVIGATION =====
    @FXML
    private void handleBackToDashboard() {
        User user = AuthService.getCurrentUser();
        if (user != null && user.getRole() == User.Role.HR)
            SceneUtil.switchScene("recruiter_dashboard.fxml");
        else if (user != null && user.getRole() == User.Role.ADMIN)
            SceneUtil.switchScene("admin_dashboard.fxml");
        else
            SceneUtil.switchScene("dashboard.fxml");
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
        /* Already on profile */ }

    @FXML
    private void handleMyCircle() {
        talentospidev.utils.SceneUtil.switchScene("my_circle.fxml");
    }

    @FXML
    private void handleNotifications() {
        talentospidev.utils.SceneUtil.switchScene("notifications.fxml");
    }

    @FXML
    private void handleSettings() {
        talentospidev.utils.SceneUtil.switchScene("settings.fxml");
    }

    @FXML
    private void handleLogout() {
        AuthService.logout();
        SceneUtil.switchScene("login.fxml");
    }
}
