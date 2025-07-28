package org.example.unogame.model.machine;

import java.util.ArrayList;

import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.player.Player;
import org.example.unogame.view.GameUnoStage;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * The ThreadWinGame class represents the thread that handles the logic for checking the win condition in the Uno game.
 */
public class ThreadWinGame implements Runnable {
    private GameUnoController gameUnoController;
    private Player machinePlayer;
    private Player humanPlayer;
    private boolean running = true;
    private Deck deckOfCards;

    /**
     * Constructor for the ThreadWinGame class.
     *
     * @param humanPlayer       the human player
     * @param machinePlayer     the machine player
     * @param deckOfCards       the deck of cards
     * @param gameUnoController the Uno game controller
     */
    public ThreadWinGame(Player humanPlayer, Player machinePlayer, Deck deckOfCards, GameUnoController gameUnoController) {
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        this.deckOfCards = deckOfCards;
        this.gameUnoController = gameUnoController;
    }

    /**
     * Method that runs when the thread starts.
     */
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep((long) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            verifiedGame();
        }
    }

    /**
     * Verifies the game state to determine if the game has been won or if the deck is empty.
     */
    private void verifiedGame() {
        int numsCardsMachine = machinePlayer.getCardsPlayer().size();
        int numsCardsHuman = humanPlayer.getCardsPlayer().size();
        
        if (numsCardsHuman == 0) {
            // Stop all threads immediately
            gameUnoController.setRunningOneThread(false);
            gameUnoController.setRunningPlayMachineThread(false);
            stopThread();
            
            // jugador gana
            Platform.runLater(() -> {
                GameUnoStage.deleteInstance();
                showAlert("FELICITACIONES! GANASTE!", "Tienes 0 cartas", "El Jugador gana por quedarse sin cartas");
            });
            
        } else if (numsCardsMachine == 0) {
            // Stop all threads immediately
            gameUnoController.setRunningOneThread(false);
            gameUnoController.setRunningPlayMachineThread(false);
            stopThread();
            
            // maquina gana
            Platform.runLater(() -> {
                GameUnoStage.deleteInstance();
                showAlert("MAQUINA GANA", "Máquina tiene 0 cartas", "Máquina gana por quedarse sin cartas");
            });
            
        } else if (deckOfCards.isEmpty()) {
            // Stop all threads immediately
            gameUnoController.setRunningOneThread(false);
            gameUnoController.setRunningPlayMachineThread(false);
            stopThread();
            
            System.out.println("Mazo vacio");
            int pointsMachine = calculateCardValuesWhenDeckEmpty(machinePlayer.getCardsPlayer(), machinePlayer);
            int pointsHuman = calculateCardValuesWhenDeckEmpty(humanPlayer.getCardsPlayer(), humanPlayer);
            
            Platform.runLater(() -> {
                if (pointsMachine < pointsHuman) {
                    Platform.runLater(() -> GameUnoStage.deleteInstance());
                    gameUnoController.setTurnLabel("Maquina gana");
                    showAlert("Máquina gana", "PUNTAJE DEL JUGADOR: " + pointsHuman + "\nPUNTAJE DE LA MÁQUINA: " + pointsMachine, "La máquina gana por puntaje");
                    
                } else {
                    Platform.runLater(() -> GameUnoStage.deleteInstance());
                    gameUnoController.setTurnLabel("Jugador gana");
                    showAlert("HAS GANADO :D!", "PUNTAJE DEL JUGADOR: " + pointsHuman + "\nPUNTAJE DE LA MÁQUINA: " + pointsMachine, "El jugador gana por puntaje");
                }
            });
        }
    }

    /**
     * Calculates the total value of the cards when the deck is empty.
     *
     * @param cards  the list of cards
     * @param player the player
     * @return the total value of the cards
     */
    private int calculateCardValuesWhenDeckEmpty(ArrayList<Card> cards, Player player) {
        int total = 0;
        for (Card card : cards) {
            int value = convertCardValueToInt(card.getValue());
            total = total + value;
        }
        return total;
    }

    /**
     * Converts the card value from String to int.
     *
     * @param value the card value as String
     * @return the card value as int
     */
    private int convertCardValueToInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            // Handle non-integer values (special cards)
            switch (value) {
                case "SKIP":
                case "RESERVE":
                    return 20;
                case "+2":
                    return 20;
                case "WILD":
                    return 50;
                case "+4":
                    return 50;
                default:
                    return 10; // Default value if conversion fails
            }
        }
    }

    /**
     * Shows an alert dialog with the specified title, header, and content.
     *
     * @param title   the alert title
     * @param header  the alert header
     * @param content the alert content
     */
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Stops the thread by setting the running flag to false.
     */
    public void stopThread() {
        running = false;
    }
} 