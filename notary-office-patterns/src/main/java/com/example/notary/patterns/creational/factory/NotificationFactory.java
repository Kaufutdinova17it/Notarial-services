package com.example.notary.patterns.creational.factory;

import com.example.notary.model.Notification;
import com.example.notary.model.NotificationStatus;
import com.example.notary.model.NotificationType;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * ПОРОЖДАЮЩИЙ ПАТТЕРН — ФАБРИЧНЫЙ МЕТОД (Factory Method).
 *
 * Задача из предметной области: по каждому событию заявки (создание,
 * смена статуса, готовность документов, отклонение, отмена) нужно
 * сформировать уведомление со своим текстом. Базовый класс задаёт общий
 * алгоритм создания уведомления, а конкретные фабрики переопределяют
 * фабричный метод createMessage().
 */
public abstract class NotificationFactory {

    /** Шаблонный метод: общий алгоритм создания уведомления. */
    public final Notification create(ApplicationEvent event) {
        Notification notification = new Notification();
        notification.setClientId(event.getApplication().getClientId());
        notification.setApplicationId(event.getApplication().getId());
        notification.setType(getType());
        notification.setMessage(createMessage(event));
        notification.setStatus(NotificationStatus.CREATED);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    protected abstract NotificationType getType();

    /** Фабричный метод — текст уведомления определяет конкретная фабрика. */
    protected abstract String createMessage(ApplicationEvent event);

    protected static String shortId(ApplicationEvent event) {
        return event.getApplication().shortId();
    }
}
