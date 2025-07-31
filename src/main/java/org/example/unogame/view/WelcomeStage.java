package org.example.unogame.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Primary welcome window for the Uno application.
 *
 * <p>This stage loads and displays the FXML-defined welcome screen. It is configured
 * as non-resizable and uses a simple singleton-like holder to provide a shared instance.</p>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>The window title and UI strings remain in Spanish by design.</li>
 *   <li>The FXML is expected at {@code /org/example/unogame/welcome-view.fxml} on the classpath.</li>
 * </ul>
 */
public class WelcomeStage extends Stage {

    /**
     * Creates and shows the welcome stage by loading the FXML layout.
     *
     * @throws IOException if the FXML cannot be found or loaded
     */
    public WelcomeStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/unogame/welcome-view.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            // Re-throw with context for upstream handlers
            throw new IOException("Error while loading FXML file", e);
        }

        Scene scene = new Scene(root);

        // Basic stage configuration (strings intentionally kept in Spanish)
        setTitle("UNO - Bienvenido");
        setScene(scene);
        setResizable(false);
        show();
    }

    /**
     * Returns a shared instance of the welcome stage, creating it on first access.
     *
     * <p>This is a lightweight singleton holder; callers are responsible for lifecycle
     * management (e.g., closing and re-opening as needed).</p>
     *
     * @return the shared {@link WelcomeStage} instance
     * @throws IOException if instance creation fails due to FXML loading
     */
    public static WelcomeStage getInstance() throws IOException {
        return WelcomeStageHolder.INSTANCE != null
                ? WelcomeStageHolder.INSTANCE
                : (WelcomeStageHolder.INSTANCE = new WelcomeStage());
    }

    /**
     * Lazy holder for the shared {@link WelcomeStage} instance.
     */
    private static class WelcomeStageHolder {
        private static WelcomeStage INSTANCE;
    }
}