package com.example.notary.patterns.creational.builder;

import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.Document;
import com.example.notary.model.DocumentType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * ПОРОЖДАЮЩИЙ ПАТТЕРН — СТРОИТЕЛЬ (Builder).
 *
 * Задача из предметной области: заявка клиента создаётся пошагово —
 * выбирается услуга, заполняется примечание, прикладываются исходные
 * документы. Строитель отделяет пошаговую сборку заявки от её
 * использования и проверяет обязательные поля перед созданием объекта.
 */
public class ApplicationBuilder {

    private UUID clientId;
    private UUID serviceId;
    private String note;
    private final List<String> sourceDocumentUrls = new ArrayList<>();

    public ApplicationBuilder forClient(UUID clientId) {
        this.clientId = clientId;
        return this;
    }

    public ApplicationBuilder withService(UUID serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public ApplicationBuilder withNote(String note) {
        this.note = note;
        return this;
    }

    public ApplicationBuilder withSourceDocument(String fileUrl) {
        this.sourceDocumentUrls.add(fileUrl);
        return this;
    }

    public ApplicationBuilder withSourceDocuments(List<String> fileUrls) {
        this.sourceDocumentUrls.addAll(fileUrls);
        return this;
    }

    /** Завершает сборку: проверяет обязательные поля и создаёт заявку со статусом «Создана». */
    public Application build() {
        if (clientId == null) {
            throw new IllegalStateException("Не указан клиент заявки");
        }
        if (serviceId == null) {
            throw new IllegalStateException("Не выбрана нотариальная услуга");
        }
        Application application = new Application();
        application.setClientId(clientId);
        application.setServiceId(serviceId);
        application.setNote(note);
        application.setStatus(ApplicationStatus.CREATED);
        application.setCreatedAt(LocalDateTime.now());
        for (String url : sourceDocumentUrls) {
            Document doc = new Document();
            doc.setApplicationId(application.getId());
            doc.setFileUrl(url);
            doc.setType(DocumentType.SOURCE);
            doc.setSigned(false);
            application.getDocuments().add(doc);
        }
        return application;
    }
}
