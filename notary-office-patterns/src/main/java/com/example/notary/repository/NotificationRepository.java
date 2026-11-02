package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Notification;
import com.example.notary.model.NotificationStatus;
import com.example.notary.model.NotificationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Репозиторий уведомлений (таблица notifications). */
public class NotificationRepository {

    private Connection conn() {
        return Database.getInstance().getConnection();
    }

    public Notification save(Notification n) {
        String sql = "MERGE INTO notifications (id, client_id, application_id, notification_type, "
                   + "message, send_status, created_at) KEY(id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, n.getId());
            ps.setObject(2, n.getClientId());
            ps.setObject(3, n.getApplicationId());
            ps.setString(4, n.getType().name());
            ps.setString(5, n.getMessage());
            ps.setString(6, n.getStatus().name());
            ps.setObject(7, n.getCreatedAt());
            ps.executeUpdate();
            return n;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения уведомления", e);
        }
    }

    public Optional<Notification> findById(UUID id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM notifications WHERE id = ?")) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска уведомления", e);
        }
    }

    public List<Notification> findByClientId(UUID clientId) {
        List<Notification> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM notifications WHERE client_id = ? ORDER BY created_at")) {
            ps.setObject(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения уведомлений клиента", e);
        }
    }

    public List<Notification> findAll() {
        List<Notification> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM notifications ORDER BY created_at")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения уведомлений", e);
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getObject("id", UUID.class));
        n.setClientId(rs.getObject("client_id", UUID.class));
        n.setApplicationId(rs.getObject("application_id", UUID.class));
        n.setType(NotificationType.valueOf(rs.getString("notification_type")));
        n.setMessage(rs.getString("message"));
        n.setStatus(NotificationStatus.valueOf(rs.getString("send_status")));
        n.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return n;
    }
}
