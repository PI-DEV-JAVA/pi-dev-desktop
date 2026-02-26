package talentos.pidev.dao;

import talentos.pidev.models.TentativeQuiz;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.time.LocalDateTime;

public class TentativeDAO {

    private final Connection connection;

    public TentativeDAO() {
        connection = DB.getInstance().getMyConnection();
    }

    public void add(TentativeQuiz t) throws SQLException {
        String sql = "INSERT INTO tentative_quiz(quiz_id, candidat_email, score, total, started_at, finished_at) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, t.getQuizId());
            ps.setString(2, t.getCandidatEmail());
            ps.setInt(3, t.getScore());
            ps.setInt(4, t.getTotal());

            LocalDateTime start = t.getStartedAt() != null ? t.getStartedAt() : LocalDateTime.now();
            ps.setTimestamp(5, Timestamp.valueOf(start));

            if (t.getFinishedAt() != null) ps.setTimestamp(6, Timestamp.valueOf(t.getFinishedAt()));
            else ps.setNull(6, Types.TIMESTAMP);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setId(rs.getInt(1));
            }
        }
    }
}
