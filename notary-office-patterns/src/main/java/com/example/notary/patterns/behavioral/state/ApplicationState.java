package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

/**
 * ПОВЕДЕНЧЕСКИЙ ПАТТЕРН — СОСТОЯНИЕ (State), базовое состояние заявки.
 *
 * Задача из предметной области: у заявки есть жизненный цикл
 * (Создана -> В процессе -> Готова -> Получена, а также Отклонена и
 * Отменена), и набор допустимых операций зависит от текущего статуса.
 * Каждое состояние само решает, какие переходы из него возможны;
 * недопустимый переход приводит к исключению.
 */
public abstract class ApplicationState {

    private final ApplicationStatus status;

    protected ApplicationState(ApplicationStatus status) {
        this.status = status;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    /** Нотариус начинает обработку заявки. */
    public ApplicationStatus startProcessing() {
        throw transitionDenied("начало обработки");
    }

    /** Нотариус завершает подготовку документов. */
    public ApplicationStatus markReady() {
        throw transitionDenied("подготовка документов");
    }

    /** Клиент подтверждает получение готовых документов. */
    public ApplicationStatus confirmReceived() {
        throw transitionDenied("подтверждение получения");
    }

    /** Нотариус отклоняет заявку. */
    public ApplicationStatus reject() {
        throw transitionDenied("отклонение");
    }

    /** Клиент или нотариус отменяет заявку. */
    public ApplicationStatus cancel() {
        throw transitionDenied("отмена");
    }

    protected IllegalStateException transitionDenied(String operation) {
        return new IllegalStateException("Недопустимый переход: заявка в статусе «"
                + status.getTitle() + "», операция «" + operation + "» невозможна");
    }
}
