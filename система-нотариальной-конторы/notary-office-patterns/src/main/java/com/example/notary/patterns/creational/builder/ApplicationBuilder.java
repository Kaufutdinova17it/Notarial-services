package com.example.notary.patterns.creational.builder;

import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.Client;
import com.example.notary.model.Document;
import com.example.notary.model.DocumentType;
import com.example.notary.model.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Паттерн «Строитель» (Builder) — пошаговое создание заявки:
 * выбор услуги, примечание, прикрепление исходных документов.
 * Строитель проверяет обязательные поля и собирает заявку со статусом «Создана».
 */
public class ApplicationBuilder {

    private Client client;
    private Service service;
    private String note;
    private final List<Document> sourceDocuments = new ArrayList<>();

    /** Шаг 1: выбор клиента (обязательное поле). */
    public ApplicationBuilder forClient(Client client) {
        this.client = client;
        return this;
    }

    /** Шаг 2: выбор нотариальной услуги (обязательное поле). */
    public ApplicationBuilder withService(Service service) {
        this.service = service;
        return this;
    }

    /** Шаг 3: произвольное примечание к заявке. */
    public ApplicationBuilder withNote(String note) {
        this.note = note;
        return this;
    }

    /** Шаг 4: прикрепление исходного документа (можно несколько). */
    public ApplicationBuilder attachSourceDocument(String fileUrl) {
        sourceDocuments.add(new Document(null, null, fileUrl, DocumentType.SOURCE, false));
        return this;
    }

    /** Исходные документы, прикреплённые на шагах сборки. */
    public List<Document> getSourceDocuments() {
        return List.copyOf(sourceDocuments);
    }

    /**
     * Собирает заявку со статусом «Создана».
     *
     * @throws IllegalStateException если не заполнены обязательные поля
     */
    public Application build() {
        List<String> errors = new ArrayList<>();
        if (client == null || client.getId() == null) {
            errors.add("не выбран клиент");
        }
        if (service == null || service.getId() == null) {
            errors.add("не выбрана услуга");
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("Невозможно создать заявку: " + String.join(", ", errors));
        }
        Application application = new Application();
        application.setClientId(client.getId());
        application.setServiceId(service.getId());
        application.setNote(note);
        application.setStatus(ApplicationStatus.CREATED);
        application.setCreatedAt(LocalDateTime.now());
        return application;
    }
}
