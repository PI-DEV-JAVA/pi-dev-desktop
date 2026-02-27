package talentospidev.dao.ActivityDAO;

import talentospidev.utils.DB;
import talentospidev.models.Activity.ActivityFile;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityFileDAO {

    public void addFile(ActivityFile file) {
        String sql = "INSERT INTO activity_files (activity_id, file_name, file_path, file_size, file_type) VALUES (?, ?, ?, ?, ?)";
        try {
            Connection conn = DB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, file.getActivityId());
            ps.setString(2, file.getFileName());
            ps.setString(3, file.getFilePath());
            ps.setLong(4, file.getFileSize());
            ps.setString(5, file.getFileType());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next())
                file.setId(keys.getInt(1));
            keys.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ActivityFile> getFilesByActivityId(int activityId) {
        List<ActivityFile> files = new ArrayList<>();
        String sql = "SELECT * FROM activity_files WHERE activity_id = ? ORDER BY uploaded_at DESC";
        try {
            Connection conn = DB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, activityId);
            ResultSet rs = ps.executeQuery();
            while (rs.next())
                files.add(mapResultSet(rs));
            rs.close();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return files;
    }

    public void deleteFile(int fileId) {
        String sql = "DELETE FROM activity_files WHERE id = ?";
        try {
            Connection conn = DB.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, fileId);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ActivityFile mapResultSet(ResultSet rs) throws SQLException {
        ActivityFile file = new ActivityFile();
        file.setId(rs.getInt("id"));
        file.setActivityId(rs.getInt("activity_id"));
        file.setFileName(rs.getString("file_name"));
        file.setFilePath(rs.getString("file_path"));
        file.setFileSize(rs.getLong("file_size"));
        file.setFileType(rs.getString("file_type"));
        file.setUploadedAt(rs.getTimestamp("uploaded_at").toLocalDateTime());
        return file;
    }
}