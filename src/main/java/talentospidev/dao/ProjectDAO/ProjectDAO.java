package talentospidev.dao.ProjectDAO;

import talentospidev.utils.DB;
import talentospidev.models.Project.Project;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectDAO {

    public void add(Project p) {
        String sql = "INSERT INTO project (name, description, status, start_date, end_date, budget, project_manager_id) VALUES (?,?,?,?,?,?,?)";
        try {
            Connection cnx = DB.getConnection();
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getStatus());
            ps.setDate(4, Date.valueOf(p.getStartDate()));
            ps.setDate(5, Date.valueOf(p.getEndDate()));
            ps.setDouble(6, p.getBudget());
            ps.setInt(7, p.getProjectManagerId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to add project: " + e.getMessage(), e);
        }
    }

    public List<Project> getAll() {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM project";
        try {
            Connection cnx = DB.getConnection();
            Statement st = cnx.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next())
                list.add(mapResultSet(rs));
            rs.close();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to get projects: " + e.getMessage(), e);
        }
        return list;
    }

    public List<Project> getByManagerId(int managerId) {
        List<Project> list = new ArrayList<>();
        String sql = "SELECT * FROM project WHERE project_manager_id = ? ORDER BY created_at DESC";
        try {
            Connection cnx = DB.getConnection();
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, managerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                list.add(mapResultSet(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Project getById(int id) {
        String sql = "SELECT * FROM project WHERE id = ?";
        try {
            Connection cnx = DB.getConnection();
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            Project p = null;
            if (rs.next())
                p = mapResultSet(rs);
            rs.close();
            ps.close();
            return p;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void update(Project p) {
        String sql = "UPDATE project SET name=?, description=?, status=?, start_date=?, end_date=?, budget=? WHERE id=?";
        try {
            Connection cnx = DB.getConnection();
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setString(3, p.getStatus());
            ps.setDate(4, Date.valueOf(p.getStartDate()));
            ps.setDate(5, Date.valueOf(p.getEndDate()));
            ps.setDouble(6, p.getBudget());
            ps.setInt(7, p.getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to update project: " + e.getMessage(), e);
        }
    }

    public void delete(int id) {
        String sql = "DELETE FROM project WHERE id=?";
        try {
            Connection cnx = DB.getConnection();
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, id);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Project mapResultSet(ResultSet rs) throws SQLException {
        Project p = new Project();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setStatus(rs.getString("status"));
        p.setStartDate(rs.getDate("start_date") != null ? rs.getDate("start_date").toLocalDate() : null);
        p.setEndDate(rs.getDate("end_date") != null ? rs.getDate("end_date").toLocalDate() : null);
        p.setBudget(rs.getDouble("budget"));
        try {
            p.setProjectManagerId(rs.getInt("project_manager_id"));
        } catch (SQLException ignored) {
        }
        return p;
    }
}