package org.example.unogame.view.Alert;

/**
 * Interface defining the contract for alert dialog boxes in the Uno game application.
 * 
 * <p>This interface provides a standardized way to display different types of alert dialogs
 * to users during gameplay. It abstracts the JavaFX Alert functionality to provide a
 * consistent user experience across the application.</p>
 * 
 * <p>The interface supports two main types of alerts:</p>
 * <ul>
 *   <li><strong>Error Messages:</strong> Used to display critical errors, validation failures,
 *       or game rule violations that require user attention</li>
 *   <li><strong>Confirmation Messages:</strong> Used to display informational messages,
 *       game state updates, or confirmations that the user should acknowledge</li>
 * </ul>
 * 
 * <p>All alert dialogs are modal, meaning they block user interaction with the main
 * application window until the user dismisses them. This ensures that important
 * information is not missed by the user.</p>
 * 
 * <p>Common use cases in the Uno game include:</p>
 * <ul>
 *   <li>Displaying game rule violations (invalid card plays)</li>
 *   <li>Showing error messages for file operations (save/load failures)</li>
 *   <li>Confirming game state changes (turn transitions, UNO calls)</li>
 *   <li>Notifying users about game events (penalties, special card effects)</li>
 * </ul>
 * 
 */
public interface IAlertBox {
    
    /**
     * Displays an error message dialog to the user.
     * 
     * <p>This method creates and shows a modal error alert dialog with the specified
     * title, header, and content. Error dialogs are typically used to inform users
     * about critical issues that require immediate attention.</p>
     * 
     * <p>The dialog will:</p>
     * <ul>
     *   <li>Display with an error icon and styling</li>
     *   <li>Block interaction with the main application window</li>
     *   <li>Wait for user acknowledgment before closing</li>
     *   <li>Return control to the calling code after dismissal</li>
     * </ul>
     * 
     * <p>Common error scenarios in the Uno game include:</p>
     * <ul>
     *   <li>Invalid card play attempts</li>
     *   <li>File I/O errors during save/load operations</li>
     *   <li>Game state inconsistencies</li>
     *   <li>Network or system errors</li>
     * </ul>
     * 
     * @param title the title displayed in the alert dialog's title bar
     * @param header the header text displayed prominently in the dialog
     * @param content the main content/message text displayed in the dialog body
     * @throws IllegalArgumentException if any of the parameters are null
     */
    void showMessage(String title, String header, String content);

    /**
     * Displays a confirmation/information message dialog to the user.
     * 
     * <p>This method creates and shows a modal information alert dialog with the specified
     * title, header, and content. Confirmation dialogs are typically used to inform users
     * about successful operations, game state changes, or provide general information.</p>
     * 
     * <p>The dialog will:</p>
     * <ul>
     *   <li>Display with an information icon and styling</li>
     *   <li>Block interaction with the main application window</li>
     *   <li>Wait for user acknowledgment before closing</li>
     *   <li>Return control to the calling code after dismissal</li>
     * </ul>
     * 
     * <p>Common confirmation scenarios in the Uno game include:</p>
     * <ul>
     *   <li>Successful game state saves</li>
     *   <li>Turn transitions and player changes</li>
     *   <li>UNO call confirmations</li>
     *   <li>Special card effect notifications</li>
     *   <li>Game completion messages</li>
     * </ul>
     * 
     * @param title the title displayed in the alert dialog's title bar
     * @param header the header text displayed prominently in the dialog
     * @param content the main content/message text displayed in the dialog body
     * @throws IllegalArgumentException if any of the parameters are null
     */
    void showConfirm(String title, String header, String content);
}
