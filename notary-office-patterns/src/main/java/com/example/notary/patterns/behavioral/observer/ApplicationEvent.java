package com.example.notary.patterns.behavioral.observer;

import com.example.notary.model.Application;
import com.example.notary.model.ApplicationStatus;

/** Событие по заявке: создание или изменение статуса. */
public class ApplicationEvent {

    private final Application application;
    private final ApplicationStatus oldStatus;
    private final ApplicationStatus newStatus;
    private final String actor;

    public ApplicationEvent(Application application, ApplicationStatus oldStatus,
                            ApplicationStatus newStatus, String actor) {
        this.application = application;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.actor = actor;
    }

    public Application getApplication() {
        return application;
    }

    public ApplicationStatus getOldStatus() {
        return oldStatus;
    }

    public ApplicationStatus getNewStatus() {
        return newStatus;
    }

    public String getActor() {
        return actor;
    }

    /** true, если событие — создание заявки. */
    public boolean isCreation() {
        return oldStatus == null;
    }
}
