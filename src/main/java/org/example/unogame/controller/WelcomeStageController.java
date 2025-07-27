package org.example.unogame.controller;

import javafx.fxml.FXML;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.example.unogame.view.GameUnoStage;
import org.example.unogame.view.WelcomeStage;

import java.io.IOException;

public class WelcomeStageController {

    @FXML
    private ImageView playButton;

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
