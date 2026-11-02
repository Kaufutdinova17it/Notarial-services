package com.example.notary.patterns.structural.decorator;

/** Компонент: отправитель уведомлений. */
public interface NotificationSender {

    /**
     * Отправляет уведомление.
     *
     * @return true, если отправка прошла успешно
     */
    boolean send(String email, String subject, String message);
}
