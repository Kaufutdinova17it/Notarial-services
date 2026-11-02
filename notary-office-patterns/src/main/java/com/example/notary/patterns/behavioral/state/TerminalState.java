package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

/**
 * Конечные состояния («Получена», «Отклонена», «Отменена»):
 * ни одна операция недоступна, все переходы запрещены.
 */
public class TerminalState extends ApplicationState {

    public TerminalState(ApplicationStatus status) {
        super(status);
        if (status != ApplicationStatus.RECEIVED
                && status != ApplicationStatus.REJECTED
                && status != ApplicationStatus.CANCELLED) {
            throw new IllegalArgumentException("TerminalState допустим только для конечных статусов");
        }
    }
}
