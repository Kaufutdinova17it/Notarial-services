package com.example.notary.patterns.structural.decorator;

import com.example.notary.model.Notification;

/**
 * Конкретный компонент паттерна «Декоратор» — отправка уведомления на e-mail.
 * Поддерживает имитацию разовой ошибки доставки для демонстрации
 * декоратора повторных попыток.
 */
public class EmailNotificationSender implements NotificationSender {

    private boolean failNextAttempt;

    /** Следующая попытка отправки завершится ошибкой (имитация сбоя SMTP). */
    public void failNextAttempt() {
        this.failNextAttempt = true;
    }

    @Override
    public void send(Notification notification, String emailAddress) {
        if (failNextAttempt) {
            failNextAttempt = false;
            throw new IllegalStateException("SMTP-сервер временно недоступен");
        }
        System.out.printf("      [EMAIL] → %s: %s%n", emailAddress, notification.getMessage());
    }
}
