package org.example.unogame.controller;

import org.example.unogame.model.table.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the {@link GameUnoController} class.
 * <p>
 * These tests ensure correct behavior of the game controller logic, specifically
 * for recognizing special cards according to the game rules.
 * </p>
 *
 * <p><b>Note:</b> The application is designed for Spanish-speaking users, so all user-facing strings remain in Spanish.</p>
 */
class GameUnoControllerTest {

    /** Instance of the game controller used in each test. */
    private GameUnoController controller;

    /**
     * Initializes the test environment by creating a new controller instance
     * before each test execution.
     */
    @BeforeEach
    void setUp() {
        controller = new GameUnoController();
    }

    /**
     * Verifies that the {@code isSpecial} method correctly identifies
     * special Uno cards such as "WILD", "+4", "RESERVE", and "SKIP",
     * and properly excludes regular cards such as "5".
     */
    @Test
    void testIsSpecialCard_various() {
        assertTrue(controller.isSpecial("WILD"));
        assertTrue(controller.isSpecial("+4"));
        assertTrue(controller.isSpecial("RESERVE"));
        assertTrue(controller.isSpecial("SKIP"));
        assertFalse(controller.isSpecial("5"));
    }
}
