package com.example.notary.patterns.creational.factory;

import com.example.notary.model.NotificationType;
import com.example.notary.patterns.behavioral.observer.ApplicationEvent;

/** Уведомление об отклонении заявки. */
public class ApplicationRejectedNotificationFactory extends NotificationFactory {

    @Override
    protected NotificationType getType() {
        return NotificationType.APPLICATION_REJECTED;
    }

    @Override
    protected String createMessage(ApplicationEvent event) {
        return "К сожалению, ваша заявка №" + shortId(event)
                + " отклонена. Для уточнения причины обратитесь в нотариальную контору.";
    }
}
