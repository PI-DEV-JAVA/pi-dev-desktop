package talentospidev.dao.coursesDAO;

import talentospidev.models.courses.Choix;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChoixDAO {
    private final Connection conn = DB.getConnection();

    public void add(Choix c) throws SQLException {
        String sql = "INSERT INTO choix (question_id,texte,est_correct) VALUES (?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, c.getQuestionId());
            ps.setString(2, c.getTexte());
            ps.setBoolean(3, c.isEstCorrect());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) c.setId(rs.getInt(1)); }
        }
    }

    public void update(Choix c) throws SQLException {
        String sql = "UPDATE choix SET texte=?,est_correct=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getTexte());
            ps.setBoolean(2, c.isEstCorrect());
            ps.setInt(3, c.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM choix WHERE id=?")) { ps.setInt(1, id); ps.executeUpdate(); }
    }

    public List<Choix> getByQuestionId(int questionId) throws SQLException {
        List<Choix> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM choix WHERE question_id=? ORDER BY id ASC")) {
            ps.setInt(1, questionId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public void deleteByQuestionId(int questionId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM choix WHERE question_id=?")) { ps.setInt(1, questionId); ps.executeUpdate(); }
    }

    private Choix map(ResultSet rs) throws SQLException {
        Choix c = new Choix();
        c.setId(rs.getInt("id"));
        c.setQuestionId(rs.getInt("question_id"));
        c.setTexte(rs.getString("texte"));
        c.setEstCorrect(rs.getBoolean("est_correct"));
        return c;
    }
}
