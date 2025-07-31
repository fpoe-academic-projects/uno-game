package org.example.unogame.model.player;

import org.example.unogame.model.card.Card;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlayerTest {

    @Test
    void testAddCard_addsCardToPlayerHand() {
        Player player = new Player("Human");
        Card card = new Card("7", "BLUE");

        player.addCard(card);

        assertEquals(1, player.getCardsPlayer().size());
        assertEquals(card, player.getCardsPlayer().get(0));
    }
}