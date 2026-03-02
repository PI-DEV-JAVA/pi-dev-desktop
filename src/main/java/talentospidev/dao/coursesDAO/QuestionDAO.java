package talentospidev.dao.coursesDAO;

import talentospidev.models.courses.Question;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {
    private final Connection conn = DB.getConnection();

    public void add(Question q) throws SQLException {
        String sql = "INSERT INTO question (quiz_id,enonce,points,ordre,is_published) VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q.getQuizId());
            ps.setString(2, q.getEnonce());
            ps.setInt(3, q.getPoints());
            ps.setInt(4, q.getOrdre());
            ps.setBoolean(5, q.isPublished());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) q.setId(rs.getInt(1)); }
        }
    }

    public void update(Question q) throws SQLException {
        String sql = "UPDATE question SET enonce=?,points=?,ordre=?,is_published=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, q.getEnonce());
            ps.setInt(2, q.getPoints());
            ps.setInt(3, q.getOrdre());
            ps.setBoolean(4, q.isPublished());
            ps.setInt(5, q.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM question WHERE id=?")) { ps.setInt(1, id); ps.executeUpdate(); }
    }

    public List<Question> getByQuizId(int quizId) throws SQLException {
        List<Question> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM question WHERE quiz_id=? ORDER BY ordre ASC")) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public boolean hasQuestionsAndChoices(int quizId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM question q JOIN choix c ON c.question_id=q.id WHERE q.quiz_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1) > 0; }
        }
        return false;
    }

    private Question map(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setId(rs.getInt("id"));
        q.setQuizId(rs.getInt("quiz_id"));
        q.setEnonce(rs.getString("enonce"));
        q.setPoints(rs.getInt("points"));
        q.setOrdre(rs.getInt("ordre"));
        q.setPublished(rs.getBoolean("is_published"));
        return q;
    }
}
