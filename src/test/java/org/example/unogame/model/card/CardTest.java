package org.example.unogame.model.card;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class CardTest {

    @Test
    void testSetColor_changesCardColorSuccessfully() {
        Card card = new Card("7", "BLUE");
        card.setColor("GREEN");
        assertEquals("GREEN", card.getColor());
    }
}
