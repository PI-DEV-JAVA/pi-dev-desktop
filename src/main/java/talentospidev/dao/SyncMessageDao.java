package talentospidev.dao;

import talentospidev.models.SyncMessage;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SyncMessageDao {

    /** Send a message. */
    public void send(SyncMessage msg) {
        String sql = "INSERT INTO sync_messages (sync_id, sender_id, message) VALUES (?,?,?)";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, msg.getSyncId());
            stmt.setInt(2, msg.getSenderId());
            stmt.setString(3, msg.getMessage());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next())
                msg.setId(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Error sending message", e);
        }
    }

    /** Get all messages in a sync thread. */
    public List<SyncMessage> getMessages(int syncId) {
        List<SyncMessage> list = new ArrayList<>();
        String sql = """
                    SELECT m.*, CONCAT(p.first_name,' ',p.last_name) AS sender_name
                    FROM sync_messages m
                    LEFT JOIN profiles p ON m.sender_id = p.user_id
                    WHERE m.sync_id = ?
                    ORDER BY m.created_at ASC
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, syncId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SyncMessage m = new SyncMessage();
                m.setId(rs.getInt("id"));
                m.setSyncId(rs.getInt("sync_id"));
                m.setSenderId(rs.getInt("sender_id"));
                m.setSenderName(rs.getString("sender_name"));
                m.setMessage(rs.getString("message"));
                m.setRead(rs.getBoolean("is_read"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null)
                    m.setCreatedAt(ts.toLocalDateTime());
                list.add(m);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading messages", e);
        }
        return list;
    }

    /** Mark all messages in a sync as read for a user (messages sent by others). */
    public void markRead(int syncId, int userId) {
        String sql = "UPDATE sync_messages SET is_read = TRUE WHERE sync_id = ? AND sender_id != ?";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, syncId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking read", e);
        }
    }

    /** Total unread messages across all syncs for a user. */
    public int getUnreadCount(int userId) {
        String sql = """
                    SELECT COUNT(*) FROM sync_messages m
                    JOIN syncs s ON m.sync_id = s.id
                    WHERE (s.sender_id = ? OR s.receiver_id = ?)
                      AND m.sender_id != ?
                      AND m.is_read = FALSE
                      AND s.status = 'ACCEPTED'
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unread", e);
        }
        return 0;
    }

    /**
     * Get recent chats — one entry per sync, showing last message + unread count.
     * Used for the Pulse tab's chat list.
     */
    public List<SyncMessage> getRecentChats(int userId) {
        List<SyncMessage> list = new ArrayList<>();
        String sql = """
                    SELECT s.id AS sync_id,
                           CASE WHEN s.sender_id = ? THEN s.receiver_id ELSE s.sender_id END AS other_user_id,
                           CASE WHEN s.sender_id = ?
                                THEN CONCAT(pr.first_name,' ',pr.last_name)
                                ELSE CONCAT(ps.first_name,' ',ps.last_name)
                           END AS other_user_name,
                           (SELECT m2.message FROM sync_messages m2 WHERE m2.sync_id = s.id ORDER BY m2.created_at DESC LIMIT 1) AS last_message,
                           (SELECT m2.created_at FROM sync_messages m2 WHERE m2.sync_id = s.id ORDER BY m2.created_at DESC LIMIT 1) AS last_message_time,
                           (SELECT COUNT(*) FROM sync_messages m3 WHERE m3.sync_id = s.id AND m3.sender_id != ? AND m3.is_read = FALSE) AS unread_count
                    FROM syncs s
                    LEFT JOIN profiles ps ON s.sender_id = ps.user_id
                    LEFT JOIN profiles pr ON s.receiver_id = pr.user_id
                    WHERE (s.sender_id = ? OR s.receiver_id = ?) AND s.status = 'ACCEPTED'
                    HAVING last_message IS NOT NULL
                    ORDER BY last_message_time DESC
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);
            stmt.setInt(4, userId);
            stmt.setInt(5, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SyncMessage m = new SyncMessage();
                m.setSyncId(rs.getInt("sync_id"));
                m.setOtherUserId(rs.getInt("other_user_id"));
                m.setOtherUserName(rs.getString("other_user_name"));
                m.setMessage(rs.getString("last_message"));
                m.setUnreadCount(rs.getInt("unread_count"));
                Timestamp ts = rs.getTimestamp("last_message_time");
                if (ts != null)
                    m.setCreatedAt(ts.toLocalDateTime());
                list.add(m);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading recent chats", e);
        }
        return list;
    }
}
