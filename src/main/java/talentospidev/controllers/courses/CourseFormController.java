package talentospidev.controllers.courses;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import talentospidev.dao.coursesDAO.FormationDAO;
import talentospidev.models.User;
import talentospidev.models.courses.Formation;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

public class CourseFormController {

    @FXML private Label pageTitleLbl, errorLbl;
    @FXML private TextField nomField, categorieField, formateurField, lieuField, capaciteField, prerequisField;
    @FXML private TextArea descField, contenuField;
    @FXML private ComboBox<String> difficulteBox, modeBox;
    @FXML private DatePicker dateDebutPicker, dateFinPicker;
    @FXML private VBox sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final FormationDAO dao = new FormationDAO();
    private Formation editing = null;

    private static final String OK = "-fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12;";
    private static final String ERR = "-fx-border-color: #ef4444; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 8 12; -fx-background-color: #fef2f2;";

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        difficulteBox.setItems(FXCollections.observableArrayList("DEBUTANT", "INTERMEDIAIRE", "AVANCE"));
        modeBox.setItems(FXCollections.observableArrayList("EN_LIGNE", "PRESENTIEL", "HYBRIDE"));

        // Inline validation listeners
        nomField.textProperty().addListener((o, a, b) -> nomField.setStyle(b.trim().isEmpty() ? ERR : OK));
        categorieField.textProperty().addListener((o, a, b) -> categorieField.setStyle(b.trim().isEmpty() ? ERR : OK));
        formateurField.textProperty().addListener((o, a, b) -> formateurField.setStyle(b.trim().isEmpty() ? ERR : OK));

        // Load existing if editing
        int fId = ViewContext.getSelectedFormationId();
        if (fId > 0) {
            try {
                editing = dao.getById(fId);
                if (editing != null) {
                    pageTitleLbl.setText("✏ Edit Course");
                    nomField.setText(editing.getNom());
                    descField.setText(editing.getDescription());
                    categorieField.setText(editing.getCategorie());
                    difficulteBox.setValue(editing.getDifficulte());
                    modeBox.setValue(editing.getMode());
                    lieuField.setText(editing.getLieu());
                    formateurField.setText(editing.getFormateur());
                    prerequisField.setText(editing.getPrerequis());
                    capaciteField.setText(String.valueOf(editing.getCapaciteMax()));
                    dateDebutPicker.setValue(editing.getDateDebut());
                    dateFinPicker.setValue(editing.getDateFin());
                    contenuField.setText(editing.getContenu());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void onSave() {
        StringBuilder err = new StringBuilder();
        if (nomField.getText().trim().isEmpty()) { err.append("Name required. "); nomField.setStyle(ERR); }
        if (categorieField.getText().trim().isEmpty()) { err.append("Category required. "); categorieField.setStyle(ERR); }
        if (formateurField.getText().trim().isEmpty()) { err.append("Instructor required. "); formateurField.setStyle(ERR); }
        if (difficulteBox.getValue() == null) err.append("Difficulty required. ");
        if (modeBox.getValue() == null) err.append("Mode required. ");
        if (dateDebutPicker.getValue() == null) err.append("Start date required. ");
        if (dateFinPicker.getValue() == null) err.append("End date required. ");
        if (dateDebutPicker.getValue() != null && dateFinPicker.getValue() != null &&
                dateFinPicker.getValue().isBefore(dateDebutPicker.getValue())) err.append("End date must be after start. ");

        if (!err.isEmpty()) {
            errorLbl.setText("⚠ " + err.toString().trim());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
            return;
        }

        try {
            Formation f = editing != null ? editing : new Formation();
            f.setNom(nomField.getText().trim());
            f.setDescription(descField.getText());
            f.setCategorie(categorieField.getText().trim());
            f.setDifficulte(difficulteBox.getValue());
            f.setMode(modeBox.getValue());
            f.setLieu(lieuField.getText().trim());
            f.setFormateur(formateurField.getText().trim());
            f.setPrerequis(prerequisField.getText().trim());
            f.setCapaciteMax(capaciteField.getText().isEmpty() ? 0 : Integer.parseInt(capaciteField.getText().trim()));
            f.setDateDebut(dateDebutPicker.getValue());
            f.setDateFin(dateFinPicker.getValue());
            f.setContenu(contenuField.getText());
            f.setStatut("OUVERTE");
            User u = AuthService.getCurrentUser();
            if (u != null) f.setRecruiterId(u.getId());

            if (editing == null || editing.getId() == 0) dao.add(f); else dao.update(f);
            SceneUtil.switchScene("Courses/CoursesRH.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLbl.setText("⚠ " + ex.getMessage());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
        }
    }

    @FXML private void onCancel() { SceneUtil.switchScene("Courses/CoursesRH.fxml"); }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleCourses()       { SceneUtil.switchScene("Courses/CoursesRH.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
