package com.example.notary.patterns.structural.decorator;

import com.example.notary.model.Notification;

/**
 * Паттерн «Декоратор» (Decorator) — динамически дополняет отправку
 * уведомления логированием каждой попытки (требование проектного решения).
 */
public class LoggingNotificationSender implements NotificationSender {

    private final NotificationSender delegate;

    public LoggingNotificationSender(NotificationSender delegate) {
        this.delegate = delegate;
    }

    @Override
    public void send(Notification notification, String emailAddress) {
        System.out.printf("      [LOG] Отправка уведомления «%s» клиенту #%d...%n",
                notification.getType().getDisplayName(), notification.getClientId());
        delegate.send(notification, emailAddress);
        System.out.println("      [LOG] Уведомление успешно доставлено");
    }
}
