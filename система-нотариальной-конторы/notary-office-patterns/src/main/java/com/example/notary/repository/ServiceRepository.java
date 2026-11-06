package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.PricingType;
import com.example.notary.model.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-репозиторий каталога нотариальных услуг (таблица {@code services}).
 */
public class ServiceRepository {

    private Connection connection() {
        return Database.getInstance().getConnection();
    }

    public Service save(Service service) {
        String sql = "INSERT INTO services (name, description, price, pricing_type) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, service.getName());
            ps.setString(2, service.getDescription());
            ps.setBigDecimal(3, service.getPrice());
            ps.setString(4, service.getPricingType().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    service.setId(keys.getLong(1));
                }
            }
            return service;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка сохранения услуги", e);
        }
    }

    public Optional<Service> findById(long id) {
        String sql = "SELECT * FROM services WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка поиска услуги #" + id, e);
        }
    }

    public Service update(Service service) {
        String sql = "UPDATE services SET name = ?, description = ?, price = ?, pricing_type = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, service.getName());
            ps.setString(2, service.getDescription());
            ps.setBigDecimal(3, service.getPrice());
            ps.setString(4, service.getPricingType().name());
            ps.setLong(5, service.getId());
            ps.executeUpdate();
            return service;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка обновления услуги #" + service.getId(), e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM services WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Нельзя удалить услугу, которая используется в заявках", e);
        }
    }

    public List<Service> findAll() {
        String sql = "SELECT * FROM services ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Service> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения каталога услуг", e);
        }
    }

    private Service map(ResultSet rs) throws SQLException {
        return new Service(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getBigDecimal("price"),
                PricingType.valueOf(rs.getString("pricing_type")));
    }
}
