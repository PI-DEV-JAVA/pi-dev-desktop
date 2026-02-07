package pi_dev.model;

import javafx.beans.property.*;
import java.time.LocalDate;

public class Activity {

    private final IntegerProperty idActivity = new SimpleIntegerProperty();
    private final IntegerProperty employeeId = new SimpleIntegerProperty();
    private final IntegerProperty projectId = new SimpleIntegerProperty();
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final StringProperty description = new SimpleStringProperty();
    private final IntegerProperty hours = new SimpleIntegerProperty();

    // ===== CONSTRUCTORS =====

    public Activity() {}

    public Activity(int employeeId, int projectId, LocalDate date, String description, int hours) {
        this.employeeId.set(employeeId);
        this.projectId.set(projectId);
        this.date.set(date);
        this.description.set(description);
        this.hours.set(hours);
    }

    public Activity(int idActivity, int employeeId, int projectId,
                    LocalDate date, String description, int hours) {
        this.idActivity.set(idActivity);
        this.employeeId.set(employeeId);
        this.projectId.set(projectId);
        this.date.set(date);
        this.description.set(description);
        this.hours.set(hours);
    }

    // ===== GETTERS / SETTERS =====

    public int getIdActivity() { return idActivity.get(); }
    public void setIdActivity(int id) { this.idActivity.set(id); }
    public IntegerProperty idActivityProperty() { return idActivity; }

    public int getEmployeeId() { return employeeId.get(); }
    public void setEmployeeId(int id) { this.employeeId.set(id); }
    public IntegerProperty employeeIdProperty() { return employeeId; }

    public int getProjectId() { return projectId.get(); }
    public void setProjectId(int id) { this.projectId.set(id); }
    public IntegerProperty projectIdProperty() { return projectId; }

    public LocalDate getDate() { return date.get(); }
    public void setDate(LocalDate d) { this.date.set(d); }
    public ObjectProperty<LocalDate> dateProperty() { return date; }

    public String getDescription() { return description.get(); }
    public void setDescription(String d) { this.description.set(d); }
    public StringProperty descriptionProperty() { return description; }

    public int getHours() { return hours.get(); }
    public void setHours(int h) { this.hours.set(h); }
    public IntegerProperty hoursProperty() { return hours; }

    @Override
    public String toString() {
        return "Activity{" +
                "id=" + getIdActivity() +
                ", emp=" + getEmployeeId() +
                ", project=" + getProjectId() +
                ", date=" + getDate() +
                ", hours=" + getHours() +
                '}';
    }
}
