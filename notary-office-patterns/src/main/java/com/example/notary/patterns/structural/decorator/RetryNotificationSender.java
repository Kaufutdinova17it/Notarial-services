package com.example.notary.patterns.structural.decorator;

/**
 * СТРУКТУРНЫЙ ПАТТЕРН — ДЕКОРАТОР (Decorator), конкретный декоратор.
 * Добавляет повторные попытки отправки при ошибке
 * (требование из проектного решения: «при ошибке отправки выполнение
 * повторной попытки»).
 */
public class RetryNotificationSender extends NotificationSenderDecorator {

    private final int maxAttempts;

    public RetryNotificationSender(NotificationSender wrappee, int maxAttempts) {
        super(wrappee);
        this.maxAttempts = maxAttempts;
    }

    @Override
    public boolean send(String email, String subject, String message) {
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            if (wrappee.send(email, subject, message)) {
                return true;
            }
            if (attempt < maxAttempts) {
                System.out.println("      [Retry] Попытка " + attempt + " не удалась, повторная отправка...");
            }
        }
        return false;
    }
}
