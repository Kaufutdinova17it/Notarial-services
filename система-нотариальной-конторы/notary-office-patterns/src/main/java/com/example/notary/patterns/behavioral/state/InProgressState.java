package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

import java.util.Set;

/**
 * Паттерн «Состояние» — заявка в работе у нотариуса.
 * Допустимые переходы: готова, отклонена или отменена.
 */
public class InProgressState implements ApplicationState {

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.IN_PROGRESS;
    }

    @Override
    public Set<ApplicationStatus> getAllowedTransitions() {
        return Set.of(ApplicationStatus.READY, ApplicationStatus.REJECTED, ApplicationStatus.CANCELLED);
    }
}
