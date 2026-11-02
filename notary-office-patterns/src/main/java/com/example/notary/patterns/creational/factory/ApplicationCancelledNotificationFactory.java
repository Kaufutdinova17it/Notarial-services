package com.example.notary.patterns.creational.factory;

import com.example.notary.model.NotificationType;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;

/** Уведомление об отмене заявки. */
public class ApplicationCancelledNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.APPLICATION_CANCELLED;
    }

    @Override
    protected String createMessage(ApplicationEvent event) {
        return "Ваша заявка №" + shortId(event) + " отменена.";
    }
}
