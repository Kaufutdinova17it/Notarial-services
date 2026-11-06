package com.example.notary.patterns.behavioral.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Паттерн «Наблюдатель» (Observer) — издатель событий заявок.
 * ApplicationService публикует событие после создания заявки или смены статуса,
 * все подписчики получают его автоматически.
 */
public class ApplicationEventPublisher {

    private final List<ApplicationListener> listeners = new CopyOnWriteArrayList<>();

    public void subscribe(ApplicationListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(ApplicationListener listener) {
        listeners.remove(listener);
    }

    public void publish(ApplicationEvent event) {
        for (ApplicationListener listener : listeners) {
            listener.onApplicationEvent(event);
        }
    }
}
