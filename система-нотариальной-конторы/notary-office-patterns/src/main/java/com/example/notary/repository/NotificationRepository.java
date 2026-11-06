package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Notification;
import com.example.notary.model.NotificationStatus;
import com.example.notary.model.NotificationType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-репозиторий уведомлений (таблица {@code notifications}).
 */
public class NotificationRepository {

    private Connection connection() {
        return Database.getInstance().getConnection();
    }

    public Notification save(Notification notification) {
        String sql = "INSERT INTO notifications (client_id, application_id, type, message, status, created_at)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, notification.getClientId());
            if (notification.getApplicationId() != null) {
                ps.setLong(2, notification.getApplicationId());
            } else {
                ps.setNull(2, java.sql.Types.BIGINT);
            }
            ps.setString(3, notification.getType().name());
            ps.setString(4, notification.getMessage());
            ps.setString(5, notification.getStatus().name());
            ps.setTimestamp(6, Timestamp.valueOf(notification.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    notification.setId(keys.getLong(1));
                }
            }
            return notification;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка сохранения уведомления", e);
        }
    }

    public List<Notification> findByClientId(long clientId) {
        String sql = "SELECT * FROM notifications WHERE client_id = ? ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Notification> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения уведомлений клиента #" + clientId, e);
        }
    }

    public List<Notification> findAll() {
        String sql = "SELECT * FROM notifications ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Notification> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения списка уведомлений", e);
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        long applicationId = rs.getLong("application_id");
        return new Notification(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.wasNull() ? null : applicationId,
                NotificationType.valueOf(rs.getString("type")),
                rs.getString("message"),
                NotificationStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
