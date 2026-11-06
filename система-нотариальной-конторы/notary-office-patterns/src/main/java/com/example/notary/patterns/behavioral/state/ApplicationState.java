package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

import java.util.Set;

/**
 * Паттерн «Состояние» (State) — состояние жизненного цикла заявки.
 * Каждое состояние само определяет допустимые переходы.
 */
public interface ApplicationState {

    /** Статус, соответствующий состоянию. */
    ApplicationStatus getStatus();

    /** Множество статусов, в которые разрешён переход из текущего состояния. */
    Set<ApplicationStatus> getAllowedTransitions();

    /** Проверяет переход; недопустимый переход вызывает исключение. */
    default void ensureTransitionAllowed(ApplicationStatus target) {
        if (!getAllowedTransitions().contains(target)) {
            throw new IllegalStateException(
                    "Недопустимый переход статуса заявки: «%s» → «%s»".formatted(
                            getStatus().getDisplayName(), target.getDisplayName()));
        }
    }
}
