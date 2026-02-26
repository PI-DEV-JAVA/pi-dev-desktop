package talentos.pidev.dao;

import talentos.pidev.models.Choix;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChoixDAO {

    private final Connection cnx;

    public ChoixDAO() {
        cnx = DB.getInstance().getMyConnection();
    }

    public List<Choix> getByQuestion(int questionId) throws SQLException {
        List<Choix> list = new ArrayList<>();
        String sql = "SELECT id, question_id, texte, est_correct, created_at " +
                "FROM choix WHERE question_id=? ORDER BY id ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Choix c = new Choix();
                    c.setId(rs.getInt("id"));
                    c.setQuestionId(rs.getInt("question_id"));
                    c.setTexte(rs.getString("texte"));
                    c.setEstCorrect(rs.getInt("est_correct") == 1);
                    Timestamp ts = rs.getTimestamp("created_at");
                    if (ts != null) c.setCreatedAt(ts.toLocalDateTime());
                    list.add(c);
                }
            }
        }
        return list;
    }

    public void add(Choix c) throws SQLException {
        String sql = "INSERT INTO choix(question_id, texte, est_correct) VALUES(?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getQuestionId());
            ps.setString(2, c.getTexte());
            ps.setInt(3, c.isEstCorrect() ? 1 : 0);
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
        }

        // si on ajoute un choix correct => rendre les autres incorrect
        if (c.isEstCorrect()) {
            setCorrectUnique(c.getId(), c.getQuestionId());
        }
    }

    public void update(Choix c) throws SQLException {
        String sql = "UPDATE choix SET texte=?, est_correct=? WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, c.getTexte());
            ps.setInt(2, c.isEstCorrect() ? 1 : 0);
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        }

        if (c.isEstCorrect()) {
            setCorrectUnique(c.getId(), c.getQuestionId());
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM choix WHERE id=?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** ✅ Rend ce choix correct et tous les autres du même questionId => incorrect */
    public void setCorrectUnique(int choixId, int questionId) throws SQLException {
        cnx.setAutoCommit(false);
        try {
            try (PreparedStatement ps1 = cnx.prepareStatement(
                    "UPDATE choix SET est_correct=0 WHERE question_id=?")) {
                ps1.setInt(1, questionId);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = cnx.prepareStatement(
                    "UPDATE choix SET est_correct=1 WHERE id=?")) {
                ps2.setInt(1, choixId);
                ps2.executeUpdate();
            }

            cnx.commit();
        } catch (SQLException e) {
            cnx.rollback();
            throw e;
        } finally {
            cnx.setAutoCommit(true);
        }
    }
}