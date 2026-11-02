package com.example.notary.patterns.structural.decorator;

/**
 * СТРУКТУРНЫЙ ПАТТЕРН — ДЕКОРАТОР (Decorator), конкретный декоратор.
 * Добавляет логирование каждой отправки уведомления.
 */
public class LoggingNotificationSender extends NotificationSenderDecorator {

    public LoggingNotificationSender(NotificationSender wrappee) {
        super(wrappee);
    }

    @Override
    public boolean send(String email, String subject, String message) {
        System.out.println("      [Log] Начата отправка уведомления: " + email + ", тема: «" + subject + "»");
        boolean result = wrappee.send(email, subject, message);
        System.out.println("      [Log] Результат отправки: " + (result ? "успешно" : "ошибка"));
        return result;
    }
}
