package org.example.unogame.view.Alert;

import javafx.scene.control.Alert;

public class AlertBox implements IAlertBox{
    // Method for displaying an error message
    @Override
    public void showMessage(String title, String header, String content) {
        // Create an ERROR alert window"12.
        Alert alert = new Alert(Alert.AlertType.ERROR);
        // Set the title, header, and content of the message
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        // Shows the alert window and wait for the user to close it"
        alert.showAndWait();
    }

    // Method for showing a confirmation message
    @Override
    public void showConfirm(String title, String header, String content) {
        // Create an INFORMATION alert window
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        // Set the title, header, and content of the message
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        // Shows the alert window and wait for the user to close it
        alert.showAndWait();
    }
}