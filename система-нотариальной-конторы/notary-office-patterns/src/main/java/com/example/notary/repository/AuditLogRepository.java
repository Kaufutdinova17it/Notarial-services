package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.AuditEntry;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC-репозиторий журнала аудита (таблица {@code audit_log}).
 */
public class AuditLogRepository {

    private Connection connection() {
        return Database.getInstance().getConnection();
    }

    public void save(String eventType, String description) {
        String sql = "INSERT INTO audit_log (event_type, description, created_at) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, eventType);
            ps.setString(2, description);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка записи в журнал аудита", e);
        }
    }

    public List<AuditEntry> findAll() {
        String sql = "SELECT * FROM audit_log ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<AuditEntry> result = new ArrayList<>();
            while (rs.next()) {
                result.add(new AuditEntry(
                        rs.getLong("id"),
                        rs.getString("event_type"),
                        rs.getString("description"),
                        rs.getTimestamp("created_at").toLocalDateTime()));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения журнала аудита", e);
        }
    }
}
