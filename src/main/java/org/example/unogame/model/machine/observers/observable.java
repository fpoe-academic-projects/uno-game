package org.example.unogame.model.machine.observers;

/**
 * Interface defining the contract for objects that can be observed in the Observer pattern.
 * 
 * <p>This interface represents the Subject in the Observer design pattern. Classes implementing
 * this interface can maintain a list of observers and notify them when state changes occur.
 * The observable object is responsible for:</p>
 * <ul>
 *   <li>Managing a collection of observers</li>
 *   <li>Adding new observers to the collection</li>
 *   <li>Removing observers from the collection</li>
 *   <li>Notifying all registered observers of events</li>
 * </ul>
 * 
 * <p>In the context of the Uno game, this interface is used to notify observers about
 * game events such as UNO calls, card plays, and turn changes.</p>
 * 
 */
public interface observable {

    /**
     * Registers a new observer to receive notifications from this observable object.
     * 
     * <p>Once registered, the observer will receive notifications for all events
     * until it is explicitly removed using {@link #deleteObserver(observer)}.</p>
     * 
     * @param o the observer to register for notifications
     * @throws IllegalArgumentException if the observer parameter is null
     */
    void addObserver(observer o);

    /**
     * Unregisters an observer from receiving notifications from this observable object.
     * 
     * <p>After calling this method, the observer will no longer receive any notifications
     * from this observable object. If the observer was not previously registered,
     * this method call has no effect.</p>
     * 
     * @param o the observer to unregister from notifications
     */
    void deleteObserver(observer o);

    /**
     * Notifies all registered observers about an event by calling their {@link observer#update(String)} method.
     * 
     * <p>This method iterates through all registered observers and calls their update method
     * with the specified event string. The event string typically contains information about
     * what change occurred that triggered the notification.</p>
     * 
     * <p>Common event types in the Uno game include:</p>
     * <ul>
     *   <li>"HUMAN_SAID_UNO" - Human player called UNO</li>
     *   <li>"HUMAN_SAID_UNO_TO_MACHINE" - Human called UNO against machine</li>
     *   <li>"MACHINE_PLAYED_CARD" - Machine player played a card</li>
     *   <li>"TURN_CHANGED" - Turn changed between players</li>
     * </ul>
     * 
     * @param event a string describing the event that occurred
     * @throws IllegalArgumentException if the event parameter is null
     */
    void notification(String event);
}
