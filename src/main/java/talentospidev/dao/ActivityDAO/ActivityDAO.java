package talentospidev.dao.ActivityDAO;

import talentospidev.utils.DB;
import talentospidev.models.Activity.Activity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;

public class ActivityDAO {

    public void add(Activity a) {
        String sql = """
                    INSERT INTO activities
                    (employee_id, project_id, activity_date, description, hours_worked)
                    VALUES (?, ?, ?, ?, ?)
                """;
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, a.getEmployeeId());
            ps.setInt(2, a.getProjectId());
            ps.setDate(3, Date.valueOf(a.getActivityDate()));
            ps.setString(4, a.getDescription());
            ps.setDouble(5, a.getHoursWorked());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next())
                a.setIdActivity(keys.getInt(1));
            keys.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Activity> getAll() {
        List<Activity> list = new ArrayList<>();
        String sql = "SELECT * FROM activities ORDER BY activity_date DESC";
        try {
            Connection c = DB.getConnection();
            Statement st = c.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next())
                list.add(mapResultSet(rs));
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Activity> getByProjectId(int projectId) {
        List<Activity> list = new ArrayList<>();
        String sql = "SELECT * FROM activities WHERE project_id = ? ORDER BY activity_date DESC";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapResultSet(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void update(Activity a) {
        String sql = """
                    UPDATE activities SET
                    employee_id = ?, project_id = ?, activity_date = ?, description = ?, hours_worked = ?
                    WHERE id_activity = ?
                """;
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, a.getEmployeeId());
            ps.setInt(2, a.getProjectId());
            ps.setDate(3, Date.valueOf(a.getActivityDate()));
            ps.setString(4, a.getDescription());
            ps.setDouble(5, a.getHoursWorked());
            ps.setInt(6, a.getIdActivity());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM activities WHERE id_activity = ?";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Activity getById(int id) {
        String sql = "SELECT * FROM activities WHERE id_activity = ?";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Activity a = null;
            if (rs.next())
                a = mapResultSet(rs);
            rs.close();
            ps.close();
            return a;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Activity> getByEmployeeId(int employeeId) {
        List<Activity> list = new ArrayList<>();
        String sql = "SELECT * FROM activities WHERE employee_id = ? ORDER BY activity_date DESC";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapResultSet(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Activity> getByDateRange(LocalDate startDate, LocalDate endDate) {
        List<Activity> list = new ArrayList<>();
        String sql = "SELECT * FROM activities WHERE activity_date BETWEEN ? AND ? ORDER BY activity_date DESC";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapResultSet(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public double getTotalHoursByEmployee(int employeeId) {
        String sql = "SELECT SUM(hours_worked) as total FROM activities WHERE employee_id = ?";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, employeeId);
            ResultSet rs = ps.executeQuery();
            double total = 0;
            if (rs.next())
                total = rs.getDouble("total");
            rs.close();
            ps.close();
            return total;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public double getTotalHoursByProject(int projectId) {
        String sql = "SELECT SUM(hours_worked) as total FROM activities WHERE project_id = ?";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql);
            ps.setInt(1, projectId);
            ResultSet rs = ps.executeQuery();
            double total = 0;
            if (rs.next())
                total = rs.getDouble("total");
            rs.close();
            ps.close();
            return total;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Activity mapResultSet(ResultSet rs) throws SQLException {
        Activity a = new Activity();
        a.setIdActivity(rs.getInt("id_activity"));
        a.setEmployeeId(rs.getInt("employee_id"));
        a.setProjectId(rs.getInt("project_id"));
        a.setActivityDate(rs.getDate("activity_date").toLocalDate());
        a.setDescription(rs.getString("description"));
        a.setHoursWorked(rs.getDouble("hours_worked"));
        return a;
    }
}