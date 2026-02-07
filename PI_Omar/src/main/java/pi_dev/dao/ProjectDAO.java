package pi_dev.dao;

import pi_dev.config.DBConnection;
import pi_dev.model.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    // REMOVE this field - don't store connection
    // private Connection cnx;

    // REMOVE this constructor
    // public ProjectDAO() {
    //     cnx = DBConnection.getConnection();
    // }

    // CREATE
    public void add(Project p) {
        String sql = "INSERT INTO project (name, description, status, start_date, end_date, budget) VALUES (?,?,?,?,?,?)";
        
        try (Connection cnx = DBConnection.getConnection();  // Get fresh connection
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getStatus());
            ps.setDate(4, Date.valueOf(p.getStartDate()));
            ps.setDate(5, Date.valueOf(p.getEndDate()));
            ps.setDouble(6, p.getBudget());
            
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add project: " + e.getMessage(), e);
        }
    }

    // READ ALL
    public List<Project> getAll() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM project";

        try (Connection cnx = DBConnection.getConnection();
             Statement st = cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Project(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("description"),
                        rs.getString("status"),
                        rs.getDate("start_date").toLocalDate(),
                        rs.getDate("end_date").toLocalDate(),
                        rs.getDouble("budget")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get projects: " + e.getMessage(), e);
        }
        return list;
    }

    // READ BY ID
    public Project getById(int id) {
        String sql = "SELECT * FROM project WHERE id = ?";
        
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                return new Project(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getDate("start_date").toLocalDate(),
                    rs.getDate("end_date").toLocalDate(),
                    rs.getDouble("budget")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get project by ID: " + e.getMessage(), e);
        }
        return null;
    }

    // UPDATE
    public void update(Project p) {
        String sql = "UPDATE project SET name=?, description=?, status=?, start_date=?, end_date=?, budget=? WHERE id=?";
        
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getStatus());
            ps.setDate(4, Date.valueOf(p.getStartDate()));
            ps.setDate(5, Date.valueOf(p.getEndDate()));
            ps.setDouble(6, p.getBudget());
            ps.setInt(7, p.getId());
            
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update project: " + e.getMessage(), e);
        }
    }

    // DELETE
    public void delete(int id) {
        String sql = "DELETE FROM project WHERE id=?";
        
        try (Connection cnx = DBConnection.getConnection();
             PreparedStatement ps = cnx.prepareStatement(sql)) {
            
            ps.setInt(1, id);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to delete project: " + e.getMessage(), e);
        }
    }
}