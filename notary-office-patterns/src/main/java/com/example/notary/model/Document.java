package com.example.notary.model;

import java.util.UUID;

/** Документ, связанный с заявкой (исходный или готовый). */
public class Document {
    private UUID id = UUID.randomUUID();
    private UUID applicationId;
    private String fileUrl;
    private DocumentType type = DocumentType.SOURCE;
    private boolean signed;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getApplicationId() { return applicationId; }
    public void setApplicationId(UUID applicationId) { this.applicationId = applicationId; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
    public DocumentType getType() { return type; }
    public void setType(DocumentType type) { this.type = type; }
    public boolean isSigned() { return signed; }
    public void setSigned(boolean signed) { this.signed = signed; }

    @Override
    public String toString() {
        return type.getTitle() + " документ " + fileUrl + (signed ? " [подписан]" : " [не подписан]");
    }
}
