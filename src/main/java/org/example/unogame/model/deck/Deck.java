package org.example.unogame.model.deck;

import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.unoenum.UnoEnum;
import org.example.unogame.model.card.Card;

import java.util.Collections;
import java.util.Stack;

/**
 * Represents a deck of Uno cards.
 */
public class Deck {
    private Stack<Card> deckOfCards;

    /**
     * Constructs a new deck of Uno cards and initializes it.
     */
    public Deck() throws GameException {
        deckOfCards = new Stack<>();
        initializeDeck();
    }

    /**
     * Initializes the deck with cards based on the UnoEnum values.
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
                    throw new GameException.IllegalCardValue("Valor inválido para: " + cardEnum.name());
                }
                if (color == null) {
                    throw new GameException.IllegalCardColor("Color inválido para: " + cardEnum.name());
                }

                Card card = new Card(cardEnum.getFilePath(), value, color);
                deckOfCards.push(card);
            }
        }
        Collections.shuffle(deckOfCards);
    }

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

    public static String getCardColor(String name) {
        if (name.contains("GREEN")) return "GREEN";
        if (name.contains("YELLOW")) return "YELLOW";
        if (name.contains("BLUE")) return "BLUE";
        if (name.contains("RED")) return "RED";
        if (name.equals("WILD") || name.equals("FOUR_WILD_DRAW")) return "BLACK";
        return null;
    }

    /**
     * Takes a card from the top of the deck.
     *
     * @return the card from the top of the deck
     * @throws GameException.OutOfCardsInDeck if the deck is empty
     */
    public Card takeCard() throws GameException.OutOfCardsInDeck {
        if (deckOfCards.isEmpty()) {
            throw new GameException.OutOfCardsInDeck();
        }
        return deckOfCards.pop();
    }

    public void reloadFrom(java.util.List<Card> cards) {
        if (cards == null || cards.isEmpty()) return;
        deckOfCards.addAll(cards);
        Collections.shuffle(deckOfCards);
    }

    /**
     * Checks if the deck is empty.
     *
     * @return true if the deck is empty, false otherwise
     */
    public boolean isEmpty() {
        return deckOfCards.isEmpty();
    }
}
