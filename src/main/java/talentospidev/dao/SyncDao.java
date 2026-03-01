package talentospidev.dao;

import talentospidev.models.Sync;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SyncDao {

    /** Send a sync request. */
    public void sendRequest(int senderId, int receiverId, String reason) {
        String sql = "INSERT INTO syncs (sender_id, receiver_id, reason) VALUES (?,?,?)";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, reason);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error sending sync request", e);
        }
    }

    /** Accept a sync request. */
    public void acceptRequest(int syncId) {
        String sql = "UPDATE syncs SET status = 'ACCEPTED', accepted_at = NOW() WHERE id = ?";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, syncId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error accepting sync", e);
        }
    }

    /** Decline a sync request. */
    public void declineRequest(int syncId) {
        String sql = "UPDATE syncs SET status = 'DECLINED' WHERE id = ?";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, syncId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error declining sync", e);
        }
    }

    /**
     * Check if any sync relationship exists between two users (in either
     * direction).
     */
    public Sync getSyncBetween(int userId1, int userId2) {
        String sql = """
                    SELECT s.*,
                           CONCAT(ps.first_name,' ',ps.last_name) AS sender_name, ps.professional_title AS sender_title, us.role AS sender_role,
                           CONCAT(pr.first_name,' ',pr.last_name) AS receiver_name, pr.professional_title AS receiver_title, ur.role AS receiver_role
                    FROM syncs s
                    JOIN users us ON s.sender_id = us.id LEFT JOIN profiles ps ON s.sender_id = ps.user_id
                    JOIN users ur ON s.receiver_id = ur.id LEFT JOIN profiles pr ON s.receiver_id = pr.user_id
                    WHERE (s.sender_id = ? AND s.receiver_id = ?) OR (s.sender_id = ? AND s.receiver_id = ?)
                    LIMIT 1
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            stmt.setInt(3, userId2);
            stmt.setInt(4, userId1);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return mapSync(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error checking sync", e);
        }
        return null;
    }

    /** All accepted syncs for a user (My Circle). */
    public List<Sync> getSyncsByUser(int userId) {
        List<Sync> list = new ArrayList<>();
        String sql = """
                    SELECT s.*,
                           CONCAT(ps.first_name,' ',ps.last_name) AS sender_name, ps.professional_title AS sender_title, us.role AS sender_role,
                           CONCAT(pr.first_name,' ',pr.last_name) AS receiver_name, pr.professional_title AS receiver_title, ur.role AS receiver_role
                    FROM syncs s
                    JOIN users us ON s.sender_id = us.id LEFT JOIN profiles ps ON s.sender_id = ps.user_id
                    JOIN users ur ON s.receiver_id = ur.id LEFT JOIN profiles pr ON s.receiver_id = pr.user_id
                    WHERE (s.sender_id = ? OR s.receiver_id = ?) AND s.status = 'ACCEPTED'
                    ORDER BY s.accepted_at DESC
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(mapSync(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error loading syncs", e);
        }
        return list;
    }

    /** Pending incoming requests for a user. */
    public List<Sync> getPendingForUser(int userId) {
        List<Sync> list = new ArrayList<>();
        String sql = """
                    SELECT s.*,
                           CONCAT(ps.first_name,' ',ps.last_name) AS sender_name, ps.professional_title AS sender_title, us.role AS sender_role,
                           CONCAT(pr.first_name,' ',pr.last_name) AS receiver_name, pr.professional_title AS receiver_title, ur.role AS receiver_role
                    FROM syncs s
                    JOIN users us ON s.sender_id = us.id LEFT JOIN profiles ps ON s.sender_id = ps.user_id
                    JOIN users ur ON s.receiver_id = ur.id LEFT JOIN profiles pr ON s.receiver_id = pr.user_id
                    WHERE s.receiver_id = ? AND s.status = 'PENDING'
                    ORDER BY s.created_at DESC
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                list.add(mapSync(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error loading pending syncs", e);
        }
        return list;
    }

    /** Count of accepted syncs for a user (for profile display). */
    public int getSyncCount(int userId) {
        String sql = "SELECT COUNT(*) FROM syncs WHERE (sender_id = ? OR receiver_id = ?) AND status = 'ACCEPTED'";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting syncs", e);
        }
        return 0;
    }

    /** Count of pending incoming requests (for badge). */
    public int getPendingCount(int userId) {
        String sql = "SELECT COUNT(*) FROM syncs WHERE receiver_id = ? AND status = 'PENDING'";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting pending", e);
        }
        return 0;
    }

    /** Breakdown by reason: e.g. {COLLABORATE: 3, MENTOR: 2, ...}. */
    public java.util.Map<String, Integer> getSyncCountByReason(int userId) {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = """
                    SELECT reason, COUNT(*) as cnt FROM syncs
                    WHERE (sender_id = ? OR receiver_id = ?) AND status = 'ACCEPTED'
                    GROUP BY reason
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                map.put(rs.getString("reason"), rs.getInt("cnt"));
        } catch (SQLException e) {
            throw new RuntimeException("Error counting by reason", e);
        }
        return map;
    }

    private Sync mapSync(ResultSet rs) throws SQLException {
        Sync s = new Sync();
        s.setId(rs.getInt("id"));
        s.setSenderId(rs.getInt("sender_id"));
        s.setReceiverId(rs.getInt("receiver_id"));
        s.setSenderName(rs.getString("sender_name"));
        s.setReceiverName(rs.getString("receiver_name"));
        s.setSenderTitle(rs.getString("sender_title"));
        s.setReceiverTitle(rs.getString("receiver_title"));
        s.setSenderRole(rs.getString("sender_role"));
        s.setReceiverRole(rs.getString("receiver_role"));
        s.setReason(rs.getString("reason"));
        s.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null)
            s.setCreatedAt(ts.toLocalDateTime());
        Timestamp as = rs.getTimestamp("accepted_at");
        if (as != null)
            s.setAcceptedAt(as.toLocalDateTime());
        return s;
    }
}
