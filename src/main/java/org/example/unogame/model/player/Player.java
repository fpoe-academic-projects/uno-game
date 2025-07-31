package org.example.unogame.model.player;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a player in the Uno game.
 */
public class Player implements IPlayer, Serializable {
    private ArrayList<Card> cardsPlayer;
    private String typePlayer;

    /**
     * Constructs a new Player object with an empty hand of cards.
     */
    public Player(String typePlayer){
        this.cardsPlayer = new ArrayList<>();
        this.typePlayer = typePlayer;
    }

    /**
     * Adds a card to the player's hand.
     *
     * @param card The card to be added to the player's hand.
     * @throws GameException.NullCardException if the card is null.
     */
    @Override
    public void addCard(Card card) throws GameException.NullCardException {
        if (card == null) {
            throw new GameException.NullCardException();
        }
        cardsPlayer.add(card);
    }

    /**
     * Retrieves all cards currently held by the player.
     *
     * @return An ArrayList containing all cards in the player's hand.
     */
    @Override
    public ArrayList<Card> getCardsPlayer() {
        return cardsPlayer;
    }

    /**
     * Removes a card from the player's hand based on its index.
     *
     * @param index The index of the card to remove.
     * @throws GameException.InvalidCardIndex if the index is invalid.
     */
    @Override
    public void removeCard(int index) throws GameException.InvalidCardIndex {
        if (index < 0 || index >= cardsPlayer.size()) {
            throw new GameException.InvalidCardIndex(index);
        }
        cardsPlayer.remove(index);
    }

    /**
     * Retrieves a card from the player's hand based on its index.
     *
     * @param index The index of the card to retrieve.
     * @return The card at the specified index in the player's hand.
     * @throws GameException.InvalidCardIndex if the index is invalid.
     */
    @Override
    public Card getCard(int index) throws GameException.InvalidCardIndex {
        if (index < 0 || index >= cardsPlayer.size()) {
            throw new GameException.InvalidCardIndex(index);
        }
        return cardsPlayer.get(index);
    }

    public String getTypePlayer() {
        return typePlayer;
    }
}
