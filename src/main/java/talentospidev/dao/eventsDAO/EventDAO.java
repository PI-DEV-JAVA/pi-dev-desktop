package talentospidev.dao.eventsDAO;

import talentospidev.models.events.Event;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDAO {
    private final Connection conn = DB.getConnection();

    public void add(Event e) throws SQLException {
        String sql = "INSERT INTO event (title,description,event_type,event_date,end_date,location,latitude,longitude,is_online,online_link,max_capacity,cover_image,organizer_id,status) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getEventType());
            ps.setTimestamp(4, Timestamp.valueOf(e.getEventDate()));
            ps.setTimestamp(5, e.getEndDate() != null ? Timestamp.valueOf(e.getEndDate()) : null);
            ps.setString(6, e.getLocation());
            if (e.getLatitude() != null) ps.setDouble(7, e.getLatitude()); else ps.setNull(7, Types.DOUBLE);
            if (e.getLongitude() != null) ps.setDouble(8, e.getLongitude()); else ps.setNull(8, Types.DOUBLE);
            ps.setBoolean(9, e.isOnline());
            ps.setString(10, e.getOnlineLink());
            ps.setInt(11, e.getMaxCapacity());
            ps.setString(12, e.getCoverImage());
            ps.setInt(13, e.getOrganizerId());
            ps.setString(14, e.getStatus() != null ? e.getStatus() : "UPCOMING");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) e.setId(rs.getInt(1));
            }
        }
    }

    public void update(Event e) throws SQLException {
        String sql = "UPDATE event SET title=?,description=?,event_type=?,event_date=?,end_date=?,location=?,latitude=?,longitude=?,is_online=?,online_link=?,max_capacity=?,cover_image=?,status=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            ps.setString(3, e.getEventType());
            ps.setTimestamp(4, Timestamp.valueOf(e.getEventDate()));
            ps.setTimestamp(5, e.getEndDate() != null ? Timestamp.valueOf(e.getEndDate()) : null);
            ps.setString(6, e.getLocation());
            if (e.getLatitude() != null) ps.setDouble(7, e.getLatitude()); else ps.setNull(7, Types.DOUBLE);
            if (e.getLongitude() != null) ps.setDouble(8, e.getLongitude()); else ps.setNull(8, Types.DOUBLE);
            ps.setBoolean(9, e.isOnline());
            ps.setString(10, e.getOnlineLink());
            ps.setInt(11, e.getMaxCapacity());
            ps.setString(12, e.getCoverImage());
            ps.setString(13, e.getStatus());
            ps.setInt(14, e.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM event WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public Event getById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM event WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public List<Event> getAll() throws SQLException {
        List<Event> list = new ArrayList<>();
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM event ORDER BY event_date DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Event> getUpcoming() throws SQLException {
        List<Event> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM event WHERE status='UPCOMING' AND event_date >= NOW() ORDER BY event_date ASC")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Event> getByOrganizerId(int organizerId) throws SQLException {
        List<Event> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM event WHERE organizer_id=? ORDER BY event_date DESC")) {
            ps.setInt(1, organizerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Event map(ResultSet rs) throws SQLException {
        Event e = new Event();
        e.setId(rs.getInt("id"));
        e.setTitle(rs.getString("title"));
        e.setDescription(rs.getString("description"));
        e.setEventType(rs.getString("event_type"));
        Timestamp ed = rs.getTimestamp("event_date"); if (ed != null) e.setEventDate(ed.toLocalDateTime());
        Timestamp endd = rs.getTimestamp("end_date"); if (endd != null) e.setEndDate(endd.toLocalDateTime());
        e.setLocation(rs.getString("location"));
        double lat = rs.getDouble("latitude"); if (!rs.wasNull()) e.setLatitude(lat);
        double lon = rs.getDouble("longitude"); if (!rs.wasNull()) e.setLongitude(lon);
        e.setOnline(rs.getBoolean("is_online"));
        e.setOnlineLink(rs.getString("online_link"));
        e.setMaxCapacity(rs.getInt("max_capacity"));
        e.setCoverImage(rs.getString("cover_image"));
        e.setOrganizerId(rs.getInt("organizer_id"));
        e.setStatus(rs.getString("status"));
        Timestamp ca = rs.getTimestamp("created_at"); if (ca != null) e.setCreatedAt(ca.toLocalDateTime());
        return e;
    }
}
