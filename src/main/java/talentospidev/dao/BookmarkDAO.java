package talentospidev.dao;

import talentospidev.utils.DB;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BookmarkDAO {

    private final Connection connection;

    public BookmarkDAO() {
        this.connection = DB.getConnection();
    }

    public boolean addBookmark(int candidateId, int offerId) throws SQLException {
        String sql = "INSERT INTO bookmarks (candidate_id, offer_id, saved_at) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, candidateId);
            pstmt.setInt(2, offerId);
            pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean removeBookmark(int candidateId, int offerId) throws SQLException {
        String sql = "DELETE FROM bookmarks WHERE candidate_id = ? AND offer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, candidateId);
            pstmt.setInt(2, offerId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean isBookmarked(int candidateId, int offerId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM bookmarks WHERE candidate_id = ? AND offer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, candidateId);
            pstmt.setInt(2, offerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public List<Integer> getBookmarkedOfferIds(int candidateId) throws SQLException {
        List<Integer> offerIds = new ArrayList<>();
        String sql = "SELECT offer_id FROM bookmarks WHERE candidate_id = ? ORDER BY saved_at DESC";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, candidateId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    offerIds.add(rs.getInt("offer_id"));
                }
            }
        }
        return offerIds;
    }

    public boolean updateNotes(int candidateId, int offerId, String notes) throws SQLException {
        String sql = "UPDATE bookmarks SET notes = ? WHERE candidate_id = ? AND offer_id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, notes);
            pstmt.setInt(2, candidateId);
            pstmt.setInt(3, offerId);
            return pstmt.executeUpdate() > 0;
        }
    }
}
