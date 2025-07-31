package org.example.unogame.model.machine.observers;

/**
 * Interface defining the contract for objects that observe changes in observable objects.
 * 
 * <p>This interface represents the Observer in the Observer design pattern. Classes implementing
 * this interface can receive notifications from observable objects when state changes occur.
 * The observer is responsible for:</p>
 * <ul>
 *   <li>Receiving notifications about events from observable objects</li>
 *   <li>Processing the event information appropriately</li>
 *   <li>Reacting to state changes in the observed system</li>
 * </ul>
 * 
 * <p>In the context of the Uno game, observers are used to:</p>
 * <ul>
 *   <li>Monitor UNO calling opportunities</li>
 *   <li>Track game state changes</li>
 *   <li>React to player actions</li>
 *   <li>Coordinate between different game components</li>
 * </ul>
 * 
 * <p>The update method is called by observable objects whenever an event occurs that
 * the observer should be aware of. The event string contains information about what
 * change occurred.</p>
 * 
 * @author Uno Game Team
 * @version 1.0
 * @since 2024
 * @see observable
 * @see observableClass
 * @see observerExample
 */
public interface observer {
    
    /**
     * Called by an observable object to notify this observer about an event.
     * 
     * <p>This method is invoked whenever the observed object experiences a state change
     * or event that observers should be aware of. The implementation should handle the
     * event appropriately based on the observer's specific responsibilities.</p>
     * 
     * <p>Common event types in the Uno game include:</p>
     * <ul>
     *   <li>"HUMAN_SAID_UNO" - Human player called UNO</li>
     *   <li>"HUMAN_SAID_UNO_TO_MACHINE" - Human called UNO against machine</li>
     *   <li>"MACHINE_PLAYED_CARD" - Machine player played a card</li>
     *   <li>"TURN_CHANGED" - Turn changed between players</li>
     *   <li>"CARD_PLAYED" - Any card was played on the table</li>
     *   <li>"GAME_ENDED" - Game has ended (win condition met)</li>
     * </ul>
     * 
     * <p>Implementations should be designed to handle events efficiently and avoid
     * blocking operations that could delay other observers or the main game thread.</p>
     * 
     * @param event a string describing the event that occurred
     * @throws IllegalArgumentException if the event parameter is null
     */
    void update(String event);
}
