package com.example.notary.repository;

import com.example.notary.db.Database;
import com.example.notary.model.Document;
import com.example.notary.model.DocumentType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Репозиторий документов (таблица documents). */
public class DocumentRepository {

    private Connection conn() {
        return Database.getInstance().getConnection();
    }

    public Document save(Document d) {
        String sql = "MERGE INTO documents (id, application_id, file_url, doc_type, signed) "
                   + "KEY(id) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setObject(1, d.getId());
            ps.setObject(2, d.getApplicationId());
            ps.setString(3, d.getFileUrl());
            ps.setString(4, d.getType().name());
            ps.setBoolean(5, d.isSigned());
            ps.executeUpdate();
            return d;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения документа", e);
        }
    }

    public Optional<Document> findById(UUID id) {
        try (PreparedStatement ps = conn().prepareStatement("SELECT * FROM documents WHERE id = ?")) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(map(rs)) : Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска документа", e);
        }
    }

    public List<Document> findByApplicationId(UUID applicationId) {
        List<Document> result = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT * FROM documents WHERE application_id = ?")) {
            ps.setObject(1, applicationId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result.add(map(rs));
            }
            return result;
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка чтения документов заявки", e);
        }
    }

    private Document map(ResultSet rs) throws SQLException {
        Document d = new Document();
        d.setId(rs.getObject("id", UUID.class));
        d.setApplicationId(rs.getObject("application_id", UUID.class));
        d.setFileUrl(rs.getString("file_url"));
        d.setType(DocumentType.valueOf(rs.getString("doc_type")));
        d.setSigned(rs.getBoolean("signed"));
        return d;
    }
}
