package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.NotaryService;
import com.example.notary.model.PricingType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Репозиторий каталога нотариальных услуг (таблица services). */
public class ServiceRepository {

    private Connection conn() {
        return Database.getInstance().getConnection();
    }

    public NotaryService save(NotaryService s) {
        String sql = "MERGE INTO services (id, name, description, price, pricing_type) "
                   + "KEY(id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, s.getId());
            ps.setString(2, s.getName());
            ps.setString(3, s.getDescription());
            ps.setBigDecimal(4, s.getPrice());
            ps.setString(5, s.getPricingType().name());
            ps.executeUpdate();
            return s;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения услуги", e);
        }
    }

    public Optional<NotaryService> findById(UUID id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM services WHERE id = ?")) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска услуги", e);
        }
    }

    public List<NotaryService> findAll() {
        List<NotaryService> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM services ORDER BY name")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения каталога услуг", e);
        }
    }

    private NotaryService map(ResultSet rs) throws SQLException {
        NotaryService s = new NotaryService();
        s.setId(rs.getObject("id", UUID.class));
        s.setName(rs.getString("name"));
        s.setDescription(rs.getString("description"));
        s.setPrice(rs.getBigDecimal("price"));
        s.setPricingType(PricingType.valueOf(rs.getString("pricing_type")));
        return s;
    }
}
