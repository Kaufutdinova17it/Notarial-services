# Система для клиентов нотариальной конторы — реализация паттернов проектирования

Учебный проект по предметной области «Система для клиентов нотариальной конторы»
(см. архитектурный план и проектное решение). Реализованы паттерны GoF и
подключена база данных H2. Каждый паттерн закрывает конкретную задачу
из предметной области.

## Реализованные паттерны

### Порождающие

| Паттерн | Классы | Задача из предметной области |
|---|---|---|
| **Строитель (Builder)** | `patterns/creational/builder/ApplicationBuilder` | Заявка создаётся пошагово: выбор услуги, примечание, прикрепление исходных документов. Строитель проверяет обязательные поля и собирает заявку со статусом «Создана» |
| **Фабричный метод (Factory Method)** | `patterns/creational/factory/NotificationFactory` + 5 конкретных фабрик | По событию заявки (создание, смена статуса, готовность документов, отклонение, отмена) формируется уведомление со своим текстом. Базовый класс задаёт общий алгоритм, конкретные фабрики — текст сообщения |
| **Одиночка (Singleton)** *(бонус)* | `db/Database` | Единственное подключение к базе данных H2, используется всеми репозиториями |

### Структурные

| Паттерн | Классы | Задача из предметной области |
|---|---|---|
| **Адаптер (Adapter)** | `patterns/structural/adapter/PaymentGateway`, `ExternalPaymentStub`, `PaymentStubAdapter` | Платёжный сервис-заглушка имеет несовместимый интерфейс (сумма в копейках, числовые коды результата). Адаптер согласует его с интерфейсом `PaymentGateway`, используемым системой |
| **Декоратор (Decorator)** | `patterns/structural/decorator/NotificationSender`, `EmailNotificationSender`, `LoggingNotificationSender`, `RetryNotificationSender` | Отправка уведомлений на e-mail динамически дополняется логированием и повторными попытками при ошибке (требование из проектного решения) |
| **Фасад (Facade)** | `patterns/structural/facade/NotaryOfficeFacade` | Единая точка входа для клиентского приложения (аналог API Gateway из архитектурного плана): скрывает координацию ClientService, ServiceCatalogService, ApplicationService и PaymentService |

### Поведенческие

| Паттерн | Классы | Задача из предметной области |
|---|---|---|
| **Наблюдатель (Observer)** | `patterns/behavioral/observer/ApplicationEventPublisher`, `ApplicationListener`, `AuditLogListener`; подписчик — `NotificationService` | Событийное взаимодействие из архитектурного плана: после создания заявки или изменения её статуса ApplicationService публикует событие, NotificationService отправляет уведомление, AuditLogListener пишет в журнал аудита |
| **Состояние (State)** | `patterns/behavioral/state/ApplicationState`, `CreatedState`, `InProgressState`, `ReadyState`, `TerminalState`, `ApplicationStateMachine` | Жизненный цикл заявки: Создана → В процессе → Готова → Получена (+ Отклонена, Отменена). Каждое состояние само определяет допустимые переходы; недопустимый переход вызывает исключение |
| **Стратегия (Strategy)** | `patterns/behavioral/strategy/PricingStrategy`, `FixedPricingStrategy`, `PercentagePricingStrategy`, `PrivilegedDiscountStrategy` | Расчёт стоимости услуги: фиксированный тариф, процент от суммы сделки (удостоверение договоров), льготный тариф со скидкой. Алгоритм выбирается во время выполнения |

## База данных

Встраиваемая СУБД **H2** (файл `./data/notarydb`, создаётся при первом запуске).
Схема соответствует модели данных из архитектурного плана:

| Таблица | Сущность |
|---|---|
| `clients` | Client — клиент (id, fullName, passportData, phone, email, authData) |
| `services` | Service — нотариальная услуга (id, name, description, price, pricingType) |
| `applications` | Application — заявка (id, clientId, serviceId, status, note, createdAt) |
| `documents` | Document — документ (id, applicationId, fileUrl, type, signed) |
| `payments` | Payment — платёж (id, applicationId, amount, status, createdAt) |
| `notifications` | Notification — уведомление (id, clientId, applicationId, type, message, status, createdAt) |
| `audit_log` | Журнал аудита — протоколирование действий пользователя |

Доступ к данным — через JDBC-репозитории (`repository/`).

## Структура проекта

```
src/main/java/com/example/notary/
├── Main.java                        — демонстрационный сценарий
├── db/Database.java                 — Singleton, подключение и схема БД
├── model/                           — сущности и перечисления (статусы, типы)
├── repository/                      — JDBC-репозитории для всех таблиц
├── service/                         — ClientService, ServiceCatalogService,
│                                      ApplicationService, PaymentService,
│                                      NotificationService
└── patterns/
    ├── creational/                  — builder/, factory/
    ├── structural/                  — adapter/, decorator/, facade/
    └── behavioral/                  — observer/, state/, strategy/
```

## Запуск

Требования: JDK 17+, Maven 3.6+.

```bash
mvn compile exec:java
```

Демонстрационный сценарий (`Main`):

1. Регистрация двух клиентов.
2. Просмотр каталога нотариальных услуг.
3. Подача заявки (Builder) через фасад → событие (Observer) → уведомление
   (Factory Method + Decorator: логирование и повторная отправка после
   имитированной сбойной попытки).
4. Смена статусов заявки нотариусом (State), каждая смена — событие
   и уведомление клиенту.
5. Генерация и подписание готового документа.
6. Оплата по фиксированному тарифу (Strategy) через платёжную заглушку
   (Adapter).
7. Заявка льготного клиента — тариф со скидкой 30 % (Strategy).
8. Расчёт стоимости удостоверения договора — 0,5 % от суммы сделки (Strategy).
9. Попытка недопустимого перехода «Получена» → «В процессе» — отклоняется
   машиной состояний (State).
10. Вывод уведомлений клиента и журнала аудита из БД, итоговая статистика.
