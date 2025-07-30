package org.example.unogame.model.machine;

import javafx.application.Platform;
import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.player.Player;
import org.example.unogame.model.table.Table;

import javafx.scene.image.ImageView;

import java.util.List;

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

    @Override
    public void run() {
        while (running) {
            if (!controller.isHumanTurn()) {
                controller.refreshGameView();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // Si el hilo es interrumpido, simplemente salimos
                    return;
                }

                try {
                    putCardOnTheTable();
                } catch (GameException.InvalidCardIndex e) {
                    System.err.println("Índice inválido al jugar carta de máquina: " + e.getMessage());
                } catch (GameException.NullCardException e) {
                    System.err.println("Carta nula encontrada al jugar: " + e.getMessage());
                } catch (GameException.OutOfCardsInDeck e) {
                    System.err.println("No hay más cartas en el mazo para robar.");
                } catch (GameException.EmptyTableException | GameException.IllegalCardColor e) {
                    System.err.println("La mesa está vacía cuando no debería estarlo.");
                }

                controller.refreshGameView();
            }
        }
    }

    private void putCardOnTheTable() throws GameException.InvalidCardIndex, GameException.NullCardException, GameException.OutOfCardsInDeck, GameException.EmptyTableException, GameException.IllegalCardColor {
        boolean cardPlayed = false;

        for (int i = 0; i < machinePlayer.getCardsPlayer().size(); i++) {
            Card card = machinePlayer.getCard(i);  // puede lanzar InvalidCardIndex

            if (controller.canPlayCard(card, table)) {
                table.addCardOnTheTable(card);
                Platform.runLater(() -> {
                    tableImageView.setImage(card.getImage());
                });
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
            // Si no puede jugar ninguna carta, roba una
            if (deck.isEmpty()) {
                List<Card> discards = table.collectDiscardsExceptTop(true); // true = resets wild/ +4 to black
                deck.reloadFrom(discards);
            }

            Card drawnCard = deck.takeCard();
            machinePlayer.addCard(drawnCard);
            System.out.println("La maquina comio");
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
