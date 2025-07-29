package org.example.unogame.controller;

import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.game.GameUno;
import org.example.unogame.model.machine.ThreadPlayMachine;
import org.example.unogame.model.machine.ThreadSingUNOMachine;
import org.example.unogame.model.machine.ThreadWinGame;
import org.example.unogame.model.player.Player;
import org.example.unogame.model.table.Table;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.unogame.model.unoenum.UnoEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for the Uno game.
 */
public class GameUnoController {

    @FXML private GridPane gridPaneCardsMachine;
    @FXML private GridPane gridPaneCardsPlayer;
    @FXML private ImageView tableImageView;
    @FXML private VBox colorVBox;
    @FXML private ImageView exitButton;
    @FXML private ImageView deckButton;
    @FXML private ImageView nextButton;
    @FXML private ImageView backButton;
    @FXML private ImageView unoButton;

    @FXML public Label turnLabel;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private Card card;
    private int posInitCardToShow;
    private volatile boolean isHumanTurn;

    // UNO calling state variables
    private boolean humanCanSayONE = true;
    private boolean humanCanSayONEToMachine = true;
    private boolean machineSayOne = false;
    private boolean playHuman = true;

    // Thread control variables
    private boolean runningOneThread = true;
    private boolean runningPlayMachineThread = true;

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;
    private ThreadWinGame threadWinGame;

    /**
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        initVariables();
        this.gameUno.startGame();
        updateGridPaneMargin();

        applyHoverEffect(exitButton);
        applyHoverEffect(deckButton);
        applyHoverEffect(nextButton);
        applyHoverEffect(backButton);
        applyHoverEffect(unoButton);

        tableImageView.setImage(this.table.getCurrentCardOnTheTable().getImage()); // mostrar visualmente a carta inciial en la mesa
        refreshGameView();

        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.tableImageView, this, this.deck);
        threadPlayMachine.start();

        threadSingUNOMachine = new ThreadSingUNOMachine(
            this.humanPlayer.getCardsPlayer(),
            this.machinePlayer.getCardsPlayer(),
            this,
            this.threadPlayMachine
        );
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        threadWinGame = new ThreadWinGame(
            this.humanPlayer,
            this.machinePlayer,
            this.deck,
            this
        );
        Thread winThread = new Thread(threadWinGame, "ThreadWinGame");
        winThread.start();
    }

    /**
     * Initializes the variables for the game.
     */
    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
        this.isHumanTurn = true;
    }

    /**
     * Prints the human player's cards on the grid pane.
     */
    private void printCardsHumanPlayer() {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);
        updateTurnLabel();

        // Add safety check for null or empty array
        if (currentVisibleCardsHumanPlayer == null || currentVisibleCardsHumanPlayer.length == 0) {
            return;
        }

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            Rectangle cardRectangle = card.getCard();
            applyHoverEffectCards(cardRectangle);

            cardRectangle.setOnMouseClicked((MouseEvent event) -> {
                if (!isHumanTurn) return; // solo si es su turno

                if (canPlayCard(card, table)) { // verifica si puede jugar la carta
                    gameUno.playCard(card);
                    tableImageView.setImage(card.getImage());
                    humanPlayer.removeCard(findPosCardsHumanPlayer(card));

                    if (isSpecial(card.getValue())) { // verifica si es una carta especial
                        specialCard(card, humanPlayer, machinePlayer);
                    } else {
                        // carta normal -> turno maquina
                        setHumanTurn(false);
                    }
                    refreshGameView();
                }
            });

            this.gridPaneCardsPlayer.add(cardRectangle, i, 0);
        }
    }

    private void printCardsMachinePlayer() {
        this.gridPaneCardsMachine.getChildren().clear();

        List<Card> safeCopy;
        synchronized (machinePlayer) {
            safeCopy = new ArrayList<>(machinePlayer.getCardsPlayer());
        }

        // Add safety check for null or empty array
        if (safeCopy == null || safeCopy.size() == 0) {
            return;
        }

        int maxVisibleCards = 4;
        int cardsToShow = Math.min(safeCopy.size(), maxVisibleCards);

        // Show 4 cards
        for (int i = 0; i < cardsToShow; i++) {
            Rectangle backCard = Card.getBackCardRectangle();
            applyHoverEffectCards(backCard);
            this.gridPaneCardsMachine.add(backCard, i, 0);
        }

        // If there are more than 4 cards, show a +N label on top of the last visible card
        if (safeCopy.size() > maxVisibleCards) {
            int remaining = safeCopy.size() - maxVisibleCards;
            Label plusLabel = new Label("+" + remaining);
            plusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.8); -fx-padding: 5px;");
            this.gridPaneCardsMachine.add(plusLabel, maxVisibleCards - 1, 0);
        }
    }

    /**
     * Finds the position of a specific card in the human player's hand.
     *
     * @param card the card to find
     * @return the position of the card, or -1 if not found
     */
    private Integer findPosCardsHumanPlayer(Card card) {
        for (int i = 0; i < this.humanPlayer.getCardsPlayer().size(); i++) {
            if (this.humanPlayer.getCardsPlayer().get(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }

    public boolean canPlayCard(Card cardPlay, Table table) { // recibe carta a jugar y la mesa
        Card currentCard = table.getCurrentCardOnTheTable(); // con el tablero que recibe de parametro, obtenemos la carta actual en la mesa

        String colorToPlay = cardPlay.getColor();
        String valueToPlay = cardPlay.getValue();

        String colorOnTable = currentCard.getColor();
        String valueOnTable = currentCard.getValue();

        // siempre se pueden jugar WILD o +4
        if ("WILD".equals(valueToPlay) || "+4".equals(valueToPlay)) {
            return true;
        }

        if ("+4".equals(valueOnTable)) { // para poder poner cualquier carta despues del +4 (el jugador que la puso)
            return true;
        }

        // coincide el color
        if (colorToPlay != null && colorOnTable != null && colorToPlay.equals(colorOnTable)) {
            return true;
        }

        // coincide el valor
        if (valueToPlay != null && valueOnTable != null && valueToPlay.equals(valueOnTable)) {
            return true;
        }

        refreshGameView();

        return false;
    }

    public void specialCard(Card card, Player currentPlayer, Player otherPlayer) {
        String value = card.getValue();
        Card currentCard = table.getCurrentCardOnTheTable();

        switch (value) {
            case "WILD":
                if (currentPlayer.equals(humanPlayer)) {
                    this.card = card;
                    showColorPicker(); // usuario elige color de la carta
                    // no cambiar turno hasta que el usuario seleccione color
                    deckButton.setDisable(true);
                } else {
                    String randomColor = threadPlayMachine.getRandomColorFromHand();
                    table.setColorOnTheTable(randomColor);
                    currentCard.setColor(randomColor);
                    // cambia el turno al otro jugador tras asignar color automatico
                    setHumanTurn(!currentPlayer.equals(humanPlayer));
                    deckButton.setDisable(!isHumanTurn);
                }
                break;

            case "+2":
                for (int i = 0; i < 2; i++) {
                    otherPlayer.addCard(deck.takeCard()); // pone a comer al jugador contrario
                }
                // quien juega +2 repite turno
                setHumanTurn(currentPlayer.equals(humanPlayer));
                deckButton.setDisable(!isHumanTurn);
                break;

            case "+4":
                for (int i = 0; i < 4; i++) {
                    otherPlayer.addCard(deck.takeCard());
                }
                if (currentPlayer.equals(humanPlayer)) {
                    setHumanTurn(currentPlayer.equals(humanPlayer));
                    deckButton.setDisable(!isHumanTurn);
                } else {
                    String randomColor = threadPlayMachine.getRandomColorFromHand(); // si es maquina, escoge un color al azar del mazo y lo pone
                    table.setColorOnTheTable(randomColor);
                    // quien juega +4 repite turno
                    setHumanTurn(currentPlayer.equals(humanPlayer));
                    deckButton.setDisable(!isHumanTurn);
                }
                break;

            case "SKIP":
            case "RESERVE":
                setHumanTurn(currentPlayer.equals(humanPlayer)); // vuelve a jugar quien pone la carta
                deckButton.setDisable(!isHumanTurn);
                break;

            default:
                // para cualquier carta normal no especial
                // cambia el turno a oponente
                setHumanTurn(!currentPlayer.equals(humanPlayer));
                deckButton.setDisable(!isHumanTurn);
                break;
        }
    }

    public boolean isSpecial(String value) {
        return "SKIP".equals(value) || "+2".equals(value) || "+4".equals(value) || "RESERVE".equals(value) || "WILD".equals(value);
    }

    public void showColorPicker() {
        colorVBox.setVisible(true);
        colorVBox.setManaged(true);
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Rectangle cardRectangle = card.getCard();
            cardRectangle.setOnMouseClicked((MouseEvent event) -> {
                if (isHumanTurn) return;
            });
        }
    }

    public void hideColorPicker() {
        colorVBox.setVisible(false);
        colorVBox.setManaged(false);
        updateGridPaneMargin();
    }

    public void setHumanTurn(boolean humanTurn) {
        this.isHumanTurn = humanTurn;
        deckButton.setDisable(!humanTurn);
        updateTurnLabel();
    }

    public boolean isHumanTurn() {
        return isHumanTurn;
    }

    public Player getHumanPlayer(){
        return humanPlayer;
    }

    /**
     * Gets the machine player.
     *
     * @return the machine player
     */
    public Player getMachinePlayer(){
        return machinePlayer;
    }

    /**
     * Sets whether the human player can say UNO.
     *
     * @param humanCanSayONE true if the human player can say UNO, false otherwise
     */
    public void setHumanCanSayONE(boolean humanCanSayONE) {
        this.humanCanSayONE = humanCanSayONE;
    }

    /**
     * Sets whether the human player can say UNO to the machine.
     *
     * @param humanCanSayONEToMachine true if the human player can say UNO to the machine, false otherwise
     */
    public void setHumanCanSayONEToMachine(boolean humanCanSayONEToMachine) {
        this.humanCanSayONEToMachine = humanCanSayONEToMachine;
    }

    /**
     * Sets whether the machine has said UNO.
     *
     * @param machineSayOne true if the machine has said UNO, false otherwise
     */
    public void setMachineSayOne(boolean machineSayOne) {
        this.machineSayOne = machineSayOne;
    }

    /**
     * Sets whether the human player can play.
     *
     * @param playHuman true if the human player can play, false otherwise
     */
    public void setPlayHuman(boolean playHuman) {
        this.playHuman = playHuman;
    }

    /**
     * Sets the turn label text.
     *
     * @param text the text to display in the turn label
     */
    public void setTurnLabel(String text) {
        Platform.runLater(() -> {
            this.turnLabel.setText(text);
        });
    }

    /**
     * Gets whether the human player can say UNO.
     *
     * @return true if the human player can say UNO, false otherwise
     */
    public boolean isHumanCanSayONE() {
        return humanCanSayONE;
    }

    /**
     * Gets whether the human player can say UNO to the machine.
     *
     * @return true if the human player can say UNO to the machine, false otherwise
     */
    public boolean isHumanCanSayONEToMachine() {
        return humanCanSayONEToMachine;
    }

    /**
     * Gets whether the machine has said UNO.
     *
     * @return true if the machine has said UNO, false otherwise
     */
    public boolean isMachineSayOne() {
        return machineSayOne;
    }

    /**
     * Gets whether the human player can play.
     *
     * @return true if the human player can play, false otherwise
     */
    public boolean isPlayHuman() {
        return playHuman;
    }

    /**
     * Sets whether the UNO thread is running.
     *
     * @param runningOneThread true if the UNO thread is running, false otherwise
     */
    public void setRunningOneThread(boolean runningOneThread) {
        this.runningOneThread = runningOneThread;
        if (threadSingUNOMachine != null) {
            threadSingUNOMachine.setRunning(runningOneThread);
        }
    }

    /**
     * Sets whether the play machine thread is running.
     *
     * @param runningPlayMachineThread true if the play machine thread is running, false otherwise
     */
    public void setRunningPlayMachineThread(boolean runningPlayMachineThread) {
        this.runningPlayMachineThread = runningPlayMachineThread;
        if (threadPlayMachine != null) {
            threadPlayMachine.setRunning(runningPlayMachineThread);
        }
    }

    /**
     * Gets whether the UNO thread is running.
     *
     * @return true if the UNO thread is running, false otherwise
     */
    public boolean isRunningOneThread() {
        return runningOneThread;
    }

    private void updateTurnLabel() {
        String turn = isHumanTurn ? "humano" : "máquina";
        String color = table.getColorOnTheTable();
        Platform.runLater(() -> {
            turnLabel.setText("Turno: " + turn + " | Color: " + color);
        });
    }

    public void refreshGameView() {
        Platform.runLater(() -> {
            printCardsHumanPlayer();
            printCardsMachinePlayer();
            updateTurnLabel();
        });
    }

    private void applyHoverEffect(ImageView button) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), button);
        shrink.setToX(0.9);
        shrink.setToY(0.9);

        ScaleTransition grow = new ScaleTransition(Duration.millis(150), button);
        grow.setToX(1.0);
        grow.setToY(1.0);

        button.setOnMouseEntered(e -> shrink.playFromStart());
        button.setOnMouseExited(e -> grow.playFromStart());
    }

    private void applyHoverEffectCards(Rectangle card) {
        ScaleTransition shrink = new ScaleTransition(Duration.millis(150), card);
        shrink.setToX(0.9);
        shrink.setToY(0.9);

        ScaleTransition grow = new ScaleTransition(Duration.millis(150), card);
        grow.setToX(1.0);
        grow.setToY(1.0);

        card.setOnMouseEntered(e -> shrink.playFromStart());
        card.setOnMouseExited(e -> grow.playFromStart());
    }

    private void updateGridPaneMargin(){
        colorVBox.visibleProperty().addListener((obs, wasVisible, isNowVisible) -> {
            if (!isNowVisible) {
                HBox.setMargin(gridPaneCardsMachine, new Insets(0, 0, 0, 78));
            } else {
                HBox.setMargin(gridPaneCardsMachine, new Insets(0));
            }
        });
    }

    /**
     * Handles the "Back" button action to show the previous set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleBack(MouseEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            printCardsHumanPlayer();
        }
    }


    /**
     * Handles the "Next" button action to show the next set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleNext(MouseEvent event) {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printCardsHumanPlayer();
        }
    }

    @FXML
    private void onColorSelected(ActionEvent event) { // se activa al darle click a los botones de colores
        Button source = (Button) event.getSource();
        String selectedColor = source.getText().toUpperCase();
        Card currentCard = table.getCurrentCardOnTheTable();

        table.setColorOnTheTable(selectedColor); //cambia el color en la mesa y en todo el juego
        currentCard.setColor(selectedColor);
        hideColorPicker();

        if ("WILD".equals(card.getValue())) {
            // tras elegir color, el turno pasa a la maquina
            setHumanTurn(false);
            deckButton.setDisable(true);
        }

        refreshGameView();
    }

    /**
     * Handles the action of taking a card.
     *
     * @param event the action event
     */
    @FXML
    void onHandleTakeCard(MouseEvent event) {
        if (!isHumanTurn) return; // solo si es turno humano
        if (deckButton.isDisable()) return; // ya comio

        Card drawCard = deck.takeCard();
        humanPlayer.addCard(drawCard);
        printCardsHumanPlayer();

        deckButton.setDisable(true); // solo puede robar 1 carta por turno
        setHumanTurn(false);
        refreshGameView();
    }

    @FXML
    private void handleExitClick(MouseEvent event) {
        Stage currentStage = (Stage) ((ImageView) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    /**
     * Handles the action of saying "Uno".
     *
     * @param event the mouse event
     */
    @FXML
    void onHandleUno(MouseEvent event) {
        if (humanPlayer.getCardsPlayer().size() == 1 && humanCanSayONE) {
            setTurnLabel("¡UNO!");
            setHumanCanSayONE(false);
            setMachineSayOne(false);
        } else if (machinePlayer.getCardsPlayer().size() == 1 && humanCanSayONEToMachine) {
            setTurnLabel("¡UNO!");
            setHumanCanSayONEToMachine(false);
            setMachineSayOne(false);
        } else {
            setTurnLabel("Cannot say UNO at this time");
        }
    }
}
