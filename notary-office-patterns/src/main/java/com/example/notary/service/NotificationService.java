package com.example.notary.service;

import com.example.notary.model.ApplicationStatus;
import com.example.notary.model.Client;
import com.example.notary.model.Notification;
import com.example.notary.model.NotificationStatus;
import com.example.notary.model.NotificationType;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;
import com.example.notary.patterns.behavioral.observer.ApplicationListener;
import com.example.notary.patterns.creational.factory.ApplicationCancelledNotificationFactory;
import com.example.notary.patterns.creational.factory.ApplicationCreatedNotificationFactory;
import com.example.notary.patterns.creational.factory.ApplicationRejectedNotificationFactory;
import com.example.notary.patterns.creational.factory.DocumentsReadyNotificationFactory;
import com.example.notary.patterns.creational.factory.NotificationFactory;
import com.example.notary.patterns.creational.factory.StatusChangedNotificationFactory;
import com.example.notary.patterns.structural.decorator.NotificationSender;
import com.example.notary.repository.ClientRepository;
import com.example.notary.repository.NotificationRepository;

import java.util.Map;

/**
 * Сервис уведомлений — подписчик событий по заявкам.
 * Текст уведомления создаётся фабрикой (паттерн «Фабричный метод»),
 * отправка — цепочкой декораторов (паттерн «Декоратор»).
 */
public class NotificationService implements ApplicationListener {

    private final NotificationRepository notificationRepository;
    private final ClientRepository clientRepository;
    private final NotificationSender sender;

    private final Map<NotificationType, NotificationFactory> factories = Map.of(
            NotificationType.APPLICATION_CREATED, new ApplicationCreatedNotificationFactory(),
            NotificationType.STATUS_CHANGED, new StatusChangedNotificationFactory(),
            NotificationType.DOCUMENTS_READY, new DocumentsReadyNotificationFactory(),
            NotificationType.APPLICATION_REJECTED, new ApplicationRejectedNotificationFactory(),
            NotificationType.APPLICATION_CANCELLED, new ApplicationCancelledNotificationFactory()
    );

    public NotificationService(NotificationRepository notificationRepository,
                               ClientRepository clientRepository,
                               NotificationSender sender) {
        this.notificationRepository = notificationRepository;
        this.clientRepository = clientRepository;
        this.sender = sender;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        NotificationType type = resolveType(event.getNewStatus());
        Notification notification = factories.get(type).create(event);
        notificationRepository.save(notification);

        String email = clientRepository.findById(notification.getClientId())
                .map(Client::getEmail)
                .orElse(null);
        boolean sent = email != null
                && sender.send(email, "Уведомление нотариальной конторы", notification.getMessage());

        notification.setStatus(sent ? NotificationStatus.SENT : NotificationStatus.ERROR);
        notificationRepository.save(notification);
    }

    /** Выбор типа уведомления по новому статусу заявки (правила из архитектурного плана). */
    private NotificationType resolveType(ApplicationStatus status) {
        return switch (status) {
            case CREATED -> NotificationType.APPLICATION_CREATED;
            case READY -> NotificationType.DOCUMENTS_READY;
            case REJECTED -> NotificationType.APPLICATION_REJECTED;
            case CANCELLED -> NotificationType.APPLICATION_CANCELLED;
            default -> NotificationType.STATUS_CHANGED;
        };
    }

    public Notification getById(java.util.UUID id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Уведомление не найдено: " + id));
    }
}
