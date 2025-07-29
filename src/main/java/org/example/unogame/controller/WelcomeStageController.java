package org.example.unogame.controller;

import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
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

    public void initialize() {
        applyHoverEffect(playButton);
        applyHoverEffect(continueButton);
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
