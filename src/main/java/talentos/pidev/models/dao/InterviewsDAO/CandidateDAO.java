package talentos.pidev.models.dao.InterviewsDAO;

import talentos.pidev.utils.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class CandidateDAO {

    public Map<Long, String> getAllCandidates() {

        Map<Long, String> candidates = new LinkedHashMap<>();
        String sql = "SELECT id_user,email FROM users WHERE role = 'candidate'";

        try (
                Connection conn = DB.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery();
        ) {

            while (rs.next()) {
                candidates.put(
                        rs.getLong("id_user"),
                        rs.getString("email")
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return candidates;
    }
}