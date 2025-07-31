package org.example.unogame.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.fileHandlers.SerializableFileHandler;
import org.example.unogame.model.game.GameUno;
import org.example.unogame.view.GameUnoStage;
import org.example.unogame.view.WelcomeStage;

import java.io.IOException;

/**
 * Controller for the welcome screen. It applies simple logo animations
 * and handles navigation to the main game stage.
 *
 * <p><strong>UI threading:</strong> All animation setup and UI updates
 * are expected to run on the JavaFX Application Thread.</p>
 */
public class WelcomeStageController {

    /** Play button image (click to start a new game). */
    @FXML
    private ImageView playButton;

    /** Continue button image (reserved for resuming a session, if available). */
    @FXML
    private ImageView continueButton;

    /** UNO logo used for animated effects on the welcome screen. */
    @FXML
    private ImageView unoLogo;

    private SerializableFileHandler serializableFileHandler = new SerializableFileHandler();


    /** Animation helper to apply hover effects to controls. */
    private IAnimations animations = new AnimationsAdapter();

    /**
     * Initializes the controller after the FXML is loaded.
     * <ul>
     *   <li>Applies hover effects to actionable buttons.</li>
     *   <li>Starts logo glow, pulse, and floating animations.</li>
     * </ul>
     */
    public void initialize() {
        animations.applyHoverEffect(playButton);
        animations.applyHoverEffect(continueButton);
        applyLogoEffect();

        
    }



    /**
     * Configures and starts the animated effects applied to the UNO logo:
     * <ul>
     *   <li>A subtle glow that oscillates between two levels.</li>
     *   <li>A gentle scale (pulse) animation.</li>
     *   <li>A slow vertical floating motion.</li>
     * </ul>
     *
     * <p>All animations are configured to auto-reverse and loop indefinitely.</p>
     */
    private void applyLogoEffect() {
        // Glow setup
        Glow glow = new Glow(0.4);
        unoLogo.setEffect(glow);

        // Pulse (scale) animation
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.2), unoLogo);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        // Glow oscillation
        Timeline glowTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.25)),
                new KeyFrame(Duration.seconds(1.2), new KeyValue(glow.levelProperty(), 0.8))
        );
        glowTimeline.setAutoReverse(true);
        glowTimeline.setCycleCount(Animation.INDEFINITE);
        glowTimeline.play();

        // Floating effect
        TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(2.5), unoLogo);
        floatAnim.setByY(-6);
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.play();
    }

    /**
     * Handles the action of starting the game when the play button is clicked.
     * It opens (or ensures) the main game stage and closes the welcome window.
     *
     * @param event the mouse click event on the play button
     */
    @FXML
    private void handlePlayClicked(MouseEvent event) {
        GameUno gameUno = null;
        try {
            GameUnoStage.getInstance(gameUno);
            ((WelcomeStage) playButton.getScene().getWindow()).close();
        } catch (IOException e) {
            // In a production app, use a logger and show a user-friendly message.
            e.printStackTrace();
        } catch (GameException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Handles the click event on the "Continue" button.
     * <p>
     * This method attempts to deserialize a previously saved {@link GameUno} instance from disk.
     * If successful, it loads the game into the main game stage and displays it. The current welcome
     * window is then closed.
     * </p>
     *
     * @param event the {@link MouseEvent} triggered by the user clicking the "Continue" button
     * @throws IOException if there is an error during file input or stage loading
     * @throws ClassNotFoundException if the class definition of the serialized object cannot be found
     */
    @FXML
    private void handleContinueClicked(MouseEvent event) throws IOException, ClassNotFoundException {
        GameUno gameUno = (GameUno) serializableFileHandler.deserialize("uno_saved_game.ser");

        try {
            GameUnoStage stage = GameUnoStage.getInstance(gameUno);
            stage.getController().loadGameState();
            stage.show();

            // Close the welcome stage after launching the game
            ((WelcomeStage) continueButton.getScene().getWindow()).close();

        } catch (IOException | GameException e) {
            e.printStackTrace();
        }
    }

}