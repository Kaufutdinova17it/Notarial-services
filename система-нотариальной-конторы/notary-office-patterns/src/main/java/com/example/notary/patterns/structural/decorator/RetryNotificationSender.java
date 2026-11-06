package com.example.notary.patterns.structural.decorator;

import com.example.notary.model.Notification;

/**
 * Паттерн «Декоратор» (Decorator) — динамически дополняет отправку
 * уведомления повторными попытками при ошибке (требование проектного решения).
 */
public class RetryNotificationSender implements NotificationSender {

    private final NotificationSender delegate;
    private final int maxAttempts;

    public RetryNotificationSender(NotificationSender delegate, int maxAttempts) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("Число попыток должно быть не меньше 1");
        }
        this.delegate = delegate;
        this.maxAttempts = maxAttempts;
    }

    @Override
    public void send(Notification notification, String emailAddress) {
        for (int attempt = 1; ; attempt++) {
            try {
                delegate.send(notification, emailAddress);
                return;
            } catch (RuntimeException e) {
                if (attempt >= maxAttempts) {
                    throw e;
                }
                System.out.printf("      [RETRY] Попытка %d не удалась (%s). Повторная отправка...%n",
                        attempt, e.getMessage());
            }
        }
    }
}
