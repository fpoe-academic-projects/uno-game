package org.example.unogame.model.table;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the shared table (discard pile/top card) in the Uno game.
 *
 * <p>This class stores the sequence of cards placed on the table, exposing
 * operations to add a new top card, query the current top card (and its color),
 * change the color on the top (for wilds), and recycle discards back to a deck
 * while keeping the top card in place.</p>
 *
 * <h2>Thread-safety</h2>
 * <p>All public methods are synchronized to provide basic thread-safety when
 * accessed by multiple worker threads (e.g., human vs. machine turns). Callers
 * should still avoid holding locks for extended periods and keep UI updates off
 * the synchronized methods.</p>
 */

public class Table implements Serializable {
    public static class TableRecycleException extends RuntimeException {
        public TableRecycleException(String message) { super(message); }
    }

    /** Sequence of cards placed on the table; the last element is the current top card. */
    private ArrayList<Card> cardsTable = null;

    /**
     * Creates a table with an initially empty discard pile.
     */
    public Table() {
        this.cardsTable = new ArrayList<>();
    }

    /**
     * Places a new card on top of the table.
     *
     * @param card the card to add
     */
    public synchronized void addCardOnTheTable(Card card){
        this.cardsTable.add(card);
    }

    /**
     * Returns the current top card on the table.
     *
     * @return the last (top) card placed on the table
     * @throws GameException.EmptyTableException if the table has no cards
     */
    public synchronized Card getCurrentCardOnTheTable() throws GameException.EmptyTableException {
        if (cardsTable.isEmpty()) {
            // Keep original Spanish string by design
            throw new GameException.EmptyTableException("No hay cartas sobre la mesa.");
        }
        return this.cardsTable.get(this.cardsTable.size() - 1);
    }

    /**
     * Returns the color of the current top card on the table.
     *
     * @return the color of the top card
     * @throws GameException.EmptyTableException if the table has no cards
     */
    public synchronized String getColorOnTheTable() throws GameException.EmptyTableException {
        if (cardsTable.isEmpty()) {
            // Keep original Spanish string by design
            throw new GameException.EmptyTableException("No hay cartas sobre la mesa.");
        }
        return this.cardsTable.get(this.cardsTable.size() - 1).getColor();
    }

    /**
     * Updates the color of the current top card on the table (commonly used after playing a wild).
     *
     * @param color the color to apply
     * @throws GameException.EmptyTableException if the table has no cards
     * @throws GameException.IllegalCardColor    if the provided color is invalid for the card
     */
    public synchronized void setColorOnTheTable(String color)
            throws GameException.EmptyTableException, GameException.IllegalCardColor {
        if (cardsTable.isEmpty()) {
            // Keep original Spanish string by design
            throw new GameException.EmptyTableException("No hay cartas sobre la mesa.");
        }
        this.cardsTable.get(this.cardsTable.size() - 1).setColor(color);
    }

    /**
     * Collects all discard cards from the table <em>except</em> the top card, leaving
     * only the current top card in place. Optionally resets the color of wilds back to BLACK.
     *
     * <p>This is typically used when the deck runs out and needs to be reloaded from
     * discards while keeping the active top card on the table.</p>
     *
     * @param resetWildToBlack if {@code true}, cards with value "WILD" or "+4" are reset to color "BLACK"
     *                         before being returned (útil cuando se recicla el mazo).
     * @return a modifiable list containing the collected discards (may be empty)
     * @throws GameException.IllegalCardColor if resetting a card color is considered illegal
     * @throws TableRecycleException          if a null card is found among discards
     */
    public synchronized List<Card> collectDiscardsExceptTop(boolean resetWildToBlack)
            throws GameException.IllegalCardColor {

        int size = cardsTable.size();
        if (size <= 1) {
            return Collections.emptyList();
        }

        List<Card> discards = new ArrayList<>(cardsTable.subList(0, size - 1));

        if (resetWildToBlack) {
            // Defensive checks and normalization of wild card colors
            if (discards == null) {
                // Keep message in Spanish by design
                throw new IllegalArgumentException("No hay cartas descartadas.");
            }
            for (int i = 0; i < discards.size(); i++) {
                Card card = discards.get(i);
                if (card == null) {
                    // Keep message in Spanish by design
                    throw new TableRecycleException("Se encontró una carta nula en el índice " + i + ".");
                }
                String value = card.getValue();
                if ("WILD".equals(value) || "+4".equals(value)) {
                    card.setColor("BLACK");
                }
            }
        }

        // Preserve the current top card and clear the rest
        Card top = cardsTable.get(size - 1);
        cardsTable.clear();
        cardsTable.add(top);

        return discards;
    }

    /**
     * @return the current number of cards on the table
     */
    public synchronized int size() {
        return cardsTable.size();
    }
}
