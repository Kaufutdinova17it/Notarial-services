package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Payment;
import com.example.notary.model.PaymentStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Репозиторий платежей (таблица payments). */
public class PaymentRepository {

    private Connection conn() {
        return Database.getInstance().getConnection();
    }

    public Payment save(Payment p) {
        String sql = "MERGE INTO payments (id, application_id, amount, status, created_at) "
                   + "KEY(id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, p.getId());
            ps.setObject(2, p.getApplicationId());
            ps.setBigDecimal(3, p.getAmount());
            ps.setString(4, p.getStatus().name());
            ps.setObject(5, p.getCreatedAt());
            ps.executeUpdate();
            return p;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения платежа", e);
        }
    }

    public void updateStatus(UUID id, PaymentStatus status) {
        try (PreparedStatement ps = conn().prepareStatement("UPDATE payments SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setObject(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка обновления статуса платежа", e);
        }
    }

    public Optional<Payment> findById(UUID id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM payments WHERE id = ?")) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска платежа", e);
        }
    }

    public Optional<Payment> findByApplicationId(UUID applicationId) {
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM payments WHERE application_id = ? ORDER BY created_at DESC LIMIT 1")) {
            ps.setObject(1, applicationId);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска платежа по заявке", e);
        }
    }

    public List<Payment> findAll() {
        List<Payment> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM payments ORDER BY created_at")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения платежей", e);
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getObject("id", UUID.class));
        p.setApplicationId(rs.getObject("application_id", UUID.class));
        p.setAmount(rs.getBigDecimal("amount"));
        p.setStatus(PaymentStatus.valueOf(rs.getString("status")));
        p.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return p;
    }
}
