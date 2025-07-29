package org.example.unogame.model.machine;

import java.util.ArrayList;

import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;

/**
 * The ThreadSingUNOMachine class represents the thread that handles the logic for the machine to call UNO in the Uno game.
 */
public class ThreadSingUNOMachine implements Runnable {
    private ArrayList<Card> cardsPlayer;
    private ArrayList<Card> machineCardsPlayer;
    private boolean machineCanSayOne = true;
    private boolean machineCanSayOneToPlayer = true;
    private GameUnoController gameUnoController;
    private ThreadPlayMachine threadPlayMachine;
    private boolean running = true;

    /**
     * Constructor for the ThreadSingUNOMachine class.
     *
     * @param cardsPlayer        the player's cards
     * @param machineCardsPlayer the machine's cards
     * @param gameUnoController  the Uno game controller
     * @param threadPlayMachine  the thread that handles the machine's play
     */
    public ThreadSingUNOMachine(ArrayList<Card> cardsPlayer, ArrayList<Card> machineCardsPlayer, 
                               GameUnoController gameUnoController, ThreadPlayMachine threadPlayMachine) {
        this.cardsPlayer = cardsPlayer;
        this.machineCardsPlayer = machineCardsPlayer;
        this.gameUnoController = gameUnoController;
        this.threadPlayMachine = threadPlayMachine;
    }

    /**
     * Method that runs when the thread starts.
     */
    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep((long) (Math.random() * 5000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            // Check if the thread should continue running
            if (!gameUnoController.isRunningOneThread()) {
                running = false;
                break;
            }
            
            hasOneCardTheHumanPlayer();

            synchronized (this) {
                notify();  // Notifies ThreadPlayMachine
            }
        }
    }

    /**
     * Checks if the human player or the machine has one card and updates the game state accordingly.
     */
    private void hasOneCardTheHumanPlayer() {
        // Get current card counts from the controller to ensure we have the latest state
        int humanCardsCount = gameUnoController.getHumanPlayer().getCardsPlayer().size();
        int machineCardsCount = gameUnoController.getMachinePlayer().getCardsPlayer().size();
        
        // Don't call UNO if either player has 0 cards (game should be ending)
        if (humanCardsCount == 0 || machineCardsCount == 0) {
            // Stop the thread immediately if game is ending
            running = false;
            return;
        }
        
        if (humanCardsCount == 1 && machineCanSayOneToPlayer) {
            // Machine calls UNO on the human player
            gameUnoController.setTurnLabel("¡UNO!");
            gameUnoController.setHumanCanSayONE(false);
            gameUnoController.setPlayHuman(false);
            gameUnoController.setMachineSayOne(true);
            
        } else if (machineCardsCount == 1 && machineCanSayOne) {
            // Machine calls UNO for defense
            gameUnoController.setTurnLabel("¡UNO!");
            gameUnoController.setHumanCanSayONEToMachine(false);
        }
    }

    /**
     * Sets whether the machine can call UNO.
     *
     * @param machineCanSayOne true if the machine can call UNO, false otherwise
     */
    public void setMachineCanSayOne(boolean machineCanSayOne) {
        this.machineCanSayOne = machineCanSayOne;
    }

    /**
     * Sets whether the machine can call UNO to the player.
     *
     * @param machineCanSayOneToPlayer true if the machine can call UNO to the player, false otherwise
     */
    public void setMachineCanSayOneToPlayer(boolean machineCanSayOneToPlayer) {
        this.machineCanSayOneToPlayer = machineCanSayOneToPlayer;
    }

    /**
     * Sets whether the thread is running.
     *
     * @param running true if the thread is running, false otherwise
     */
    public void setRunning(boolean running) {
        this.running = running;
        if (!running) {
            // Notify any waiting threads to wake up and check the running flag
            synchronized (this) {
                notifyAll();
            }
        }
    }
}