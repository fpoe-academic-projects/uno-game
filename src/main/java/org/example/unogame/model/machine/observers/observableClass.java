package org.example.unogame.model.machine.observers;

import java.util.HashSet;
import java.util.Set;

/**
 * Class that implements the {@code observable} interface and manages a set of observers.
 */
public class observableClass implements observable {

    Set<observer> observerSet = new HashSet<>();

    /**
     * Adds an observer to the set of observers.
     *
     * @param o the observer to add
     */
    @Override
    public void addObserver(observer o) {
        observerSet.add(o);
    }

    /**
     * Removes an observer from the set of observers.
     *
     * @param o the observer to remove
     */
    @Override
    public void deleteObserver(observer o) {
        observerSet.remove(o);
    }

    /**
     * Notifies all observers by calling their {@code update} method.
     */
    @Override
    public void notification(String event) {
        for (observer obs : observerSet){
            obs.update(event);
        }
    }
}

