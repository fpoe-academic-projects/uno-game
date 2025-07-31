package org.example.unogame.model.machine;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.player.Player;
import org.example.unogame.model.table.Table;

import java.io.Serializable;
import java.util.List;

public class ThreadPlayMachine extends Thread implements Serializable {

    private Table table;
    private Player machinePlayer;
    private Deck deck;
    private boolean running = true;

    private transient ImageView tableImageView;
    private transient GameUnoController controller;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, GameUnoController controller, Deck deck) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.controller = controller;
        this.deck = deck;
    }



    @Override
    public void run() {
        while (running) {
            if (!controller.isHumanTurn()) {
                controller.refreshGameView();

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }

                try {
                    putCardOnTheTable();
                } catch (GameException e) {
                    System.err.println("Error durante jugada de máquina: " + e.getMessage());
                }

                controller.refreshGameView();
            }
        }
    }

    private void putCardOnTheTable() throws GameException {
        boolean cardPlayed = false;

        for (int i = 0; i < machinePlayer.getCardsPlayer().size(); i++) {
            Card card = machinePlayer.getCard(i);

            if (controller.canPlayCard(card, table)) {
                table.addCardOnTheTable(card);
                Platform.runLater(() -> tableImageView.setImage(card.getImage()));
                machinePlayer.removeCard(i);
                cardPlayed = true;

                if (controller.isSpecial(card.getValue())) {
                    controller.specialCard(card, machinePlayer, controller.getHumanPlayer());
                } else {
                    controller.setHumanTurn(true);
                }

                break;
            }
        }

        if (!cardPlayed) {
            if (deck.isEmpty()) {
                List<Card> discards = table.collectDiscardsExceptTop(true);
                deck.reloadFrom(discards);
            }

            Card drawnCard = deck.takeCard();
            machinePlayer.addCard(drawnCard);
            System.out.println("La máquina comió una carta.");
            controller.setHumanTurn(true);
        }
    }

    public String getRandomColorFromHand() {
        String[] colors = {"RED", "BLUE", "YELLOW", "GREEN"};
        return colors[(int) (Math.random() * colors.length)];
    }

    public void setRunning(boolean running) {
        this.running = running;
    }
}
