package org.example.unogame.model.machine;

import java.io.Serializable;
import java.util.ArrayList;
import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.exception.GameException;

/**
 * The ThreadSingUNOMachine class represents the thread that handles the logic for the machine to call UNO in the Uno game.
 */
public class ThreadSingUNOMachine implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;

    private ArrayList<Card> cardsPlayer;
    private ArrayList<Card> machineCardsPlayer;

    private boolean machineCanSayOne = true;
    private boolean machineCanSayOneToPlayer = true;
    private boolean running = true;

    // Transient: se reinician manualmente con init()
    private transient GameUnoController gameUnoController;
    private transient ThreadPlayMachine threadPlayMachine;

    /**
     * Constructor principal del hilo para decir UNO por la máquina.
     */
    public ThreadSingUNOMachine(ArrayList<Card> cardsPlayer, ArrayList<Card> machineCardsPlayer,
                                GameUnoController gameUnoController, ThreadPlayMachine threadPlayMachine)
            throws GameException.ThreadInitializationException {
        if (cardsPlayer == null || machineCardsPlayer == null || gameUnoController == null || threadPlayMachine == null) {
            throw new GameException.ThreadInitializationException("UNO: Uno de los parámetros del constructor es null");
        }

        this.cardsPlayer = cardsPlayer;
        this.machineCardsPlayer = machineCardsPlayer;
        this.gameUnoController = gameUnoController;
        this.threadPlayMachine = threadPlayMachine;
    }


    public void init(GameUnoController gameUnoController, ThreadPlayMachine threadPlayMachine)
            throws GameException.ThreadInitializationException {
        if (gameUnoController == null || threadPlayMachine == null) {
            throw new GameException.ThreadInitializationException("UNO: No se puede inicializar el hilo UNO con referencias null");
        }
        this.gameUnoController = gameUnoController;
        this.threadPlayMachine = threadPlayMachine;
    }

    @Override
    public void run() {
        while (running) {
            try {
                Thread.sleep((long) (Math.random() * 5000));
            } catch (InterruptedException e) {
                try {
                    throw new GameException.ThreadInterruptedException("El hilo UNO fue interrumpido inesperadamente", e);
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

            synchronized (this) {
                notify();
            }
        }
    }

    /**
     * Verifica si alguno de los jugadores tiene una sola carta para decir UNO.
     */
    private void hasOneCardTheHumanPlayer() throws GameException.NullPlayerException {
        if (gameUnoController.getHumanPlayer() == null || gameUnoController.getMachinePlayer() == null) {
            throw new GameException.NullPlayerException("Uno de los jugadores es null al verificar si tienen una carta");
        }

        int humanCardsCount = gameUnoController.getHumanPlayer().getCardsPlayer().size();
        int machineCardsCount = gameUnoController.getMachinePlayer().getCardsPlayer().size();

        if (humanCardsCount == 0 || machineCardsCount == 0) {
            running = false;
            return;
        }

        if (humanCardsCount == 1 && machineCanSayOneToPlayer) {
            gameUnoController.setTurnLabel("¡UNO!");
            gameUnoController.setHumanCanSayONE(false);
            gameUnoController.setPlayHuman(false);
            gameUnoController.setMachineSayOne(true);

        } else if (machineCardsCount == 1 && machineCanSayOne) {
            gameUnoController.setTurnLabel("¡UNO!");
            gameUnoController.setHumanCanSayONEToMachine(false);
        }
    }

    // Métodos para controlar el comportamiento del hilo
    public void setMachineCanSayOne(boolean machineCanSayOne) {
        this.machineCanSayOne = machineCanSayOne;
    }

    public void setMachineCanSayOneToPlayer(boolean machineCanSayOneToPlayer) {
        this.machineCanSayOneToPlayer = machineCanSayOneToPlayer;
    }

    public void setRunning(boolean running) {
        this.running = running;
        if (!running) {
            synchronized (this) {
                notifyAll();
            }
        }
    }
}
