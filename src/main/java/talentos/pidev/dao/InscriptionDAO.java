package talentos.pidev.dao;

import talentos.pidev.models.Inscription;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InscriptionDAO {

    private final Connection cnx = DB.getInstance().getMyConnection();

    // ========================= ADD =========================
    public int addInscription(Inscription i) throws SQLException {
        String sql = "INSERT INTO inscription (formation_id, candidat_nom, candidat_email, statut) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, i.getFormationId());
            ps.setString(2, i.getCandidatNom());
            ps.setString(3, i.getCandidatEmail());
            ps.setString(4, (i.getStatut() == null || i.getStatut().isBlank()) ? "EN_ATTENTE" : i.getStatut());

            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    // ========================= UPDATE STATUT =========================
    public void updateStatutInscription(int id, String statut) throws SQLException {
        String sql = "UPDATE inscription SET statut=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    // ========================= DELETE =========================
    public void deleteInscription(int id) throws SQLException {
        String sql = "DELETE FROM inscription WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ========================= GET ALL =========================
    public List<Inscription> getAllInscriptions() throws SQLException {
        String sql = "SELECT * FROM inscription ORDER BY created_at DESC";
        List<Inscription> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    // ========================= GET BY FORMATION =========================
    public List<Inscription> getInscriptionsByFormation(int formationId) throws SQLException {
        String sql = "SELECT * FROM inscription WHERE formation_id=? ORDER BY created_at DESC";
        List<Inscription> list = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    // ========================= EXISTS (email + formation) =========================
    public boolean exists(String candidatEmail, int formationId) throws SQLException {
        String sql = "SELECT 1 FROM inscription WHERE candidat_email=? AND formation_id=? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, candidatEmail);
            ps.setInt(2, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // ========================= OPTIONAL: GET ONE BY EMAIL+FORMATION =========================
    public Inscription findByEmailAndFormation(String candidatEmail, int formationId) throws SQLException {
        String sql = "SELECT * FROM inscription WHERE candidat_email=? AND formation_id=? LIMIT 1";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, candidatEmail);
            ps.setInt(2, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    // ========================= MAP =========================
    private Inscription map(ResultSet rs) throws SQLException {
        Inscription i = new Inscription();
        i.setId(rs.getInt("id"));
        i.setFormationId(rs.getInt("formation_id"));
        i.setCandidatNom(rs.getString("candidat_nom"));
        i.setCandidatEmail(rs.getString("candidat_email"));

        Timestamp tIns = rs.getTimestamp("date_inscription");
        i.setDateInscription(tIns == null ? null : tIns.toLocalDateTime());

        i.setStatut(rs.getString("statut"));

        double s = rs.getDouble("score_quiz");
        i.setScoreQuiz(rs.wasNull() ? null : s);

        Timestamp tCreated = rs.getTimestamp("created_at");
        i.setCreatedAt(tCreated == null ? null : tCreated.toLocalDateTime());

        return i;
    }

    // ========================= UPDATE SCORE QUIZ (formation + email) =========================
    public void updateScoreQuiz(int formationId, String candidatEmail, double scoreQuiz) throws SQLException {
        String sql = "UPDATE inscription SET score_quiz=? WHERE formation_id=? AND candidat_email=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, scoreQuiz);
            ps.setInt(2, formationId);
            ps.setString(3, candidatEmail);
            ps.executeUpdate();
        }
    }

    // (Optionnel mais utile) update score par inscription_id
    public void updateScoreQuizById(int inscriptionId, double scoreQuiz) throws SQLException {
        String sql = "UPDATE inscription SET score_quiz=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setDouble(1, scoreQuiz);
            ps.setInt(2, inscriptionId);
            ps.executeUpdate();
        }
    }
}