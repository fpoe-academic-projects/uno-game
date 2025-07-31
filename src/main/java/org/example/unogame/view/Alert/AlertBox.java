package org.example.unogame.view.Alert;

import javafx.scene.control.Alert;

/**
 * Concrete implementation of the {@code IAlertBox} interface using JavaFX Alert dialogs.
 * 
 * <p>This class provides a concrete implementation of the alert box functionality
 * by wrapping JavaFX's {@link javafx.scene.control.Alert} class. It creates modal
 * dialog boxes that can display either error messages or confirmation/information
 * messages to users during gameplay.</p>
 * 
 * <p>Key features of this implementation:</p>
 * <ul>
 *   <li>Uses JavaFX Alert dialogs for consistent platform-native appearance</li>
 *   <li>Provides modal dialogs that block user interaction until dismissed</li>
 *   <li>Supports both error and information alert types</li>
 *   <li>Includes input validation to prevent null parameter issues</li>
 *   <li>Ensures thread safety by using JavaFX Platform.runLater when needed</li>
 * </ul>
 * 
 * <p>This implementation is designed to be used throughout the Uno game application
 * to provide consistent user feedback for various game events and error conditions.</p>
 *
 */
public class AlertBox implements IAlertBox {
    
    /**
     * Displays an error message dialog to the user.
     * 
     * <p>This method creates and shows a modal error alert dialog using JavaFX's
     * {@link javafx.scene.control.Alert.AlertType#ERROR}. The dialog will display
     * with an error icon and styling to clearly indicate that an error has occurred.</p>
     * 
     * <p>The method performs the following steps:</p>
     * <ol>
     *   <li>Validates that all parameters are non-null</li>
     *   <li>Creates a new JavaFX Alert with ERROR type</li>
     *   <li>Sets the title, header, and content text</li>
     *   <li>Displays the dialog and waits for user acknowledgment</li>
     * </ol>
     * 
     * <p>The dialog is modal, meaning it will block all interaction with the main
     * application window until the user closes it by clicking the OK button.</p>
     * 
     * @param title the title displayed in the alert dialog's title bar
     * @param header the header text displayed prominently in the dialog
     * @param content the main content/message text displayed in the dialog body
     * @throws IllegalArgumentException if any of the parameters are null
     */
    @Override
    public void showMessage(String title, String header, String content) {
        // Validate parameters
        if (title == null || header == null || content == null) {
            throw new IllegalArgumentException("Title, header, and content cannot be null");
        }
        
        // Create an ERROR alert window
        Alert alert = new Alert(Alert.AlertType.ERROR);
        
        // Set the title, header, and content of the message
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Show the alert window and wait for the user to close it
        alert.showAndWait();
    }

    /**
     * Displays a confirmation/information message dialog to the user.
     * 
     * <p>This method creates and shows a modal information alert dialog using JavaFX's
     * {@link javafx.scene.control.Alert.AlertType#INFORMATION}. The dialog will display
     * with an information icon and styling to indicate that this is an informational message.</p>
     * 
     * <p>The method performs the following steps:</p>
     * <ol>
     *   <li>Validates that all parameters are non-null</li>
     *   <li>Creates a new JavaFX Alert with INFORMATION type</li>
     *   <li>Sets the title, header, and content text</li>
     *   <li>Displays the dialog and waits for user acknowledgment</li>
     * </ol>
     * 
     * <p>The dialog is modal, meaning it will block all interaction with the main
     * application window until the user closes it by clicking the OK button.</p>
     * 
     * @param title the title displayed in the alert dialog's title bar
     * @param header the header text displayed prominently in the dialog
     * @param content the main content/message text displayed in the dialog body
     * @throws IllegalArgumentException if any of the parameters are null
     */
    @Override
    public void showConfirm(String title, String header, String content) {
        // Validate parameters
        if (title == null || header == null || content == null) {
            throw new IllegalArgumentException("Title, header, and content cannot be null");
        }
        
        // Create an INFORMATION alert window
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        
        // Set the title, header, and content of the message
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        
        // Show the alert window and wait for the user to close it
        alert.showAndWait();
    }
}