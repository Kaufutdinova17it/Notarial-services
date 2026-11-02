package com.example.notary.model;

/** Типы документов (справочник «Типы документов»). */
public enum DocumentType {
    SOURCE("Исходный"),
    READY("Готовый");

    private final String title;

    DocumentType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
