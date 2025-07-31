package org.example.unogame.controller;

import org.example.unogame.model.table.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameUnoControllerTest {
    private GameUnoController controller;
    private Table table;

    @BeforeEach
    void setUp() {
        controller = new GameUnoController();
        table = new Table();
    }

    @Test
    void testIsSpecialCard_various() {
        assertTrue(controller.isSpecial("WILD"));
        assertTrue(controller.isSpecial("+4"));
        assertTrue(controller.isSpecial("RESERVE"));
        assertTrue(controller.isSpecial("SKIP"));
        assertFalse(controller.isSpecial("5"));
    }
}