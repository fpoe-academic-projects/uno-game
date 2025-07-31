package org.example.unogame.model.deck;

import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.unoenum.UnoEnum;
import org.example.unogame.model.card.Card;

import java.util.Collections;
import java.util.Stack;

/**
 * Represents the Uno deck, including creation, shuffling, and card drawing.
 *
 * <p>The deck is backed by a {@link Stack} and initialized from {@link UnoEnum}
 * resources, creating {@link Card} instances for each enumerated asset.</p>
 *
 * <h2>Thread-safety</h2>
 * <p>This class is <em>not</em> thread-safe. If accessed from multiple threads,
 * clients must provide their own synchronization.</p>
 */
public class Deck {
    /** LIFO container for the deck; the top is at the end of the stack. */
    private Stack<Card> deckOfCards;

    /**
     * Creates a new deck and fully initializes it from {@link UnoEnum} values.
     * The deck is shuffled after creation.
     *
     * @throws GameException if any card resource cannot be created or validated
     */
    public Deck() throws GameException {
        deckOfCards = new Stack<>();
        initializeDeck();
    }

    /**
     * Scans all {@link UnoEnum} constants, filters valid card entries,
     * creates {@link Card} objects, and pushes them onto the stack.
     * Finally shuffles the deck.
     *
     * @throws GameException if a card has an invalid value/color or its image cannot be loaded
     */
    private void initializeDeck() throws GameException {
        for (UnoEnum cardEnum : UnoEnum.values()) {
            if (cardEnum.name().startsWith("GREEN_") ||
                    cardEnum.name().startsWith("YELLOW_") ||
                    cardEnum.name().startsWith("BLUE_") ||
                    cardEnum.name().startsWith("RED_") ||
                    cardEnum.name().startsWith("SKIP_") ||
                    cardEnum.name().startsWith("RESERVE_") ||
                    cardEnum.name().startsWith("TWO_WILD_DRAW_") ||
                    cardEnum.name().equals("FOUR_WILD_DRAW") ||
                    cardEnum.name().equals("WILD")) {

                String value = getCardValue(cardEnum.name());
                String color = getCardColor(cardEnum.name());

                if (value == null) {
                    // Keep string as-is (Spanish by design)
                    throw new GameException.IllegalCardValue("Valor inválido para: " + cardEnum.name());
                }
                if (color == null) {
                    // Keep string as-is (Spanish by design)
                    throw new GameException.IllegalCardColor("Color inválido para: " + cardEnum.name());
                }

                Card card = new Card(cardEnum.getFilePath(), value, color);
                deckOfCards.push(card);
            }
        }
        Collections.shuffle(deckOfCards);
    }

    /**
     * Derives the logical Uno value from an enum constant name.
     *
     * @param name enum constant name (e.g., "GREEN_7", "SKIP_RED", "FOUR_WILD_DRAW")
     * @return the card value (e.g., "7", "SKIP", "+4", "WILD"), or {@code null} if not recognized
     */
    public static String getCardValue(String name) {
        if (name.endsWith("0")) return "0";
        if (name.endsWith("1")) return "1";
        if (name.endsWith("2")) return "2";
        if (name.endsWith("3")) return "3";
        if (name.endsWith("4")) return "4";
        if (name.endsWith("5")) return "5";
        if (name.endsWith("6")) return "6";
        if (name.endsWith("7")) return "7";
        if (name.endsWith("8")) return "8";
        if (name.endsWith("9")) return "9";
        if (name.startsWith("SKIP_")) return "SKIP";
        if (name.startsWith("RESERVE_")) return "RESERVE";
        if (name.startsWith("TWO_WILD_DRAW_")) return "+2";
        if (name.equals("FOUR_WILD_DRAW")) return "+4";
        if (name.equals("WILD")) return "WILD";
        return null;
    }

    /**
     * Derives the Uno color from an enum constant name.
     *
     * @param name enum constant name
     * @return one of "GREEN", "YELLOW", "BLUE", "RED", "BLACK" (for wild cards), or {@code null} if unknown
     */
    public static String getCardColor(String name) {
        if (name.contains("GREEN")) return "GREEN";
        if (name.contains("YELLOW")) return "YELLOW";
        if (name.contains("BLUE")) return "BLUE";
        if (name.contains("RED")) return "RED";
        if (name.equals("WILD") || name.equals("FOUR_WILD_DRAW")) return "BLACK";
        return null;
    }

    /**
     * Removes and returns the top card from the deck.
     *
     * @return the top {@link Card}
     * @throws GameException.OutOfCardsInDeck if the deck is empty
     */
    public Card takeCard() throws GameException.OutOfCardsInDeck {
        if (deckOfCards.isEmpty()) {
            throw new GameException.OutOfCardsInDeck();
        }
        return deckOfCards.pop();
    }

    /**
     * Reloads the deck from a list of cards (e.g., recycled discards) and shuffles it.
     * If the argument is {@code null} or empty, the method does nothing.
     *
     * @param cards a list of cards to push back into the deck
     */
    public void reloadFrom(java.util.List<Card> cards) {
        if (cards == null || cards.isEmpty()) return;
        deckOfCards.addAll(cards);
        Collections.shuffle(deckOfCards);
    }

    /**
     * Indicates whether the deck has no cards left.
     *
     * @return {@code true} if there are no cards in the deck; {@code false} otherwise
     */
    public boolean isEmpty() {
        return deckOfCards.isEmpty();
    }
}
