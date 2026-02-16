package talentospidev.dao;

import talentospidev.models.User;
import talentospidev.utils.DB;

import java.sql.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import talentospidev.models.UserViewModel;

public class UserDao {

    private final Connection connection;

    public UserDao() {
        this.connection = DB.getConnection();
    }

    /*
     * =========================
     * LOCAL USER
     * =========================
     */

    /**
     * [CREATE] Saves a new user using Local authentication (Email/Password).
     * This inserts a new record into the 'users' table.
     */
    public void saveLocal(User user) {
        String sql = """
                    INSERT INTO users
                    (email, password_hash, role, auth_provider, active, created_at)
                    VALUES (?, ?, ?, 'LOCAL', true, NOW())
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getRole().name());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving local user", e);
        }
    }

    /*
     * =========================
     * GOOGLE / OAUTH USER
     * =========================
     */

    public void saveOAuth(User user) {
        String sql = """
                    INSERT INTO users
                    (email, role, auth_provider, provider_id, active, created_at)
                    VALUES (?, ?, ?, ?, true, NOW())
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getRole().name());
            stmt.setString(3, user.getAuthProvider().name());
            stmt.setString(4, user.getProviderId());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                user.setId(keys.getInt(1));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving OAuth user", e);
        }
    }

    /*
     * =========================
     * FIND METHODS
     * =========================
     */

    public User findByEmail(String email) {
        String sql = """
                    SELECT * FROM users
                    WHERE email = ? AND active = true
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by email", e);
        }
        return null;
    }

    public User findByProviderId(String providerId) {
        String sql = """
                    SELECT * FROM users
                    WHERE provider_id = ? AND active = true
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, providerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return map(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding user by providerId", e);
        }
        return null;
    }

    /*
     * =========================
     * MAPPER
     * =========================
     */

    /*
     * =========================
     * ADMIN METHODS
     * =========================
     */

    /**
     * [READ] Fetches all users along with their profile details.
     * Uses a LEFT JOIN to combine 'users' and 'profiles' tables.
     * This is used for the Admin Dashboard table.
     */
    public List<UserViewModel> findAllWithDetails() {
        List<UserViewModel> list = new ArrayList<>();
        String sql = """
                    SELECT u.id, u.email, u.role, u.active, u.created_at,
                           p.first_name, p.last_name, p.birth_date, p.phone_number,
                           p.location, p.professional_title, p.summary
                    FROM users u
                    LEFT JOIN profiles p ON u.id = p.user_id
                    ORDER BY u.created_at DESC
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String role = rs.getString("role");
                boolean active = rs.getBoolean("active");
                Timestamp createdAt = rs.getTimestamp("created_at");
                LocalDate joinDate = (createdAt != null) ? createdAt.toLocalDateTime().toLocalDate() : null;

                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                String fullName = (first != null && last != null) ? first + " " + last : "N/A";

                String phone = rs.getString("phone_number");
                if (phone == null)
                    phone = "N/A";

                Date birthDate = rs.getDate("birth_date");
                int age = 0;
                if (birthDate != null) {
                    age = Period.between(birthDate.toLocalDate(), LocalDate.now()).getYears();
                }

                String status = active ? "active" : "inactive";
                String location = rs.getString("location");
                String title = rs.getString("professional_title");
                String summary = rs.getString("summary");

                list.add(new UserViewModel(id, fullName, email, role, age, phone, joinDate, status, location, title,
                        summary));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching all users", e);
        }
        return list;
    }

    /**
     * [READ + FILTER] Fetches only completed profiles for the main feed.
     * Filters:
     * 1. Excludes the current logged-in user (WHERE u.id != ?)
     * 2. Only includes users with completed profiles (AND p.profile_completed =
     * true)
     */
    public List<UserViewModel> findAllActiveProfiles(int excludeUserId) {
        List<UserViewModel> list = new ArrayList<>();
        // Only completed profiles, exclude current user
        String sql = """
                    SELECT u.id, u.email, u.role, u.active, u.created_at,
                           p.first_name, p.last_name, p.birth_date, p.phone_number,
                           p.location, p.professional_title, p.summary
                    FROM users u
                    JOIN profiles p ON u.id = p.user_id
                    WHERE u.id != ? AND p.profile_completed = true
                    ORDER BY u.created_at DESC
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, excludeUserId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                String email = rs.getString("email");
                String role = rs.getString("role");
                boolean active = rs.getBoolean("active");
                Timestamp createdAt = rs.getTimestamp("created_at");
                LocalDate joinDate = (createdAt != null) ? createdAt.toLocalDateTime().toLocalDate() : null;

                String first = rs.getString("first_name");
                String last = rs.getString("last_name");
                String fullName = first + " " + last;

                String phone = rs.getString("phone_number");
                if (phone == null)
                    phone = "N/A";

                Date birthDate = rs.getDate("birth_date");
                int age = 0;
                if (birthDate != null) {
                    age = Period.between(birthDate.toLocalDate(), LocalDate.now()).getYears();
                }

                String status = active ? "active" : "inactive";
                String location = rs.getString("location");
                String title = rs.getString("professional_title");
                String summary = rs.getString("summary");

                list.add(new UserViewModel(id, fullName, email, role, age, phone, joinDate, status, location, title,
                        summary));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching feed", e);
        }
        return list;
    }

    /**
     * [UPDATE] Updates core user information.
     * Modifies email, role, and active status in the 'users' table.
     */
    public void updateUsers(User user) {
        String sql = "UPDATE users SET email = ?, role = ?, active = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getRole().name());
            stmt.setBoolean(3, user.isActive());
            stmt.setInt(4, user.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user", e);
        }
    }

    /**
     * [UPDATE] Toggles the active status of a user.
     */
    public void setActive(int userId, boolean active) {
        String sql = "UPDATE users SET active = ? WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setBoolean(1, active);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user active status", e);
        }
    }

    /**
     * [DELETE] Removes a user and their associated profile.
     * Transactional operation:
     * 1. Deletes from 'profiles' first (to satisfy Foreign Key constraints).
     * 2. Deletes from 'users'.
     */
    public void delete(int userId) {
        // Cascase delete handled by DB logic? Or manual?
        // Typically profile should be deleted first if FK constraint exists without
        // CASCADE
        // But for now let's try deleting user. If FK fails, we need to delete profile
        // first.
        String deleteProfile = "DELETE FROM profiles WHERE user_id = ?";
        String deleteUser = "DELETE FROM users WHERE id = ?";

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement pStmt = connection.prepareStatement(deleteProfile)) {
                pStmt.setInt(1, userId);
                pStmt.executeUpdate();
            }

            try (PreparedStatement uStmt = connection.prepareStatement(deleteUser)) {
                uStmt.setInt(1, userId);
                uStmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
            }
            throw new RuntimeException("Error deleting user", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
            }
        }
    }

    /*
     * =========================
     * MAPPER
     * =========================
     */

    private User map(ResultSet rs) throws SQLException {
        User user = new User();

        user.setId(rs.getInt("id"));
        user.setEmail(rs.getString("email"));
        user.setPasswordHash(rs.getString("password_hash"));

        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setAuthProvider(User.AuthProvider.valueOf(rs.getString("auth_provider")));
        user.setProviderId(rs.getString("provider_id"));

        user.setActive(rs.getBoolean("active"));

        Timestamp created = rs.getTimestamp("created_at");
        if (created != null) {
            user.setCreatedAt(created.toLocalDateTime());
        }

        return user;
    }
}
