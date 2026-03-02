package talentos.pidev.dao;

import talentos.pidev.models.Question;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDAO {

    private final Connection connection;

    public QuestionDAO() {
        connection = DB.getInstance().getMyConnection();
    }

    public void add(Question q) throws SQLException {
        String sql = "INSERT INTO question(quiz_id, enonce) VALUES(?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, q.getQuizId());
            ps.setString(2, q.getEnonce());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) q.setId(rs.getInt(1));
            }
        }
    }

    public void update(Question q) throws SQLException {
        String sql = "UPDATE question SET enonce=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, q.getEnonce());
            ps.setInt(2, q.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM question WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public List<Question> getByQuiz(int quizId) throws SQLException {
        List<Question> list = new ArrayList<>();

        String sql =
                "SELECT q.id, q.quiz_id, q.enonce, q.is_published, " +
                        "       (SELECT COUNT(*) FROM choix c WHERE c.question_id=q.id) AS nb_choix, " +
                        "       (SELECT c.texte FROM choix c " +
                        "          WHERE c.question_id=q.id AND c.est_correct=1 LIMIT 1) AS correct_preview " +
                        "FROM question q " +
                        "WHERE q.quiz_id=? " +
                        "ORDER BY q.ordre ASC, q.id ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quizId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getInt("id"));
                    q.setQuizId(rs.getInt("quiz_id"));
                    q.setEnonce(rs.getString("enonce"));
                    q.setNbChoix(rs.getInt("nb_choix"));
                    q.setCorrectPreview(rs.getString("correct_preview"));
                    q.setPublished(rs.getInt("is_published") == 1);
                    list.add(q);
                }
            }
        }

        return list;
    }

    public void setPublished(int questionId, boolean published) throws SQLException {
        String sql = "UPDATE question SET is_published=? WHERE id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, published ? 1 : 0);
            ps.setInt(2, questionId);
            ps.executeUpdate();
        }
    }
    public List<Question> getPublishedByQuiz(int quizId) throws SQLException {
        List<Question> list = new ArrayList<>();

        String sql =
                "SELECT q.id, q.quiz_id, q.enonce, q.is_published, " +
                        "       (SELECT COUNT(*) FROM choix c WHERE c.question_id=q.id) AS nb_choix, " +
                        "       (SELECT c.texte FROM choix c WHERE c.question_id=q.id AND c.est_correct=1 LIMIT 1) AS correct_preview " +
                        "FROM question q " +
                        "WHERE q.quiz_id=? AND q.is_published=1 " +
                        "ORDER BY q.ordre ASC, q.id ASC";

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Question q = new Question();
                    q.setId(rs.getInt("id"));
                    q.setQuizId(rs.getInt("quiz_id"));
                    q.setEnonce(rs.getString("enonce"));
                    q.setNbChoix(rs.getInt("nb_choix"));
                    q.setCorrectPreview(rs.getString("correct_preview"));
                    q.setPublished(rs.getInt("is_published") == 1);
                    list.add(q);
                }
            }
        }

        return list;
    }
    // في QuestionDAO

    public int countByQuiz(int quizId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM question WHERE quiz_id=?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    /** ✅ true si le quiz a au moins 1 question ET au moins 1 choix lié */
    public boolean hasQuestionsAndChoices(int quizId) throws SQLException {
        // au moins 1 question ET au moins 1 choix associé
        String sql = """
        SELECT COUNT(*) AS cnt
        FROM question q
        JOIN choix c ON c.question_id = q.id
        WHERE q.quiz_id = ?
        """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, quizId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cnt") > 0;
            }
        }
        return false;
    }

}
