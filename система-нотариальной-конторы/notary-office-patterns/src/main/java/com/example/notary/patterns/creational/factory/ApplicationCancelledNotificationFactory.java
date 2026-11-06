package com.example.notary.patterns.creational.factory;

import com.example.notary.model.Application;
import com.example.notary.model.Client;
import com.example.notary.model.NotificationType;

/**
 * Конкретная фабрика: уведомление об отмене заявки.
 */
public class ApplicationCancelledNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.APPLICATION_CANCELLED;
    }

    @Override
    protected String buildMessage(Application application, Client client) {
        return "Уважаемый(ая) %s, ваша заявка №%d отменена."
                .formatted(client.getFullName(), application.getId());
    }
}
