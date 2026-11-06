package com.example.notary.web;

import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.AuditEntry;
import com.example.notary.model.Client;
import com.example.notary.model.Document;
import com.example.notary.model.Notification;
import com.example.notary.model.Payment;
import com.example.notary.model.PricingType;
import com.example.notary.model.Service;
import com.example.notary.patterns.behavioral.strategy.PricingContext;
import com.example.notary.patterns.structural.facade.NotaryOfficeFacade;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public final class NotaryWebServer {

    private static final int MAX_BODY_SIZE = 12 * 1024 * 1024;
    private static final Path UPLOADS = Path.of("data", "uploads");
    private static final SecureRandom RANDOM = new SecureRandom();

    private final NotaryOfficeFacade facade = new NotaryOfficeFacade();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();
    private final HttpServer server;

    public NotaryWebServer(int port) throws IOException {
        Files.createDirectories(UPLOADS);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::handle);
        server.setExecutor(Executors.newFixedThreadPool(8));
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            String path = exchange.getRequestURI().getPath();
            if (path.startsWith("/api/")) {
                routeApi(exchange, path);
            } else {
                serveStatic(exchange, path);
            }
        } catch (HttpError e) {
            sendJson(exchange, e.status, Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            sendJson(exchange, 400, Map.of("error", message(e)));
        } catch (IllegalStateException e) {
            sendJson(exchange, 409, Map.of("error", message(e)));
        } catch (Exception e) {
            e.printStackTrace();
            sendJson(exchange, 500, Map.of("error", "Внутренняя ошибка сервера"));
        } finally {
            exchange.close();
        }
    }

    private void routeApi(HttpExchange exchange, String path) throws IOException {
        String method = exchange.getRequestMethod();
        List<String> parts = parts(path);

        if (parts.equals(List.of("api", "health")) && "GET".equals(method)) {
            sendJson(exchange, 200, Map.of("status", "ok"));
            return;
        }
        if (parts.equals(List.of("api", "client", "register")) && "POST".equals(method)) {
            Map<String, Object> body = body(exchange);
            Client client = facade.registerClient(text(body, "fullName"), text(body, "passportData"),
                    text(body, "phone"), text(body, "email"), text(body, "password"));
            sendJson(exchange, 201, clientView(client));
            return;
        }
        if (parts.equals(List.of("api", "client", "login")) && "POST".equals(method)) {
            Map<String, Object> body = body(exchange);
            Client client = facade.login(text(body, "email"), text(body, "password"));
            String token = newSession(new Session("CLIENT", client.getId()));
            sendJson(exchange, 200, Map.of("token", token, "role", "CLIENT", "client", clientView(client)));
            return;
        }
        if (parts.equals(List.of("api", "notary", "login")) && "POST".equals(method)) {
            Map<String, Object> body = body(exchange);
            String expectedLogin = System.getenv().getOrDefault("NOTARY_LOGIN", "notary");
            String expectedPassword = System.getenv().getOrDefault("NOTARY_PASSWORD", "notary123");
            if (!expectedLogin.equals(text(body, "login")) || !expectedPassword.equals(text(body, "password"))) {
                throw new HttpError(401, "Неверный логин или пароль нотариуса");
            }
            String token = newSession(new Session("NOTARY", 0));
            sendJson(exchange, 200, Map.of("token", token, "role", "NOTARY"));
            return;
        }

        if (parts.size() >= 2 && "services".equals(parts.get(1)) && "GET".equals(method)) {
            if (parts.size() == 2) {
                sendJson(exchange, 200, facade.getServiceCatalog().stream().map(this::serviceView).toList());
                return;
            }
            if (parts.size() == 3) {
                sendJson(exchange, 200, serviceView(facade.getService(id(parts, 2))));
                return;
            }
        }

        Session session = requireSession(exchange);
        if (parts.equals(List.of("api", "logout")) && "POST".equals(method)) {
            sessions.remove(token(exchange));
            sendJson(exchange, 200, Map.of("success", true));
            return;
        }

        if (parts.size() >= 2 && "client".equals(parts.get(1))) {
            routeClient(exchange, method, parts, session);
            return;
        }
        if (parts.size() >= 2 && "services".equals(parts.get(1))) {
            routeServices(exchange, method, parts, session);
            return;
        }
        if (parts.size() >= 2 && "applications".equals(parts.get(1))) {
            routeApplications(exchange, method, parts, session);
            return;
        }
        if (parts.size() >= 2 && "payment".equals(parts.get(1))) {
            routePayments(exchange, method, parts, session);
            return;
        }
        if (parts.size() == 3 && "api".equals(parts.get(0)) && "notifications".equals(parts.get(1))
                && "GET".equals(method)) {
            long clientId = id(parts, 2);
            requireClientAccess(session, clientId);
            sendJson(exchange, 200, facade.getClientNotifications(clientId).stream()
                    .map(this::notificationView).toList());
            return;
        }
        if (parts.equals(List.of("api", "audit")) && "GET".equals(method)) {
            requireNotary(session);
            sendJson(exchange, 200, facade.getAuditLog().stream().map(this::auditView).toList());
            return;
        }
        if (parts.equals(List.of("api", "statistics")) && "GET".equals(method)) {
            sendJson(exchange, 200, Map.of("text", facade.getStatistics()));
            return;
        }
        throw new HttpError(404, "API-маршрут не найден");
    }

    private void routeClient(HttpExchange exchange, String method, List<String> parts, Session session)
            throws IOException {
        if (parts.size() != 3) {
            throw new HttpError(404, "API-маршрут не найден");
        }
        long clientId = id(parts, 2);
        requireClientAccess(session, clientId);
        if ("GET".equals(method)) {
            sendJson(exchange, 200, clientView(facade.getClient(clientId)));
            return;
        }
        if ("PUT".equals(method)) {
            Map<String, Object> body = body(exchange);
            Client client = facade.updateClient(clientId, text(body, "fullName"), text(body, "passportData"),
                    text(body, "phone"), text(body, "email"), text(body, "password"));
            sendJson(exchange, 200, clientView(client));
            return;
        }
        throw new HttpError(405, "Метод не поддерживается");
    }

    private void routeServices(HttpExchange exchange, String method, List<String> parts, Session session)
            throws IOException {
        requireNotary(session);
        if (parts.size() == 2 && "POST".equals(method)) {
            Map<String, Object> body = body(exchange);
            Service service = facade.createService(text(body, "name"), text(body, "description"),
                    decimal(body, "price"), pricingType(body));
            sendJson(exchange, 201, serviceView(service));
            return;
        }
        if (parts.size() == 3 && "PUT".equals(method)) {
            Map<String, Object> body = body(exchange);
            Service service = facade.updateService(id(parts, 2), text(body, "name"), text(body, "description"),
                    decimal(body, "price"), pricingType(body));
            sendJson(exchange, 200, serviceView(service));
            return;
        }
        if (parts.size() == 3 && "DELETE".equals(method)) {
            facade.deleteService(id(parts, 2));
            sendJson(exchange, 200, Map.of("success", true));
            return;
        }
        throw new HttpError(405, "Метод не поддерживается");
    }

    private void routeApplications(HttpExchange exchange, String method, List<String> parts, Session session)
            throws IOException {
        if (parts.size() == 2 && "GET".equals(method)) {
            List<Application> applications = session.client
                    ? facade.getClientApplications(session.clientId) : facade.getApplications();
            sendJson(exchange, 200, applications.stream().map(this::applicationView).toList());
            return;
        }
        if (parts.size() == 2 && "POST".equals(method)) {
            requireClient(session);
            Map<String, Object> body = body(exchange);
            Application application = facade.submitApplication(session.clientId,
                    number(body, "serviceId").longValue(), text(body, "note"));
            sendJson(exchange, 201, applicationView(application));
            return;
        }
        if (parts.size() < 3) {
            throw new HttpError(404, "API-маршрут не найден");
        }
        long applicationId = id(parts, 2);
        Application application = accessibleApplication(applicationId, session);

        if (parts.size() == 3 && "GET".equals(method)) {
            Map<String, Object> result = applicationView(application);
            result.put("documents", facade.getApplicationDocuments(applicationId).stream()
                    .map(this::documentView).toList());
            result.put("payments", facade.getApplicationPayments(applicationId).stream()
                    .map(this::paymentView).toList());
            sendJson(exchange, 200, result);
            return;
        }
        if (parts.size() == 3 && "PUT".equals(method)) {
            requireOwner(session, application);
            sendJson(exchange, 200, applicationView(facade.updateApplicationNote(applicationId,
                    text(body(exchange), "note"))));
            return;
        }
        if (parts.size() == 3 && "DELETE".equals(method)) {
            sendJson(exchange, 200, applicationView(facade.cancelApplication(applicationId)));
            return;
        }
        if (parts.size() == 4 && "status".equals(parts.get(3)) && "PATCH".equals(method)) {
            requireNotary(session);
            ApplicationStatus status = ApplicationStatus.valueOf(text(body(exchange), "status"));
            sendJson(exchange, 200, applicationView(facade.changeApplicationStatus(applicationId, status)));
            return;
        }
        if (parts.size() == 4 && "receive".equals(parts.get(3)) && "POST".equals(method)) {
            requireOwner(session, application);
            sendJson(exchange, 200, applicationView(facade.receiveDocuments(applicationId)));
            return;
        }
        if (parts.size() >= 4 && "documents".equals(parts.get(3))) {
            routeDocuments(exchange, method, parts, session, application);
            return;
        }
        throw new HttpError(404, "API-маршрут не найден");
    }

    private void routeDocuments(HttpExchange exchange, String method, List<String> parts, Session session,
                                Application application) throws IOException {
        long applicationId = application.getId();
        if (parts.size() == 4 && "POST".equals(method)) {
            requireOwner(session, application);
            Map<String, Object> body = body(exchange);
            Path file = saveUploadedFile(text(body, "fileName"), text(body, "contentBase64"));
            try {
                Document document = facade.addSourceDocument(applicationId, file.toString());
                sendJson(exchange, 201, documentView(document));
            } catch (RuntimeException e) {
                Files.deleteIfExists(file);
                throw e;
            }
            return;
        }
        if (parts.size() == 5 && "generate".equals(parts.get(4)) && "POST".equals(method)) {
            requireNotary(session);
            String fileName = text(body(exchange), "fileName");
            if (fileName.isBlank()) {
                fileName = "notarial-document-" + applicationId + ".txt";
            }
            Path file = UPLOADS.resolve(UUID.randomUUID() + "-" + safeFileName(fileName));
            Client client = facade.getClient(application.getClientId());
            Service service = facade.getService(application.getServiceId());
            String content = "НОТАРИАЛЬНАЯ КОНТОРА\n\nГотовый документ по заявке №" + applicationId
                    + "\nКлиент: " + client.getFullName() + "\nУслуга: " + service.getName()
                    + "\nОписание: " + application.getNote() + "\n";
            Files.writeString(file, content, StandardCharsets.UTF_8);
            sendJson(exchange, 201, documentView(facade.issueReadyDocument(applicationId, file.toString())));
            return;
        }
        if (parts.size() == 6) {
            long documentId = id(parts, 4);
            Document document = facade.getDocument(documentId);
            if (!document.getApplicationId().equals(applicationId)) {
                throw new HttpError(404, "Документ не найден в этой заявке");
            }
            if ("download".equals(parts.get(5)) && "GET".equals(method)) {
                sendFile(exchange, document);
                return;
            }
            if ("sign".equals(parts.get(5)) && "POST".equals(method)) {
                requireOwner(session, application);
                sendJson(exchange, 200, documentView(facade.signDocument(applicationId, documentId)));
                return;
            }
            if ("DELETE".equals(method)) {
                requireOwner(session, application);
                facade.deleteSourceDocument(applicationId, documentId);
                Path file = Path.of(document.getFileUrl());
                if (file.startsWith(UPLOADS)) {
                    Files.deleteIfExists(file);
                }
                sendJson(exchange, 200, Map.of("success", true));
                return;
            }
        }
        throw new HttpError(404, "API-маршрут документа не найден");
    }

    private void routePayments(HttpExchange exchange, String method, List<String> parts, Session session)
            throws IOException {
        if (parts.size() == 3 && "POST".equals(method)) {
            long applicationId = id(parts, 2);
            Application application = accessibleApplication(applicationId, session);
            requireOwner(session, application);
            Map<String, Object> body = body(exchange);
            BigDecimal dealAmount = decimal(body, "dealAmount");
            PricingContext context = dealAmount == null ? PricingContext.EMPTY : PricingContext.ofDealAmount(dealAmount);
            sendJson(exchange, 201, paymentView(facade.payForApplication(applicationId, context)));
            return;
        }
        if (parts.size() == 4 && "status".equals(parts.get(3)) && "GET".equals(method)) {
            Payment payment = facade.getPayment(id(parts, 2));
            accessibleApplication(payment.getApplicationId(), session);
            sendJson(exchange, 200, paymentView(payment));
            return;
        }
        throw new HttpError(404, "API-маршрут платежа не найден");
    }

    private Path saveUploadedFile(String fileName, String encoded) throws IOException {
        if (fileName == null || fileName.isBlank() || encoded == null || encoded.isBlank()) {
            throw new IllegalArgumentException("Выберите файл для загрузки");
        }
        byte[] content;
        try {
            content = Base64.getDecoder().decode(encoded);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректное содержимое файла");
        }
        if (content.length > 8 * 1024 * 1024) {
            throw new IllegalArgumentException("Размер файла не должен превышать 8 МБ");
        }
        Path file = UPLOADS.resolve(UUID.randomUUID() + "-" + safeFileName(fileName));
        Files.write(file, content);
        return file;
    }

    private String safeFileName(String fileName) {
        String name = Path.of(fileName).getFileName().toString();
        String safe = name.replaceAll("[^\\p{L}\\p{N}._-]", "_");
        if (safe.isBlank()) {
            throw new IllegalArgumentException("Некорректное имя файла");
        }
        return safe;
    }

    private Application accessibleApplication(long id, Session session) {
        Application application = facade.getApplication(id);
        if (session.client && application.getClientId() != session.clientId) {
            throw new HttpError(403, "Нет доступа к чужой заявке");
        }
        return application;
    }

    private void requireClientAccess(Session session, long clientId) {
        if (session.client && session.clientId != clientId) {
            throw new HttpError(403, "Нет доступа к профилю другого клиента");
        }
    }

    private void requireOwner(Session session, Application application) {
        requireClient(session);
        if (application.getClientId() != session.clientId) {
            throw new HttpError(403, "Операция доступна только владельцу заявки");
        }
    }

    private void requireClient(Session session) {
        if (!session.client) {
            throw new HttpError(403, "Операция доступна только клиенту");
        }
    }

    private void requireNotary(Session session) {
        if (session.client) {
            throw new HttpError(403, "Операция доступна только нотариусу");
        }
    }

    private Session requireSession(HttpExchange exchange) {
        Session session = sessions.get(token(exchange));
        if (session == null) {
            throw new HttpError(401, "Требуется вход в систему");
        }
        return session;
    }

    private String token(HttpExchange exchange) {
        String authorization = exchange.getRequestHeaders().getFirst("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return "";
        }
        return authorization.substring(7);
    }

    private String newSession(Session session) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sessions.put(token, session);
        return token;
    }

    private Map<String, Object> clientView(Client client) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", client.getId());
        result.put("fullName", client.getFullName());
        result.put("passportData", client.getPassportData());
        result.put("phone", client.getPhone());
        result.put("email", client.getEmail());
        return result;
    }

    private Map<String, Object> serviceView(Service service) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", service.getId());
        result.put("name", service.getName());
        result.put("description", service.getDescription());
        result.put("price", service.getPrice());
        result.put("pricingType", service.getPricingType().name());
        result.put("pricingTypeDisplay", service.getPricingType().getDisplayName());
        return result;
    }

    private Map<String, Object> applicationView(Application application) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", application.getId());
        result.put("clientId", application.getClientId());
        result.put("clientName", facade.getClient(application.getClientId()).getFullName());
        result.put("serviceId", application.getServiceId());
        result.put("serviceName", facade.getService(application.getServiceId()).getName());
        result.put("status", application.getStatus().name());
        result.put("statusDisplay", application.getStatus().getDisplayName());
        result.put("note", application.getNote());
        result.put("createdAt", application.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }

    private Map<String, Object> documentView(Document document) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", document.getId());
        result.put("applicationId", document.getApplicationId());
        result.put("fileName", displayFileName(document.getFileUrl()));
        result.put("type", document.getType().name());
        result.put("typeDisplay", document.getType().getDisplayName());
        result.put("signed", document.isSigned());
        return result;
    }

    private Map<String, Object> paymentView(Payment payment) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", payment.getId());
        result.put("applicationId", payment.getApplicationId());
        result.put("amount", payment.getAmount());
        result.put("status", payment.getStatus().name());
        result.put("statusDisplay", payment.getStatus().getDisplayName());
        result.put("createdAt", payment.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }

    private Map<String, Object> notificationView(Notification notification) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", notification.getId());
        result.put("applicationId", notification.getApplicationId());
        result.put("type", notification.getType().getDisplayName());
        result.put("message", notification.getMessage());
        result.put("status", notification.getStatus().getDisplayName());
        result.put("createdAt", notification.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }

    private Map<String, Object> auditView(AuditEntry entry) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", entry.getId());
        result.put("eventType", entry.getEventType());
        result.put("description", entry.getDescription());
        result.put("createdAt", entry.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return result;
    }

    private String displayFileName(String fileUrl) {
        String name = Path.of(fileUrl).getFileName().toString();
        return name.length() > 37 && name.charAt(36) == '-' ? name.substring(37) : name;
    }

    private Map<String, Object> body(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readNBytes(MAX_BODY_SIZE + 1);
        if (bytes.length > MAX_BODY_SIZE) {
            throw new HttpError(413, "Слишком большой запрос");
        }
        return Json.parseObject(new String(bytes, StandardCharsets.UTF_8));
    }

    private String text(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? "" : value.toString();
    }

    private BigDecimal number(Map<String, Object> body, String key) {
        BigDecimal value = decimal(body, key);
        if (value == null) {
            throw new IllegalArgumentException("Не указано поле " + key);
        }
        return value;
    }

    private BigDecimal decimal(Map<String, Object> body, String key) {
        String value = text(body, key);
        return value.isBlank() ? null : new BigDecimal(value);
    }

    private PricingType pricingType(Map<String, Object> body) {
        try {
            return PricingType.valueOf(text(body, "pricingType"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Неизвестный тип тарификации");
        }
    }

    private long id(List<String> parts, int index) {
        try {
            return Long.parseLong(parts.get(index));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный числовой идентификатор");
        }
    }

    private List<String> parts(String path) {
        List<String> result = new ArrayList<>();
        for (String part : path.split("/")) {
            if (!part.isBlank()) {
                result.add(URLDecoder.decode(part, StandardCharsets.UTF_8));
            }
        }
        return result;
    }

    private void sendFile(HttpExchange exchange, Document document) throws IOException {
        Path file = Path.of(document.getFileUrl());
        if (!Files.isRegularFile(file)) {
            throw new HttpError(404, "Файл документа не найден на диске");
        }
        byte[] bytes = Files.readAllBytes(file);
        String fileName = displayFileName(document.getFileUrl());
        exchange.getResponseHeaders().set("Content-Type", contentType(fileName));
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename*=UTF-8''"
                + URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20"));
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private void serveStatic(HttpExchange exchange, String path) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            throw new HttpError(405, "Метод не поддерживается");
        }
        String resourcePath = path.equals("/") ? "/index.html" : path;
        if (resourcePath.contains("..")) {
            throw new HttpError(400, "Некорректный путь");
        }
        try (InputStream resource = NotaryWebServer.class.getResourceAsStream("/web" + resourcePath)) {
            if (resource == null) {
                throw new HttpError(404, "Страница не найдена");
            }
            byte[] bytes = resource.readAllBytes();
            exchange.getResponseHeaders().set("Content-Type", contentType(resourcePath));
            exchange.getResponseHeaders().set("Cache-Control", "no-cache");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream output = exchange.getResponseBody()) {
                output.write(bytes);
            }
        }
    }

    private String contentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css")) return "text/css; charset=UTF-8";
        if (path.endsWith(".js")) return "application/javascript; charset=UTF-8";
        if (path.endsWith(".svg")) return "image/svg+xml";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".pdf")) return "application/pdf";
        if (path.endsWith(".json")) return "application/json; charset=UTF-8";
        return "application/octet-stream";
    }

    private void sendJson(HttpExchange exchange, int status, Object value) throws IOException {
        byte[] bytes = Json.stringify(value).getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private String message(Exception e) {
        return e.getMessage() == null ? "Операция не выполнена" : e.getMessage();
    }

    private static final class Session {
        private final String role;
        private final long clientId;
        private final boolean client;

        private Session(String role, long clientId) {
            this.role = role;
            this.clientId = clientId;
            this.client = "CLIENT".equals(role);
        }
    }

    private static final class HttpError extends RuntimeException {
        private final int status;

        private HttpError(int status, String message) {
            super(message);
            this.status = status;
        }
    }
}
