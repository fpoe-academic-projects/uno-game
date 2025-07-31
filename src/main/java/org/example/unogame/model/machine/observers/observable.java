package org.example.unogame.model.machine.observers;

/**
 * Implementation of the interface that will be observed by the observer
 */
public interface observable {

    void addObserver(observer o);

    void deleteObserver(observer o);

    void notification(String event);
}
