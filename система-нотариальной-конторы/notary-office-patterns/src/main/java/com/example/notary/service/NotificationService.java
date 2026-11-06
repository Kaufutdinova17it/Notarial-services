package com.example.notary.service;

import com.example.notary.model.Application;
import com.example.notary.model.Client;
import com.example.notary.model.Notification;
import com.example.notary.model.NotificationStatus;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;
import com.example.notary.patterns.behavioral.observer.ApplicationListener;
import com.example.notary.patterns.creational.factory.NotificationFactory;
import com.example.notary.patterns.structural.decorator.NotificationSender;
import com.example.notary.repository.ApplicationRepository;
import com.example.notary.repository.ClientRepository;
import com.example.notary.repository.NotificationRepository;

/**
 * Сервис уведомлений — подписчик событий заявок (паттерн «Наблюдатель»).
 * Текст уведомления создаёт конкретная фабрика (паттерн «Фабричный метод»),
 * отправку выполняет декорированный отправитель (паттерн «Декоратор»).
 */
public class NotificationService implements ApplicationListener {

    private final NotificationRepository notificationRepository;
    private final ClientRepository clientRepository;
    private final ApplicationRepository applicationRepository;
    private final NotificationSender sender;

    public NotificationService(NotificationRepository notificationRepository,
                               ClientRepository clientRepository,
                               ApplicationRepository applicationRepository,
                               NotificationSender sender) {
        this.notificationRepository = notificationRepository;
        this.clientRepository = clientRepository;
        this.applicationRepository = applicationRepository;
        this.sender = sender;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        Client client = clientRepository.findById(event.getClientId())
                .orElseThrow(() -> new IllegalArgumentException("Клиент #" + event.getClientId() + " не найден"));
        Application application = applicationRepository.findById(event.getApplicationId())
                .orElseThrow(() -> new IllegalArgumentException("Заявка #" + event.getApplicationId() + " не найдена"));

        Notification notification = NotificationFactory.forEvent(event.getType())
                .createNotification(application, client);
        try {
            sender.send(notification, client.getEmail());
            notification.setStatus(NotificationStatus.SENT);
        } catch (RuntimeException e) {
            notification.setStatus(NotificationStatus.FAILED);
        }
        notificationRepository.save(notification);
    }
}
