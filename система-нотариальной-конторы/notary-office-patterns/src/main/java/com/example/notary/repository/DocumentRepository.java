package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Document;
import com.example.notary.model.DocumentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * JDBC-репозиторий документов заявок (таблица {@code documents}).
 */
public class DocumentRepository {

    private Connection connection() {
        return Database.getInstance().getConnection();
    }

    public Document save(Document document) {
        String sql = "INSERT INTO documents (application_id, file_url, type, signed) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, document.getApplicationId());
            ps.setString(2, document.getFileUrl());
            ps.setString(3, document.getType().name());
            ps.setBoolean(4, document.isSigned());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    document.setId(keys.getLong(1));
                }
            }
            return document;
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка сохранения документа", e);
        }
    }

    public List<Document> findByApplicationId(long applicationId) {
        String sql = "SELECT * FROM documents WHERE application_id = ? ORDER BY id";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, applicationId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Document> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(map(rs));
                }
                return result;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка чтения документов заявки #" + applicationId, e);
        }
    }

    public Optional<Document> findById(long id) {
        String sql = "SELECT * FROM documents WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка поиска документа #" + id, e);
        }
    }

    public void updateSigned(long id, boolean signed) {
        String sql = "UPDATE documents SET signed = ? WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setBoolean(1, signed);
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка подписания документа #" + id, e);
        }
    }

    public void delete(long id) {
        String sql = "DELETE FROM documents WHERE id = ?";
        try (PreparedStatement ps = connection().prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Ошибка удаления документа #" + id, e);
        }
    }

    private Document map(ResultSet rs) throws SQLException {
        return new Document(
                rs.getLong("id"),
                rs.getLong("application_id"),
                rs.getString("file_url"),
                DocumentType.valueOf(rs.getString("type")),
                rs.getBoolean("signed"));
    }
}
