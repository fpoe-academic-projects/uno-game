package org.example.unogame.model.game;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.player.Player;

/**
 * Interface representing the Uno game functionality.
 */
public interface IGameUno {

    /**
     * Starts the Uno game.
     */
    void startGame() throws GameException.OutOfCardsInDeck, GameException.NullCardException;

    /**
     * Makes a player draw a specified number of cards from the deck.
     *
     * @param player the player who will draw the cards
     * @param numberOfCards the number of cards to be drawn
     */
    void eatCard(Player player, int numberOfCards) throws GameException.OutOfCardsInDeck, GameException.NullCardException;

    /**
     * Plays a card in the game, adding it to the table.
     *
     * @param card the card to be played
     */
    void playCard(Card card) throws GameException.NullCardException;

    /**
     * Handles the action when a player shouts "Uno".
     *
     * @param playerWhoSang the identifier of the player who shouted "Uno"
     */
    void haveSungOne(String playerWhoSang) throws GameException.OutOfCardsInDeck, GameException.NullCardException;

    /**
     * Retrieves the current visible cards of the human player starting from a specific position.
     *
     * @param posInitCardToShow the starting position of the cards to be shown
     * @return an array of cards that are currently visible to the human player
     */
    Card[] getCurrentVisibleCardsHumanPlayer(int posInitCardToShow) throws GameException.InvalidCardIndex;

    /**
     * Retrieves the current visible cards of the machine player starting from a specific position.
     *
     * @param posInitCardToShow the starting position of the cards to be shown
     * @return an array of cards that are currently visible to the machine player
     */
    Card[] getCurrentVisibleCardsMachinePlayer(int posInitCardToShow) throws GameException.InvalidCardIndex;

    /**
     * Checks if the game is over.
     *
     * @return true if the game is over, false otherwise
     */
    Boolean isGameOver();
}
