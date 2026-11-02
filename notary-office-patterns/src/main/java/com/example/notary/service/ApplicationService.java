package com.example.notary.service;

import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.Document;
import com.example.notary.model.DocumentType;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;
import com.example.notary.patterns.behavioral.observer.ApplicationEventPublisher;
import com.example.notary.patterns.behavioral.state.ApplicationStateMachine;
import com.example.notary.repository.ApplicationRepository;
import com.example.notary.repository.DocumentRepository;

import java.util.List;
import java.util.UUID;

/**
 * Сервис заявок и документов. После создания заявки или изменения её
 * статуса публикует событие для подписчиков (паттерн «Наблюдатель»);
 * допустимость переходов проверяет машина состояний (паттерн «Состояние»).
 */
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ApplicationStateMachine stateMachine = new ApplicationStateMachine();

    public ApplicationService(ApplicationRepository applicationRepository,
                              DocumentRepository documentRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.eventPublisher = eventPublisher;
    }

    /** Создание заявки (уже собранной через Builder). */
    public Application create(Application application) {
        applicationRepository.save(application);
        for (Document doc : application.getDocuments()) {
            documentRepository.save(doc);
        }
        eventPublisher.publish(new ApplicationEvent(application, null, ApplicationStatus.CREATED, "Система"));
        return application;
    }

    /** Изменение статуса заявки с проверкой допустимости перехода. */
    public Application changeStatus(UUID applicationId, ApplicationStatus newStatus, String actor) {
        Application application = getById(applicationId);
        ApplicationStatus oldStatus = application.getStatus();
        ApplicationStatus validated = stateMachine.transition(oldStatus, newStatus);
        application.setStatus(validated);
        applicationRepository.updateStatus(application.getId(), validated);
        eventPublisher.publish(new ApplicationEvent(application, oldStatus, validated, actor));
        return application;
    }

    /** Добавление исходного документа к заявке. */
    public Document addSourceDocument(UUID applicationId, String fileUrl) {
        Document doc = new Document();
        doc.setApplicationId(applicationId);
        doc.setFileUrl(fileUrl);
        doc.setType(DocumentType.SOURCE);
        return documentRepository.save(doc);
    }

    /** Генерация готового нотариального документа. */
    public Document generateReadyDocument(UUID applicationId, String fileUrl) {
        Document doc = new Document();
        doc.setApplicationId(applicationId);
        doc.setFileUrl(fileUrl);
        doc.setType(DocumentType.READY);
        return documentRepository.save(doc);
    }

    /** Подписание документа клиентом. */
    public void signDocument(UUID documentId) {
        Document doc = documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Документ не найден: " + documentId));
        doc.setSigned(true);
        documentRepository.save(doc);
    }

    public Application getById(UUID id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка не найдена: " + id));
        application.getDocuments().addAll(documentRepository.findByApplicationId(id));
        return application;
    }

    public List<Application> getByClient(UUID clientId) {
        return applicationRepository.findByClientId(clientId);
    }

    public List<Document> getDocuments(UUID applicationId) {
        return documentRepository.findByApplicationId(applicationId);
    }
}
