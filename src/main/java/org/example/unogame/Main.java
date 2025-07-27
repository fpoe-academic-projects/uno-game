package org.example.unogame;

import javafx.application.Application;
import javafx.stage.Stage;
import org.example.unogame.view.GameUnoStage;
import org.example.unogame.view.WelcomeStage;

import java.io.IOException;

/**
 * The main class of the Uno Game application.
 */
public class Main extends Application {

    /**
     * The main method of the application.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Starts the application.
     *
     * @param primaryStage the primary stage of the application
     * @throws IOException if an error occurs while loading the stage
     */
    @Override
    public void start(Stage primaryStage) throws IOException {
        WelcomeStage.getInstance();
    }
}