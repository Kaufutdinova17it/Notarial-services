package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-репозиторий заявок (таблица {@code applications}).
 */
public class ApplicationRepository {

    private Connection connection() {
        return Database.getInstance().getConnection();
    }

    public Application save(Application application) {
        String sql = "INSERT INTO applications (client_id, service_id, status, note, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, application.getClientId());
            ps.setLong(2, application.getServiceId());
            ps.setString(3, application.getStatus().name());
            ps.setString(4, application.getNote());
            ps.setTimestamp(5, Timestamp.valueOf(application.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    application.setId(keys.getLong(1));
                }
            }
            return application;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка сохранения заявки", e);
        }
    }

    public void updateStatus(long applicationId, ApplicationStatus status) {
        String sql = "UPDATE applications SET status = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, applicationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка обновления статуса заявки #" + applicationId, e);
        }
    }

    public void updateNote(long applicationId, String note) {
        String sql = "UPDATE applications SET note = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, note);
            ps.setLong(2, applicationId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка обновления заявки #" + applicationId, e);
        }
    }

    public Optional<Application> findById(long id) {
        String sql = "SELECT * FROM applications WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка поиска заявки #" + id, e);
        }
    }

    public List<Application> findByClientId(long clientId) {
        String sql = "SELECT * FROM applications WHERE client_id = ? ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Application> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения заявок клиента #" + clientId, e);
        }
    }

    public List<Application> findAll() {
        String sql = "SELECT * FROM applications ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Application> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения списка заявок", e);
        }
    }

    private Application map(ResultSet rs) throws SQLException {
        return new Application(
                rs.getLong("id"),
                rs.getLong("client_id"),
                rs.getLong("service_id"),
                ApplicationStatus.valueOf(rs.getString("status")),
                rs.getString("note"),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
