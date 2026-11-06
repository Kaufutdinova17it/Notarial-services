package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

import java.util.Set;

/**
 * Паттерн «Состояние» — заявка только что создана.
 * Допустимые переходы: в работу или отмена клиентом.
 */
public class CreatedState implements ApplicationState {

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.CREATED;
    }

    @Override
    public Set<ApplicationStatus> getAllowedTransitions() {
        return Set.of(ApplicationStatus.IN_PROGRESS, ApplicationStatus.CANCELLED);
    }
}
