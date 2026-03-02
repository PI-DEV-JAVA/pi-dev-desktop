package talentospidev.dao.coursesDAO;

import talentospidev.models.courses.TentativeQuiz;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TentativeDAO {
    private final Connection conn = DB.getConnection();

    public void add(TentativeQuiz t) throws SQLException {
        String sql = "INSERT INTO tentative_quiz (quiz_id,user_id,score,total) VALUES (?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, t.getQuizId());
            ps.setInt(2, t.getUserId());
            ps.setInt(3, t.getScore());
            ps.setInt(4, t.getTotal());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) t.setId(rs.getInt(1)); }
        }
    }

    public List<TentativeQuiz> getByUserAndQuiz(int userId, int quizId) throws SQLException {
        List<TentativeQuiz> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM tentative_quiz WHERE user_id=? AND quiz_id=? ORDER BY created_at DESC")) {
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) { while (rs.next()) list.add(map(rs)); }
        }
        return list;
    }

    public TentativeQuiz getBestAttempt(int userId, int quizId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM tentative_quiz WHERE user_id=? AND quiz_id=? ORDER BY score DESC LIMIT 1")) {
            ps.setInt(1, userId);
            ps.setInt(2, quizId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return map(rs); }
        }
        return null;
    }

    private TentativeQuiz map(ResultSet rs) throws SQLException {
        TentativeQuiz t = new TentativeQuiz();
        t.setId(rs.getInt("id"));
        t.setQuizId(rs.getInt("quiz_id"));
        t.setUserId(rs.getInt("user_id"));
        t.setScore(rs.getInt("score"));
        t.setTotal(rs.getInt("total"));
        Timestamp ts = rs.getTimestamp("created_at"); if (ts != null) t.setCreatedAt(ts.toLocalDateTime());
        return t;
    }
}
