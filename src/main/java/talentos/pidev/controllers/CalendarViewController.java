package talentos.pidev.controllers;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.StackPane;
import talentos.pidev.models.dao.InterviewMeetDAO;
import talentos.pidev.models.schema.InterviewMeet;

import java.sql.SQLException;
import java.time.LocalTime;
import java.util.List;

public class CalendarViewController {
    @FXML
    private StackPane calendarContainer;

    private CalendarView calendarView;
    private Calendar interviewCalendar;
    private InterviewMeetDAO meetDAO;

    public void initialize() {
        try {
            meetDAO = new InterviewMeetDAO();
            setupCalendarView();
            loadMeetings();
            startClockThread();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupCalendarView() {
        calendarView = new CalendarView();
        interviewCalendar = new Calendar("Interviews");
        
        // 1. Force the Month View only
        calendarView.showMonthPage(); 
        
        // 2. Hide unnecessary UI elements for a cleaner look
        // calendarView.setShowAddCalendarSourceMenuItem(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowPageSwitcher(false);      
        calendarView.setShowSourceTrayButton(false);  
        calendarView.setShowSearchField(false);       
        
       calendarView.getMonthPage();
        
        calendarContainer.getChildren().add(calendarView);
    }

    public void loadMeetings() {
        interviewCalendar.clear();
        List<InterviewMeet> meets = meetDAO.findByUserId(1);

        for (InterviewMeet meet : meets) {
            Entry<InterviewMeet> entry = new Entry<>(meet.getUuid());
            
            entry.changeStartDate(meet.getScheduledAt().toLocalDate());
            entry.changeStartTime(meet.getScheduledAt().toLocalTime());
            
            entry.changeEndDate(meet.getScheduledAt().toLocalDate());
            entry.changeEndTime(meet.getScheduledAt().toLocalTime().plusHours(1));
            
            entry.setUserObject(meet);
            
            interviewCalendar.addEntry(entry);
        }
    }

    private void startClockThread() {
        Thread thread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> calendarView.setTime(LocalTime.now()));
                try {
                    Thread.sleep(60000); // Update every minute
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    
    public void joinSelectedMeeting(InterviewMeet meet) {
        // PythonManager.startScripts(String.valueOf(meet.getId()));
    }
}
