package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Репозиторий клиентов (таблица clients). */
public class ClientRepository {

    private Connection conn() {
        return Database.getInstance().getConnection();
    }

    public Client save(Client c) {
        String sql = "MERGE INTO clients (id, full_name, passport_data, phone, email, auth_data) "
                   + "KEY(id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, c.getId());
            ps.setString(2, c.getFullName());
            ps.setString(3, c.getPassportData());
            ps.setString(4, c.getPhone());
            ps.setString(5, c.getEmail());
            ps.setString(6, c.getAuthData());
            ps.executeUpdate();
            return c;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения клиента", e);
        }
    }

    public Optional<Client> findById(UUID id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM clients WHERE id = ?")) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска клиента", e);
        }
    }

    public Optional<Client> findByEmail(String email) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM clients WHERE email = ?")) {
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска клиента по email", e);
        }
    }

    public List<Client> findAll() {
        List<Client> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM clients ORDER BY full_name")) {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения клиентов", e);
        }
    }

    private Client map(ResultSet rs) throws SQLException {
        Client c = new Client();
        c.setId(rs.getObject("id", UUID.class));
        c.setFullName(rs.getString("full_name"));
        c.setPassportData(rs.getString("passport_data"));
        c.setPhone(rs.getString("phone"));
        c.setEmail(rs.getString("email"));
        c.setAuthData(rs.getString("auth_data"));
        return c;
    }
}
