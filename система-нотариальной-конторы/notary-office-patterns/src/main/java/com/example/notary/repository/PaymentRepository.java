package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Payment;
import com.example.notary.model.PaymentStatus;

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
 * JDBC-репозиторий платежей (таблица {@code payments}).
 */
public class PaymentRepository {

    private Connection connection() {
        return Database.getInstance().getConnection();
    }

    public Payment save(Payment payment) {
        String sql = "INSERT INTO payments (application_id, amount, status, created_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, payment.getApplicationId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getStatus().name());
            ps.setTimestamp(4, Timestamp.valueOf(payment.getCreatedAt()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setId(keys.getLong(1));
                }
            }
            return payment;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка сохранения платежа", e);
        }
    }

    public List<Payment> findByApplicationId(long applicationId) {
        String sql = "SELECT * FROM payments WHERE application_id = ? ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Payment> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения платежей заявки #" + applicationId, e);
        }
    }

    public Optional<Payment> findById(long id) {
        String sql = "SELECT * FROM payments WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка поиска платежа #" + id, e);
        }
    }

    public List<Payment> findAll() {
        String sql = "SELECT * FROM payments ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Payment> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения списка платежей", e);
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getLong("id"),
                rs.getLong("application_id"),
                rs.getBigDecimal("amount"),
                PaymentStatus.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime());
    }
}
