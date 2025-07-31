package org.example.unogame.model.card;

import static org.junit.jupiter.api.Assertions.*;

import org.example.unogame.model.exception.GameException;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@link Card} class.
 * <p>
 * This test class ensures that core functionality related to the manipulation
 * of card attributes (such as color) behaves as expected.
 * </p>
 *
 * <p><b>Note:</b> Although the documentation is in English, user-facing strings remain in Spanish
 * since the application is designed for a Spanish-speaking audience.</p>
 */
class CardTest {

    /**
     * Tests that the {@code setColor} method correctly updates the card's color.
     * <p>
     * This test checks whether the color of the card changes from its initial value
     * to the new value as expected.
     * </p>
     *
     * @throws GameException.IllegalCardColor if the provided color is not valid for the card
     */
    @Test
    void testSetColor_changesCardColorSuccessfully() throws GameException.IllegalCardColor {
        Card card = new Card("7", "BLUE");
        card.setColor("GREEN");
        assertEquals("GREEN", card.getColor());
    }
}
