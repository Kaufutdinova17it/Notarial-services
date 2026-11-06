package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

import java.util.Set;

/**
 * Паттерн «Состояние» — документы готовы к выдаче.
 * Единственный допустимый переход: получена клиентом.
 */
public class ReadyState implements ApplicationState {

    @Override
    public ApplicationStatus getStatus() {
        return ApplicationStatus.READY;
    }

    @Override
    public Set<ApplicationStatus> getAllowedTransitions() {
        return Set.of(ApplicationStatus.RECEIVED);
    }
}
