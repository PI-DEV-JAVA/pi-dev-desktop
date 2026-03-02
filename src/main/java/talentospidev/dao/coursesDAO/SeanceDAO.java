package talentospidev.dao.coursesDAO;

import talentospidev.models.courses.Seance;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeanceDAO {
    private final Connection conn = DB.getConnection();

    public void add(Seance s) throws SQLException {
        String sql = "INSERT INTO seance (formation_id,titre,type,date_debut,date_fin,adresse,latitude,longitude,video_path,duree_minutes,statut) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getFormationId());
            ps.setString(2, s.getTitre());
            ps.setString(3, s.getType());
            ps.setTimestamp(4, Timestamp.valueOf(s.getDateDebut()));
            ps.setTimestamp(5, Timestamp.valueOf(s.getDateFin()));
            ps.setString(6, s.getAdresse());
            if (s.getLatitude() != null) ps.setDouble(7, s.getLatitude()); else ps.setNull(7, Types.DOUBLE);
            if (s.getLongitude() != null) ps.setDouble(8, s.getLongitude()); else ps.setNull(8, Types.DOUBLE);
            ps.setString(9, s.getVideoPath());
            if (s.getDureeMinutes() != null) ps.setInt(10, s.getDureeMinutes()); else ps.setNull(10, Types.INTEGER);
            ps.setString(11, s.getStatut());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) s.setId(rs.getInt(1)); }
        }
    }

    public void update(Seance s) throws SQLException {
        String sql = "UPDATE seance SET titre=?,type=?,date_debut=?,date_fin=?,adresse=?,latitude=?,longitude=?,video_path=?,duree_minutes=?,statut=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, s.getTitre());
            ps.setString(2, s.getType());
            ps.setTimestamp(3, Timestamp.valueOf(s.getDateDebut()));
            ps.setTimestamp(4, Timestamp.valueOf(s.getDateFin()));
            ps.setString(5, s.getAdresse());
            if (s.getLatitude() != null) ps.setDouble(6, s.getLatitude()); else ps.setNull(6, Types.DOUBLE);
            if (s.getLongitude() != null) ps.setDouble(7, s.getLongitude()); else ps.setNull(7, Types.DOUBLE);
            ps.setString(8, s.getVideoPath());
            if (s.getDureeMinutes() != null) ps.setInt(9, s.getDureeMinutes()); else ps.setNull(9, Types.INTEGER);
            ps.setString(10, s.getStatut());
            ps.setInt(11, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM seance WHERE id=?")) { ps.setInt(1, id); ps.executeUpdate(); }
    }

    public List<Seance> getByFormationId(int formationId) throws SQLException {
        List<Seance> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM seance WHERE formation_id=? ORDER BY date_debut ASC")) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public Seance getById(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM seance WHERE id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    private Seance map(ResultSet rs) throws SQLException {
        Seance s = new Seance();
        s.setId(rs.getInt("id"));
        s.setFormationId(rs.getInt("formation_id"));
        s.setTitre(rs.getString("titre"));
        s.setType(rs.getString("type"));
        Timestamp ts1 = rs.getTimestamp("date_debut"); if (ts1 != null) s.setDateDebut(ts1.toLocalDateTime());
        Timestamp ts2 = rs.getTimestamp("date_fin"); if (ts2 != null) s.setDateFin(ts2.toLocalDateTime());
        s.setAdresse(rs.getString("adresse"));
        double lat = rs.getDouble("latitude"); if (!rs.wasNull()) s.setLatitude(lat);
        double lon = rs.getDouble("longitude"); if (!rs.wasNull()) s.setLongitude(lon);
        s.setVideoPath(rs.getString("video_path"));
        int dur = rs.getInt("duree_minutes"); if (!rs.wasNull()) s.setDureeMinutes(dur);
        s.setStatut(rs.getString("statut"));
        return s;
    }
}
