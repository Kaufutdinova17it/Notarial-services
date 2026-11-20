# Система для клиентов нотариальной конторы — микросервисная архитектура

Учебный проект по предметной области «Система для клиентов нотариальной конторы».
Реализация соответствует исправленному архитектурному плану: пять самостоятельных
доменных сервисов, API Gateway, отдельная база PostgreSQL для каждого сервиса и
событийная передача изменений заявок в сервис уведомлений. Паттерны GoF сохранены.

## Архитектура

| Модуль | Порт | Ответственность | База PostgreSQL |
|---|---:|---|---|
| `api-gateway` | 8080 | Единая точка входа, веб-интерфейс, сессии и роли | Не используется |
| `client-service` | 8081 | Регистрация, авторизация и профили клиентов | `notary_client` |
| `catalog-service` | 8082 | Каталог услуг и управление тарифами | `notary_catalog` |
| `application-service` | 8083 | Заявки, документы, состояния и аудит | `notary_application` |
| `payment-service` | 8084 | Расчёт стоимости и имитация оплаты | `notary_payment` |
| `notification-service` | 8085 | Формирование и хранение уведомлений | `notary_notification` |

Все базы находятся на одном PostgreSQL-сервере, по умолчанию `localhost:5432`.
Таблицы и стандартный каталог услуг создаются автоматически при запуске сервисов.
Прямого доступа к чужим таблицам и внешних ключей между базами нет.

Клиент обращается только к API Gateway. Между сервисами используется HTTP/JSON.
После создания заявки или смены статуса ApplicationService отправляет событие в
`POST /api/events/applications` сервиса уведомлений. Платёжный шлюз и отправка
e-mail остаются учебными заглушками.

## Структура проекта

```text
notary-office-patterns/
├── pom.xml                         — родительский Maven-проект
├── common/                         — модели, JSON, JDBC и HTTP-инфраструктура
├── client-service/                 — ClientService и ClientRepository
├── catalog-service/                — ServiceCatalogService и ServiceRepository
├── application-service/            — ApplicationService, документы и аудит
├── payment-service/                — PaymentService и платёжный адаптер
├── notification-service/           — NotificationService и отправители
├── api-gateway/                    — HTTP Gateway и ресурсы web/
├── launcher/                       — запуск JAR как отдельных процессов
└── db/postgresql/
    └── create-databases.sql        — создание пяти баз на одном сервере
```

## 1. Создание баз в DataGrip

Требуется запущенный PostgreSQL-сервер и пользователь с правом создавать базы.

1. В DataGrip выберите **Database → New → Data Source → PostgreSQL**.
2. Подключитесь к серверу, указав хост `localhost`, порт `5432`, пользователя и пароль.
   Для первого подключения можно выбрать стандартную базу `postgres`.
3. Откройте файл `db/postgresql/create-databases.sql` в консоли запросов и выполните
   его целиком:

   ```sql
   CREATE DATABASE notary_client;
   CREATE DATABASE notary_catalog;
   CREATE DATABASE notary_application;
   CREATE DATABASE notary_payment;
   CREATE DATABASE notary_notification;
   ```

   Скрипт предназначен для однократного выполнения. Если базы уже существуют,
   повторно создавать их не нужно.
4. Обновите список баз в DataGrip. Для просмотра таблиц можно добавить каждую базу
   как отдельный Data Source либо раскрыть их внутри существующего подключения.

После запуска приложения в базах появятся таблицы `clients`, `services`,
`applications`, `documents`, `audit_log`, `payments` и `notifications` — каждая в
базе своего микросервиса.

## 2. Настройка подключения приложения

По умолчанию используются:

```text
DB_TYPE=postgresql
POSTGRES_SERVER_URL=jdbc:postgresql://localhost:5432
POSTGRES_USER=postgres
```

Пароль необходимо задать через `POSTGRES_PASSWORD`. Пример для PowerShell:

```powershell
$env:POSTGRES_USER = "postgres"
$env:POSTGRES_PASSWORD = "ваш_пароль"
```

Если PostgreSQL работает на другом компьютере или порту:

```powershell
$env:POSTGRES_SERVER_URL = "jdbc:postgresql://192.168.1.10:5432"
```

При необходимости можно переопределить URL каждой базы через `CLIENT_DB_URL`,
`CATALOG_DB_URL`, `APPLICATION_DB_URL`, `PAYMENT_DB_URL`, `NOTIFICATION_DB_URL`.
Переменные среды наследуются всеми процессами, которые запускает launcher. Пароль в
код и SQL-файлы записывать не требуется.

## 3. Запуск всего приложения

Требования: JDK 17+, Maven 3.6+ и PostgreSQL. В том же терминале, где заданы
переменные среды, выполните:

```bash
mvn clean package
java -jar launcher/target/notary-launcher-1.0.0.jar
```

Launcher последовательно запускает шесть исполняемых JAR как независимые процессы,
проверяет их готовность и останавливает при завершении. Откройте
[http://localhost:8080](http://localhost:8080). Демо-доступ нотариуса:
`notary` / `notary123`.

Старая монолитная база `data/notarydb` не удаляется, но PostgreSQL-конфигурацией не
используется. Автоматическая миграция её записей не выполняется.

## Независимый запуск сервисов

После `mvn clean package` каждый сервис можно запустить отдельно:

```bash
java -jar client-service/target/client-service-1.0.0.jar
java -jar catalog-service/target/catalog-service-1.0.0.jar
java -jar notification-service/target/notification-service-1.0.0.jar
java -jar application-service/target/application-service-1.0.0.jar
java -jar payment-service/target/payment-service-1.0.0.jar
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

Порты задаются первым аргументом или переменными `CLIENT_SERVICE_PORT`,
`CATALOG_SERVICE_PORT`, `APPLICATION_SERVICE_PORT`, `PAYMENT_SERVICE_PORT`,
`NOTIFICATION_SERVICE_PORT`, `GATEWAY_PORT`. Адреса зависимостей настраиваются через
`CLIENT_SERVICE_URL`, `CATALOG_SERVICE_URL`, `APPLICATION_SERVICE_URL`,
`PAYMENT_SERVICE_URL`, `NOTIFICATION_SERVICE_URL`. Каталог файлов —
`NOTARY_UPLOADS_DIR`. Логин и пароль нотариуса — `NOTARY_LOGIN` и `NOTARY_PASSWORD`.

### Режим H2 для локальной проверки

PostgreSQL является режимом по умолчанию. Для временного возврата к файловым базам
H2 задайте `DB_TYPE=h2`. Базы будут созданы в каталоге `NOTARY_DATA_DIR` (по
умолчанию `data`). Этот режим не синхронизируется с PostgreSQL.

## Пользовательский интерфейс и API

Внешние маршруты монолитного приложения сохранены. Доступны регистрация и
авторизация, редактирование профиля, каталог услуг, создание и отмена заявки,
загрузка и скачивание документов, подписание, имитация оплаты, подтверждение
получения и история уведомлений. Нотариус может управлять заявками, статусами,
документами, услугами и журналом аудита.

Пароли клиентов хранятся в виде PBKDF2-хешей. Gateway разделяет роли и проверяет
владельца заявки. Файлы сохраняются в `data/uploads`, а их метаданные — только в
базе ApplicationService.

## Реализованные паттерны

| Паттерн | Расположение | Назначение |
|---|---|---|
| **Builder** | `application-service`, `ApplicationBuilder` | Пошаговая сборка заявки |
| **Factory Method** | `notification-service`, `NotificationFactory` | Текст уведомления по типу события |
| **Singleton** | `common`, `Database` | Одно соединение с собственной базой внутри сервиса |
| **Adapter** | `payment-service`, `PaymentStubAdapter` | Согласование платёжной заглушки с API |
| **Decorator** | `notification-service`, отправители | Логирование и повторные попытки отправки |
| **Facade** | `api-gateway`, `NotaryWebServer` | Единый интерфейс поверх доменных сервисов |
| **Observer** | `application-service`, издатель и подписчики | Аудит и события уведомлений |
| **State** | `application-service`, `ApplicationStateMachine` | Допустимые переходы заявки |
| **Strategy** | `payment-service`, стратегии тарификации | Расчёт разных видов тарифов |

Подробности приведены в `ОТЧЕТ_ПО_ПРОЕКТУ.md`.

## Unit-тестирование

В проекте используются JUnit 5 и Mockito. Зависимости от баз данных, HTTP и платёжного
шлюза заменяются моками, поэтому для unit-тестов PostgreSQL и запущенные микросервисы
не нужны. Тесты следуют структуре Arrange–Act–Assert и содержат положительные,
пустые, граничные и исключительные сценарии.

| Микросервис | Тестовый класс | Проверяемые сценарии |
|---|---|---|
| ClientService | `ClientServiceTest` | Регистрация, вход, пустое ФИО, пароли из 5 и 6 символов, повторный e-mail, неверный пароль, отсутствующий клиент |
| PaymentService | `PaymentServiceTest` | Фиксированный и процентный тарифы, нулевой тариф, отсутствующие заявка и сумма сделки, ошибка шлюза, отсутствующий платёж |

Запуск всех тестов:

```bash
mvn clean test
```

Запуск тестов отдельного микросервиса:

```bash
mvn -pl client-service -am test
mvn -pl payment-service -am test
```

Отчёты Maven Surefire сохраняются в `target/surefire-reports` соответствующего
модуля. Команда `mvn clean package` также выполняет все тесты перед сборкой JAR.

## Проверка запущенного приложения

Проверка готовности каждого процесса: `GET http://localhost:{port}/api/health`.
