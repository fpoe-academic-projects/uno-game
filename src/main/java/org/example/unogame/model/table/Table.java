package org.example.unogame.model.table;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;

import java.util.ArrayList;

/**
 * Represents the table in the Uno game where cards are played.
 */
public class Table {
    private ArrayList<Card> cardsTable;

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
    public void addCardOnTheTable(Card card) {
        this.cardsTable.add(card);
    }

    /**
     * Retrieves the current card on the table.
     *
     * @return The card currently on the table.
     * @throws GameException.EmptyTableException if there are no cards on the table.
     */
    public Card getCurrentCardOnTheTable() throws GameException.EmptyTableException {
        if (cardsTable.isEmpty()) {
            throw new GameException.EmptyTableException("No hay cartas sobre la mesa.");
        }
        return this.cardsTable.get(this.cardsTable.size() - 1);
    }

    /**
     * Gets the color of the current card on the table.
     *
     * @return Color of the current card.
     * @throws GameException.EmptyTableException if there are no cards on the table.
     */
    public String getColorOnTheTable() throws GameException.EmptyTableException {
        if (cardsTable.isEmpty()) {
            throw new GameException.EmptyTableException("No hay cartas sobre la mesa.");
        }
        return this.cardsTable.get(this.cardsTable.size() - 1).getColor();
    }

    /**
     * Sets the color of the current card on the table.
     *
     * @param color The color to set.
     * @throws GameException.EmptyTableException if there are no cards on the table.
     * @throws GameException.IllegalCardColor if the color is not valid.
     */
    public void setColorOnTheTable(String color) throws GameException.EmptyTableException, GameException.IllegalCardColor {
        if (cardsTable.isEmpty()) {
            throw new GameException.EmptyTableException("No hay cartas sobre la mesa.");
        }
        this.cardsTable.get(this.cardsTable.size() - 1).setColor(color);
    }
}
