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

### Пример схемы PostgreSQL

Файл `src/main/resources/db/postgresql/schema.sql` содержит PostgreSQL-версию
схемы: ограничения, внешние ключи, индексы, комментарии и демонстрационные данные.
Для создания примера в отдельной базе:

```bash
createdb -U postgres notary_office
psql -U postgres -d notary_office -f src/main/resources/db/postgresql/schema.sql
```

Скрипт можно повторять в той же базе: таблицы и индексы создаются при их
отсутствии, а демонстрационные записи не дублируются. Значения `auth_data` в
примере являются заполнителями и не предназначены для входа в приложение.
Основное приложение по-прежнему использует H2; этот скрипт не меняет конфигурацию.

## Пользовательский интерфейс и API

Браузерное приложение реализует функции архитектурного плана без дополнительных
библиотек: встроенный HTTP-сервер Java раздаёт HTML/CSS/JavaScript и REST API.
`NotaryWebServer` выступает API Gateway и обращается только к `NotaryOfficeFacade`.

В кабинете клиента доступны регистрация и авторизация, редактирование профиля,
каталог услуг, создание и отмена заявки, загрузка и скачивание документов,
подписание готовых документов, имитация оплаты, подтверждение получения и история
уведомлений. В кабинете нотариуса доступны все заявки, изменение их статусов,
формирование готовых документов, CRUD услуг и журнал аудита.

Пароли клиентов хранятся в виде PBKDF2-хешей. HTTP-сессии разделяют роли клиента
и нотариуса; API проверяет владельца заявки. Файлы сохраняются в `data/uploads`,
а сведения о них — в H2. Платёж и отправка e-mail остаются учебными заглушками,
реальные деньги и письма через внешние сервисы не передаются.

## Структура проекта

```
src/main/java/com/example/notary/
├── Main.java                        — консольная демонстрация паттернов
├── WebApplication.java              — запуск веб-приложения
├── db/Database.java                 — Singleton, подключение и схема БД
├── model/                           — сущности и перечисления (статусы, типы)
├── repository/                      — JDBC-репозитории для всех таблиц
├── service/                         — ClientService, ServiceCatalogService,
│                                      ApplicationService, PaymentService,
│                                      NotificationService
├── web/                             — HTTP API Gateway и JSON-сериализация
└── patterns/
    ├── creational/                  — builder/, factory/
    ├── structural/                  — adapter/, decorator/, facade/
    └── behavioral/                  — observer/, state/, strategy/

src/main/resources/web/              — интерфейс (HTML, CSS, JavaScript)
```

## Запуск веб-приложения

Требования: JDK 17+, Maven 3.6+.

```bash
mvn compile exec:java
```

Откройте [http://localhost:8080](http://localhost:8080). Клиент может создать
учётную запись на вкладке «Регистрация». Демо-доступ нотариуса: `notary` /
`notary123`. Для другого порта передайте его аргументом, например
`-Dexec.args=8090`. Логин и пароль нотариуса можно изменить переменными среды
`NOTARY_LOGIN` и `NOTARY_PASSWORD`.

## Консольная демонстрация паттернов

```bash
mvn compile exec:java -Dexec.mainClass=com.example.notary.Main
```

Сценарий `Main` регистрирует клиентов, создаёт заявки, демонстрирует события и
уведомления, смену состояний, расчёт тарифов, платёжную заглушку, обработку
недопустимого перехода и чтение итоговых данных из H2.

## Проверка

```bash
mvn clean package
```
