package talentospidev.dao.ActivityDAO;

import talentospidev.utils.DB;
import talentospidev.models.Activity.Activity;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActivityDAO {

    // Existing methods (add, getAll, getByProjectId, update, delete, getById, getByEmployeeId, etc.)
    
    public void add(Activity a) {
        String sql = """
                    INSERT INTO activities
                    (employee_id, project_id, activity_date, description, hours_worked, 
                     start_time, last_activity_time, total_tracked_seconds, is_tracking)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, a.getEmployeeId());
            ps.setInt(2, a.getProjectId());
            ps.setDate(3, Date.valueOf(a.getActivityDate()));
            ps.setString(4, a.getDescription());
            ps.setDouble(5, a.getHoursWorked());
            ps.setTimestamp(6, a.getStartTime() != null ? Timestamp.valueOf(a.getStartTime()) : null);
            ps.setTimestamp(7, a.getLastActivityTime() != null ? Timestamp.valueOf(a.getLastActivityTime()) : null);
            ps.setLong(8, a.getTotalTrackedSeconds());
            ps.setBoolean(9, a.isTracking());
            
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
                    employee_id = ?, project_id = ?, activity_date = ?, description = ?, 
                    hours_worked = ?, start_time = ?, last_activity_time = ?, 
                    total_tracked_seconds = ?, is_tracking = ?
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
            ps.setTimestamp(6, a.getStartTime() != null ? Timestamp.valueOf(a.getStartTime()) : null);
            ps.setTimestamp(7, a.getLastActivityTime() != null ? Timestamp.valueOf(a.getLastActivityTime()) : null);
            ps.setLong(8, a.getTotalTrackedSeconds());
            ps.setBoolean(9, a.isTracking());
            ps.setInt(10, a.getIdActivity());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(int id) {
        // First delete tracking history
        String deleteHistory = "DELETE FROM activity_tracking_history WHERE activity_id = ?";
        try {
            Connection c = DB.getConnection();
            PreparedStatement ps = c.prepareStatement(deleteHistory);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Then delete the activity
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

    // ========== NEW TIME TRACKING METHODS ==========

    public void saveTrackingSession(int activityId, LocalDateTime sessionStart, 
                                    LocalDateTime sessionEnd, int secondsTracked) {
        String sql = "INSERT INTO activity_tracking_history (activity_id, session_start, session_end, seconds_tracked) " +
                     "VALUES (?, ?, ?, ?)";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, activityId);
            ps.setTimestamp(2, Timestamp.valueOf(sessionStart));
            ps.setTimestamp(3, sessionEnd != null ? Timestamp.valueOf(sessionEnd) : null);
            ps.setInt(4, secondsTracked);
            
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTrackingStatus(int activityId, boolean isTracking, LocalDateTime lastActivityTime) {
        String sql = "UPDATE activities SET is_tracking = ?, last_activity_time = ? WHERE id_activity = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setBoolean(1, isTracking);
            ps.setTimestamp(2, lastActivityTime != null ? Timestamp.valueOf(lastActivityTime) : null);
            ps.setInt(3, activityId);
            
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateTotalTrackedTime(int activityId) {
        String sql = "UPDATE activities a " +
                     "SET a.total_tracked_seconds = (" +
                     "    SELECT COALESCE(SUM(seconds_tracked), 0) " +
                     "    FROM activity_tracking_history " +
                     "    WHERE activity_id = ?" +
                     ") " +
                     "WHERE a.id_activity = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, activityId);
            ps.setInt(2, activityId);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getTrackingHistory(int activityId) {
        List<Map<String, Object>> history = new ArrayList<>();
        String sql = "SELECT * FROM activity_tracking_history WHERE activity_id = ? ORDER BY session_start DESC";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, activityId);
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> session = new HashMap<>();
                session.put("id", rs.getInt("id"));
                session.put("activity_id", rs.getInt("activity_id"));
                session.put("session_start", rs.getTimestamp("session_start").toLocalDateTime());
                Timestamp endTime = rs.getTimestamp("session_end");
                session.put("session_end", endTime != null ? endTime.toLocalDateTime() : null);
                session.put("seconds_tracked", rs.getInt("seconds_tracked"));
                session.put("created_at", rs.getTimestamp("created_at").toLocalDateTime());
                history.add(session);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }

    public Map<String, Object> getActivityTrackingSummary(int activityId) {
        Map<String, Object> summary = new HashMap<>();
        String sql = "SELECT " +
                     "    COUNT(*) as total_sessions, " +
                     "    COALESCE(SUM(seconds_tracked), 0) as total_seconds, " +
                     "    MIN(session_start) as first_session, " +
                     "    MAX(session_start) as last_session, " +
                     "    COUNT(CASE WHEN session_end IS NULL THEN 1 END) as active_sessions " +
                     "FROM activity_tracking_history " +
                     "WHERE activity_id = ?";
        
        try (Connection conn = DB.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, activityId);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                summary.put("total_sessions", rs.getInt("total_sessions"));
                summary.put("total_seconds", rs.getLong("total_seconds"));
                summary.put("total_hours", rs.getLong("total_seconds") / 3600.0);
                summary.put("first_session", rs.getTimestamp("first_session"));
                summary.put("last_session", rs.getTimestamp("last_session"));
                summary.put("active_sessions", rs.getInt("active_sessions"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return summary;
    }

    public List<Map<String, Object>> getAllActivitiesWithTracking() {
        List<Map<String, Object>> activities = new ArrayList<>();
        String sql = "SELECT " +
                     "    a.*, " +
                     "    COALESCE(SUM(h.seconds_tracked), 0) as actual_tracked_seconds, " +
                     "    COUNT(DISTINCT h.id) as session_count, " +
                     "    MAX(h.session_start) as last_tracking_session " +
                     "FROM activities a " +
                     "LEFT JOIN activity_tracking_history h ON a.id_activity = h.activity_id " +
                     "GROUP BY a.id_activity " +
                     "ORDER BY a.activity_date DESC";
        
        try (Connection conn = DB.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            
            while (rs.next()) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", rs.getInt("id_activity"));
                activity.put("employee_id", rs.getInt("employee_id"));
                activity.put("project_id", rs.getInt("project_id"));
                activity.put("description", rs.getString("description"));
                activity.put("assigned_hours", rs.getDouble("hours_worked"));
                activity.put("tracked_seconds", rs.getLong("actual_tracked_seconds"));
                activity.put("tracked_hours", rs.getLong("actual_tracked_seconds") / 3600.0);
                activity.put("session_count", rs.getInt("session_count"));
                activity.put("is_tracking", rs.getBoolean("is_tracking"));
                activity.put("last_tracking", rs.getTimestamp("last_tracking_session"));
                activities.add(activity);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activities;
    }

    private Activity mapResultSet(ResultSet rs) throws SQLException {
        Activity a = new Activity();
        a.setIdActivity(rs.getInt("id_activity"));
        a.setEmployeeId(rs.getInt("employee_id"));
        a.setProjectId(rs.getInt("project_id"));
        a.setActivityDate(rs.getDate("activity_date").toLocalDate());
        a.setDescription(rs.getString("description"));
        a.setHoursWorked(rs.getDouble("hours_worked"));
        
        // Map new time tracking fields
        Timestamp startTime = rs.getTimestamp("start_time");
        if (startTime != null) {
            a.setStartTime(startTime.toLocalDateTime());
        }
        
        Timestamp lastActivity = rs.getTimestamp("last_activity_time");
        if (lastActivity != null) {
            a.setLastActivityTime(lastActivity.toLocalDateTime());
        }
        
        a.setTotalTrackedSeconds(rs.getLong("total_tracked_seconds"));
        a.setTracking(rs.getBoolean("is_tracking"));
        
        return a;
    }
}