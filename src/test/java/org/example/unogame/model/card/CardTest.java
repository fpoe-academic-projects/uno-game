package org.example.unogame.model.card;

import static org.junit.jupiter.api.Assertions.*;

import org.example.unogame.model.exception.GameException;
import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    void testSetColor_changesCardColorSuccessfully() throws GameException.IllegalCardColor {
        Card card = new Card("7", "BLUE");
        card.setColor("GREEN");
        assertEquals("GREEN", card.getColor());
    }
}
