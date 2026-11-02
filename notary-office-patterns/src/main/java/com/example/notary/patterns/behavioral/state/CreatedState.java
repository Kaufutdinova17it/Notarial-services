package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

/** Состояние «Создана»: можно начать обработку, отклонить или отменить. */
public class CreatedState extends ApplicationState {

    public CreatedState() {
        super(ApplicationStatus.CREATED);
    }

    @Override
    public ApplicationStatus startProcessing() {
        return ApplicationStatus.IN_PROGRESS;
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
