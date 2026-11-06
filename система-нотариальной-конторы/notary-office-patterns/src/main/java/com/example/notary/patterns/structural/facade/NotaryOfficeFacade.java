package com.example.notary.patterns.structural.facade;

import com.example.notary.db.Database;
import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.AuditEntry;
import com.example.notary.model.Client;
import com.example.notary.model.Document;
import com.example.notary.model.Notification;
import com.example.notary.model.Payment;
import com.example.notary.model.Service;
import com.example.notary.patterns.behavioral.observer.ApplicationEventPublisher;
import com.example.notary.patterns.behavioral.observer.AuditLogListener;
import com.example.notary.patterns.behavioral.strategy.PricingContext;
import com.example.notary.patterns.creational.builder.ApplicationBuilder;
import com.example.notary.patterns.structural.adapter.ExternalPaymentStub;
import com.example.notary.patterns.structural.adapter.PaymentStubAdapter;
import com.example.notary.patterns.structural.decorator.EmailNotificationSender;
import com.example.notary.patterns.structural.decorator.LoggingNotificationSender;
import com.example.notary.patterns.structural.decorator.NotificationSender;
import com.example.notary.patterns.structural.decorator.RetryNotificationSender;
import com.example.notary.repository.ApplicationRepository;
import com.example.notary.repository.AuditLogRepository;
import com.example.notary.repository.ClientRepository;
import com.example.notary.repository.DocumentRepository;
import com.example.notary.repository.NotificationRepository;
import com.example.notary.repository.PaymentRepository;
import com.example.notary.repository.ServiceRepository;
import com.example.notary.service.ApplicationService;
import com.example.notary.service.ClientService;
import com.example.notary.service.NotificationService;
import com.example.notary.service.PaymentService;
import com.example.notary.service.ServiceCatalogService;

import java.util.List;

/**
 * Паттерн «Фасад» (Facade) — единая точка входа для клиентского приложения
 * (аналог API Gateway из архитектурного плана). Скрывает координацию
 * ClientService, ServiceCatalogService, ApplicationService и PaymentService,
 * а также сборку конвейера событий и уведомлений.
 */
public class NotaryOfficeFacade {

    private final ClientService clientService;
    private final ServiceCatalogService catalogService;
    private final ApplicationService applicationService;
    private final PaymentService paymentService;

    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationRepository applicationRepository;
    private final PaymentRepository paymentRepository;

    private final EmailNotificationSender emailSender;

    public NotaryOfficeFacade() {
        // Единственное подключение к БД (Singleton), схема создаётся при первом обращении
        Database.getInstance();

        ClientRepository clientRepository = new ClientRepository();
        ServiceRepository serviceRepository = new ServiceRepository();
        this.applicationRepository = new ApplicationRepository();
        DocumentRepository documentRepository = new DocumentRepository();
        this.paymentRepository = new PaymentRepository();
        this.notificationRepository = new NotificationRepository();
        this.auditLogRepository = new AuditLogRepository();

        // Конвейер событий: Наблюдатель — подписчики получают события заявок
        ApplicationEventPublisher eventPublisher = new ApplicationEventPublisher();

        // Декоратор: e-mail + логирование + повторные попытки при ошибке
        this.emailSender = new EmailNotificationSender();
        NotificationSender sender = new RetryNotificationSender(
                new LoggingNotificationSender(emailSender), 3);

        eventPublisher.subscribe(new NotificationService(
                notificationRepository, clientRepository, applicationRepository, sender));
        eventPublisher.subscribe(new AuditLogListener(auditLogRepository));

        this.clientService = new ClientService(clientRepository);
        this.catalogService = new ServiceCatalogService(serviceRepository);
        this.applicationService = new ApplicationService(
                applicationRepository, documentRepository, eventPublisher);
        // Адаптер: платёжная заглушка с несовместимым интерфейсом
        this.paymentService = new PaymentService(
                paymentRepository, applicationRepository, serviceRepository,
                new PaymentStubAdapter(new ExternalPaymentStub()));

        catalogService.ensureDefaultServices();
    }

    public Client registerClient(String fullName, String passportData, String phone, String email) {
        return clientService.registerClient(fullName, passportData, phone, email);
    }

    public Client registerClient(String fullName, String passportData, String phone, String email,
                                 String password) {
        return clientService.registerClient(fullName, passportData, phone, email, password);
    }

    public Client login(String email, String password) {
        return clientService.login(email, password);
    }

    public Client getClient(long id) {
        return clientService.findById(id);
    }

    public List<Client> getClients() {
        return clientService.findAll();
    }

    public Client updateClient(long id, String fullName, String passportData, String phone,
                               String email, String newPassword) {
        return clientService.updateProfile(id, fullName, passportData, phone, email, newPassword);
    }

    public List<Service> getServiceCatalog() {
        return catalogService.listServices();
    }

    public Service getService(long id) {
        return catalogService.findById(id);
    }

    public Service createService(String name, String description, java.math.BigDecimal price,
                                 com.example.notary.model.PricingType pricingType) {
        return catalogService.create(name, description, price, pricingType);
    }

    public Service updateService(long id, String name, String description, java.math.BigDecimal price,
                                 com.example.notary.model.PricingType pricingType) {
        return catalogService.update(id, name, description, price, pricingType);
    }

    public void deleteService(long id) {
        catalogService.delete(id);
    }

    /**
     * Подача заявки: Строитель собирает заявку пошагово,
     * далее событие запускает уведомление и запись в аудит.
     */
    public Application submitApplication(long clientId, long serviceId, String note,
                                         String... sourceDocuments) {
        Client client = clientService.findById(clientId);
        Service service = catalogService.findById(serviceId);

        ApplicationBuilder builder = new ApplicationBuilder()
                .forClient(client)
                .withService(service)
                .withNote(note);
        if (sourceDocuments != null) {
            for (String fileUrl : sourceDocuments) {
                builder.attachSourceDocument(fileUrl);
            }
        }
        return applicationService.createApplication(builder);
    }

    /** Смена статуса заявки нотариусом (проверяется машиной состояний). */
    public Application changeApplicationStatus(long applicationId, ApplicationStatus newStatus) {
        return applicationService.changeStatus(applicationId, newStatus);
    }

    public Document issueReadyDocument(long applicationId, String fileUrl) {
        return applicationService.issueReadyDocument(applicationId, fileUrl);
    }

    public Document addSourceDocument(long applicationId, String fileUrl) {
        return applicationService.addSourceDocument(applicationId, fileUrl);
    }

    public Application updateApplicationNote(long applicationId, String note) {
        return applicationService.updateNote(applicationId, note);
    }

    public Application getApplication(long id) {
        return applicationService.findById(id);
    }

    public List<Application> getClientApplications(long clientId) {
        return applicationService.findByClientId(clientId);
    }

    public List<Application> getApplications() {
        return applicationService.findAll();
    }

    public List<Document> getApplicationDocuments(long applicationId) {
        return applicationService.getDocuments(applicationId);
    }

    public Document getDocument(long id) {
        return applicationService.findDocument(id);
    }

    public Document signDocument(long applicationId, long documentId) {
        return applicationService.signDocument(applicationId, documentId);
    }

    public void deleteSourceDocument(long applicationId, long documentId) {
        applicationService.deleteSourceDocument(applicationId, documentId);
    }

    public Application receiveDocuments(long applicationId) {
        return applicationService.receiveDocuments(applicationId);
    }

    public Application cancelApplication(long applicationId) {
        return applicationService.cancel(applicationId);
    }

    public Payment payForApplication(long applicationId, PricingContext context) {
        return paymentService.payForApplication(applicationId, context);
    }

    public Payment getPayment(long id) {
        return paymentService.findById(id);
    }

    public List<Payment> getApplicationPayments(long applicationId) {
        return paymentService.findByApplicationId(applicationId);
    }

    public List<Notification> getClientNotifications(long clientId) {
        return notificationRepository.findByClientId(clientId);
    }

    /** Журнал аудита из БД. */
    public List<AuditEntry> getAuditLog() {
        return auditLogRepository.findAll();
    }

    /** Итоговая статистика по данным БД. */
    public String getStatistics() {
        return "клиентов: %d, заявок: %d, платежей: %d, уведомлений: %d, записей аудита: %d".formatted(
                clientService.findAll().size(),
                applicationRepository.findAll().size(),
                paymentRepository.findAll().size(),
                notificationRepository.findAll().size(),
                auditLogRepository.findAll().size());
    }

    /** Имитация разового сбоя почтового сервера (демонстрация повторной отправки). */
    public void simulateNextEmailFailure() {
        emailSender.failNextAttempt();
    }
}
