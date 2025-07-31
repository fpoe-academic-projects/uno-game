package org.example.unogame.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.example.unogame.controller.AnimationsAdapter;
import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.game.GameUno;

import java.io.IOException;

/**
 * Represents the main stage of the Uno game application.
 * This stage displays the game interface to the user.
 */
public class GameUnoStage extends Stage {

    private GameUnoController controller;

    /**
     * Constructs a new instance of GameUnoStage.
     *
     * @throws IOException if an error occurs while loading the FXML file for the game interface.
     */
    public GameUnoStage(GameUno game) throws IOException, GameException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/unogame/game-uno-view.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            // Re-throwing the caught IOException
            throw new IOException("Error while loading FXML file", e);
        }

        this.controller = loader.getController();
        this.controller.setAnimations(new AnimationsAdapter());
        this.controller.initmatch(game);

        Scene scene = new Scene(root);
        // Configuring the stage
        setTitle("Uno Game"); // Sets the title of the stage
        setScene(scene); // Sets the scene for the stage
        setResizable(false); // Disallows resizing of the stage
        show(); // Displays the stage

        controller.setupAutoSaveOnClose(this);
    }

    /**
     * Gets the controller associated with this stage.
     * @return the GameUnoController instance.
     */
    public GameUnoController getController() {
        return controller;
    }

    /**
     * Closes the instance of GameUnoStage.
     * This method is used to clean up resources when the game stage is no longer needed.
     */
    public static void deleteInstance() {
        GameUnoStageHolder.INSTANCE.close();
        GameUnoStageHolder.INSTANCE = null;
    }

    /**
     * Retrieves the singleton instance of GameUnoStage.
     *
     * @return the singleton instance of GameUnoStage.
     * @throws IOException if an error occurs while creating the instance.
     */
    public static GameUnoStage getInstance(GameUno game) throws IOException, GameException {
        return GameUnoStageHolder.INSTANCE != null ?
                GameUnoStageHolder.INSTANCE :
                (GameUnoStageHolder.INSTANCE = new GameUnoStage(game));
    }

    /**
     * Holder class for the singleton instance of GameUnoStage.
     * This class ensures lazy initialization of the singleton instance.
     */
    private static class GameUnoStageHolder {
        private static GameUnoStage INSTANCE;
    }
}
