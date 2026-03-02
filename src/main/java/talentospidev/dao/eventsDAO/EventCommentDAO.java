package talentospidev.dao.eventsDAO;

import talentospidev.models.events.EventComment;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventCommentDAO {
    private final Connection conn = DB.getConnection();

    public void add(EventComment c) throws SQLException {
        String sql = "INSERT INTO event_comment (event_id, user_id, content) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getEventId());
            ps.setInt(2, c.getUserId());
            ps.setString(3, c.getContent());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM event_comment WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<EventComment> getByEventId(int eventId) throws SQLException {
        List<EventComment> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM event_comment WHERE event_id=? ORDER BY created_at ASC")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int countByEventId(int eventId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM event_comment WHERE event_id=?")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private EventComment map(ResultSet rs) throws SQLException {
        EventComment c = new EventComment();
        c.setId(rs.getInt("id"));
        c.setEventId(rs.getInt("event_id"));
        c.setUserId(rs.getInt("user_id"));
        c.setContent(rs.getString("content"));
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) c.setCreatedAt(ca.toLocalDateTime());
        return c;
    }
}
