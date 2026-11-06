package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

/**
 * Паттерн «Состояние» (State) — машина состояний заявки.
 * Хранит текущее состояние и делегирует ему проверку переходов.
 */
public class ApplicationStateMachine {

    private ApplicationState currentState;

    public ApplicationStateMachine(ApplicationStatus initialStatus) {
        this.currentState = stateOf(initialStatus);
    }

    public ApplicationStatus getCurrentStatus() {
        return currentState.getStatus();
    }

    public boolean isTerminal() {
        return currentState instanceof TerminalState;
    }

    /**
     * Переводит заявку в целевой статус.
     *
     * @throws IllegalStateException если переход недопустим из текущего состояния
     */
    public ApplicationStatus transitionTo(ApplicationStatus target) {
        currentState.ensureTransitionAllowed(target);
        currentState = stateOf(target);
        return target;
    }

    /** Фабрика состояний по статусу заявки. */
    public static ApplicationState stateOf(ApplicationStatus status) {
        return switch (status) {
            case CREATED -> new CreatedState();
            case IN_PROGRESS -> new InProgressState();
            case READY -> new ReadyState();
            case RECEIVED, REJECTED, CANCELLED -> new TerminalState(status);
        };
    }
}
