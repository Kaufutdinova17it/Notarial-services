package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Репозиторий заявок (таблица applications). */
public class ApplicationRepository {

    private Connection conn() {
        return Database.getInstance().getConnection();
    }

    public Application save(Application a) {
        String sql = "MERGE INTO applications (id, client_id, service_id, status, note, created_at) "
                   + "KEY(id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, a.getId());
            ps.setObject(2, a.getClientId());
            ps.setObject(3, a.getServiceId());
            ps.setString(4, a.getStatus().name());
            ps.setString(5, a.getNote());
            ps.setObject(6, a.getCreatedAt());
            ps.executeUpdate();
            return a;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения заявки", e);
        }
    }

    public void updateStatus(UUID id, ApplicationStatus status) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE applications SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setObject(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления статуса заявки", e);
        }
    }

    public Optional<Application> findById(UUID id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM applications WHERE id = ?")) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска заявки", e);
        }
    }

    public List<Application> findByClientId(UUID clientId) {
        List<Application> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM applications WHERE client_id = ? ORDER BY created_at")) {
            ps.setObject(1, clientId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения заявок клиента", e);
        }
    }

    public List<Application> findAll() {
        List<Application> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM applications ORDER BY created_at")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения заявок", e);
        }
    }

    private Application map(ResultSet rs) throws SQLException {
        Application a = new Application();
        a.setId(rs.getObject("id", UUID.class));
        a.setClientId(rs.getObject("client_id", UUID.class));
        a.setServiceId(rs.getObject("service_id", UUID.class));
        a.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
        a.setNote(rs.getString("note"));
        a.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return a;
    }
}
