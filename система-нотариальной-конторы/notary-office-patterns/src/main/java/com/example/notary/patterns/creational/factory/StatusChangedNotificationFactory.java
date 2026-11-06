package com.example.notary.patterns.creational.factory;

import com.example.notary.model.Application;
import com.example.notary.model.Client;
import com.example.notary.model.NotificationType;

/**
 * Конкретная фабрика: уведомление о смене статуса заявки.
 */
public class StatusChangedNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.STATUS_CHANGED;
    }

    @Override
    protected String buildMessage(Application application, Client client) {
        return "Уважаемый(ая) %s, статус вашей заявки №%d изменён на «%s»."
                .formatted(client.getFullName(), application.getId(),
                        application.getStatus().getDisplayName());
    }
}
