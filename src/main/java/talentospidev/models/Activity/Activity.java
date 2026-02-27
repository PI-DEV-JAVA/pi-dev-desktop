package talentospidev.models.Activity;

import java.time.LocalDate;

public class Activity {
    private int idActivity;
    private int employeeId;
    private int projectId;
    private LocalDate activityDate;
    private String description;
    private double hoursWorked;

    public Activity() {
    }

    public Activity(int employeeId, int projectId, LocalDate activityDate, String description, double hoursWorked) {
        this.employeeId = employeeId;
        this.projectId = projectId;
        this.activityDate = activityDate;
        this.description = description;
        this.hoursWorked = hoursWorked;
    }

    public Activity(int idActivity, int employeeId, int projectId, LocalDate activityDate, String description,
            double hoursWorked) {
        this.idActivity = idActivity;
        this.employeeId = employeeId;
        this.projectId = projectId;
        this.activityDate = activityDate;
        this.description = description;
        this.hoursWorked = hoursWorked;
    }

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

    @Override
    public String toString() {
        return "Activity{id=" + idActivity + ", employeeId=" + employeeId +
                ", projectId=" + projectId + ", date=" + activityDate +
                ", desc='" + description + "', hours=" + hoursWorked + '}';
    }
}