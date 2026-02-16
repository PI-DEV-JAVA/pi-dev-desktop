package talentos.pidev.dao;

import talentos.pidev.models.Inscription;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class InscriptionDAO {

    private final Connection cnx;

    public InscriptionDAO() {
        cnx = DB.getInstance().getMyConnection();
    }

    public void addInscription(Inscription i) throws SQLException {
        String sql = "INSERT INTO inscription (formation_id, candidat_nom, candidat_email, statut) VALUES (?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, i.getFormationId());
            ps.setString(2, i.getCandidatNom());
            ps.setString(3, i.getCandidatEmail());
            ps.setString(4, i.getStatut() == null ? "EN_ATTENTE" : i.getStatut());
            ps.executeUpdate();
        }
    }

    public List<Inscription> getByFormation(int formationId) throws SQLException {
        String sql = "SELECT * FROM inscription WHERE formation_id=? ORDER BY date_inscription DESC";
        List<Inscription> list = new ArrayList<>();

        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, formationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Inscription i = new Inscription();
                    i.setId(rs.getInt("id"));
                    i.setFormationId(rs.getInt("formation_id"));
                    i.setCandidatNom(rs.getString("candidat_nom"));
                    i.setCandidatEmail(rs.getString("candidat_email"));

                    Timestamp ts = rs.getTimestamp("date_inscription");
                    i.setDateInscription(ts == null ? null : ts.toLocalDateTime());

                    i.setStatut(rs.getString("statut"));

                    Object score = rs.getObject("score_quiz");
                    i.setScoreQuiz(score == null ? null : rs.getDouble("score_quiz"));

                    list.add(i);
                }
            }
        }
        return list;
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM inscription WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public void updateStatut(int id, String statut) throws SQLException {
        String sql = "UPDATE inscription SET statut=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, statut);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }
}
