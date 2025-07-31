package org.example.unogame.model.player;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link Player} class.
 * <p>
 * This test class verifies that player-related functionality works correctly,
 * particularly the behavior of adding cards to a player's hand.
 * </p>
 *
 * <p><b>Note:</b> While documentation is provided in English, user-visible strings
 * remain in Spanish as the target audience is Spanish-speaking.</p>
 */
class PlayerTest {

    /**
     * Tests that a card is correctly added to the player's hand.
     * <p>
     * This ensures that the {@code addCard} method functions as expected
     * when a valid card is passed and updates the internal hand state correctly.
     * </p>
     *
     * @throws GameException.NullCardException if the provided card is {@code null}
     */
    @Test
    void testAddCard_addsCardToPlayerHand() throws GameException.NullCardException {
        Player player = new Player("HUMAN_PLAYER");
        Card card = new Card("7", "BLUE");

        player.addCard(card);

        assertEquals(1, player.getCardsPlayer().size());
        assertEquals(card, player.getCardsPlayer().get(0));
    }
}
