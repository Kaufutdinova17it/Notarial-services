package com.example.notary.patterns.structural.decorator;

/** Базовый декоратор отправителя уведомлений. */
public abstract class NotificationSenderDecorator implements NotificationSender {

    protected final NotificationSender wrappee;

    protected NotificationSenderDecorator(NotificationSender wrappee) {
        this.wrappee = wrappee;
    }
}
