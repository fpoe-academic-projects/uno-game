package org.example.unogame.controller;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;

/**
 * Default JavaFX implementation of {@link IAnimations}.
 * <p>
 * Provides lightweight visual effects for JavaFX {@link Node}s:
 * <ul>
 *   <li><b>Hover effect</b>: scales the node down on mouse enter and restores it on exit.</li>
 *   <li><b>Card animation</b>: minimal fade-in when a card is shown/placed.</li>
 * </ul>
 */
public class AnimationsAdapter implements IAnimations {

    /**
     * Creates a new {@code AnimationsAdapter}.
     * <p>
     * This class is stateless; a single instance can be reused across controllers.
     */
    public AnimationsAdapter() {}

    /**
     * Applies a hover effect to the given {@link Node}.
     * <p>
     * @param node the JavaFX node to decorate; must not be {@code null}
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @Override
    public void applyHoverEffect(Node node) {
        if (node == null) throw new NullPointerException("node must not be null");

        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), node);
        shrink.setToX(0.9);
        shrink.setToY(0.9);

        ScaleTransition grow = new ScaleTransition(Duration.millis(150), node);
        grow.setToX(1.0);
        grow.setToY(1.0);

        node.setOnMouseEntered(e -> { grow.stop(); shrink.playFromStart(); });
        node.setOnMouseExited(e -> { shrink.stop(); grow.playFromStart(); });
    }

    /**
     * Plays a minimal fade-in when showing/placing a card node.
     * <p>
     * Sets the node's opacity to {@code 0}, then animates to {@code 1.0}
     * over ~200 milliseconds using a {@link FadeTransition}.
     *
     * @param node the JavaFX node to animate; must not be {@code null}
     * @throws NullPointerException if {@code node} is {@code null}
     */
    @Override
    public void cardAnimation(Node node) {
        if (node == null) throw new NullPointerException("node must not be null");
        node.setOpacity(0);
        FadeTransition ft = new FadeTransition(Duration.millis(200), node);
        ft.setToValue(1.0);
        ft.playFromStart();
    }
}
