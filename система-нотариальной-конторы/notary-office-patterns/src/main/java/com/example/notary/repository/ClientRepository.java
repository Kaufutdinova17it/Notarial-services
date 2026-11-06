package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Client;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-репозиторий клиентов (таблица {@code clients}).
 */
public class ClientRepository {

    private Connection connection() {
        return Database.getInstance().getConnection();
    }

    public Client save(Client client) {
        String sql = "INSERT INTO clients (full_name, passport_data, phone, email, auth_data) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getPassportData());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getEmail());
            ps.setString(5, client.getAuthData());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    client.setId(keys.getLong(1));
                }
            }
            return client;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка сохранения клиента", e);
        }
    }

    public Optional<Client> findById(long id) {
        String sql = "SELECT * FROM clients WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка поиска клиента #" + id, e);
        }
    }

    public Optional<Client> findByEmail(String email) {
        String sql = "SELECT * FROM clients WHERE LOWER(email) = LOWER(?)";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка поиска клиента по e-mail", e);
        }
    }

    public Client update(Client client) {
        String sql = "UPDATE clients SET full_name = ?, passport_data = ?, phone = ?, email = ?, auth_data = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setString(1, client.getFullName());
            ps.setString(2, client.getPassportData());
            ps.setString(3, client.getPhone());
            ps.setString(4, client.getEmail());
            ps.setString(5, client.getAuthData());
            ps.setLong(6, client.getId());
            ps.executeUpdate();
            return client;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка обновления клиента #" + client.getId(), e);
        }
    }

    public List<Client> findAll() {
        String sql = "SELECT * FROM clients ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Client> result = new ArrayList<>();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения списка клиентов", e);
        }
    }

    private Client map(ResultSet rs) throws SQLException {
        return new Client(
                rs.getLong("id"),
                rs.getString("full_name"),
                rs.getString("passport_data"),
                rs.getString("phone"),
                rs.getString("email"),
                rs.getString("auth_data"));
    }
}
