package org.example.unogame.model.player;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;

import java.util.ArrayList;

/**
 * Represents a player participating in the Uno game.
 *
 * <p>This class stores the player's hand and a simple type label (e.g., "HUMAN_PLAYER",
 * "MACHINE_PLAYER"). It provides basic operations to add, remove, and retrieve cards.</p>
 *
 * <h2>Thread-safety</h2>
 * <p>This class is <em>not</em> thread-safe. If accessed from multiple threads,
 * callers must apply external synchronization.</p>
 */
public class Player implements IPlayer {
    /** The player's current hand. The list is mutable and grows/shrinks during the game. */
    private ArrayList<Card> cardsPlayer;

    /** A descriptive label for this player (e.g., human vs. machine). */
    private String typePlayer;

    /**
     * Creates a player with an empty hand and the given type label.
     *
     * @param typePlayer a descriptive identifier for the player (e.g., "HUMAN_PLAYER")
     */
    public Player(String typePlayer){
        this.cardsPlayer = new ArrayList<>();
        this.typePlayer = typePlayer;
    }

    /**
     * Adds a card to the player's hand.
     *
     * @param card the card to add
     * @throws GameException.NullCardException if {@code card} is {@code null}
     */
    @Override
    public void addCard(Card card) throws GameException.NullCardException {
        if (card == null) {
            throw new GameException.NullCardException();
        }
        cardsPlayer.add(card);
    }

    /**
     * Returns the list of cards currently held by the player.
     *
     * <p><strong>Note:</strong> This returns a live, mutable list reference.
     * Modifying the returned list will affect the player's hand. Callers should
     * avoid exposing or altering it without proper control.</p>
     *
     * @return the player's hand as an {@link ArrayList}
     */
    @Override
    public ArrayList<Card> getCardsPlayer() {
        return cardsPlayer;
    }

    /**
     * Removes the card at the specified index from the player's hand.
     *
     * @param index the zero-based index of the card to remove
     * @throws GameException.InvalidCardIndex if {@code index} is out of bounds
     */
    @Override
    public void removeCard(int index) throws GameException.InvalidCardIndex {
        if (index < 0 || index >= cardsPlayer.size()) {
            throw new GameException.InvalidCardIndex(index);
        }
        cardsPlayer.remove(index);
    }

    /**
     * Retrieves the card at the specified index from the player's hand.
     *
     * @param index the zero-based index of the card to retrieve
     * @return the card at the requested position
     * @throws GameException.InvalidCardIndex if {@code index} is out of bounds
     */
    @Override
    public Card getCard(int index) throws GameException.InvalidCardIndex {
        if (index < 0 || index >= cardsPlayer.size()) {
            throw new GameException.InvalidCardIndex(index);
        }
        return cardsPlayer.get(index);
    }

    /**
     * Returns the player type label.
     *
     * @return a descriptive string identifying the kind of player
     */
    public String getTypePlayer() {
        return typePlayer;
    }
}
