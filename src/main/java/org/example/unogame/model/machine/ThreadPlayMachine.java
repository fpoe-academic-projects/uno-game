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
    private volatile boolean isPlayerTurn;
    private GameUnoController controller;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, GameUnoController controller) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.controller = controller;
        this.isPlayerTurn = true;
    }

    public void run() {
        while (true) {
            if (!isPlayerTurn) {
                try {
                    Thread.sleep(2000); // espera 2 segundos para simular "pensar"
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                putCardOnTheTable();

                Platform.runLater(() -> {
                    controller.updateCardsMachinePlayer();  // llama al metodo en el controlador que actualiza las cartas de la maquina
                });

                isPlayerTurn = true;
                setPlayerTurn(true);
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

    public void setPlayerTurn(boolean hasPlayerPlayed) {
        this.isPlayerTurn = hasPlayerPlayed;
    }

    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }
}
