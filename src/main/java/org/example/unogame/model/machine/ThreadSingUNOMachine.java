package org.example.unogame.model.machine;

import java.io.Serializable;
import java.util.ArrayList;

import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.machine.observers.observer;

/**
 * Background worker that simulates the machine's behavior for calling "UNO".
 *
 * <p>This runnable periodically checks the number of cards each player has and, based
 * on simple conditions, triggers "UNO" calls through the {@link GameUnoController}.
 * It also coordinates flags that affect whether the human can call "UNO" or whether the
 * machine can call "UNO" against the human.</p>
 *
 * <h2>Threading</h2>
 * <ul>
 *   <li>This class is intended to run on a dedicated thread (e.g., via {@link Thread}).</li>
 *   <li>It uses a simple loop with a randomized sleep to simulate reaction time.</li>
 *   <li>External code can stop the loop by calling {@link #setRunning(boolean)}.</li>
 *   <li>UI updates are delegated to the controller; those should be performed on the JavaFX thread.</li>
 * </ul>
 */
public class ThreadSingUNOMachine implements Runnable, observer, Serializable {
    private static final long serialVersionUID = 1L;

    /** Human player's hand reference. */
    private ArrayList<Card> cardsPlayer;

    /** Machine player's hand reference. */
    private ArrayList<Card> machineCardsPlayer;

    /** Whether the machine is allowed to call UNO for itself. */
    private boolean machineCanSayOne = true;

    /** Whether the machine is allowed to call UNO against the human. */
    private boolean machineCanSayOneToPlayer = true;

    /** Controller used to update UI state and flags. */
    private transient GameUnoController gameUnoController;

    /** Reference to the machine-turn thread (not used for control here, just held). */
    private transient ThreadPlayMachine threadPlayMachine;

    /** Main loop flag; set to {@code false} to stop this runnable. */
    private boolean running = true;

    /**
     * Creates a new UNO-calling worker.
     *
     * @param cardsPlayer        reference to the human player's cards
     * @param machineCardsPlayer reference to the machine player's cards
     * @param gameUnoController  controller used for UI updates and shared flags
     * @param threadPlayMachine  the machine's play thread (required reference)
     * @throws GameException.ThreadInitializationException if any argument is {@code null}
     */
    public ThreadSingUNOMachine(ArrayList<Card> cardsPlayer, ArrayList<Card> machineCardsPlayer,
                                GameUnoController gameUnoController, ThreadPlayMachine threadPlayMachine)
            throws GameException.ThreadInitializationException {
        if (cardsPlayer == null || machineCardsPlayer == null || gameUnoController == null || threadPlayMachine == null) {
            // Keep original message (Spanish by design)
            throw new GameException.ThreadInitializationException("UNO: Uno de los parámetros del constructor es null");
        }

        this.cardsPlayer = cardsPlayer;
        this.machineCardsPlayer = machineCardsPlayer;
        this.gameUnoController = gameUnoController;
        this.threadPlayMachine = threadPlayMachine;
    }

    /**
     * Main loop: sleeps for a random short interval, checks if it should continue running,
     * then evaluates whether any player has exactly one card to trigger corresponding "UNO" logic.
     *
     * <p>If the owning controller disables the UNO thread (see {@link GameUnoController#isRunningOneThread()}),
     * this loop stops.</p>
     *
     * <p>Interrupted sleeps are wrapped into a {@link GameException.ThreadInterruptedException} and
     * propagated as a {@link RuntimeException} to terminate the thread.</p>
     */
    @Override
    public void run() {
        while (running) {
            try {
                // Randomized delay to simulate reaction time
                Thread.sleep((long) (Math.random() * 5000));
            } catch (InterruptedException e) {
                try {
                    // Preserve original behavior: wrap and rethrow as unchecked to terminate
                    throw new GameException.ThreadInterruptedException("El hilo UNO fue interrumpido", e);
                } catch (GameException.ThreadInterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }

            if (!gameUnoController.isRunningOneThread()) {
                running = false;
                break;
            }

            try {
                hasOneCardTheHumanPlayer();
            } catch (GameException.NullPlayerException e) {
                System.err.println(e.getMessage());
                running = false;
            }

            // Notifies any waiter on this monitor (no corresponding wait() in this class)
            synchronized (this) {
                notify();
            }
        }
    }

    /**
     * Checks the current hand sizes and triggers "UNO" through the controller when applicable.
     *
     * <ul>
     *   <li>If the human has exactly one card and the machine is allowed to call UNO against the human,
     *       the controller is instructed to show "¡UNO!", disable the human's own UNO call, prevent
     *       immediate human play, and mark that the machine has called UNO.</li>
     *   <li>If the machine has exactly one card and it is allowed to call UNO for itself,
     *       the controller is instructed to show "¡UNO!" and disable the human's ability
     *       to call UNO against the machine.</li>
     * </ul>
     *
     * <p>If either player has zero cards, the thread stops running.</p>
     *
     * @throws GameException.NullPlayerException if either player reference is {@code null}
     */
    private void hasOneCardTheHumanPlayer() throws GameException.NullPlayerException {
        if (gameUnoController.getHumanPlayer() == null || gameUnoController.getMachinePlayer() == null) {
            // Keep original message (Spanish by design)
            throw new GameException.NullPlayerException("Uno de los jugadores es null al verificar si tienen una carta");
        }

        int humanCardsCount = gameUnoController.getHumanPlayer().getCardsPlayer().size();
        int machineCardsCount = gameUnoController.getMachinePlayer().getCardsPlayer().size();

        if (humanCardsCount == 0 || machineCardsCount == 0) {
            running = false;
            return;
        }

        if (humanCardsCount == 1 && machineCanSayOneToPlayer) {
            // Notificar evento usando Observer pattern
            gameUnoController.notifyGameEvent("HUMAN_HAS_ONE_CARD");
        } else if (machineCardsCount == 1 && machineCanSayOne && gameUnoController.isHumanCanSayONEToMachine()) {
            // Notificar evento usando Observer pattern
            gameUnoController.notifyGameEvent("MACHINE_HAS_ONE_CARD");
        }
    }

    /**
     * Enables or disables the machine's ability to call UNO for itself.
     *
     * @param machineCanSayOne {@code true} to allow; {@code false} to block
     */
    public void setMachineCanSayOne(boolean machineCanSayOne) {
        this.machineCanSayOne = machineCanSayOne;
    }

    /**
     * Enables or disables the machine's ability to call UNO against the human player.
     *
     * @param machineCanSayOneToPlayer {@code true} to allow; {@code false} to block
     */
    public void setMachineCanSayOneToPlayer(boolean machineCanSayOneToPlayer) {
        this.machineCanSayOneToPlayer = machineCanSayOneToPlayer;
    }

    /**
     * Starts or stops the worker loop. When stopping, notifies any thread
     * waiting on this monitor to avoid potential deadlocks.
     *
     * @param running {@code true} to keep running; {@code false} to stop
     */
    public void setRunning(boolean running) {
        this.running = running;
        if (!running) {
            synchronized (this) {
                notifyAll();
            }
        }
    }
    
    /**
     * Observer pattern implementation - reacts to game events.
     *
     * @param event the game event that occurred
     */
    @Override
    public void update(String event) {
        switch (event) {
                    case "HUMAN_SAID_UNO":
            // Human said UNO for themselves - disable UNO calling
            gameUnoController.setHumanCanSayONE(false);
            break;
                
                    case "HUMAN_SAID_UNO_TO_MACHINE":
            // Human said UNO against machine - disable UNO calling and apply penalty
            gameUnoController.setHumanCanSayONEToMachine(false);
            try {
                gameUnoController.penalizeMachineForNotCallingUNO();
            } catch (Exception e) {
                System.err.println("Error al aplicar penalización a la máquina: " + e.getMessage());
            }
            break;
                
            case "HUMAN_HAS_ONE_CARD":
                // Start timer for human to say UNO
                if (machineCanSayOneToPlayer) {
                    gameUnoController.setTurnLabel("¡Tienes 1 carta!");
                    
                    try {
                        gameUnoController.showUNOTimer(5);
                    } catch (Exception e) {
                        System.err.println("Error al mostrar timer UNO: " + e.getMessage());
                    }
                    
                    // If timer wasn't cancelled, apply penalty
                    if (!gameUnoController.cancelTimer) {
                        gameUnoController.setTurnLabel("¡UNO! La máquina canta UNO");
                        gameUnoController.setPlayHuman(false);
                        
                        try {
                            gameUnoController.penalizeHumanForNotCallingUNO();
                        } catch (Exception e) {
                            System.err.println("Error al aplicar penalización UNO: " + e.getMessage());
                        }
                    }
                }
                break;
                
            case "MACHINE_HAS_ONE_CARD":
                // Start timer for human to say UNO against machine
                if (machineCanSayOne && gameUnoController.isHumanCanSayONEToMachine()) {
                    gameUnoController.setTurnLabel("¡La máquina tiene 1 carta!");
                    
                    try {
                        gameUnoController.showUNOTimer(5);
                    } catch (Exception e) {
                        System.err.println("Error al mostrar timer UNO contra máquina: " + e.getMessage());
                    }
                    
                    // If timer wasn't cancelled, machine says UNO
                    if (!gameUnoController.cancelTimer) {
                        gameUnoController.setTurnLabel("¡UNO! La máquina canta UNO");
                    }
                }
                break;
        }
    }
}
