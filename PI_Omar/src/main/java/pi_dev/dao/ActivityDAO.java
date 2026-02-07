package pi_dev.dao;

import pi_dev.config.DBConnection;
import pi_dev.model.Activity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, a.getEmployeeId());
            ps.setInt(2, a.getProjectId());
            ps.setDate(3, Date.valueOf(a.getDate()));
            ps.setString(4, a.getDescription());
            ps.setInt(5, a.getHours());

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
        String sql = "SELECT * FROM activities";

        try (Connection c = DBConnection.getConnection();
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

        try (Connection c = DBConnection.getConnection();
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

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, a.getEmployeeId());
            ps.setInt(2, a.getProjectId());
            ps.setDate(3, Date.valueOf(a.getDate()));
            ps.setString(4, a.getDescription());
            ps.setInt(5, a.getHours());
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

        try (Connection c = DBConnection.getConnection();
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

        try (Connection c = DBConnection.getConnection();
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

        try (Connection c = DBConnection.getConnection();
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
       MAPPER (CLEAN & REUSABLE)
       ========================= */
    private Activity mapResultSet(ResultSet rs) throws SQLException {
        Activity a = new Activity();
        a.setIdActivity(rs.getInt("id_activity"));
        a.setEmployeeId(rs.getInt("employee_id"));
        a.setProjectId(rs.getInt("project_id"));
        a.setDate(rs.getDate("activity_date").toLocalDate());
        a.setDescription(rs.getString("description"));
        a.setHours(rs.getInt("hours_worked"));
        return a;
    }
}
