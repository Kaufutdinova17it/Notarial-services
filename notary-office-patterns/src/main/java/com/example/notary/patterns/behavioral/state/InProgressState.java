package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

/** Состояние «В процессе»: можно подготовить документы, отклонить или отменить. */
public class InProgressState extends ApplicationState {

    public InProgressState() {
        super(ApplicationStatus.IN_PROGRESS);
    }

    @Override
    public ApplicationStatus markReady() {
        return ApplicationStatus.READY;
    }

    @Override
    public ApplicationStatus reject() {
        return ApplicationStatus.REJECTED;
    }

    @Override
    public ApplicationStatus cancel() {
        return ApplicationStatus.CANCELLED;
    }
}
