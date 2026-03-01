package talentospidev.dao;

import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;

import java.util.List;

public class SkillDao {

    /** Add a skill for a user. Ignores duplicates. */
    public void addSkill(int userId, String skill) {
        String sql = "INSERT IGNORE INTO user_skills (user_id, skill) VALUES (?,?)";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, skill.trim().toLowerCase());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding skill", e);
        }
    }

    /** Remove a skill. */
    public void removeSkill(int userId, String skill) {
        String sql = "DELETE FROM user_skills WHERE user_id = ? AND skill = ?";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, skill.trim().toLowerCase());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing skill", e);
        }
    }

    /** Get all skills for a user. */
    public List<String> getSkills(int userId) {
        List<String> skills = new ArrayList<>();
        String sql = "SELECT skill FROM user_skills WHERE user_id = ? ORDER BY skill";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                skills.add(rs.getString("skill"));
        } catch (SQLException e) {
            throw new RuntimeException("Error loading skills", e);
        }
        return skills;
    }

    /** Get shared skills between two users. */
    public List<String> getSharedSkills(int userId1, int userId2) {
        List<String> shared = new ArrayList<>();
        String sql = """
                    SELECT a.skill FROM user_skills a
                    INNER JOIN user_skills b ON a.skill = b.skill
                    WHERE a.user_id = ? AND b.user_id = ?
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId1);
            stmt.setInt(2, userId2);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                shared.add(rs.getString("skill"));
        } catch (SQLException e) {
            throw new RuntimeException("Error finding shared skills", e);
        }
        return shared;
    }

    /** Set multiple skills at once (replaces existing). */
    public void setSkills(int userId, List<String> skills) {
        try {
            // Remove old
            PreparedStatement del = DB.getConnection().prepareStatement("DELETE FROM user_skills WHERE user_id = ?");
            del.setInt(1, userId);
            del.executeUpdate();
            del.close();
            // Insert new
            String sql = "INSERT IGNORE INTO user_skills (user_id, skill) VALUES (?,?)";
            PreparedStatement ins = DB.getConnection().prepareStatement(sql);
            for (String skill : skills) {
                String clean = skill.trim().toLowerCase();
                if (!clean.isEmpty()) {
                    ins.setInt(1, userId);
                    ins.setString(2, clean);
                    ins.addBatch();
                }
            }
            ins.executeBatch();
            ins.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error setting skills", e);
        }
    }
}
