package org.example.unogame.model.player;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;

import java.util.ArrayList;

/**
 * Interface representing a player in the Uno game.
 * Provides methods to interact with the player's hand of cards.
 */
public interface IPlayer {

    /**
     * Adds a card to the player's hand.
     *
     * @param card The card to be added to the player's hand.
     * @throws GameException.NullCardException if the card is null.
     */
    void addCard(Card card) throws GameException.NullCardException;

    /**
     * Retrieves a card from the player's hand based on its index.
     *
     * @param index The index of the card to retrieve.
     * @return The card at the specified index in the player's hand.
     * @throws GameException.InvalidCardIndex if the index is invalid.
     */
    Card getCard(int index) throws GameException.InvalidCardIndex;

    /**
     * Retrieves all cards currently held by the player.
     *
     * @return An ArrayList containing all cards in the player's hand.
     */
    ArrayList<Card> getCardsPlayer();

    /**
     * Removes a card from the player's hand based on its index.
     *
     * @param index The index of the card to remove.
     * @throws GameException.InvalidCardIndex if the index is invalid.
     */
    void removeCard(int index) throws GameException.InvalidCardIndex;
}
