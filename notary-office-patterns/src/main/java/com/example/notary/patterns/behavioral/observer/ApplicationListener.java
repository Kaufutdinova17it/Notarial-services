package com.example.notary.patterns.behavioral.observer;

/** Наблюдатель: получает события по заявкам. */
public interface ApplicationListener {

    void onApplicationEvent(ApplicationEvent event);
}
