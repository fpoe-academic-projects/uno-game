package org.example.unogame.model.machine;

import javafx.application.Platform;
import org.example.unogame.controller.AnimationsAdapter;
import org.example.unogame.controller.GameUnoController;
import org.example.unogame.controller.IAnimations;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.player.Player;
import org.example.unogame.model.table.Table;

import javafx.scene.image.ImageView;

import java.util.List;

/**
 * Background thread that automates the machine player's turns.
 *
 * <p>When it is not the human's turn, this thread attempts to play a valid card
 * from the machine hand; if none is available, it draws one card. It updates
 * the table image and triggers UI animations on the JavaFX thread.</p>
 *
 * <h2>Threading</h2>
 * <ul>
 *   <li>UI updates are wrapped in {@link Platform#runLater(Runnable)}.</li>
 *   <li>The loop exits when {@link #setRunning(boolean)} is called with {@code false}
 *       or when the thread is interrupted during sleep.</li>
 * </ul>
 */
public class ThreadPlayMachine extends Thread {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private GameUnoController controller;
    private Deck deck;
    private boolean running = true;
    private IAnimations animations = new AnimationsAdapter();

    /**
     * Creates a machine-play thread bound to the current table, machine player, and UI.
     *
     * @param table          the shared table state (top card, discards)
     * @param machinePlayer  the machine player instance
     * @param tableImageView image view displaying the top card on the table
     * @param controller     controller used to query rules and flip turns
     * @param deck           the shared deck used to draw when needed
     */
    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, GameUnoController controller, Deck deck) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.controller = controller;
        this.deck = deck;
    }

    /**
     * Main loop: when it is not the human's turn, wait briefly to simulate thinking,
     * attempt to play a card, otherwise draw, then refresh the UI.
     */
    @Override
    public void run() {
        while (running) {
            if (!controller.isHumanTurn()) {
                controller.refreshGameView();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    // If interrupted, exit the thread.
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

    /**
     * Attempts to play the first valid card from the machine's hand according to the rules.
     * If none can be played, draws exactly one card (reloading the deck from discards if needed).
     *
     * <p>On a successful play, updates the table image and triggers a card animation.
     * Also handles special card effects via the controller.</p>
     *
     * @throws GameException.InvalidCardIndex if an index access is invalid
     * @throws GameException.NullCardException if a null card is unexpectedly encountered
     * @throws GameException.OutOfCardsInDeck if the deck is empty and cannot be reloaded
     * @throws GameException.EmptyTableException if the table is unexpectedly empty
     * @throws GameException.IllegalCardColor if applying a color is illegal
     */
    private void putCardOnTheTable() throws GameException.InvalidCardIndex, GameException.NullCardException,
            GameException.OutOfCardsInDeck, GameException.EmptyTableException, GameException.IllegalCardColor {

        boolean cardPlayed = false;

        // Try to play the first legal card found in hand
        for (int i = 0; i < machinePlayer.getCardsPlayer().size(); i++) {
            Card card = machinePlayer.getCard(i);  // may throw InvalidCardIndex

            if (controller.canPlayCard(card, table)) {
                table.addCardOnTheTable(card);
                Platform.runLater(() -> {
                    tableImageView.setImage(card.getImage());
                    animations.cardAnimation(tableImageView);
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

        // If no card could be played, draw one
        if (!cardPlayed) {
            if (deck.isEmpty()) {
                List<Card> discards = table.collectDiscardsExceptTop(true); // true = resets wild/ +4 to black
                deck.reloadFrom(discards);
            }

            Card drawnCard = deck.takeCard();
            machinePlayer.addCard(drawnCard);

            // Keep message as-is (Spanish by design)
            System.out.println("La maquina comio");
            controller.setHumanTurn(true);
        }
    }

    /**
     * Picks a random Uno color (uniformly) without considering the machine's current hand.
     * This is a simple heuristic placeholder.
     *
     * @return one of "RED", "BLUE", "YELLOW", or "GREEN"
     */
    public String getRandomColorFromHand() {
        String[] colors = {"RED", "BLUE", "YELLOW", "GREEN"};
        return colors[(int) (Math.random() * colors.length)];
    }

    /**
     * Starts or stops the main loop of this thread.
     *
     * @param running {@code true} to continue running; {@code false} to request stop
     */
    public void setRunning(boolean running) {
        this.running = running;
    }
}
