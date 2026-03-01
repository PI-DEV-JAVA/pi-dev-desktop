package talentospidev.dao;

import talentospidev.models.SupportTicket;
import talentospidev.models.TicketReply;
import talentospidev.utils.DB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupportTicketDao {

    // ====== TICKETS ======

    /** Create a new support ticket. */
    public void createTicket(SupportTicket ticket) {
        String sql = "INSERT INTO support_tickets (user_id, subject, message, category, priority) VALUES (?,?,?,?,?)";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ticket.getUserId());
            stmt.setString(2, ticket.getSubject());
            stmt.setString(3, ticket.getMessage());
            stmt.setString(4, ticket.getCategory());
            stmt.setString(5, ticket.getPriority());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next())
                ticket.setId(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Error creating support ticket", e);
        }
    }

    /** All tickets (admin view) — includes user info and reply counts. */
    public List<SupportTicket> findAll() {
        List<SupportTicket> list = new ArrayList<>();
        String sql = """
                    SELECT t.*, u.email AS user_email,
                           CONCAT(p.first_name, ' ', p.last_name) AS user_full_name,
                           (SELECT COUNT(*) FROM ticket_replies r WHERE r.ticket_id = t.id) AS reply_count
                    FROM support_tickets t
                    JOIN users u ON t.user_id = u.id
                    LEFT JOIN profiles p ON t.user_id = p.user_id
                    ORDER BY t.created_at DESC
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next())
                list.add(mapTicket(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Error loading tickets", e);
        }
        return list;
    }

    /** Tickets from a specific user. */
    public List<SupportTicket> findByUserId(int userId) {
        List<SupportTicket> list = new ArrayList<>();
        String sql = """
                    SELECT t.*, u.email AS user_email,
                           CONCAT(p.first_name, ' ', p.last_name) AS user_full_name,
                           (SELECT COUNT(*) FROM ticket_replies r WHERE r.ticket_id = t.id) AS reply_count,
                           (SELECT COUNT(*) FROM ticket_replies r
                            WHERE r.ticket_id = t.id AND r.sender_id != ? AND r.is_read = FALSE) AS unread_count
                    FROM support_tickets t
                    JOIN users u ON t.user_id = u.id
                    LEFT JOIN profiles p ON t.user_id = p.user_id
                    WHERE t.user_id = ?
                    ORDER BY t.created_at DESC
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                SupportTicket t = mapTicket(rs);
                t.setUnreadCount(rs.getInt("unread_count"));
                list.add(t);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading user tickets", e);
        }
        return list;
    }

    /** Single ticket by ID. */
    public SupportTicket findById(int ticketId) {
        String sql = """
                    SELECT t.*, u.email AS user_email,
                           CONCAT(p.first_name, ' ', p.last_name) AS user_full_name,
                           (SELECT COUNT(*) FROM ticket_replies r WHERE r.ticket_id = t.id) AS reply_count
                    FROM support_tickets t
                    JOIN users u ON t.user_id = u.id
                    LEFT JOIN profiles p ON t.user_id = p.user_id
                    WHERE t.id = ?
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return mapTicket(rs);
        } catch (SQLException e) {
            throw new RuntimeException("Error loading ticket", e);
        }
        return null;
    }

    /** Admin changes ticket status. */
    public void updateStatus(int ticketId, String status) {
        String sql = "UPDATE support_tickets SET status = ? WHERE id = ?";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, ticketId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating ticket status", e);
        }
    }

    /** Count of open tickets (for admin stats). */
    public int countOpen() {
        String sql = "SELECT COUNT(*) FROM support_tickets WHERE status IN ('OPEN','IN_PROGRESS')";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting tickets", e);
        }
        return 0;
    }

    /** Count unread replies for a user (replies from others that are unread). */
    public int countUnreadForUser(int userId) {
        String sql = """
                    SELECT COUNT(*) FROM ticket_replies r
                    JOIN support_tickets t ON r.ticket_id = t.id
                    WHERE t.user_id = ? AND r.sender_id != ? AND r.is_read = FALSE
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error counting unread", e);
        }
        return 0;
    }

    // ====== REPLIES ======

    /** Add a reply to a ticket. */
    public void addReply(TicketReply reply) {
        String sql = "INSERT INTO ticket_replies (ticket_id, sender_id, message) VALUES (?,?,?)";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reply.getTicketId());
            stmt.setInt(2, reply.getSenderId());
            stmt.setString(3, reply.getMessage());
            stmt.executeUpdate();
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next())
                reply.setId(keys.getInt(1));
        } catch (SQLException e) {
            throw new RuntimeException("Error adding reply", e);
        }
    }

    /** Get all replies for a ticket, ordered chronologically. */
    public List<TicketReply> getReplies(int ticketId) {
        List<TicketReply> list = new ArrayList<>();
        String sql = """
                    SELECT r.*, u.role AS sender_role,
                           CONCAT(p.first_name, ' ', p.last_name) AS sender_name
                    FROM ticket_replies r
                    JOIN users u ON r.sender_id = u.id
                    LEFT JOIN profiles p ON r.sender_id = p.user_id
                    ORDER BY r.created_at ASC
                """;
        // Filter by ticket_id
        sql = """
                    SELECT r.*, u.role AS sender_role,
                           CONCAT(p.first_name, ' ', p.last_name) AS sender_name
                    FROM ticket_replies r
                    JOIN users u ON r.sender_id = u.id
                    LEFT JOIN profiles p ON r.sender_id = p.user_id
                    WHERE r.ticket_id = ?
                    ORDER BY r.created_at ASC
                """;
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                TicketReply reply = new TicketReply();
                reply.setId(rs.getInt("id"));
                reply.setTicketId(rs.getInt("ticket_id"));
                reply.setSenderId(rs.getInt("sender_id"));
                reply.setSenderName(rs.getString("sender_name"));
                reply.setSenderRole(rs.getString("sender_role"));
                reply.setMessage(rs.getString("message"));
                reply.setRead(rs.getBoolean("is_read"));
                Timestamp ts = rs.getTimestamp("created_at");
                if (ts != null)
                    reply.setCreatedAt(ts.toLocalDateTime());
                list.add(reply);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error loading replies", e);
        }
        return list;
    }

    /** Mark all replies in a ticket as read for a specific user. */
    public void markRepliesRead(int ticketId, int userId) {
        String sql = "UPDATE ticket_replies SET is_read = TRUE WHERE ticket_id = ? AND sender_id != ?";
        try (PreparedStatement stmt = DB.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error marking replies read", e);
        }
    }

    // ====== MAPPING ======

    private SupportTicket mapTicket(ResultSet rs) throws SQLException {
        SupportTicket t = new SupportTicket();
        t.setId(rs.getInt("id"));
        t.setUserId(rs.getInt("user_id"));
        t.setUserEmail(rs.getString("user_email"));
        t.setUserFullName(rs.getString("user_full_name"));
        t.setSubject(rs.getString("subject"));
        t.setMessage(rs.getString("message"));
        t.setCategory(rs.getString("category"));
        t.setStatus(rs.getString("status"));
        t.setPriority(rs.getString("priority"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null)
            t.setCreatedAt(ts.toLocalDateTime());
        t.setReplyCount(rs.getInt("reply_count"));
        return t;
    }
}
