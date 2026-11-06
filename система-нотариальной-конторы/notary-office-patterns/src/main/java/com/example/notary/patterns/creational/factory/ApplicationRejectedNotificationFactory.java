package com.example.notary.patterns.creational.factory;

import com.example.notary.model.Application;
import com.example.notary.model.Client;
import com.example.notary.model.NotificationType;

/**
 * Конкретная фабрика: уведомление об отклонении заявки.
 */
public class ApplicationRejectedNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.APPLICATION_REJECTED;
    }

    @Override
    protected String buildMessage(Application application, Client client) {
        return "Уважаемый(ая) %s, к сожалению, ваша заявка №%d отклонена. "
                .formatted(client.getFullName(), application.getId())
                + "Подробности уточняйте у нотариуса.";
    }
}
