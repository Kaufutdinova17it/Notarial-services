package com.example.notary.patterns.structural.decorator;

import com.example.notary.model.Notification;

/**
 * Базовый интерфейс отправителя уведомлений — компонент паттерна «Декоратор».
 */
public interface NotificationSender {

    void send(Notification notification, String emailAddress);
}
