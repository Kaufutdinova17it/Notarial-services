package com.example.notary.patterns.creational.factory;

import com.example.notary.model.NotificationType;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;

/** Уведомление о готовности документов (статус «Готова»). */
public class DocumentsReadyNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.DOCUMENTS_READY;
    }

    @Override
    protected String createMessage(ApplicationEvent event) {
        return "Документы по вашей заявке №" + shortId(event)
                + " подготовлены. Вы можете скачать их в личном кабинете и получить в конторе.";
    }
}
