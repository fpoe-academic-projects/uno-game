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
                Thread.sleep((long) (Math.random() * 2000 + 2000)); // 2-4 segundos
            } catch (InterruptedException e) {
                e.printStackTrace();
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
        // Obtener el conteo actual de cartas del controlador para asegurar que tenemos el estado más reciente
        int humanCardsCount = gameUnoController.getHumanPlayer().getCardsPlayer().size();
        int machineCardsCount = gameUnoController.getMachinePlayer().getCardsPlayer().size();
        
        System.out.println("Jugador=" + humanCardsCount + ", Maquina=" + machineCardsCount);
        
        // No llamar UNO si alguno de los jugadores tiene 0 cartas (el juego debería estar terminando)
        if (humanCardsCount == 0 || machineCardsCount == 0) {
            // Detener el hilo
            running = false;
            return;
        }
        
        if (humanCardsCount == 1 && machineCanSayOneToPlayer) {
            System.out.println("Maquina detecta una sola carta del jugador");
            // Check if human still has 1 card and hasn't called UNO
            humanCardsCount = gameUnoController.getHumanPlayer().getCardsPlayer().size();
            if (humanCardsCount == 1 && gameUnoController.isHumanCanSayONE()) {
                System.out.println("Maquina llamando UNO al jugador!");
                // Machine calls UNO on the human player
                gameUnoController.setTurnLabel("La máquina cantó UNO al jugador");
                gameUnoController.setHumanCanSayONE(false);
                gameUnoController.setPlayHuman(false);
                gameUnoController.setMachineSayOne(true);
                
                // Esperar 2 segundos para que el mensaje de UNO sea visible
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                
                // aplicar penalización de UNO al jugador
                gameUnoController.applyUnoPenalty(gameUnoController.getHumanPlayer());
            } else {
                System.out.println("El jugador ya cantó UNO o ya no tiene una carta");
            }
            
        } else if (machineCardsCount == 1 && machineCanSayOne) {
            //System.out.println("Maquina canta UNO para defensa!");
            // Maquina canta UNO para defenderse
            gameUnoController.setTurnLabel("La máquina cantó UNO para defenderse");
            gameUnoController.setHumanCanSayONEToMachine(false);
        } else if (machineCardsCount == 1 && !machineCanSayOne) {
            //System.out.println("ThreadSingUNO - Machine has 1 card but machineCanSayOne is false!");
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
            synchronized (this) {
                notifyAll();
            }
        }
    }
}