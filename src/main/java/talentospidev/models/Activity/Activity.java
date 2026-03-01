package talentospidev.models.Activity;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Activity {
    private int idActivity;
    private int employeeId;
    private int projectId;
    private LocalDate activityDate;
    private String description;
    private double hoursWorked;
    
    // New time tracking fields
    private LocalDateTime startTime;
    private LocalDateTime lastActivityTime;
    private long totalTrackedSeconds;
    private boolean isTracking;

    public Activity() {
        this.totalTrackedSeconds = 0;
        this.isTracking = false;
    }

    public Activity(int employeeId, int projectId, LocalDate activityDate, String description, double hoursWorked) {
        this.employeeId = employeeId;
        this.projectId = projectId;
        this.activityDate = activityDate;
        this.description = description;
        this.hoursWorked = hoursWorked;
        this.totalTrackedSeconds = 0;
        this.isTracking = false;
    }

    public Activity(int idActivity, int employeeId, int projectId, LocalDate activityDate, String description,
            double hoursWorked) {
        this.idActivity = idActivity;
        this.employeeId = employeeId;
        this.projectId = projectId;
        this.activityDate = activityDate;
        this.description = description;
        this.hoursWorked = hoursWorked;
        this.totalTrackedSeconds = 0;
        this.isTracking = false;
    }

    // Existing getters and setters
    public int getIdActivity() {
        return idActivity;
    }

    public void setIdActivity(int idActivity) {
        this.idActivity = idActivity;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public int getProjectId() {
        return projectId;
    }

    public void setProjectId(int projectId) {
        this.projectId = projectId;
    }

    public LocalDate getActivityDate() {
        return activityDate;
    }

    public void setActivityDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public LocalDate getDate() {
        return activityDate;
    }

    public void setDate(LocalDate activityDate) {
        this.activityDate = activityDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public int getHours() {
        return (int) hoursWorked;
    }

    public void setHours(int hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    // New getters and setters for time tracking
    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getLastActivityTime() {
        return lastActivityTime;
    }

    public void setLastActivityTime(LocalDateTime lastActivityTime) {
        this.lastActivityTime = lastActivityTime;
    }

    public long getTotalTrackedSeconds() {
        return totalTrackedSeconds;
    }

    public void setTotalTrackedSeconds(long totalTrackedSeconds) {
        this.totalTrackedSeconds = totalTrackedSeconds;
    }

    public boolean isTracking() {
        return isTracking;
    }

    public void setTracking(boolean tracking) {
        isTracking = tracking;
    }

    // Helper methods
    public double getTrackedHours() {
        return totalTrackedSeconds / 3600.0;
    }

    public double getCompletionPercentage() {
        if (hoursWorked <= 0) return 0;
        return (getTrackedHours() / hoursWorked) * 100;
    }

    @Override
    public String toString() {
        return "Activity{id=" + idActivity + ", employeeId=" + employeeId +
                ", projectId=" + projectId + ", date=" + activityDate +
                ", desc='" + description + "', hours=" + hoursWorked + 
                ", tracked=" + getTrackedHours() + "h" + 
                (isTracking ? " (active)" : "") + '}';
    }
}