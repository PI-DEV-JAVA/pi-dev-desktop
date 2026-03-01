package talentos.pidev.dao;

import talentos.pidev.models.Seance;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeanceDAO {

    private final Connection cnx = DB.getInstance().getMyConnection();

    public int add(Seance s) throws SQLException {
        String sql = "INSERT INTO seance (formation_id,titre,type,date_debut,date_fin,adresse,latitude,longitude,video_path,duree_minutes,statut) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, s.getFormationId());
            ps.setString(2, s.getTitre());
            ps.setString(3, s.getType());
            ps.setTimestamp(4, Timestamp.valueOf(s.getDateDebut()));
            ps.setTimestamp(5, Timestamp.valueOf(s.getDateFin()));
            ps.setString(6, s.getAdresse());
            if (s.getLatitude() == null) ps.setNull(7, Types.DOUBLE); else ps.setDouble(7, s.getLatitude());
            if (s.getLongitude() == null) ps.setNull(8, Types.DOUBLE); else ps.setDouble(8, s.getLongitude());
            ps.setString(9, s.getVideoPath());
            if (s.getDureeMinutes() == null) ps.setNull(10, Types.INTEGER); else ps.setInt(10, s.getDureeMinutes());
            ps.setString(11, s.getStatut() == null ? "PLANIFIEE" : s.getStatut());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void update(Seance s) throws SQLException {
        String sql = "UPDATE seance SET titre=?, type=?, date_debut=?, date_fin=?, adresse=?, latitude=?, longitude=?, video_path=?, duree_minutes=?, statut=? " +
                "WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, s.getTitre());
            ps.setString(2, s.getType());
            ps.setTimestamp(3, Timestamp.valueOf(s.getDateDebut()));
            ps.setTimestamp(4, Timestamp.valueOf(s.getDateFin()));
            ps.setString(5, s.getAdresse());
            if (s.getLatitude() == null) ps.setNull(6, Types.DOUBLE); else ps.setDouble(6, s.getLatitude());
            if (s.getLongitude() == null) ps.setNull(7, Types.DOUBLE); else ps.setDouble(7, s.getLongitude());
            ps.setString(8, s.getVideoPath());
            if (s.getDureeMinutes() == null) ps.setNull(9, Types.INTEGER); else ps.setInt(9, s.getDureeMinutes());
            ps.setString(10, s.getStatut());
            ps.setInt(11, s.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM seance WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Seance> findByFormation(int formationId) throws SQLException {
        String sql = "SELECT * FROM seance WHERE formation_id=? ORDER BY date_debut ASC";
        List<Seance> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Seance map(ResultSet rs) throws SQLException {
        Seance s = new Seance();
        s.setId(rs.getInt("id"));
        s.setFormationId(rs.getInt("formation_id"));
        s.setTitre(rs.getString("titre"));
        s.setType(rs.getString("type"));

        Timestamp td = rs.getTimestamp("date_debut");
        Timestamp tf = rs.getTimestamp("date_fin");
        s.setDateDebut(td == null ? null : td.toLocalDateTime());
        s.setDateFin(tf == null ? null : tf.toLocalDateTime());

        s.setAdresse(rs.getString("adresse"));

        double lat = rs.getDouble("latitude");
        s.setLatitude(rs.wasNull() ? null : lat);

        double lon = rs.getDouble("longitude");
        s.setLongitude(rs.wasNull() ? null : lon);

        s.setVideoPath(rs.getString("video_path"));

        int d = rs.getInt("duree_minutes");
        s.setDureeMinutes(rs.wasNull() ? null : d);

        s.setStatut(rs.getString("statut"));
        return s;
    }
}