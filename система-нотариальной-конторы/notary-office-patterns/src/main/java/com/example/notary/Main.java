package com.example.notary;

import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.Client;
import com.example.notary.model.Document;
import com.example.notary.model.Notification;
import com.example.notary.model.Payment;
import com.example.notary.model.Service;
import com.example.notary.patterns.behavioral.strategy.PricingContext;
import com.example.notary.patterns.structural.facade.NotaryOfficeFacade;

import java.math.BigDecimal;
import java.util.List;

/**
 * Демонстрационный сценарий системы для клиентов нотариальной конторы.
 * Все операции выполняются через фасад {@link NotaryOfficeFacade} —
 * единую точку входа клиентского приложения.
 */
public class Main {

    private static final NotaryOfficeFacade facade = new NotaryOfficeFacade();

    public static void main(String[] args) {
        System.out.println("=".repeat(66));
        System.out.println("  Система для клиентов нотариальной конторы");
        System.out.println("  Демонстрация паттернов GoF + база данных H2");
        System.out.println("=".repeat(66));

        List<Client> clients = step1Clients();
        Client ivanov = clients.get(0);
        Client petrova = clients.get(1);

        List<Service> catalog = step2Catalog();

        Application application = step3Submit(ivanov, catalog.get(0));
        step4Statuses(application);
        step5Document(application);
        step6PaymentFixed(application);
        step7Privileged(petrova, catalog.get(2));
        step8Percentage(ivanov, catalog.get(3));
        step9InvalidTransition(application);
        step10Results(ivanov);
    }

    /** Шаг 1. Регистрация двух клиентов. */
    private static List<Client> step1Clients() {
        section(1, "Регистрация клиентов");
        Client first = facade.registerClient(
                "Иванов Иван Иванович", "4510 123456", "+7 900 111-22-33", "ivanov@example.com");
        Client second = facade.registerClient(
                "Петрова Анна Сергеевна", "4511 654321", "+7 900 444-55-66", "petrova@example.com");
        System.out.println("  Зарегистрирован: " + first);
        System.out.println("  Зарегистрирован: " + second + " (льготная категория)");
        return List.of(first, second);
    }

    /** Шаг 2. Просмотр каталога нотариальных услуг. */
    private static List<Service> step2Catalog() {
        section(2, "Каталог нотариальных услуг");
        List<Service> catalog = facade.getServiceCatalog();
        catalog.forEach(service -> System.out.println("  " + service));
        return catalog;
    }

    /**
     * Шаг 3. Подача заявки: Строитель → Фасад → событие (Наблюдатель) →
     * уведомление (Фабричный метод + Декоратор: логирование и повторная
     * отправка после имитированной сбойной попытки).
     */
    private static Application step3Submit(Client client, Service service) {
        section(3, "Подача заявки (Builder → Facade → Observer → Factory + Decorator)");
        System.out.println("  Имитируем разовый сбой почтового сервера...");
        facade.simulateNextEmailFailure();
        Application application = facade.submitApplication(
                client.getId(), service.getId(),
                "Доверенность на распоряжение автомобилем",
                "files/source/passport-scan.pdf",
                "files/source/car-certificate.pdf");
        System.out.println("  Создана: " + application);
        return application;
    }

    /** Шаг 4. Смена статусов заявки нотариусом (Состояние), каждая смена — событие и уведомление. */
    private static void step4Statuses(Application application) {
        section(4, "Работа нотариуса: смена статусов заявки (State + Observer)");
        facade.changeApplicationStatus(application.getId(), ApplicationStatus.IN_PROGRESS);
        System.out.println("  Статус: «В процессе» — нотариус проверяет документы");
        facade.changeApplicationStatus(application.getId(), ApplicationStatus.READY);
        System.out.println("  Статус: «Готова» — документы подготовлены");
    }

    /** Шаг 5. Генерация готового документа. */
    private static void step5Document(Application application) {
        section(5, "Генерация готового документа");
        Document document = facade.issueReadyDocument(
                application.getId(), "files/ready/doverennost-" + application.getId() + ".pdf");
        System.out.println("  Выдан: " + document);
    }

    /** Шаг 6. Оплата по фиксированному тарифу (Стратегия) через платёжную заглушку (Адаптер). */
    private static void step6PaymentFixed(Application application) {
        section(6, "Оплата заявки — фиксированный тариф (Strategy + Adapter)");
        Payment payment = facade.payForApplication(application.getId(), PricingContext.EMPTY);
        System.out.println("  " + payment);
        facade.changeApplicationStatus(application.getId(), ApplicationStatus.RECEIVED);
        System.out.println("  Документы выданы, заявка переведена в статус «Получена»");
    }

    /** Шаг 7. Заявка льготного клиента — тариф со скидкой 30 % (Стратегия). */
    private static void step7Privileged(Client privilegedClient, Service service) {
        section(7, "Заявка льготного клиента — скидка 30 % (Strategy)");
        Application application = facade.submitApplication(
                privilegedClient.getId(), service.getId(),
                "Завещание, льготная категория",
                "files/source/passport-petrova.pdf",
                "files/source/privilege-certificate.pdf");
        System.out.println("  Создана: " + application);
        Payment payment = facade.payForApplication(application.getId(), PricingContext.EMPTY);
        System.out.printf("  Базовый тариф: %.2f руб. → со скидкой 30 %%: %.2f руб. (%s)%n",
                service.getPrice(), payment.getAmount(), payment.getStatus().getDisplayName());
    }

    /** Шаг 8. Расчёт стоимости удостоверения договора — 0,5 % от суммы сделки (Стратегия). */
    private static void step8Percentage(Client client, Service service) {
        section(8, "Удостоверение договора — 0,5 % от суммы сделки (Strategy)");
        Application application = facade.submitApplication(
                client.getId(), service.getId(),
                "Договор купли-продажи квартиры",
                "files/source/deal-draft.pdf");
        System.out.println("  Создана: " + application);
        BigDecimal dealAmount = new BigDecimal("5000000.00");
        Payment payment = facade.payForApplication(
                application.getId(), PricingContext.ofDealAmount(dealAmount));
        System.out.printf("  Сумма сделки: %.2f руб. → тариф 0,5 %%: %.2f руб. (%s)%n",
                dealAmount, payment.getAmount(), payment.getStatus().getDisplayName());
    }

    /** Шаг 9. Попытка недопустимого перехода «Получена» → «В процессе» — отклоняется машиной состояний. */
    private static void step9InvalidTransition(Application application) {
        section(9, "Недопустимый переход «Получена» → «В процессе» (State)");
        try {
            facade.changeApplicationStatus(application.getId(), ApplicationStatus.IN_PROGRESS);
            System.out.println("  ОШИБКА ДЕМО: переход не должен был быть разрешён!");
        } catch (IllegalStateException e) {
            System.out.println("  Переход отклонён машиной состояний: " + e.getMessage());
        }
    }

    /** Шаг 10. Уведомления клиента и журнал аудита из БД, итоговая статистика. */
    private static void step10Results(Client client) {
        section(10, "Данные из БД: уведомления клиента, журнал аудита, статистика");
        System.out.println("  Уведомления клиента «%s»:".formatted(client.getFullName()));
        for (Notification notification : facade.getClientNotifications(client.getId())) {
            System.out.println("    - " + notification);
        }
        System.out.println("  Журнал аудита:");
        facade.getAuditLog().forEach(entry -> System.out.println("    - " + entry));
        System.out.println("  Итоговая статистика: " + facade.getStatistics());
        System.out.println("=".repeat(66));
        System.out.println("  Демонстрация завершена. Данные сохранены в ./data/notarydb");
        System.out.println("=".repeat(66));
    }

    private static void section(int number, String title) {
        System.out.println();
        System.out.println("[%d] %s".formatted(number, title));
        System.out.println("-".repeat(66));
    }
}
