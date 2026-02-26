package talentos.pidev.models.dao.ActivityDAO;

import talentos.pidev.utils.DB;
import talentos.pidev.models.schema.Activity.Activity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class ActivityDAO {

    /* =========================
       ADD ACTIVITY
       ========================= */
    public void add(Activity a) {
        String sql = """
            INSERT INTO activities 
            (employee_id, project_id, activity_date, description, hours_worked)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getEmployeeId());
            ps.setInt(2, a.getProjectId());
            ps.setDate(3, Date.valueOf(a.getActivityDate()));
            ps.setString(4, a.getDescription());
            ps.setDouble(5, a.getHoursWorked()); // Using getHoursWorked() which returns double

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    a.setIdActivity(keys.getInt(1));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* =========================
       GET ALL ACTIVITIES
       ========================= */
    public List<Activity> getAll() {
        List<Activity> list = new ArrayList<>();
        String sql = "SELECT * FROM activities ORDER BY activity_date DESC";

        try (Connection c = DB.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Activity a = mapResultSet(rs);
                list.add(a);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       GET ACTIVITIES BY PROJECT
       ========================= */
    public List<Activity> getByProjectId(int projectId) {
        List<Activity> list = new ArrayList<>();
        String sql = """
            SELECT * FROM activities
            WHERE project_id = ?
            ORDER BY activity_date DESC
        """;

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Activity a = mapResultSet(rs);
                list.add(a);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       UPDATE ACTIVITY
       ========================= */
    public void update(Activity a) {
        String sql = """
            UPDATE activities SET
            employee_id = ?,
            project_id = ?,
            activity_date = ?,
            description = ?,
            hours_worked = ?
            WHERE id_activity = ?
        """;

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, a.getEmployeeId());
            ps.setInt(2, a.getProjectId());
            ps.setDate(3, Date.valueOf(a.getActivityDate()));
            ps.setString(4, a.getDescription());
            ps.setDouble(5, a.getHoursWorked()); // Using getHoursWorked() which returns double
            ps.setInt(6, a.getIdActivity());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* =========================
       DELETE ACTIVITY
       ========================= */
    public void delete(int id) {
        String sql = "DELETE FROM activities WHERE id_activity = ?";

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /* =========================
       GET ACTIVITY BY ID
       ========================= */
    public Activity getById(int id) {
        String sql = "SELECT * FROM activities WHERE id_activity = ?";
        Activity a = null;

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                a = mapResultSet(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return a;
    }

    /* =========================
       GET BY EMPLOYEE
       ========================= */
    public List<Activity> getByEmployeeId(int employeeId) {
        List<Activity> list = new ArrayList<>();
        String sql = """
            SELECT * FROM activities
            WHERE employee_id = ?
            ORDER BY activity_date DESC
        """;

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       GET ACTIVITIES BY DATE RANGE
       ========================= */
    public List<Activity> getByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Activity> list = new ArrayList<>();
        String sql = """
            SELECT * FROM activities
            WHERE activity_date BETWEEN ? AND ?
            ORDER BY activity_date DESC
        """;

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    /* =========================
       GET TOTAL HOURS BY EMPLOYEE
       ========================= */
    public double getTotalHoursByEmployee(int employeeId) {
        String sql = "SELECT SUM(hours_worked) as total FROM activities WHERE employee_id = ?";
        double total = 0;

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }

    /* =========================
       GET TOTAL HOURS BY PROJECT
       ========================= */
    public double getTotalHoursByProject(int projectId) {
        String sql = "SELECT SUM(hours_worked) as total FROM activities WHERE project_id = ?";
        double total = 0;

        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return total;
    }

    /* =========================
       MAPPER (CLEAN & REUSABLE)
       ========================= */
    private Activity mapResultSet(ResultSet rs) throws SQLException {
        Activity a = new Activity();
        a.setIdActivity(rs.getInt("id_activity"));
        a.setEmployeeId(rs.getInt("employee_id"));
        a.setProjectId(rs.getInt("project_id"));
        a.setActivityDate(rs.getDate("activity_date").toLocalDate());
        a.setDescription(rs.getString("description"));
        a.setHoursWorked(rs.getDouble("hours_worked")); // Using setHoursWorked() which accepts double
        return a;
    }
}