package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.AuditRecord;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Репозиторий журнала аудита (таблица audit_log). */
public class AuditLogRepository {

    private Connection conn() {
        return Database.getInstance().getConnection();
    }

    public AuditRecord add(String actor, String action, String details) {
        AuditRecord record = new AuditRecord();
        record.setActor(actor);
        record.setAction(action);
        record.setDetails(details);
        String sql = "INSERT INTO audit_log (id, actor, action, details, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, record.getId());
            ps.setString(2, record.getActor());
            ps.setString(3, record.getAction());
            ps.setString(4, record.getDetails());
            ps.setObject(5, record.getCreatedAt());
            ps.executeUpdate();
            return record;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка записи в журнал аудита", e);
        }
    }

    public List<AuditRecord> findAll() {
        List<AuditRecord> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM audit_log ORDER BY created_at")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                AuditRecord r = new AuditRecord();
                r.setId(rs.getObject("id", java.util.UUID.class));
                r.setActor(rs.getString("actor"));
                r.setAction(rs.getString("action"));
                r.setDetails(rs.getString("details"));
                r.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
                result.add(r);
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения журнала аудита", e);
        }
    }
}
