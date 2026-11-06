package com.example.notary.service;

import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.Document;
import com.example.notary.model.DocumentType;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;
import com.example.notary.patterns.behavioral.observer.ApplicationEventPublisher;
import com.example.notary.patterns.behavioral.observer.ApplicationEventType;
import com.example.notary.patterns.behavioral.state.ApplicationStateMachine;
import com.example.notary.patterns.creational.builder.ApplicationBuilder;
import com.example.notary.repository.ApplicationRepository;
import com.example.notary.repository.DocumentRepository;

import java.util.List;

/**
 * Сервис заявок: создание (через «Строителя»), смена статусов
 * (через машину состояний) и публикация событий (паттерн «Наблюдатель»).
 */
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final DocumentRepository documentRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ApplicationService(ApplicationRepository applicationRepository,
                              DocumentRepository documentRepository,
                              ApplicationEventPublisher eventPublisher) {
        this.applicationRepository = applicationRepository;
        this.documentRepository = documentRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Создаёт заявку по собранным строителем данным и публикует событие
     * {@code APPLICATION_CREATED} — подписчики отправят уведомление
     * и запишут запись в журнал аудита.
     */
    public Application createApplication(ApplicationBuilder builder) {
        Application application = applicationRepository.save(builder.build());
        for (Document document : builder.getSourceDocuments()) {
            document.setApplicationId(application.getId());
            documentRepository.save(document);
        }
        eventPublisher.publish(new ApplicationEvent(
                ApplicationEventType.APPLICATION_CREATED,
                application.getId(), application.getClientId(),
                "заявка создана"));
        return application;
    }

    /**
     * Переводит заявку в новый статус. Машина состояний отклоняет
     * недопустимые переходы; успешный переход публикует событие.
     */
    public Application changeStatus(long applicationId, ApplicationStatus target) {
        Application application = findById(applicationId);
        ApplicationStateMachine stateMachine = new ApplicationStateMachine(application.getStatus());
        stateMachine.transitionTo(target);

        application.setStatus(target);
        applicationRepository.updateStatus(applicationId, target);

        eventPublisher.publish(new ApplicationEvent(
                eventTypeFor(target),
                application.getId(), application.getClientId(),
                "статус изменён на «%s»".formatted(target.getDisplayName())));
        return application;
    }

    public Document issueReadyDocument(long applicationId, String fileUrl) {
        Application application = findById(applicationId);
        if (application.getStatus() != ApplicationStatus.READY) {
            throw new IllegalStateException(
                    "Документ можно выдать только по заявке в статусе «Готова», текущий статус: «%s»"
                            .formatted(application.getStatus().getDisplayName()));
        }
        return documentRepository.save(new Document(null, applicationId, fileUrl, DocumentType.RESULT, false));
    }

    public Document addSourceDocument(long applicationId, String fileUrl) {
        Application application = findById(applicationId);
        if (application.getStatus() != ApplicationStatus.CREATED
                && application.getStatus() != ApplicationStatus.IN_PROGRESS) {
            throw new IllegalStateException("Исходные документы можно добавлять до завершения заявки");
        }
        return documentRepository.save(new Document(null, applicationId, fileUrl, DocumentType.SOURCE, false));
    }

    public Application updateNote(long applicationId, String note) {
        Application application = findById(applicationId);
        if (application.getStatus() != ApplicationStatus.CREATED) {
            throw new IllegalStateException("Описание можно изменить только у заявки в статусе «Создана»");
        }
        application.setNote(note == null ? "" : note.trim());
        applicationRepository.updateNote(applicationId, application.getNote());
        return application;
    }

    public List<Application> findByClientId(long clientId) {
        return applicationRepository.findByClientId(clientId);
    }

    public List<Application> findAll() {
        return applicationRepository.findAll();
    }

    public List<Document> getDocuments(long applicationId) {
        findById(applicationId);
        return documentRepository.findByApplicationId(applicationId);
    }

    public Document findDocument(long documentId) {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Документ #" + documentId + " не найден"));
    }

    public Document signDocument(long applicationId, long documentId) {
        Application application = findById(applicationId);
        Document document = findDocument(documentId);
        if (!document.getApplicationId().equals(applicationId)) {
            throw new IllegalArgumentException("Документ не относится к этой заявке");
        }
        if (application.getStatus() != ApplicationStatus.READY || document.getType() != DocumentType.RESULT) {
            throw new IllegalStateException("Подписать можно только готовый документ заявки в статусе «Готова»");
        }
        document.setSigned(true);
        documentRepository.updateSigned(documentId, true);
        return document;
    }

    public void deleteSourceDocument(long applicationId, long documentId) {
        Application application = findById(applicationId);
        Document document = findDocument(documentId);
        if (!document.getApplicationId().equals(applicationId)) {
            throw new IllegalArgumentException("Документ не относится к этой заявке");
        }
        if (application.getStatus() != ApplicationStatus.CREATED || document.getType() != DocumentType.SOURCE) {
            throw new IllegalStateException("Удалить можно только исходный документ новой заявки");
        }
        documentRepository.delete(documentId);
    }

    public Application receiveDocuments(long applicationId) {
        List<Document> documents = getDocuments(applicationId);
        boolean hasSignedResult = documents.stream()
                .anyMatch(document -> document.getType() == DocumentType.RESULT && document.isSigned());
        if (!hasSignedResult) {
            throw new IllegalStateException("Сначала подпишите хотя бы один готовый документ");
        }
        return changeStatus(applicationId, ApplicationStatus.RECEIVED);
    }

    public Application cancel(long applicationId) {
        return changeStatus(applicationId, ApplicationStatus.CANCELLED);
    }

    public Application findById(long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Заявка #" + id + " не найдена"));
    }

    private ApplicationEventType eventTypeFor(ApplicationStatus status) {
        return switch (status) {
            case READY -> ApplicationEventType.DOCUMENTS_READY;
            case REJECTED -> ApplicationEventType.APPLICATION_REJECTED;
            case CANCELLED -> ApplicationEventType.APPLICATION_CANCELLED;
            default -> ApplicationEventType.STATUS_CHANGED;
        };
    }
}
