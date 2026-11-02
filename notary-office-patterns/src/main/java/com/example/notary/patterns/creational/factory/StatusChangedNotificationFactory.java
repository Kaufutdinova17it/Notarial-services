package com.example.notary.patterns.creational.factory;

import com.example.notary.model.NotificationType;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;

/** Уведомление об изменении статуса заявки. */
public class StatusChangedNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.STATUS_CHANGED;
    }

    @Override
    protected String createMessage(ApplicationEvent event) {
        String oldTitle = event.getOldStatus() != null ? event.getOldStatus().getTitle() : "—";
        return "Статус вашей заявки №" + shortId(event) + " изменён: «" + oldTitle
                + "» -> «" + event.getNewStatus().getTitle() + "».";
    }
}
