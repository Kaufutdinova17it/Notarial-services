package com.example.notary;

import com.example.notary.db.Database;
import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.Client;
import com.example.notary.model.Document;
import com.example.notary.model.NotaryService;
import com.example.notary.model.Payment;
import com.example.notary.patterns.behavioral.observer.ApplicationEventPublisher;
import com.example.notary.patterns.behavioral.observer.AuditLogListener;
import com.example.notary.patterns.structural.adapter.ExternalPaymentStub;
import com.example.notary.patterns.structural.adapter.PaymentGateway;
import com.example.notary.patterns.structural.adapter.PaymentStubAdapter;
import com.example.notary.patterns.structural.decorator.EmailNotificationSender;
import com.example.notary.patterns.structural.decorator.LoggingNotificationSender;
import com.example.notary.patterns.structural.decorator.NotificationSender;
import com.example.notary.patterns.structural.decorator.RetryNotificationSender;
import com.example.notary.patterns.structural.facade.NotaryOfficeFacade;
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

import java.math.BigDecimal;
import java.util.List;

/**
 * Демонстрационный сценарий работы системы для клиентов нотариальной конторы.
 * Показывает работу всех реализованных паттернов и сохранение данных в БД H2.
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("  СИСТЕМА ДЛЯ КЛИЕНТОВ НОТАРИАЛЬНОЙ КОНТОРЫ");
        System.out.println("  Демонстрация паттернов проектирования и работы с базой данных");
        System.out.println("==================================================================");

        // --- Singleton: единственное подключение к БД, инициализация схемы ---
        Database.getInstance();
        System.out.println("\n[Singleton] Подключение к БД H2 установлено, схема инициализирована.");

        // Репозитории (работают через Singleton Database)
        ClientRepository clientRepository = new ClientRepository();
        ServiceRepository serviceRepository = new ServiceRepository();
        ApplicationRepository applicationRepository = new ApplicationRepository();
        DocumentRepository documentRepository = new DocumentRepository();
        PaymentRepository paymentRepository = new PaymentRepository();
        NotificationRepository notificationRepository = new NotificationRepository();
        AuditLogRepository auditLogRepository = new AuditLogRepository();

        // --- Decorator: отправка уведомлений = Email + повторные попытки + логирование ---
        NotificationSender sender = new LoggingNotificationSender(
                new RetryNotificationSender(new EmailNotificationSender(), 3));
        System.out.println("[Decorator] Отправитель уведомлений собран: Email + Retry + Logging.");

        // --- Adapter: платёжный шлюз через адаптер к внешней заглушке ---
        PaymentGateway paymentGateway = new PaymentStubAdapter(new ExternalPaymentStub(true));
        System.out.println("[Adapter] Платёжная заглушка подключена через адаптер PaymentGateway.");

        // --- Observer: издатель событий и подписчики ---
        ApplicationEventPublisher eventPublisher = new ApplicationEventPublisher();
        NotificationService notificationService =
                new NotificationService(notificationRepository, clientRepository, sender);
        eventPublisher.subscribe(notificationService);
        eventPublisher.subscribe(new AuditLogListener(auditLogRepository));
        System.out.println("[Observer] Подписчики событий по заявкам: NotificationService, журнал аудита.");

        // Сервисный слой
        ClientService clientService = new ClientService(clientRepository);
        ServiceCatalogService catalogService = new ServiceCatalogService(serviceRepository);
        ApplicationService applicationService =
                new ApplicationService(applicationRepository, documentRepository, eventPublisher);
        PaymentService paymentService = new PaymentService(paymentRepository, paymentGateway);

        // --- Facade: единая точка входа для клиентского приложения ---
        NotaryOfficeFacade facade = new NotaryOfficeFacade(
                clientService, catalogService, applicationService, paymentService, notificationRepository);

        // Наполнение каталога услуг
        catalogService.seedDefaultServices();

        // ---------- Сценарий ----------
        System.out.println("\n----- 1. Регистрация клиентов -----");
        Client ivanov = facade.registerClient(
                "Иванов Иван Иванович", "1234 567890", "+7 900 000-00-01", "ivanov@mail.ru", "secret1");
        Client petrova = facade.registerClient(
                "Петрова Анна Андреевна", "9876 543210", "+7 900 000-00-02", "petrova@mail.ru", "secret2");
        System.out.println("Зарегистрированы: " + ivanov + "; " + petrova);

        System.out.println("\n----- 2. Каталог нотариальных услуг -----");
        List<NotaryService> catalog = facade.getServiceCatalog();
        catalog.forEach(s -> System.out.println("   * " + s));

        System.out.println("\n----- 3. Подача заявки клиентом Ивановым (Builder + Facade) -----");
        NotaryService doverennost = findService(catalog, "Оформление доверенности");
        Application application = facade.submitApplication(
                ivanov.getId(), doverennost.getId(),
                "Доверенность на управление автомобилем",
                List.of("/uploads/passport_ivanov.pdf", "/uploads/pts.pdf"));
        System.out.println("   Создана: " + application);
        System.out.println("   Исходные документы: " + facade.getApplicationDocuments(application.getId()));

        System.out.println("\n----- 4. Нотариус берёт заявку в работу (State + Observer) -----");
        facade.changeApplicationStatus(application.getId(), ApplicationStatus.IN_PROGRESS,
                "Нотариус Сидорова О.В.");

        System.out.println("\n----- 5. Подготовка документов -----");
        Document readyDoc = facade.generateReadyDocument(
                application.getId(), "/generated/doverennost_" + application.shortId() + ".pdf");
        facade.changeApplicationStatus(application.getId(), ApplicationStatus.READY,
                "Нотариус Сидорова О.В.");

        System.out.println("\n----- 6. Клиент подписывает готовый документ -----");
        facade.signDocument(readyDoc.getId());
        System.out.println("   Подписан: " + readyDoc.getFileUrl());

        System.out.println("\n----- 7. Оплата услуги (Strategy: фиксированный тариф, Adapter) -----");
        Payment payment = facade.payForApplication(application.getId(), null, false);
        System.out.println("   " + payment);

        System.out.println("\n----- 8. Клиент подтверждает получение документов -----");
        facade.confirmDocumentsReceived(application.getId());
        System.out.println("   Итоговый статус: "
                + applicationService.getById(application.getId()).getStatus().getTitle());

        System.out.println("\n----- 9. Заявка льготного клиента (Strategy: льготный тариф) -----");
        NotaryService kopiya = findService(catalog, "Заверение копии документа");
        Application app2 = facade.submitApplication(
                petrova.getId(), kopiya.getId(),
                "Заверение копии паспорта", List.of("/uploads/passport_petrova.pdf"));
        Payment payment2 = facade.payForApplication(app2.getId(), null, true);
        System.out.println("   " + payment2);

        System.out.println("\n----- 10. Процентный тариф (Strategy: процент от суммы сделки) -----");
        NotaryService dogovor = findService(catalog, "Удостоверение договора");
        BigDecimal price = catalogService.calculatePrice(dogovor.getId(), new BigDecimal("6500000"), false);
        System.out.println("   Стоимость услуги «" + dogovor.getName() + "» при сумме сделки 6 500 000 руб.: "
                + price + " руб.");

        System.out.println("\n----- 11. Проверка недопустимого перехода (State) -----");
        try {
            facade.changeApplicationStatus(application.getId(), ApplicationStatus.IN_PROGRESS,
                    "Нотариус Сидорова О.В.");
        } catch (IllegalStateException e) {
            System.out.println("   Переход отклонён: " + e.getMessage());
        }

        System.out.println("\n----- 12. Уведомления клиента Иванова (из БД) -----");
        facade.getClientNotifications(ivanov.getId())
                .forEach(n -> System.out.println("   " + n));

        System.out.println("\n----- 13. Журнал аудита (из БД) -----");
        auditLogRepository.findAll().forEach(r -> System.out.println("   " + r));

        System.out.println("\n----- 14. Итоговое состояние базы данных -----");
        System.out.println("   Клиентов:      " + clientRepository.findAll().size());
        System.out.println("   Услуг:         " + serviceRepository.findAll().size());
        System.out.println("   Заявок:        " + applicationRepository.findAll().size());
        System.out.println("   Платежей:      " + paymentRepository.findAll().size());
        System.out.println("   Уведомлений:   " + notificationRepository.findAll().size());
        System.out.println("   Записей аудита: " + auditLogRepository.findAll().size());
        System.out.println("\nДанные сохранены в базе H2 (файл ./data/notarydb).");
    }

    /** Поиск услуги в каталоге по фрагменту названия. */
    private static NotaryService findService(List<NotaryService> catalog, String namePart) {
        return catalog.stream()
                .filter(s -> s.getName().contains(namePart))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Услуга не найдена в каталоге: " + namePart));
    }
}
