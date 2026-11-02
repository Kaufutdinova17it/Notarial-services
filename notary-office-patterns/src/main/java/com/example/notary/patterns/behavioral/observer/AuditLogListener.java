package com.example.notary.patterns.behavioral.observer;

import com.example.notary.repository.AuditLogRepository;

/**
 * Наблюдатель: протоколирование действий в журнале аудита
 * (требование из проектного решения — протоколируются создание
 * и изменение статуса заявок).
 */
public class AuditLogListener implements ApplicationListener {

    private final AuditLogRepository auditLogRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        String action = event.isCreation() ? "Создание заявки" : "Изменение статуса заявки";
        String details = event.isCreation()
                ? "Заявка №" + event.getApplication().shortId() + " создана, клиент: "
                    + event.getApplication().getClientId()
                : "Заявка №" + event.getApplication().shortId() + ": «"
                    + event.getOldStatus().getTitle() + "» -> «" + event.getNewStatus().getTitle() + "»";
        auditLogRepository.add(event.getActor(), action, details);
    }
}
