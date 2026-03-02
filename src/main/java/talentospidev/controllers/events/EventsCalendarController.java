package talentospidev.controllers.events;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import talentospidev.dao.eventsDAO.EventDAO;
import talentospidev.dao.eventsDAO.EventParticipationDAO;
import talentospidev.models.User;
import talentospidev.models.events.Event;
import talentospidev.models.events.EventParticipation;
import talentospidev.services.AuthService;
import talentospidev.utils.SceneUtil;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Calendar view showing the user's participated events using CalendarFX.
 */
public class EventsCalendarController {

    @FXML private StackPane calendarContainer;
    @FXML private VBox sidebar;
    @FXML private Button toDoTab, activitiesTab, projectsTab;

    private final EventDAO eventDAO = new EventDAO();
    private final EventParticipationDAO participationDAO = new EventParticipationDAO();

    @FXML
    public void initialize() {
        talentospidev.utils.SidebarUtil.configure(toDoTab, activitiesTab, projectsTab);
        loadCalendar();
    }

    private void loadCalendar() {
        try {
            User u = AuthService.getCurrentUser();
            if (u == null) return;

            CalendarView calendarView = new CalendarView();
            calendarView.setShowAddCalendarButton(false);
            calendarView.setShowPrintButton(false);
            calendarView.setShowSearchField(false);
            calendarView.setShowSourceTray(false);
            calendarView.setShowSourceTrayButton(false);

            Calendar eventsCalendar = new Calendar("My Events");
            eventsCalendar.setStyle(Calendar.Style.STYLE1);

            Calendar upcomingCalendar = new Calendar("Upcoming");
            upcomingCalendar.setStyle(Calendar.Style.STYLE2);

            // Load participated events
            List<EventParticipation> participations = participationDAO.getByUserId(u.getId());
            for (EventParticipation p : participations) {
                Event ev = eventDAO.getById(p.getEventId());
                if (ev != null && ev.getEventDate() != null) {
                    Entry<String> entry = new Entry<>(ev.getTitle());
                    entry.changeStartDate(ev.getEventDate().toLocalDate());
                    entry.changeStartTime(ev.getEventDate().toLocalTime());
                    if (ev.getEndDate() != null) {
                        entry.changeEndDate(ev.getEndDate().toLocalDate());
                        entry.changeEndTime(ev.getEndDate().toLocalTime());
                    } else {
                        entry.changeEndDate(ev.getEventDate().toLocalDate());
                        entry.changeEndTime(ev.getEventDate().toLocalTime().plusHours(2));
                    }
                    entry.setLocation(ev.isOnline() ? "Online" : ev.getLocation());
                    eventsCalendar.addEntry(entry);
                }
            }

            // Also load upcoming events not joined
            List<Event> upcoming = eventDAO.getUpcoming();
            for (Event ev : upcoming) {
                if (ev.getEventDate() != null) {
                    String status = participationDAO.getStatus(ev.getId(), u.getId());
                    if (status == null) {
                        Entry<String> entry = new Entry<>("🔓 " + ev.getTitle());
                        entry.changeStartDate(ev.getEventDate().toLocalDate());
                        entry.changeStartTime(ev.getEventDate().toLocalTime());
                        entry.changeEndDate(ev.getEventDate().toLocalDate());
                        entry.changeEndTime(ev.getEventDate().toLocalTime().plusHours(2));
                        upcomingCalendar.addEntry(entry);
                    }
                }
            }

            CalendarSource source = new CalendarSource("Events");
            source.getCalendars().addAll(eventsCalendar, upcomingCalendar);
            calendarView.getCalendarSources().add(source);
            calendarView.setToday(LocalDate.now());
            calendarView.setTime(LocalTime.now());

            calendarContainer.getChildren().add(calendarView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML private void onBack() { SceneUtil.switchScene("Events/EventsBrowse.fxml"); }

    @FXML private void handleDashboard()     { SceneUtil.switchScene("dashboard.fxml"); }
    @FXML private void handleJobOffers()     { SceneUtil.switchScene("OffersCardView.fxml"); }
    @FXML private void handleTrends()        { SceneUtil.switchScene("MarketTrendsView.fxml"); }
    @FXML private void handleInterviews()    { SceneUtil.switchScene("Interviews/InterviewView.fxml"); }
    @FXML private void handleCourses()       { SceneUtil.switchScene("Courses/CoursesBrowse.fxml"); }
    @FXML private void handleEvents()        { SceneUtil.switchScene("Events/EventsBrowse.fxml"); }
    @FXML private void handleMyCircle()      { SceneUtil.switchScene("my_circle.fxml"); }
    @FXML private void handleNotifications() { SceneUtil.switchScene("notifications.fxml"); }
    @FXML private void handleToDo()          { SceneUtil.switchScene("todo.fxml"); }
    @FXML private void handleActivities()    { SceneUtil.switchScene("activities/activities.fxml"); }
    @FXML private void handleProjects()      { SceneUtil.switchScene("projects/projects.fxml"); }
    @FXML private void handleMyProfile()     { SceneUtil.switchScene("profile-view.fxml"); }
    @FXML private void handleSettings()      { SceneUtil.switchScene("settings.fxml"); }
    @FXML private void handleLogout()        { AuthService.logout(); SceneUtil.switchScene("login.fxml"); }
}
