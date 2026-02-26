package talentos.pidev.models.schema.Activity;

import java.time.LocalDate;

public class Activity {
    private int idActivity;
    private int employeeId;
    private int projectId;
    private LocalDate activityDate;
    private String description;
    private double hoursWorked;

    // Default constructor
    public Activity() {}

    // Constructor for new activity (without id)
    public Activity(int employeeId, int projectId, LocalDate activityDate, String description, double hoursWorked) {
        this.employeeId = employeeId;
        this.projectId = projectId;
        this.activityDate = activityDate;
        this.description = description;
        this.hoursWorked = hoursWorked;
    }

    // Constructor for existing activity (with id)
    public Activity(int idActivity, int employeeId, int projectId, LocalDate activityDate, String description, double hoursWorked) {
        this.idActivity = idActivity;
        this.employeeId = employeeId;
        this.projectId = projectId;
        this.activityDate = activityDate;
        this.description = description;
        this.hoursWorked = hoursWorked;
    }

    // Getters and Setters
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

    // Alias for backward compatibility
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

    // Alias for backward compatibility
    public int getHours() {
        return (int) hoursWorked;
    }

    public void setHours(int hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    @Override
    public String toString() {
        return "Activity{" +
                "idActivity=" + idActivity +
                ", employeeId=" + employeeId +
                ", projectId=" + projectId +
                ", activityDate=" + activityDate +
                ", description='" + description + '\'' +
                ", hoursWorked=" + hoursWorked +
                '}';
    }
}