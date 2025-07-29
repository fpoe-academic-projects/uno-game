
package org.example.unogame.controller;

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

/**
 * Controller class for the Uno game.
 */
public class GameUnoController {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    private GridPane gridPaneCardsPlayer;

    @FXML
    private ImageView tableImageView;

    @FXML
    private ImageView deckButton;

    @FXML
    private VBox colorVBox;

    @FXML
    public Label turnLabel;

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
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                if (!isHumanTurn) return; // solo si es su turno

                if (canPlayCard(card, table)) { // verifica si puede jugar la carta
                    gameUno.playCard(card);
                    tableImageView.setImage(card.getImage());
                    humanPlayer.removeCard(findPosCardsHumanPlayer(card));

                    // Check if human player just played their last card
                    if (humanPlayer.getCardsPlayer().size() == 0) {
                        // Human player won - this will be handled by ThreadWinGame
                    } else if (humanPlayer.getCardsPlayer().size() == 1) {
                        // Human player has 1 card left - enable UNO calling
                        setHumanCanSayONE(true);
                        setMachineCanSayOneToPlayer(true);
                    }

                    if (isSpecial(card.getValue())) { // verifica si es una carta especial
                        specialCard(card, humanPlayer, machinePlayer);
                    } else {
                        // carta normal -> turno maquina
                        setHumanTurn(false);
                    }
                    refreshGameView();
                }
            });

            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }

    private void printCardsMachinePlayer() {
        gridPaneCardsMachine.getChildren().clear();

        Card[] currentVisibleCardsMachinePlayer = gameUno.getCurrentVisibleCardsMachinePlayer(posInitCardToShow);

        // Add safety check for null or empty array
        if (currentVisibleCardsMachinePlayer == null || currentVisibleCardsMachinePlayer.length == 0) {
            return;
        }

        for (int i = 0; i < currentVisibleCardsMachinePlayer.length; i++) {
            ImageView backCard = Card.getBackCardImageView();
            gridPaneCardsMachine.add(backCard, i, 0);
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
                // Verificar si hay suficientes cartas en el mazo antes de aplicar +2
                if (deck.isEmpty()) {
                    System.out.println("No hay suficientes cartas en el mazo para aplicar +2");
                    setHumanTurn(!currentPlayer.equals(humanPlayer));
                    deckButton.setDisable(!isHumanTurn);
                    return;
                }
                
                for (int i = 0; i < 2; i++) {
                    if (!deck.isEmpty()) {
                        otherPlayer.addCard(deck.takeCard()); // pone a comer al jugador contrario
                        System.out.println(otherPlayer.equals(humanPlayer) + " comió");
                    } else {
                        System.out.println("No hay más cartas en el mazo para completar +2");
                        break;
                    }
                }
                // quien juega +2 repite turno
                setHumanTurn(currentPlayer.equals(humanPlayer));
                deckButton.setDisable(!isHumanTurn);
                break;

            case "+4":
                // Verificar si hay suficientes cartas en el mazo antes de aplicar +4
                if (deck.isEmpty()) {
                    System.out.println("No hay suficientes cartas en el mazo para aplicar +4");
                    setHumanTurn(!currentPlayer.equals(humanPlayer));
                    deckButton.setDisable(!isHumanTurn);
                    return;
                }
                
                for (int i = 0; i < 4; i++) {
                    if (!deck.isEmpty()) {
                        otherPlayer.addCard(deck.takeCard());
                        System.out.println(otherPlayer.equals(humanPlayer) + " comió");
                    } else {
                        System.out.println("No hay más cartas en el mazo para completar +4");
                        break;
                    }
                }
                if (currentPlayer.equals(humanPlayer)) {
                    this.card = card;
                    showColorPicker(); // usuario elige color de la carta +4
                    deckButton.setDisable(true);
                } else {
                    String randomColor = threadPlayMachine.getRandomColorFromHand(); // si es maquina, escoge un color al azar del mazo y lo pone
                    table.setColorOnTheTable(randomColor);
                    currentCard.setColor(randomColor);
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
    }

    public void hideColorPicker() {
        colorVBox.setVisible(false);
        colorVBox.setManaged(false);
    }

    public void updateCardsMachinePlayer() {
        printCardsMachinePlayer();
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

    /**
     * Applies the UNO penalty by making the player draw 1 card.
     *
     * @param player the player who must draw a card
     */
    public void applyUnoPenalty(Player player) {
        Platform.runLater(() -> {
            // Verificar si hay cartas en el mazo antes de aplicar la penalización
            if (deck.isEmpty()) {
                System.out.println("No hay cartas en el mazo para aplicar penalización de UNO");
                setTurnLabel("No hay cartas en el mazo para aplicar penalización");
                refreshGameView();
                return;
            }
            
            Card penaltyCard = deck.takeCard();
            player.addCard(penaltyCard);
            
            if (player.equals(humanPlayer)) {
                setTurnLabel("El jugador robó 1 carta por no cantar UNO");
            } else {
                setTurnLabel("La máquina robó 1 carta por no cantar UNO");
            }
            
            refreshGameView();
        });
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
            updateCardsMachinePlayer();
            updateTurnLabel();
            
            // Check if machine has 1 card and enable human to call UNO
            if (machinePlayer.getCardsPlayer().size() == 1 && !machineSayOne) {
                setHumanCanSayONEToMachine(true);
            }
            
            // Check if human has 1 card and enable machine to call UNO
            if (humanPlayer.getCardsPlayer().size() == 1 && !machineSayOne) {
                setMachineCanSayOneToPlayer(true);
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

        // Verificar si hay cartas en el mazo antes de intentar tomar una
        if (deck.isEmpty()) {
            System.out.println("No hay más cartas en el mazo para robar");
            setTurnLabel("No hay más cartas en el mazo");
            refreshGameView();
            return;
        }

        Card drawCard = deck.takeCard();
        humanPlayer.addCard(drawCard);
        printCardsHumanPlayer();

        deckButton.setDisable(true); // solo puede robar 1 carta por turno

        if (canPlayCard(drawCard, table)) {
            System.out.println("¡Puedes jugar la carta que acabas de robar! Haz click para jugarla.");
        } else {
            // no puede jugar carta comida, pasa turno a maquina
            setHumanTurn(false);
            System.out.println("No puedes jugar la carta robada. Turno de máquina.");
        }

        refreshGameView();
    }

    @FXML
    private void handleExitClick(MouseEvent event) {
        // Cerramos la ventana actual
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
            setTurnLabel("El jugador cantó UNO para defenderse");
            setHumanCanSayONE(false);
            setMachineSayOne(false);
        } else if (machinePlayer.getCardsPlayer().size() == 1 && humanCanSayONEToMachine) {
            setTurnLabel("El jugador cantó UNO a la máquina");
            setHumanCanSayONEToMachine(false);
            setMachineSayOne(false);
            
            // Apply penalty: machine must draw 1 card
            applyUnoPenalty(machinePlayer);
        } else {
            setTurnLabel("No se puede cantar UNO en este momento");
        }
    }

    /**
     * Sets whether the machine can call UNO to the player.
     *
     * @param machineCanSayOneToPlayer true if the machine can call UNO to the player, false otherwise
     */
    public void setMachineCanSayOneToPlayer(boolean machineCanSayOneToPlayer) {
        if (threadSingUNOMachine != null) {
            threadSingUNOMachine.setMachineCanSayOneToPlayer(machineCanSayOneToPlayer);
        }
    }
}
