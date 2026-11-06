package com.example.notary.model;

/**
 * Типы документов заявки.
 */
public enum DocumentType {
    SOURCE("Исходный документ"),
    RESULT("Готовый документ");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
