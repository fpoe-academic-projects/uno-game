package org.example.unogame.model.table;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the table in the Uno game where cards are played.
 */
public class Table {
    public static class TableRecycleException extends RuntimeException {
        public TableRecycleException(String message) { super(message); }
    }
    private ArrayList<Card> cardsTable = null;

    /**
     * Constructs a new Table object with no cards on it.
     */
    public Table() {
        this.cardsTable = new ArrayList<>();

    }
    /**
     * Adds a card to the table.
     *
     * @param card The card to be added to the table.
     */
    public synchronized void addCardOnTheTable(Card card){
        this.cardsTable.add(card);
    }

    /**
     * Retrieves the current card on the table.
     *
     * @return The card currently on the table.
     * @throws GameException.EmptyTableException if there are no cards on the table.
     */
    public synchronized Card getCurrentCardOnTheTable() throws GameException.EmptyTableException {
        if (cardsTable.isEmpty()) {
            throw new GameException.EmptyTableException("No hay cartas sobre la mesa.");
        }
        return this.cardsTable.get(this.cardsTable.size() - 1);
    }

    public synchronized String getColorOnTheTable() throws GameException.EmptyTableException {
        if (cardsTable.isEmpty()) {
            throw new GameException.EmptyTableException("No hay cartas sobre la mesa.");
        }
        return this.cardsTable.get(this.cardsTable.size() - 1).getColor();
    }

    public synchronized void setColorOnTheTable(String color) throws GameException.EmptyTableException, GameException.IllegalCardColor {
        if (cardsTable.isEmpty()) {
            throw new GameException.EmptyTableException("No hay cartas sobre la mesa.");
        }
        this.cardsTable.get(this.cardsTable.size() - 1).setColor(color);
    }

    /**
     * Collects all discard cards from the table, EXCEPT the top card.
     * Leaves only the current top card on the table.
     *
     * @param resetWildToBlack if true, resets WILD and +4 cards back to color "BLACK"
     *                         before returning them (útil cuando se recicla el mazo).
     * @return a modifiable list containing the collected discard cards (may be empty).
     */
    public synchronized List<Card> collectDiscardsExceptTop(boolean resetWildToBlack) throws GameException.IllegalCardColor {
        int size = cardsTable.size();
        if (size <= 1) {
            return Collections.emptyList();
        }

        List<Card> discards = new ArrayList<>(cardsTable.subList(0, size - 1));

        if (resetWildToBlack) {
            if (discards == null) {
                throw new IllegalArgumentException("No hay cartas descartadas.");
            }
            for (int i = 0; i < discards.size(); i++) {
                Card card = discards.get(i);
                if (card == null) {
                    throw new TableRecycleException("Se encontró una carta nula en el índice " + i + ".");
                }
                String value = card.getValue();
                if ("WILD".equals(value) || "+4".equals(value)) {
                    card.setColor("BLACK");
                }
            }
        }

        Card top = cardsTable.get(size - 1);
        cardsTable.clear();
        cardsTable.add(top);

        return discards;
    }

    /**
     * Helper: current number of cards on the table.
     */
    public synchronized int size() {
        return cardsTable.size();
    }
}
