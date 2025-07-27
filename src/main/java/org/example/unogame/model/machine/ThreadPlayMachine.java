package org.example.unogame.model.machine;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import org.example.unogame.controller.GameUnoController;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.player.Player;
import org.example.unogame.model.table.Table;

public class ThreadPlayMachine extends Thread {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;
    private GameUnoController controller;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, GameUnoController controller) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.controller = controller;
        this.hasPlayerPlayed = false;
    }

    public void run() {
        while (true) {
            if (hasPlayerPlayed) {
                try {
                    Thread.sleep(2000); // espera 2 segundos para simular "pensar"
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                putCardOnTheTable();

                Platform.runLater(() -> {
                    controller.updateCardsMachinePlayer();  // llama al metodo en el controlador que actualiza las cartas de la maquina
                });

                hasPlayerPlayed = false;
            }
        }
    }

    private void putCardOnTheTable(){
        int index = (int) (Math.random() * machinePlayer.getCardsPlayer().size());
        Card card = machinePlayer.getCard(index);
        table.addCardOnTheTable(card);
        tableImageView.setImage(card.getImage());
        machinePlayer.removeCard(index); // quita la carta del mazo de la maquina luego de juagr la carta
    }

    public void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
    }
}
