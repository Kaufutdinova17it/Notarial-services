package com.example.notary.patterns.behavioral.observer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * ПОВЕДЕНЧЕСКИЙ ПАТТЕРН — НАБЛЮДАТЕЛЬ (Observer), издатель.
 *
 * Задача из предметной области: по архитектурному плану ApplicationService
 * и NotificationService связаны событийным взаимодействием — после
 * создания заявки или изменения её статуса ApplicationService передаёт
 * событие, а подписчики (NotificationService, журнал аудита) реагируют
 * на него независимо друг от друга.
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
