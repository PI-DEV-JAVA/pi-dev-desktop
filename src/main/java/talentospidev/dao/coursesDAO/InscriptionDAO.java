package talentospidev.dao.coursesDAO;

import talentospidev.models.courses.Inscription;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InscriptionDAO {
    private final Connection conn = DB.getConnection();

    public void add(Inscription i) throws SQLException {
        String sql = "INSERT INTO inscription (formation_id,user_id,candidat_nom,candidat_email,statut) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getFormationId());
            ps.setInt(2, i.getUserId());
            ps.setString(3, i.getCandidatNom());
            ps.setString(4, i.getCandidatEmail());
            ps.setString(5, i.getStatut() != null ? i.getStatut() : "EN_ATTENTE");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) i.setId(rs.getInt(1)); }
        }
    }

    public void updateStatut(int id, String statut) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE inscription SET statut=? WHERE id=?")) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void updateScore(int id, double score) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE inscription SET score_quiz=? WHERE id=?")) {
            ps.setDouble(1, score);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM inscription WHERE id=?")) { ps.setInt(1, id); ps.executeUpdate(); }
    }

    public List<Inscription> getByFormationId(int formationId) throws SQLException {
        List<Inscription> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM inscription WHERE formation_id=? ORDER BY date_inscription DESC")) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public List<Inscription> getByUserId(int userId) throws SQLException {
        List<Inscription> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM inscription WHERE user_id=? ORDER BY date_inscription DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public List<Inscription> getAcceptedByUserId(int userId) throws SQLException {
        List<Inscription> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM inscription WHERE user_id=? AND statut='ACCEPTEE' ORDER BY date_inscription DESC")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    /** Returns true only if user has a pending (EN_ATTENTE) enrollment */
    public boolean hasPendingEnrollment(int userId, int formationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM inscription WHERE user_id=? AND formation_id=? AND statut='EN_ATTENTE'")) {
            ps.setInt(1, userId);
            ps.setInt(2, formationId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        }
        return false;
    }

    /** Returns true if user's enrollment was accepted */
    public boolean isAccepted(int userId, int formationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM inscription WHERE user_id=? AND formation_id=? AND statut='ACCEPTEE'")) {
            ps.setInt(1, userId);
            ps.setInt(2, formationId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        }
        return false;
    }

    public boolean hasUserEnrolled(int userId, int formationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM inscription WHERE user_id=? AND formation_id=?")) {
            ps.setInt(1, userId);
            ps.setInt(2, formationId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        }
        return false;
    }

    /** Get the status of a user's enrollment in a course */
    public String getEnrollmentStatus(int userId, int formationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT statut FROM inscription WHERE user_id=? AND formation_id=? ORDER BY date_inscription DESC LIMIT 1")) {
            ps.setInt(1, userId);
            ps.setInt(2, formationId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getString("statut"); }
        }
        return null;
    }

    private Inscription map(ResultSet rs) throws SQLException {
        Inscription i = new Inscription();
        i.setId(rs.getInt("id"));
        i.setFormationId(rs.getInt("formation_id"));
        i.setUserId(rs.getInt("user_id"));
        i.setCandidatNom(rs.getString("candidat_nom"));
        i.setCandidatEmail(rs.getString("candidat_email"));
        Timestamp ts = rs.getTimestamp("date_inscription"); if (ts != null) i.setDateInscription(ts.toLocalDateTime());
        i.setStatut(rs.getString("statut"));
        double sc = rs.getDouble("score_quiz"); if (!rs.wasNull()) i.setScoreQuiz(sc);
        return i;
    }
}
