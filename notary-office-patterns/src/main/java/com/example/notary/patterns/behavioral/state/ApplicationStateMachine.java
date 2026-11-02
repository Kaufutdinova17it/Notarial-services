package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

import java.util.Map;

/**
 * Контекст паттерна «Состояние»: хранит текущее состояние заявки
 * и делегирует ему проверку перехода в целевой статус.
 */
public class ApplicationStateMachine {

    private static final Map<ApplicationStatus, ApplicationState> STATES = Map.of(
            ApplicationStatus.CREATED, new CreatedState(),
            ApplicationStatus.IN_PROGRESS, new InProgressState(),
            ApplicationStatus.READY, new ReadyState(),
            ApplicationStatus.RECEIVED, new TerminalState(ApplicationStatus.RECEIVED),
            ApplicationStatus.REJECTED, new TerminalState(ApplicationStatus.REJECTED),
            ApplicationStatus.CANCELLED, new TerminalState(ApplicationStatus.CANCELLED)
    );

    /**
     * Проверяет и выполняет переход из статуса from в статус to.
     *
     * @return целевой статус, если переход допустим
     * @throws IllegalStateException если переход недопустим для текущего состояния
     */
    public ApplicationStatus transition(ApplicationStatus from, ApplicationStatus to) {
        ApplicationState state = STATES.get(from);
        return switch (to) {
            case IN_PROGRESS -> state.startProcessing();
            case READY -> state.markReady();
            case RECEIVED -> state.confirmReceived();
            case REJECTED -> state.reject();
            case CANCELLED -> state.cancel();
            case CREATED -> throw new IllegalStateException(
                    "Возврат заявки в статус «Создана» невозможен");
        };
    }
}
