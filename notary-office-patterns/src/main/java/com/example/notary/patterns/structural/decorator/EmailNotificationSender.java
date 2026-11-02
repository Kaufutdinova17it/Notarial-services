package com.example.notary.patterns.structural.decorator;

/**
 * Конкретный компонент: отправка уведомления на электронную почту.
 * Первую отправку имитирует временную ошибку почтового сервиса —
 * для демонстрации работы декоратора повторных попыток.
 */
public class EmailNotificationSender implements NotificationSender {

    private boolean temporaryFailureSimulated = false;

    @Override
    public boolean send(String email, String subject, String message) {
        if (!temporaryFailureSimulated) {
            temporaryFailureSimulated = true;
            System.out.println("      [Email] Временная ошибка почтового сервиса при отправке на " + email);
            return false;
        }
        System.out.println("      [Email] Письмо отправлено на " + email + ", тема: «" + subject + "»");
        return true;
    }
}
