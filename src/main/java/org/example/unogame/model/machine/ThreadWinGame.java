package org.example.unogame.model.machine;

import java.util.ArrayList;

import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.player.Player;
import org.example.unogame.view.GameUnoStage;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ThreadWinGame implements Runnable {
    private GameUnoController gameUnoController;
    private Player machinePlayer;
    private Player humanPlayer;
    private boolean running = true;
    private Deck deckOfCards;

    public ThreadWinGame(Player humanPlayer, Player machinePlayer, Deck deckOfCards, GameUnoController gameUnoController) {
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        this.deckOfCards = deckOfCards;
        this.gameUnoController = gameUnoController;
    }

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

    private void verifiedGame() {
        int numsCardsMachine = machinePlayer.getCardsPlayer().size();
        int numsCardsHuman = humanPlayer.getCardsPlayer().size();

        if (numsCardsHuman == 0) {
            gameUnoController.setRunningOneThread(false);
            gameUnoController.setRunningPlayMachineThread(false);
            stopThread();

            Platform.runLater(() -> {
                GameUnoStage.deleteInstance();
                showAlert("FELICITACIONES! GANASTE!", "Tienes 0 cartas", "El Jugador gana por quedarse sin cartas");
            });

        } else if (numsCardsMachine == 0) {
            gameUnoController.setRunningOneThread(false);
            gameUnoController.setRunningPlayMachineThread(false);
            stopThread();

            Platform.runLater(() -> {
                GameUnoStage.deleteInstance();
                showAlert("MAQUINA GANA", "Máquina tiene 0 cartas", "Máquina gana por quedarse sin cartas");
            });

        } else if (deckOfCards.isEmpty()) {
            gameUnoController.setRunningOneThread(false);
            gameUnoController.setRunningPlayMachineThread(false);
            stopThread();

            try {
                throw new GameException.OutOfCardsInDeck("El mazo está vacío.");
            } catch (GameException.OutOfCardsInDeck e) {
                e.printStackTrace();
            }

            int pointsMachine = calculateCardValuesWhenDeckEmpty(machinePlayer.getCardsPlayer());
            int pointsHuman = calculateCardValuesWhenDeckEmpty(humanPlayer.getCardsPlayer());

            Platform.runLater(() -> {
                GameUnoStage.deleteInstance();
                if (pointsMachine < pointsHuman) {
                    gameUnoController.setTurnLabel("Maquina gana");
                    showAlert("Máquina gana", "PUNTAJE DEL JUGADOR: " + pointsHuman + "\nPUNTAJE DE LA MÁQUINA: " + pointsMachine, "La máquina gana por puntaje");
                } else {
                    gameUnoController.setTurnLabel("Jugador gana");
                    showAlert("HAS GANADO :D!", "PUNTAJE DEL JUGADOR: " + pointsHuman + "\nPUNTAJE DE LA MÁQUINA: " + pointsMachine, "El jugador gana por puntaje");
                }
            });
        }
    }

    private int calculateCardValuesWhenDeckEmpty(ArrayList<Card> cards) {
        int total = 0;
        for (Card card : cards) {
            try {
                int value = convertCardValueToInt(card.getValue());
                total += value;
            } catch (GameException.IllegalCardValue e) {
                e.printStackTrace();
            }
        }
        return total;
    }

    private int convertCardValueToInt(String value) throws GameException.IllegalCardValue {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            switch (value) {
                case "SKIP":
                case "RESERVE":
                case "+2":
                    return 20;
                case "WILD":
                case "+4":
                    return 50;
                default:
                    throw new GameException.IllegalCardValue("Valor no reconocido: " + value);
            }
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void stopThread() {
        running = false;
    }
}
