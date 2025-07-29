package org.example.unogame.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import org.example.unogame.view.GameUnoStage;
import org.example.unogame.view.WelcomeStage;

import java.io.IOException;

public class WelcomeStageController {

    @FXML
    private ImageView playButton;

    @FXML
    private ImageView continueButton;

    @FXML
    private ImageView unoLogo;

    public void initialize() {
        applyHoverEffect(playButton);
        applyHoverEffect(continueButton);
        applyLogoEffect();
    }

    private void applyHoverEffect(ImageView button) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), button);
        shrink.setToX(0.9);
        shrink.setToY(0.9);

        ScaleTransition grow = new ScaleTransition(Duration.millis(150), button);
        grow.setToX(1.0);
        grow.setToY(1.0);

        button.setOnMouseEntered(e -> shrink.playFromStart());
        button.setOnMouseExited(e -> grow.playFromStart());
    }

    private void applyLogoEffect() {
        Glow glow = new Glow(0.4);
        unoLogo.setEffect(glow);

        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.2), unoLogo);
        pulse.setFromX(1.0);
        pulse.setFromY(1.0);
        pulse.setToX(1.08);
        pulse.setToY(1.08);
        pulse.setAutoReverse(true);
        pulse.setCycleCount(Animation.INDEFINITE);
        pulse.play();

        Timeline glowTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(glow.levelProperty(), 0.25)),
                new KeyFrame(Duration.seconds(1.2), new KeyValue(glow.levelProperty(), 0.8))
        );
        glowTimeline.setAutoReverse(true);
        glowTimeline.setCycleCount(Animation.INDEFINITE);
        glowTimeline.play();

        TranslateTransition floatAnim = new TranslateTransition(Duration.seconds(2.5), unoLogo);
        floatAnim.setByY(-6);
        floatAnim.setAutoReverse(true);
        floatAnim.setCycleCount(Animation.INDEFINITE);
        floatAnim.play();
    }

    @FXML
    private void handlePlayClicked(MouseEvent event) {
        try {
            GameUnoStage.getInstance();
            ((WelcomeStage) playButton.getScene().getWindow()).close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
