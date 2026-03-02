package talentospidev.dao.eventsDAO;

import talentospidev.models.events.EventParticipation;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventParticipationDAO {
    private final Connection conn = DB.getConnection();

    public void add(EventParticipation p) throws SQLException {
        String sql = "INSERT INTO event_participation (event_id,user_id,status,qr_code) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getEventId());
            ps.setInt(2, p.getUserId());
            ps.setString(3, p.getStatus() != null ? p.getStatus() : "PENDING");
            ps.setString(4, p.getQrCode());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        }
    }

    public void updateStatus(int eventId, int userId, String status) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE event_participation SET status=? WHERE event_id=? AND user_id=?")) {
            ps.setString(1, status);
            ps.setInt(2, eventId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void updateQrCode(int eventId, int userId, String qrCode) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE event_participation SET qr_code=? WHERE event_id=? AND user_id=?")) {
            ps.setString(1, qrCode);
            ps.setInt(2, eventId);
            ps.setInt(3, userId);
            ps.executeUpdate();
        }
    }

    public void delete(int eventId, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM event_participation WHERE event_id=? AND user_id=?")) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    public String getStatus(int eventId, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT status FROM event_participation WHERE event_id=? AND user_id=?")) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("status");
            }
        }
        return null;
    }

    public EventParticipation get(int eventId, int userId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM event_participation WHERE event_id=? AND user_id=?")) {
            ps.setInt(1, eventId);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<EventParticipation> getByEventId(int eventId) throws SQLException {
        List<EventParticipation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM event_participation WHERE event_id=? ORDER BY registered_at DESC")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<EventParticipation> getByUserId(int userId) throws SQLException {
        List<EventParticipation> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM event_participation WHERE user_id=? ORDER BY registered_at DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public int countByEventId(int eventId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM event_participation WHERE event_id=? AND status IN ('CONFIRMED','ATTENDED')")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countConfirmed(int eventId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM event_participation WHERE event_id=? AND status='CONFIRMED'")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public int countAttended(int eventId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM event_participation WHERE event_id=? AND status='ATTENDED'")) {
            ps.setInt(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private EventParticipation map(ResultSet rs) throws SQLException {
        EventParticipation p = new EventParticipation();
        p.setId(rs.getInt("id"));
        p.setEventId(rs.getInt("event_id"));
        p.setUserId(rs.getInt("user_id"));
        p.setStatus(rs.getString("status"));
        Timestamp ra = rs.getTimestamp("registered_at"); if (ra != null) p.setRegisteredAt(ra.toLocalDateTime());
        p.setQrCode(rs.getString("qr_code"));
        return p;
    }
}
