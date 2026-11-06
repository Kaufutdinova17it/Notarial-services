package com.example.notary.patterns.behavioral.observer;

/**
 * Паттерн «Наблюдатель» (Observer) — интерфейс подписчика на события заявок.
 * Реализации: {@code NotificationService} (отправка уведомлений)
 * и {@link AuditLogListener} (журнал аудита).
 */
public interface ApplicationListener {

    void onApplicationEvent(ApplicationEvent event);
}
