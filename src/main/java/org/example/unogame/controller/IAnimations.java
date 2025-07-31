package org.example.unogame.controller;

import javafx.scene.Node;

/**
 * Abstraction for simple UI effects over JavaFX Nodes.
 * Allows injecting a concrete implementation (adapter) into non-UI layers.
 */
public interface IAnimations {

    /**
     * Applies a hover effect to the given node (scale down on enter, restore on exit).
     * @param node JavaFX node; must not be null
     */
    void applyHoverEffect(Node node);

    /**
     * Minimal animation when placing a card on the table (quick fade-in).
     * @param node JavaFX node; must not be null
     */
    void cardAnimation(Node node);
}