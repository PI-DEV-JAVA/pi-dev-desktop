package talentospidev.dao;

import talentospidev.models.Profile;
import talentospidev.models.User;
import talentospidev.utils.DB;

import java.sql.*;

public class ProfileDao {

    /**
     * [READ] Retrieves the profile for a specific user ID.
     */
    public Profile findByUserId(int userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";
        try {
            PreparedStatement stmt = DB.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            Profile p = null;
            if (rs.next()) {
                p = new Profile();
                p.setId(rs.getInt("id"));
                p.setFirstName(rs.getString("first_name"));
                p.setLastName(rs.getString("last_name"));
                Date dbDate = rs.getDate("birth_date");
                p.setBirthDate(dbDate != null ? dbDate.toLocalDate() : null);
                p.setPhoneNumber(rs.getString("phone_number"));
                p.setLocation(rs.getString("location"));
                p.setProfessionalTitle(rs.getString("professional_title"));
                p.setYearsOfExperience(rs.getInt("years_of_experience"));
                p.setSummary(rs.getString("summary"));
                p.setProfilePicturePath(rs.getString("profile_picture_path"));
                p.setCvPath(rs.getString("cv_path"));
                p.setProfileCompleted(rs.getBoolean("profile_completed"));
            }
            rs.close();
            stmt.close();
            return p;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * [CREATE] Creates an empty profile entry when a new user registers.
     */
    public void createInitialProfile(int userId) {
        String sql = "INSERT INTO profiles (user_id, profile_completed) VALUES (?, false)";
        try {
            PreparedStatement stmt = DB.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error creating initial profile", e);
        }
    }

    /**
     * [DELETE] Resets a profile by setting all fields to NULL.
     */
    public void resetProfile(int userId) {
        String sql = """
                    UPDATE profiles SET
                    first_name = NULL, last_name = NULL, birth_date = NULL,
                    phone_number = NULL, location = NULL, professional_title = NULL,
                    years_of_experience = 0, summary = NULL,
                    profile_picture_path = NULL, cv_path = NULL,
                    profile_completed = false
                    WHERE user_id = ?
                """;
        try {
            PreparedStatement stmt = DB.getConnection().prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error resetting profile", e);
        }
    }

    /**
     * [UPDATE] Updates or Inserts profile information.
     */
    public void save(Profile profile, User user) {
        String updateSql = """
                    UPDATE profiles
                    SET first_name=?, last_name=?, birth_date=?, phone_number=?,
                        location=?, professional_title=?, years_of_experience=?, summary=?,
                        profile_picture_path=?, cv_path=?, profile_completed=true
                    WHERE user_id=?
                """;
        try {
            PreparedStatement stmt = DB.getConnection().prepareStatement(updateSql);
            stmt.setString(1, profile.getFirstName());
            stmt.setString(2, profile.getLastName());
            if (profile.getBirthDate() != null)
                stmt.setDate(3, Date.valueOf(profile.getBirthDate()));
            else
                stmt.setNull(3, Types.DATE);
            stmt.setString(4, profile.getPhoneNumber());
            stmt.setString(5, profile.getLocation());
            stmt.setString(6, profile.getProfessionalTitle());
            stmt.setInt(7, profile.getYearsOfExperience());
            stmt.setString(8, profile.getSummary());
            stmt.setString(9, profile.getProfilePicturePath());
            stmt.setString(10, profile.getCvPath());
            stmt.setInt(11, user.getId());
            int rows = stmt.executeUpdate();
            stmt.close();
            if (rows > 0)
                return;
        } catch (SQLException e) {
            throw new RuntimeException("Error updating profile", e);
        }

        // If no rows updated, insert new
        String insertSql = """
                    INSERT INTO profiles
                    (user_id, first_name, last_name, birth_date, phone_number,
                     location, professional_title, years_of_experience, summary,
                     profile_picture_path, cv_path, profile_completed)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, true)
                """;
        try {
            PreparedStatement stmt = DB.getConnection().prepareStatement(insertSql);
            stmt.setInt(1, user.getId());
            stmt.setString(2, profile.getFirstName());
            stmt.setString(3, profile.getLastName());
            if (profile.getBirthDate() != null)
                stmt.setDate(4, Date.valueOf(profile.getBirthDate()));
            else
                stmt.setNull(4, Types.DATE);
            stmt.setString(5, profile.getPhoneNumber());
            stmt.setString(6, profile.getLocation());
            stmt.setString(7, profile.getProfessionalTitle());
            stmt.setInt(8, profile.getYearsOfExperience());
            stmt.setString(9, profile.getSummary());
            stmt.setString(10, profile.getProfilePicturePath());
            stmt.setString(11, profile.getCvPath());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException("Error saving profile", e);
        }
    }
}
