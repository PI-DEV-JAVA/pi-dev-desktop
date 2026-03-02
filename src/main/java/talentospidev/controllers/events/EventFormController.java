package talentospidev.controllers.events;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import talentospidev.dao.eventsDAO.EventDAO;
import talentospidev.models.User;
import talentospidev.models.events.Event;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;
import talentospidev.utils.ViewContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventFormController {

    @FXML private Label pageTitleLbl, errorLbl;
    @FXML private TextField titleField, capacityField, startTimeField, endTimeField, locationField, onlineLinkField;
    @FXML private TextArea descField;
    @FXML private ComboBox<String> typeBox;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private CheckBox onlineCheck;
    @FXML private VBox locationSection, onlineLinkSection, sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final EventDAO eventDAO = new EventDAO();
    private Event editing = null;

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        typeBox.setItems(FXCollections.observableArrayList("MEETUP", "CONFERENCE", "WORKSHOP", "WEBINAR"));
        typeBox.setValue("MEETUP");
        startDatePicker.setValue(LocalDate.now().plusDays(7));
        endDatePicker.setValue(LocalDate.now().plusDays(7));

        onlineCheck.selectedProperty().addListener((o, a, v) -> {
            locationSection.setVisible(!v); locationSection.setManaged(!v);
            onlineLinkSection.setVisible(v); onlineLinkSection.setManaged(v);
        });

        // Edit mode
        int evId = ViewContext.getSelectedEventId();
        if (evId > 0) {
            try {
                editing = eventDAO.getById(evId);
                if (editing != null) {
                    pageTitleLbl.setText("✏ Edit Event");
                    titleField.setText(editing.getTitle());
                    descField.setText(editing.getDescription());
                    typeBox.setValue(editing.getEventType());
                    capacityField.setText(String.valueOf(editing.getMaxCapacity()));
                    if (editing.getEventDate() != null) {
                        startDatePicker.setValue(editing.getEventDate().toLocalDate());
                        startTimeField.setText(editing.getEventDate().toLocalTime().toString());
                    }
                    if (editing.getEndDate() != null) {
                        endDatePicker.setValue(editing.getEndDate().toLocalDate());
                        endTimeField.setText(editing.getEndDate().toLocalTime().toString());
                    }
                    onlineCheck.setSelected(editing.isOnline());
                    locationField.setText(editing.getLocation());
                    onlineLinkField.setText(editing.getOnlineLink());
                }
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    @FXML
    private void onSave() {
        StringBuilder err = new StringBuilder();
        if (titleField.getText().trim().isEmpty()) err.append("Title required. ");
        if (startDatePicker.getValue() == null) err.append("Start date required. ");
        try { LocalTime.parse(startTimeField.getText().trim()); } catch (Exception e) { err.append("Invalid start time (HH:mm). "); }
        try { Integer.parseInt(capacityField.getText().trim()); } catch (Exception e) { err.append("Capacity must be a number. "); }

        if (!err.isEmpty()) {
            errorLbl.setText("⚠ " + err.toString().trim());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
            return;
        }

        try {
            User u = AuthService.getCurrentUser();
            Event ev = editing != null ? editing : new Event();
            ev.setTitle(titleField.getText().trim());
            ev.setDescription(descField.getText());
            ev.setEventType(typeBox.getValue());
            ev.setMaxCapacity(Integer.parseInt(capacityField.getText().trim()));
            ev.setEventDate(LocalDateTime.of(startDatePicker.getValue(), LocalTime.parse(startTimeField.getText().trim())));
            if (endDatePicker.getValue() != null && !endTimeField.getText().trim().isEmpty()) {
                try { ev.setEndDate(LocalDateTime.of(endDatePicker.getValue(), LocalTime.parse(endTimeField.getText().trim()))); } catch (Exception ignored) {}
            }
            ev.setOnline(onlineCheck.isSelected());
            ev.setLocation(locationField.getText().trim());
            ev.setOnlineLink(onlineLinkField.getText().trim());
            ev.setStatus("UPCOMING");

            if (editing != null) {
                eventDAO.update(ev);
            } else {
                ev.setOrganizerId(u != null ? u.getId() : 0);
                eventDAO.add(ev);
            }
            SceneUtil.switchScene("Events/EventsFeed.fxml");
        } catch (Exception ex) {
            ex.printStackTrace();
            errorLbl.setText("⚠ " + ex.getMessage());
            errorLbl.setVisible(true); errorLbl.setManaged(true);
        }
    }

    @FXML private void onCancel() { SceneUtil.switchScene("Events/EventsFeed.fxml"); }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleCourses()       { SceneUtil.switchScene("Courses/CoursesRH.fxml"); }
    @FXML private void handleEvents()        { SceneUtil.switchScene("Events/EventsFeed.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
