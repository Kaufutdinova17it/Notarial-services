package com.example.notary.patterns.creational.factory;

import com.example.notary.model.Application;
import com.example.notary.model.Client;
import com.example.notary.model.Notification;
import com.example.notary.model.NotificationStatus;
import com.example.notary.model.NotificationType;
import com.example.notary.patterns.behavioral.observer.ApplicationEventType;

import java.time.LocalDateTime;

/**
 * Паттерн «Фабричный метод» (Factory Method) — базовый класс фабрики
 * уведомлений. Задаёт общий алгоритм сборки уведомления (шаблонный метод),
 * а текст сообщения определяют конкретные фабрики.
 */
public abstract class NotificationFactory {

    /**
     * Общий алгоритм создания уведомления: заполняет служебные поля,
     * а тип и текст делегирует конкретной фабрике.
     */
    public Notification createNotification(Application application, Client client) {
        Notification notification = new Notification();
        notification.setClientId(client.getId());
        notification.setApplicationId(application.getId());
        notification.setType(getType());
        notification.setMessage(buildMessage(application, client));
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    /** Тип создаваемого уведомления. */
    protected abstract NotificationType getType();

    /** Фабричный метод: текст сообщения определяет конкретная фабрика. */
    protected abstract String buildMessage(Application application, Client client);

    /** Выбор конкретной фабрики по типу события заявки. */
    public static NotificationFactory forEvent(ApplicationEventType eventType) {
        return switch (eventType) {
            case APPLICATION_CREATED -> new ApplicationCreatedNotificationFactory();
            case STATUS_CHANGED -> new StatusChangedNotificationFactory();
            case DOCUMENTS_READY -> new DocumentsReadyNotificationFactory();
            case APPLICATION_REJECTED -> new ApplicationRejectedNotificationFactory();
            case APPLICATION_CANCELLED -> new ApplicationCancelledNotificationFactory();
        };
    }
}
