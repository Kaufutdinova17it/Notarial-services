package com.example.notary.patterns.behavioral.observer;

import com.example.notary.repository.AuditLogRepository;

/**
 * Паттерн «Наблюдатель» (Observer) — подписчик, протоколирующий
 * каждое событие заявки в журнале аудита (требование проектного решения).
 */
public class AuditLogListener implements ApplicationListener {

    private final AuditLogRepository auditLogRepository;

    public AuditLogListener(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        auditLogRepository.save(
                event.getType().name(),
                "Заявка #%d (клиент #%d): %s".formatted(
                        event.getApplicationId(), event.getClientId(), event.getDetails()));
    }
}
