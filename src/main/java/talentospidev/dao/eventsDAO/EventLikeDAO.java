package talentospidev.dao.eventsDAO;

import talentospidev.models.events.EventLike;
import talentospidev.utils.DB;

import java.sql.*;

public class EventLikeDAO {
    private final Connection conn = DB.getConnection();

    public void add(int eventId, int userId) throws SQLException {
        String sql = "INSERT IGNORE INTO event_like (event_id, user_id) VALUES (?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public void delete(int eventId, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM event_like WHERE event_id=? AND user_id=?")) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public boolean hasLiked(int eventId, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM event_like WHERE event_id=? AND user_id=?")) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countByEventId(int eventId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM event_like WHERE event_id=?")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void toggle(int eventId, int userId) throws SQLException {
        if (hasLiked(eventId, userId)) {
            delete(eventId, userId);
        } else {
            add(eventId, userId);
        }
    }
}
