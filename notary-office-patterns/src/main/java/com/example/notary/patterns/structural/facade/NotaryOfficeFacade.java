package com.example.notary.patterns.structural.facade;

import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.Client;
import com.example.notary.model.Document;
import com.example.notary.model.NotaryService;
import com.example.notary.model.Notification;
import com.example.notary.model.Payment;
import com.example.notary.patterns.creational.builder.ApplicationBuilder;
import com.example.notary.repository.NotificationRepository;
import com.example.notary.service.ApplicationService;
import com.example.notary.service.ClientService;
import com.example.notary.service.PaymentService;
import com.example.notary.service.ServiceCatalogService;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * СТРУКТУРНЫЙ ПАТТЕРН — ФАСАД (Facade).
 *
 * Задача из предметной области: клиентское приложение не должно
 * обращаться к внутренним сервисам напрямую (как в архитектурном плане
 * клиент работает через API Gateway). Фасад предоставляет простой
 * интерфейс и скрывает координацию ClientService, ServiceCatalogService,
 * ApplicationService и PaymentService.
 */
public class NotaryOfficeFacade {

    private final ClientService clientService;
    private final ServiceCatalogService catalogService;
    private final ApplicationService applicationService;
    private final PaymentService paymentService;
    private final NotificationRepository notificationRepository;

    public NotaryOfficeFacade(ClientService clientService,
                              ServiceCatalogService catalogService,
                              ApplicationService applicationService,
                              PaymentService paymentService,
                              NotificationRepository notificationRepository) {
        this.clientService = clientService;
        this.catalogService = catalogService;
        this.applicationService = applicationService;
        this.paymentService = paymentService;
        this.notificationRepository = notificationRepository;
    }

    /** Регистрация клиента. */
    public Client registerClient(String fullName, String passportData,
                                 String phone, String email, String password) {
        return clientService.register(fullName, passportData, phone, email, password);
    }

    /** Каталог нотариальных услуг. */
    public List<NotaryService> getServiceCatalog() {
        return catalogService.getAll();
    }

    /** Подача заявки: сборка через Builder, сохранение через ApplicationService. */
    public Application submitApplication(UUID clientId, UUID serviceId, String note,
                                         List<String> sourceDocumentUrls) {
        Application application = new ApplicationBuilder()
                .forClient(clientId)
                .withService(serviceId)
                .withNote(note)
                .withSourceDocuments(sourceDocumentUrls)
                .build();
        return applicationService.create(application);
    }

    /** Изменение статуса заявки (нотариус / клиент / система). */
    public Application changeApplicationStatus(UUID applicationId, ApplicationStatus newStatus, String actor) {
        return applicationService.changeStatus(applicationId, newStatus, actor);
    }

    /** Генерация готового нотариального документа по заявке. */
    public Document generateReadyDocument(UUID applicationId, String fileUrl) {
        return applicationService.generateReadyDocument(applicationId, fileUrl);
    }

    /** Подписание документа клиентом. */
    public void signDocument(UUID documentId) {
        applicationService.signDocument(documentId);
    }

    /** Оплата услуги по заявке: расчёт цены (Strategy) и проведение платежа (Adapter). */
    public Payment payForApplication(UUID applicationId, BigDecimal dealAmount, boolean privilegedClient) {
        Application application = applicationService.getById(applicationId);
        BigDecimal amount = catalogService.calculatePrice(application.getServiceId(), dealAmount, privilegedClient);
        return paymentService.createPayment(applicationId, amount);
    }

    /** Подтверждение клиентом получения готовых документов. */
    public Application confirmDocumentsReceived(UUID applicationId) {
        return applicationService.changeStatus(applicationId, ApplicationStatus.RECEIVED, "Клиент");
    }

    /** Заявки клиента (личный кабинет). */
    public List<Application> getClientApplications(UUID clientId) {
        return applicationService.getByClient(clientId);
    }

    /** Документы по заявке. */
    public List<Document> getApplicationDocuments(UUID applicationId) {
        return applicationService.getDocuments(applicationId);
    }

    /** Уведомления клиента. */
    public List<Notification> getClientNotifications(UUID clientId) {
        return notificationRepository.findByClientId(clientId);
    }
}
