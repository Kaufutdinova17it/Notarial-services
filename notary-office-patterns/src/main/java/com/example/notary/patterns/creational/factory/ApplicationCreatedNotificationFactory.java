package com.example.notary.patterns.creational.factory;

import com.example.notary.model.NotificationType;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;

/** Уведомление о создании заявки. */
public class ApplicationCreatedNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.APPLICATION_CREATED;
    }

    @Override
    protected String createMessage(ApplicationEvent event) {
        return "Ваша заявка №" + shortId(event)
                + " успешно создана и принята в обработку нотариальной конторой.";
    }
}
