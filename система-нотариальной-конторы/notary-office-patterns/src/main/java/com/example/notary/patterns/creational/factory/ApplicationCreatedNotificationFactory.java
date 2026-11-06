package com.example.notary.patterns.creational.factory;

import com.example.notary.model.Application;
import com.example.notary.model.Client;
import com.example.notary.model.NotificationType;

/**
 * Конкретная фабрика: уведомление о создании заявки.
 */
public class ApplicationCreatedNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.APPLICATION_CREATED;
    }

    @Override
    protected String buildMessage(Application application, Client client) {
        return "Уважаемый(ая) %s, ваша заявка №%d успешно создана и принята в работу."
                .formatted(client.getFullName(), application.getId());
    }
}
