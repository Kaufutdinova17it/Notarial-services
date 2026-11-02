package com.example.notary.patterns.behavioral.state;

import com.example.notary.model.ApplicationStatus;

/** Состояние «Готова»: клиент может подтвердить получение документов. */
public class ReadyState extends ApplicationState {

    public ReadyState() {
        super(ApplicationStatus.READY);
    }

    @Override
    public ApplicationStatus confirmReceived() {
        return ApplicationStatus.RECEIVED;
    }
}
