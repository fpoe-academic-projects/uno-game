package org.example.unogame.model.game;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.machine.ThreadPlayMachine;
import org.example.unogame.model.machine.ThreadSingUNOMachine;
import org.example.unogame.model.machine.ThreadWinGame;
import org.example.unogame.model.player.IPlayer;
import org.example.unogame.model.player.Player;
import org.example.unogame.model.table.Table;

import java.io.Serializable;

/**
 * Core model for an Uno match.
 *
 * <p>This class coordinates game setup, card draws, card plays, and simple
 * queries for visible cards used by the UI layer. It references the human and
 * machine players, the shared deck, and the table (discard pile/top card).</p>
 *
 * <h2>Thread-safety</h2>
 * <p>This class is <em>not</em> thread-safe. If accessed concurrently, callers
 * must apply external synchronization.</p>
 */
public class GameUno implements IGameUno, Serializable {

    //private Player humanPlayer;
    //private Player machinePlayer;
    private Deck deck;
    private Table table;
    private Player humanPlayer;
    private Player machinePlayer;
    private ThreadPlayMachine threadPlayMachine;
    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadWinGame threadWinGame;

    /**
     * Constructs a new {@code GameUno} instance with the provided collaborators.
     *
     * @param humanPlayer   the human player participating in the game
     * @param machinePlayer the machine player participating in the game
     * @param deck          the deck of cards shared by both players
     * @param table         the table where cards are placed during the game
     */
    public GameUno(Player humanPlayer, Player machinePlayer, Deck deck, Table table) {
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        this.deck = deck;
        this.table = table;
    }

    /**
     * Starts the game by dealing the opening hands and placing the initial table card.
     *
     * <ul>
     *   <li>Deals <strong>5</strong> cards to the human player and <strong>5</strong> to the machine.</li>
     *   <li>Draws cards from the deck until a numeric card is found (0–9) and places it on the table.</li>
     * </ul>
     *
     * @throws GameException.OutOfCardsInDeck if the deck runs out of cards while dealing or selecting the initial card
     * @throws GameException.NullCardException if a null card is unexpectedly encountered while dealing
     */
    @Override
    public void startGame() throws GameException.OutOfCardsInDeck, GameException.NullCardException {
        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                humanPlayer.addCard(this.deck.takeCard());
            } else {
                machinePlayer.addCard(this.deck.takeCard());
            }
        }

        // Select a numeric card to start the game (only digits 0–9)
        Card initialCard;
        do {
            initialCard = deck.takeCard(); // draw from the deck
        } while (!isNumberCard(initialCard)); // keep drawing until a numeric card is found
        table.addCardOnTheTable(initialCard);
    }

    /**
     * Checks whether the given card has a numeric face value (0–9).
     *
     * @param card the card to inspect
     * @return true if the card value matches a single digit; false otherwise
     */
    private boolean isNumberCard(Card card) {
        String value = card.getValue();
        return value != null && value.matches("[0-9]");
    }

    /**
     * Allows a player to draw a specified number of cards from the deck.
     *
     * @param player        the player who will draw cards
     * @param numberOfCards the number of cards to draw (must be non-negative)
     * @throws GameException.OutOfCardsInDeck if the deck runs out of cards while drawing
     * @throws GameException.NullCardException if a drawn card is unexpectedly null
     */
    @Override
    public void eatCard(Player player, int numberOfCards) throws GameException.OutOfCardsInDeck, GameException.NullCardException {
        for (int i = 0; i < numberOfCards; i++) {
            player.addCard(this.deck.takeCard());
        }
    }

    /**
     * Places a card onto the table (top of the discard pile).
     *
     * @param card the card to place
     * @throws GameException.NullCardException if {@code card} is {@code null}
     */
    @Override
    public void playCard(Card card) throws GameException.NullCardException {
        if (card == null) {
            // Keep original string in Spanish by design
            throw new GameException.NullCardException("No se puede jugar una carta nula");
        }
        this.table.addCardOnTheTable(card);
    }

    /**
     * Handles the event where a player has shouted "UNO", causing the opponent to draw a penalty card.
     *
     * @param playerWhoSang identifier of the player who shouted "UNO" (e.g., "HUMAN_PLAYER")
     * @throws GameException.OutOfCardsInDeck if the deck has no cards to draw for the penalty
     * @throws GameException.NullCardException if a drawn card is unexpectedly null
     */
    @Override
    public void haveSungOne(String playerWhoSang) throws GameException.OutOfCardsInDeck, GameException.NullCardException {
        if (playerWhoSang.equals("HUMAN_PLAYER")) {
            machinePlayer.addCard(this.deck.takeCard());
        } else {
            humanPlayer.addCard(this.deck.takeCard());
        }
    }

    /**
     * Returns up to four currently visible cards from the human player's hand,
     * starting at {@code posInitCardToShow}. Intended for paginated UI display.
     *
     * <p>If the player has no cards or the starting position is beyond the hand size,
     * an empty array is returned.</p>
     *
     * @param posInitCardToShow zero-based starting index of the visible window
     * @return an array (length 0–4) of visible cards
     * @throws GameException.InvalidCardIndex if an internal access computes an invalid index
     */
    @Override
    public Card[] getCurrentVisibleCardsHumanPlayer(int posInitCardToShow) throws GameException.InvalidCardIndex {
        int totalCards = this.humanPlayer.getCardsPlayer().size();

        // Nothing to show or the start index is beyond available cards
        if (totalCards == 0 || posInitCardToShow >= totalCards) {
            return new Card[0];
        }

        int numVisibleCards = Math.min(4, totalCards - posInitCardToShow);

        // Defensive: ensure non-negative count
        if (numVisibleCards <= 0) {
            return new Card[0];
        }

        Card[] cards = new Card[numVisibleCards];
        for (int i = 0; i < numVisibleCards; i++) {
            cards[i] = this.humanPlayer.getCard(posInitCardToShow + i);
        }
        return cards;
    }

    /**
     * Returns up to four currently visible cards from the machine player's hand,
     * starting at {@code posInitCardToShow}. Intended for paginated UI display.
     *
     * <p>If the player has no cards or the starting position is beyond the hand size,
     * an empty array is returned.</p>
     *
     * @param posInitCardToShow zero-based starting index of the visible window
     * @return an array (length 0–4) of visible cards
     * @throws GameException.InvalidCardIndex if an internal access computes an invalid index
     */
    @Override
    public Card[] getCurrentVisibleCardsMachinePlayer(int posInitCardToShow) throws GameException.InvalidCardIndex {
        int totalCards = this.machinePlayer.getCardsPlayer().size();

        // Nothing to show or the start index is beyond available cards
        if (totalCards == 0 || posInitCardToShow >= totalCards) {
            return new Card[0];
        }

        int numVisibleCards = Math.min(4, totalCards - posInitCardToShow);

        // Defensive: ensure non-negative count
        if (numVisibleCards <= 0) {
            return new Card[0];
        }

        Card[] cards = new Card[numVisibleCards];
        for (int i = 0; i < numVisibleCards; i++) {
            cards[i] = this.machinePlayer.getCard(posInitCardToShow + i);
        }
        return cards;
    }

    /**
     * Indicates whether the game should be considered over.
     *
     * <p>Current rule: the game ends when the deck becomes empty.</p>
     *
     * @return {@code true} if the deck has no cards left; {@code false} otherwise
     */
    @Override
    public Boolean isGameOver() {
        return deck.isEmpty();
    }

    /**
     * Returns the current table associated with the game.
     *
     * @return the {@link Table} instance used in gameplay
     */
    public Table getTable() {
        return table;
    }

    /**
     * Returns the deck of cards used in the game.
     *
     * @return the {@link Deck} instance containing the remaining cards
     */
    public Deck getDeck() {
        return deck;
    }

    /**
     * Returns the human player participating in the game.
     *
     * @return the {@link Player} representing the human user
     */
    public Player getHumanPlayer() {
        return humanPlayer;
    }

    /**
     * Returns the machine player (AI opponent) in the game.
     *
     * @return the {@link Player} representing the machine
     */
    public Player getMachinePlayer() {
        return machinePlayer;
    }

    /**
     * Returns the thread responsible for the machine's automatic card play logic.
     *
     * @return the {@link ThreadPlayMachine} instance currently running
     */
    public ThreadPlayMachine getThreadPlayMachine() {
        return threadPlayMachine;
    }

    /**
     * Returns the thread responsible for monitoring when UNO should be declared.
     *
     * @return the {@link ThreadSingUNOMachine} instance currently active
     */
    public ThreadSingUNOMachine getThreadSingUNOMachine() {
        return threadSingUNOMachine;
    }

    /**
     * Returns the thread responsible for checking win conditions during gameplay.
     *
     * @return the {@link ThreadWinGame} instance currently running
     */
    public ThreadWinGame getThreadWinGame() {
        return threadWinGame;
    }

}
