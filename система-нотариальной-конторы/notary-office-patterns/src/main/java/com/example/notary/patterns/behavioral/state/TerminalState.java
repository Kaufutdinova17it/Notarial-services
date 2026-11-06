package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

import java.util.Set;

/**
 * Паттерн «Состояние» — финальное состояние заявки
 * («Получена», «Отклонена», «Отменена»). Переходов нет.
 */
public class TerminalState implements ApplicationState {

    private final ApplicationStatus status;

    public TerminalState(ApplicationStatus status) {
        if (status != ApplicationStatus.RECEIVED
                && status != ApplicationStatus.REJECTED
                && status != ApplicationStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Статус «%s» не является финальным".formatted(status.getDisplayName()));
        }
        this.status = status;
    }

    @Override
    public ApplicationStatus getStatus() {
        return status;
    }

    @Override
    public Set<ApplicationStatus> getAllowedTransitions() {
        return Set.of();
    }
}
