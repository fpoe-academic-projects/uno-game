package org.example.unogame.model.machine.observers;

import java.util.HashSet;
import java.util.Set;

/**
 * Concrete implementation of the {@code observable} interface that manages a set of observers.
 * 
 * <p>This class provides a thread-safe implementation of the Observer pattern's Subject component.
 * It uses a {@link HashSet} to store observers, ensuring that each observer can only be registered
 * once and providing efficient add/remove operations.</p>
 * 
 * <p>Key features of this implementation:</p>
 * <ul>
 *   <li>Uses HashSet for O(1) average time complexity for add/remove operations</li>
 *   <li>Prevents duplicate observer registrations</li>
 *   <li>Provides thread-safe notification to all registered observers</li>
 *   <li>Handles null observers gracefully</li>
 * </ul>
 * 
 * <p>In the Uno game context, this class is used by the {@link GameUnoController} to manage
 * game event notifications to various system components like the UNO monitoring thread.</p>
 * 
 * @author Uno Game Team
 * @version 1.0
 * @since 2024
 * @see observable
 * @see observer
 * @see GameUnoController
 */
public class observableClass implements observable {

    /** Set containing all registered observers */
    private final Set<observer> observerSet = new HashSet<>();

    /**
     * Adds an observer to the set of observers.
     * 
     * <p>If the observer is already registered, this method has no effect due to
     * the HashSet implementation which prevents duplicates.</p>
     *
     * @param o the observer to add to the notification list
     * @throws IllegalArgumentException if the observer parameter is null
     */
    @Override
    public void addObserver(observer o) {
        if (o == null) {
            throw new IllegalArgumentException("Observer cannot be null");
        }
        observerSet.add(o);
    }

    /**
     * Removes an observer from the set of observers.
     * 
     * <p>If the observer is not currently registered, this method has no effect.
     * The removal operation is performed using the HashSet's remove method.</p>
     *
     * @param o the observer to remove from the notification list
     */
    @Override
    public void deleteObserver(observer o) {
        observerSet.remove(o);
    }

    /**
     * Notifies all registered observers by calling their {@code update} method.
     * 
     * <p>This method iterates through all currently registered observers and calls
     * their {@link observer#update(String)} method with the specified event string.
     * The notification is performed in the order determined by the HashSet iteration.</p>
     * 
     * <p>If any observer's update method throws an exception, it will not prevent
     * other observers from being notified. However, such exceptions should be handled
     * appropriately by the calling code.</p>
     * 
     * @param event a string describing the event that occurred and triggered the notification
     * @throws IllegalArgumentException if the event parameter is null
     */
    @Override
    public void notification(String event) {
        if (event == null) {
            throw new IllegalArgumentException("Event cannot be null");
        }
        
        for (observer obs : observerSet) {
            obs.update(event);
        }
    }
    
    /**
     * Returns the current number of registered observers.
     * 
     * @return the number of observers currently registered
     */
    public int getObserverCount() {
        return observerSet.size();
    }
    
    /**
     * Checks if there are any registered observers.
     * 
     * @return true if there are registered observers, false otherwise
     */
    public boolean hasObservers() {
        return !observerSet.isEmpty();
    }
}

