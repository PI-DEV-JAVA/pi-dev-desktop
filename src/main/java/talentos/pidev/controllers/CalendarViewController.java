package talentos.pidev.controllers;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarSource;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import talentos.pidev.models.dao.InterviewMeetDAO;
import talentos.pidev.models.schema.InterviewMeet;

import java.io.IOException;
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
            // It is safer to load meetings AFTER the view is initialized
            loadMeetings();
            startClockThread();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupCalendarView() {
        calendarView = new CalendarView();
        interviewCalendar = new Calendar("Interviews");
        
        // --- THE FIX: CONNECT CALENDAR TO VIEW ---
        CalendarSource mySource = new CalendarSource("My Sources");
        mySource.getCalendars().add(interviewCalendar);
        calendarView.getCalendarSources().setAll(mySource);
        
        // Ensure the calendar is "checked" so entries show up
        interviewCalendar.setReadOnly(false);

        // 1. Force the Month View only
        calendarView.showMonthPage(); 
        
        // 2. Hide unnecessary UI elements
        // calendarView.setShowAddCalendarSourceMenuItem(false);
        calendarView.setShowPrintButton(false);
        calendarView.setShowPageSwitcher(false);      
        calendarView.setShowSourceTrayButton(false);  
        calendarView.setShowSearchField(false);       
        
        calendarContainer.getChildren().add(calendarView);

        calendarView.setEntryDetailsCallback(param -> {
            Entry<?> entry = param.getEntry();
            
            // Retrieve the original DB object we stored earlier
            InterviewMeet meet = (InterviewMeet) entry.getUserObject();
            
            if (meet != null) {
                System.out.println("Joining Meeting ID: " + meet.getId());
                
                joinMeet(meet);
            }
            
            return null;
        });
    }

    public void loadMeetings() {
        // Fetch data
        List<InterviewMeet> meets = meetDAO.findByUserId(1);
        System.out.println("Meets found in DB: " + (meets != null ? meets.size() : 0));

        Platform.runLater(() -> {
            interviewCalendar.clear();
            if (meets != null) {
                for (InterviewMeet meet : meets) {
                    Entry<InterviewMeet> entry = new Entry<>("Meet: " + meet.getUuid());
                    
                    // Set full interval correctly
                    entry.setInterval(meet.getScheduledAt(), meet.getScheduledAt().plusHours(1));
                    
                    entry.setUserObject(meet);
                    interviewCalendar.addEntry(entry);
                }
            }
        });
    }

    private void startClockThread() {
        Thread thread = new Thread(() -> {
            while (true) {
                Platform.runLater(() -> calendarView.setTime(LocalTime.now()));
                try {
                    Thread.sleep(60000); 
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void joinMeet(InterviewMeet meet){
        try {
        // 1. Load the FXML file
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/videoPreview.fxml"));
        Parent root = loader.load();

        MediaController controller = loader.getController();
        
        controller.initData(meet);

        Stage stage = new Stage();
        stage.setTitle("Interview Meeting - " + meet.getUuid());
        stage.setScene(new Scene(root));
        stage.show();

    } catch (IOException e) {
        System.err.println("Could not open the meeting window: " + e.getMessage());
        e.printStackTrace();
    }
    }
}