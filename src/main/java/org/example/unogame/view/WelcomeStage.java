package org.example.unogame.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeStage extends Stage {

    public WelcomeStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/unogame/welcome-view.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            // Re-throwing the caught IOException
            throw new IOException("Error while loading FXML file", e);
        }
        Scene scene = new Scene(root);
        // Configuring the stage
        setTitle("Uno Game"); // Sets the title of the stage
        setScene(scene); // Sets the scene for the stage
        setResizable(false); // Disallows resizing of the stage
        show(); // Displays the stage
    }

    public static WelcomeStage getInstance() throws IOException {
        return WelcomeStageHolder.INSTANCE != null ?
                WelcomeStageHolder.INSTANCE :
                (WelcomeStageHolder.INSTANCE = new WelcomeStage());
    }

    private static class WelcomeStageHolder {
        private static WelcomeStage INSTANCE;
    }
}
