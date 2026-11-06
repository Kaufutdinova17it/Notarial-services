package com.example.notary.model;

/**
 * Сущность «Документ» — исходный (приложен клиентом) или готовый (подписан нотариусом).
 */
public class Document {

    private Long id;
    private Long applicationId;
    private String fileUrl;
    private DocumentType type;
    private boolean signed;

    public Document() {
    }

    public Document(Long id, Long applicationId, String fileUrl, DocumentType type, boolean signed) {
        this.id = id;
        this.applicationId = applicationId;
        this.fileUrl = fileUrl;
        this.type = type;
        this.signed = signed;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public boolean isSigned() {
        return signed;
    }

    public void setSigned(boolean signed) {
        this.signed = signed;
    }

    @Override
    public String toString() {
        return "Документ #%d (%s): %s%s".formatted(
                id, type.getDisplayName(), fileUrl, signed ? ", подписан" : "");
    }
}
