package org.example.unogame.model.machine;

import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.player.Player;
import org.example.unogame.model.table.Table;

import javafx.application.Platform;
import javafx.scene.image.ImageView;

public class ThreadPlayMachine extends Thread {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private GameUnoController controller;
    private Deck deck;
    private boolean running = true;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, GameUnoController controller, Deck deck) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.controller = controller;
        this.deck = deck;
    }

    public void run() {
        while (running) {
            if (!controller.isHumanTurn()) {
                Platform.runLater(() -> {
                    controller.updateCardsMachinePlayer();
                });
                try {
                    Thread.sleep(2000); // espera 2 segundos
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                putCardOnTheTable();
                controller.refreshGameView();
            }
        }
    }

    private void putCardOnTheTable() {
        boolean cardPlayed = false;

        for (int i = 0; i < machinePlayer.getCardsPlayer().size(); i++) {
            Card card = machinePlayer.getCard(i);

            if (controller.canPlayCard(card, table)) {
                table.addCardOnTheTable(card);
                tableImageView.setImage(card.getImage());
                machinePlayer.removeCard(i);
                cardPlayed = true;

                if (controller.isSpecial(card.getValue())) {
                    controller.specialCard(card, machinePlayer, controller.getHumanPlayer());
                } else {
                    // Carta normal: pasar turno al humano
                    controller.setHumanTurn(true);
                }

                break;
            }
        }

        if (!cardPlayed) {
            // Si no puede jugar ninguna carta, roba una
            Card drawnCard = deck.takeCard();
            machinePlayer.addCard(drawnCard);
            System.out.println("La maquina comio");

            if (controller.canPlayCard(drawnCard, table)) {
                table.addCardOnTheTable(drawnCard);
                tableImageView.setImage(drawnCard.getImage());
                machinePlayer.removeCard(machinePlayer.getCardsPlayer().size() - 1); // ultima carta añadida

                if (controller.isSpecial(drawnCard.getValue())) {
                    controller.specialCard(drawnCard, machinePlayer, controller.getHumanPlayer());
                    System.out.println("La maquina jugo la carta que comio");
                } else {
                    controller.setHumanTurn(true);
                }
            } else {
                // no puede jugar la carta robada, pasa turno al humano
                controller.setHumanTurn(true);
            }
        }
    }


    public String getRandomColorFromHand() {
        String[] colors = {"RED", "BLUE", "YELLOW", "GREEN"};
        return colors[(int) (Math.random() * colors.length)];
    }

    /**
     * Sets whether the thread is running.
     *
     * @param running true if the thread is running, false otherwise
     */
    public void setRunning(boolean running) {
        this.running = running;
    }

}
