package com.example.notary.patterns.creational.factory;

import com.example.notary.model.Application;
import com.example.notary.model.Client;
import com.example.notary.model.NotificationType;

/**
 * Конкретная фабрика: уведомление о готовности документов.
 */
public class DocumentsReadyNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.DOCUMENTS_READY;
    }

    @Override
    protected String buildMessage(Application application, Client client) {
        return "Уважаемый(ая) %s, документы по вашей заявке №%d готовы к выдаче."
                .formatted(client.getFullName(), application.getId());
    }
}
