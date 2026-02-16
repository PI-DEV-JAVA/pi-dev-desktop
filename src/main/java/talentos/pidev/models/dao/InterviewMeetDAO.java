package talentos.pidev.models.dao;

import talentos.pidev.models.schema.InterviewMeet;
import talentos.pidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InterviewMeetDAO {

    private Connection conn;

    public InterviewMeetDAO() throws SQLException {
        this.conn = DB.getConnection();
    }

    public void create(InterviewMeet meet) {
        String sql = """
            INSERT INTO interview_meet (interview_id, meet_uuid, scheduled_at, status, grade)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, meet.getInterviewId());
            ps.setString(2, meet.getUuid());
            ps.setTimestamp(3, Timestamp.valueOf(meet.getScheduledAt()));
            ps.setString(4, meet.getStatus());
            if (meet.getGrade() != null)
                ps.setDouble(5, meet.getGrade());
            else
                ps.setNull(5, Types.DECIMAL);

            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) meet.setId(rs.getLong(1));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void update(InterviewMeet meet) {
        String sql = """
            UPDATE interview_meet
            SET interview_id=?, meet_uuid=?, scheduled_at=?, status=?, grade=?
            WHERE id=?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, meet.getInterviewId());
            ps.setString(2, meet.getUuid());
            ps.setTimestamp(3, Timestamp.valueOf(meet.getScheduledAt()));
            ps.setString(4, meet.getStatus());
            if (meet.getGrade() != null)
                ps.setDouble(5, meet.getGrade());
            else
                ps.setNull(5, Types.DECIMAL);
            ps.setLong(6, meet.getId());

            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM interview_meet WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public InterviewMeet findById(long id) {
        String sql = "SELECT * FROM interview_meet WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                InterviewMeet meet = mapResultSet(rs);
                return meet;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<InterviewMeet> listByInterview(long interviewId) {
        String sql = "SELECT * FROM interview_meet WHERE interview_id=? ORDER BY scheduled_at ASC";
        List<InterviewMeet> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, interviewId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private InterviewMeet mapResultSet(ResultSet rs) throws SQLException {
        InterviewMeet meet = new InterviewMeet();
        meet.setId(rs.getLong("id"));
        meet.setInterviewId(rs.getLong("interview_id"));
        meet.setUuid(rs.getString("meet_uuid"));
        meet.setScheduledAt(rs.getTimestamp("scheduled_at").toLocalDateTime());
        meet.setStatus(rs.getString("status"));
        double grade = rs.getDouble("grade");
        if (!rs.wasNull()) meet.setGrade(grade);
        return meet;
    }
}
