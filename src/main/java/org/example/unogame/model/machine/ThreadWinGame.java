package org.example.unogame.model.machine;

import java.io.Serializable;
import java.util.ArrayList;

import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.player.Player;
import org.example.unogame.view.Alert.AlertBox;
import org.example.unogame.view.GameUnoStage;

import javafx.application.Platform;

/**
 * Background worker that detects end-of-game conditions and announces the winner.
 *
 * <p>This runnable periodically checks the players' hand sizes and the deck state to determine:
 * <ul>
 *   <li>If the human has 0 cards (human wins).</li>
 *   <li>If the machine has 0 cards (machine wins).</li>
 *   <li>If the deck is empty (winner decided by score).</li>
 * </ul>
 * It then stops related threads, closes the game stage, and shows a result dialog.</p>
 *
 * <h2>Threading</h2>
 * <ul>
 *   <li>Runs on a dedicated thread and sleeps briefly between checks.</li>
 *   <li>All UI interactions (closing the stage, showing alerts, changing labels) are executed on the JavaFX thread via {@link Platform#runLater(Runnable)}.</li>
 * </ul>
 */
public class ThreadWinGame implements Runnable, Serializable {
    /** Controller used to update flags and UI labels. */
    private GameUnoController gameUnoController;

    /** Machine player reference. */
    private Player machinePlayer;

    /** Human player reference. */
    private Player humanPlayer;

    /** Main loop flag; set to {@code false} to stop this runnable. */
    private boolean running = true;

    /** Shared deck reference to check depletion and compute scoring fallback. */
    private Deck deckOfCards;
    
    /** AlertBox for showing game result dialogs. */
    private AlertBox alertBox = new AlertBox();

    /**
     * Creates a new end-of-game watcher.
     *
     * @param humanPlayer       the human player
     * @param machinePlayer     the machine player
     * @param deckOfCards       the shared deck
     * @param gameUnoController the controller to coordinate UI and thread flags
     */
    public ThreadWinGame(Player humanPlayer, Player machinePlayer, Deck deckOfCards, GameUnoController gameUnoController) {
        this.humanPlayer = humanPlayer;
        this.machinePlayer = machinePlayer;
        this.deckOfCards = deckOfCards;
        this.gameUnoController = gameUnoController;
    }

    public void init(GameUnoController gameUnoController) throws GameException.ThreadInitializationException {
        if (gameUnoController == null) {
            throw new GameException.ThreadInitializationException("GameUnoController no puede ser null.");
        }
        this.gameUnoController = gameUnoController;
    }

    /**
     * Main loop that periodically verifies end-of-game conditions.
     * Sleeps for a short, randomized interval before each verification.
     */
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep((long) (Math.random() * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace(); // In production, prefer a logger.
            }
            verifiedGame();
        }
    }

    /**
     * Evaluates win conditions and triggers the appropriate UI updates and thread shutdowns:
     * <ul>
     *   <li>Human wins if {@code humanPlayer} has 0 cards.</li>
     *   <li>Machine wins if {@code machinePlayer} has 0 cards.</li>
     *   <li>If the deck is empty, compute points for both hands and declare the lower total as winner.</li>
     * </ul>
     *
     * <p>All labels/alerts keep their original Spanish strings by design.</p>
     */
    private void verifiedGame() {
        int numsCardsMachine = machinePlayer.getCardsPlayer().size();
        int numsCardsHuman = humanPlayer.getCardsPlayer().size();

        if (numsCardsHuman == 0) {
            gameUnoController.setRunningOneThread(false);
            gameUnoController.setRunningPlayMachineThread(false);
            stopThread();

            Platform.runLater(() -> {
                GameUnoStage.deleteInstance();
                showAlert("¡FELICITACIONES, GANASTE!", "Tienes 0 cartas", "El Jugador gana por quedarse sin cartas");
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
                // Keep message in Spanish by design
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
                    showAlert("Máquina gana",
                            "PUNTAJE DEL JUGADOR: " + pointsHuman + "\nPUNTAJE DE LA MÁQUINA: " + pointsMachine,
                            "La máquina gana por puntaje");
                } else {
                    gameUnoController.setTurnLabel("Jugador gana");
                    showAlert("HAS GANADO :D!",
                            "PUNTAJE DEL JUGADOR: " + pointsHuman + "\nPUNTAJE DE LA MÁQUINA: " + pointsMachine,
                            "El jugador gana por puntaje");
                }
            });
        }
    }

    /**
     * Sums the point value of a list of cards for the score-based ending.
     *
     * <p>Numeric cards count as their face value; SKIP/RESERVE/+2 are worth 20 points;
     * WILD/+4 are worth 50 points.</p>
     *
     * @param cards the hand to evaluate
     * @return the total point value
     */
    private int calculateCardValuesWhenDeckEmpty(ArrayList<Card> cards) {
        int total = 0;
        for (Card card : cards) {
            try {
                int value = convertCardValueToInt(card.getValue());
                total += value;
            } catch (GameException.IllegalCardValue e) {
                e.printStackTrace(); // In production, prefer a logger and continue.
            }
        }
        return total;
    }

    /**
     * Converts a card value string to its scoring integer.
     *
     * @param value the card value (e.g., "7", "SKIP", "+4", "WILD")
     * @return score points for the given value
     * @throws GameException.IllegalCardValue if the value cannot be recognized
     */
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
                    // Keep message in Spanish by design
                    throw new GameException.IllegalCardValue("Valor no reconocido: " + value);
            }
        }
    }

    /**
     * Shows an informational alert dialog with the provided content.
     * (Strings are intentionally kept in Spanish.)
     *
     * @param title   the dialog title
     * @param header  the dialog header text
     * @param content the dialog content text
     */
    private void showAlert(String title, String header, String content) {
        alertBox.showConfirm(title, header, content);
    }

    /**
     * Stops the main verification loop.
     */
    public void stopThread() {
        running = false;
        System.out.println("[ThreadWinGame] Hilo detenido.");
    }

}