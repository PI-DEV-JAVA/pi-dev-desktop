package talentospidev.dao.interviewsDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import talentospidev.utils.DB;

public class CandidateDAO {

    /**
     * Returns candidates that the given recruiter has approved.
     * Joins applications → offers to find accepted applications
     * on offers created by this recruiter.
     */
    public Map<Long, String> getApprovedCandidates(int recruiterId) {
        Map<Long, String> candidates = new LinkedHashMap<>();
        String sql = """
            SELECT DISTINCT u.id, u.full_name
            FROM users u
            JOIN applications a ON a.user_id = u.id
            JOIN offers o ON o.id = a.offer_id
            WHERE o.recruiter_id = ?
              AND a.status LIKE '%accept%'
            ORDER BY u.full_name
        """;
        try {
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, recruiterId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                candidates.put(rs.getLong("id"), rs.getString("full_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return candidates;
    }

    /** Fallback: all candidates (legacy) */
    public Map<Long, String> getAllCandidates() {
        Map<Long, String> candidates = new LinkedHashMap<>();
        String sql = "SELECT id, full_name FROM users WHERE role = 'candidate'";
        try {
            Connection conn = DB.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                candidates.put(rs.getLong("id"), rs.getString("full_name"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return candidates;
    }
}
