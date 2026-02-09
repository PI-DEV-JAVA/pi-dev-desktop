package talentos.pidev.models.dao;

import talentos.pidev.utils.DB;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

public class CandidateDAO {

    public Map<Long, String> getAllCandidates() {
        Map<Long, String> candidates = new LinkedHashMap<>(); 
        String sql = "SELECT id, full_name FROM user WHERE role = 'candidate'";

        try (Connection conn = DB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                candidates.put(rs.getLong("id"), rs.getString("full_name"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return candidates;
    }
}
