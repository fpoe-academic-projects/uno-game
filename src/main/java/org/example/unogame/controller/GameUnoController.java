package org.example.unogame.controller;

import javafx.geometry.Insets;
import javafx.scene.image.Image;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.fileHandlers.ISerializableFileHandler;
import org.example.unogame.model.fileHandlers.SerializableFileHandler;
import org.example.unogame.model.game.GameUno;
import org.example.unogame.model.machine.ThreadPlayMachine;
import org.example.unogame.model.machine.ThreadSingUNOMachine;
import org.example.unogame.model.machine.ThreadWinGame;
import org.example.unogame.model.player.Player;
import org.example.unogame.model.table.Table;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;


import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Uno game screen. It coordinates user interactions,
 * machine actions, game rules enforcement, and UI updates.
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
    private IAnimations animations;
    private volatile boolean isHumanTurn;
    private volatile boolean waitingForColor = false;

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

    private static final String SAVE_FILE_PATH = "uno_saved_game.ser";
    private final ISerializableFileHandler fileHandler = new SerializableFileHandler();

    /**
     * Initializes the controller after FXML loading. Sets up the game model,
     * applies UI effects, displays the initial table card, and starts worker threads.
     *
     * @throws GameException if initialization or game start fails
     */
    @FXML
    public void initialize() throws GameException {
        // Apply hover effects to interactive UI elements
        if (animations != null) {
            animations.applyHoverEffect(exitButton);
            animations.applyHoverEffect(deckButton);
            animations.applyHoverEffect(nextButton);
            animations.applyHoverEffect(backButton);
            animations.applyHoverEffect(unoButton);
        }
    }

    /**
     * Sets the animation handler used for visual effects during gameplay.
     *
     * @param animations an implementation of {@link IAnimations} to manage game animations
     */
    public void setAnimations(IAnimations animations) {
        this.animations = animations;
    }

    /**
     * Initializes the game session based on the provided {@link GameUno} object.
     * <p>
     * If the given game instance is {@code null}, a new game is created; otherwise,
     * the previously saved game state is loaded.
     * </p>
     *
     * @param game the game instance to initialize, or {@code null} to start a new game
     * @throws GameException if there is an error during initialization
     */
    public void initmatch(GameUno game) throws GameException {
        initialize();
        if (game == null) {
            newGame();
        } else {
            loadGameState();
        }
    }

    /**
     * Starts a new game session by initializing all game components,
     * starting background threads, and updating the visual UI.
     *
     * @throws GameException if there is an error retrieving the initial table card or during setup
     */
    public void newGame() throws GameException {
        initVariables();
        this.gameUno.startGame();
        updateGridPaneMargin();

        // Display the initial card on the table
        tableImageView.setImage(this.table.getCurrentCardOnTheTable().getImage());
        refreshGameView();

        // Start machine behavior thread
        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.tableImageView, this, this.deck);
        threadPlayMachine.start();

        // Start UNO rule monitoring thread
        threadSingUNOMachine = new ThreadSingUNOMachine(
                this.humanPlayer.getCardsPlayer(),
                this.machinePlayer.getCardsPlayer(),
                this,
                this.threadPlayMachine
        );
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();

        // Start win condition monitoring thread
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
     * Prepares game entities and default controller state before starting the match.
     *
     * @throws GameException if any model component fails to initialize
     */
    private void initVariables() throws GameException {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
        this.isHumanTurn = true;
        this.animations = new AnimationsAdapter();
    }

    /**
     * Renders the human player's visible cards and wires click handlers
     * that attempt to play the selected card when valid.
     *
     * @throws GameException.InvalidCardIndex if a card index access is invalid
     * @throws GameException.EmptyTableException if there is no card on the table
     */
    private void printCardsHumanPlayer() throws GameException.InvalidCardIndex, GameException.EmptyTableException {
        this.gridPaneCardsPlayer.getChildren().clear();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);
        updateTurnLabel();

        // Defensive: nothing to draw
        if (currentVisibleCardsHumanPlayer == null || currentVisibleCardsHumanPlayer.length == 0) {
            return;
        }

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Card card = currentVisibleCardsHumanPlayer[i];
            Rectangle cardRectangle = card.getCard();
            animations.applyHoverEffect(cardRectangle);

            cardRectangle.setOnMouseClicked((MouseEvent event) -> {
                // Only act on the human turn and when no color selection is pending
                if (!isHumanTurn) return;
                try {
                    if (!canPlayCard(card, table)) return;
                } catch (GameException.EmptyTableException e) {
                    throw new RuntimeException(e);
                }
                if (waitingForColor) return;

                try {
                    if (canPlayCard(card, table)) {
                        try {
                            gameUno.playCard(card);
                        } catch (GameException.NullCardException e) {
                            throw new RuntimeException(e);
                        }
                        tableImageView.setImage(card.getImage());
                        animations.cardAnimation(tableImageView);
                        try {
                            humanPlayer.removeCard(findPosCardsHumanPlayer(card));
                        } catch (GameException.InvalidCardIndex e) {
                            throw new RuntimeException(e);
                        }

                        // Handle special vs. regular cards and turn progression
                        if (isSpecial(card.getValue())) {
                            try {
                                specialCard(card, humanPlayer, machinePlayer);
                            } catch (GameException.EmptyTableException | GameException.IllegalCardColor
                                     | GameException.OutOfCardsInDeck | GameException.NullCardException
                                     | GameException.InvalidCardIndex e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            try {
                                setHumanTurn(false);
                            } catch (GameException.EmptyTableException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        refreshGameView();
                    }
                } catch (GameException.EmptyTableException e) {
                    throw new RuntimeException(e);
                }
            });

            this.gridPaneCardsPlayer.add(cardRectangle, i, 0);
        }
    }

    /**
     * Renders the machine player's hand as hidden back-cards (up to four),
     * with a "+N" badge if there are more cards.
     */
    private void printCardsMachinePlayer() {
        this.gridPaneCardsMachine.getChildren().clear();

        List<Card> safeCopy;
        synchronized (machinePlayer) {
            safeCopy = new ArrayList<>(machinePlayer.getCardsPlayer());
        }

        // Defensive: nothing to draw
        if (safeCopy == null || safeCopy.size() == 0) {
            return;
        }

        int maxVisibleCards = 4;
        int cardsToShow = Math.min(safeCopy.size(), maxVisibleCards);

        // Show up to four back-card placeholders
        for (int i = 0; i < cardsToShow; i++) {
            Rectangle backCard = Card.getBackCardRectangle();
            animations.applyHoverEffect(backCard);
            this.gridPaneCardsMachine.add(backCard, i, 0);
        }

        // If more than four, add a count overlay to the last visible slot
        if (safeCopy.size() > maxVisibleCards) {
            int remaining = safeCopy.size() - maxVisibleCards;
            Label plusLabel = new Label("+" + remaining);
            plusLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.8); -fx-padding: 5px;");
            this.gridPaneCardsMachine.add(plusLabel, maxVisibleCards - 1, 0);
        }
    }

    /**
     * Finds the index of the given card in the human player's hand.
     *
     * @param card the card to look for
     * @return the index of the card, or -1 if it is not present
     */
    private Integer findPosCardsHumanPlayer(Card card) {
        for (int i = 0; i < this.humanPlayer.getCardsPlayer().size(); i++) {
            if (this.humanPlayer.getCardsPlayer().get(i).equals(card)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determines if the provided card can be legally placed on the table
     * over the current top card according to Uno rules.
     *
     * @param cardPlay the card the player wants to play
     * @param table the table state reference
     * @return true if the move is valid; false otherwise
     * @throws GameException.EmptyTableException if no card is currently on the table
     */
    public boolean canPlayCard(Card cardPlay, Table table) throws GameException.EmptyTableException {
        Card currentCard = table.getCurrentCardOnTheTable();

        String colorToPlay = cardPlay.getColor();
        String valueToPlay = cardPlay.getValue();

        String colorOnTable = currentCard.getColor();
        String valueOnTable = currentCard.getValue();

        if (waitingForColor)
            return false;

        // Wild and +4 are always playable
        if ("WILD".equals(valueToPlay) || "+4".equals(valueToPlay)) {
            return true;
        }

        // After a +4, the same player may play any card
        if ("+4".equals(valueOnTable)) {
            return true;
        }

        // Match by color
        if (colorToPlay != null && colorOnTable != null && colorToPlay.equals(colorOnTable)) {
            return true;
        }

        // Match by value
        if (valueToPlay != null && valueOnTable != null && valueToPlay.equals(valueOnTable)) {
            return true;
        }

        refreshGameView();
        return false;
    }

    /**
     * Saves the current game state to a file using serialization.
     * <p>
     * If the save operation fails, an error message is printed to {@code stderr}.
     * </p>
     */
    public void saveGame() {
        SerializableFileHandler fileHandler = new SerializableFileHandler();
        try {
            fileHandler.serialize(SAVE_FILE_PATH, gameUno);
            System.out.println("Juego guardado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al guardar el juego: " + e.getMessage());
        }
    }

    /**
     * Loads a previously saved game state from file and reinitializes
     * all necessary components including players, table, deck, and threads.
     * <p>
     * This method also updates the UI and starts necessary background threads
     * after loading the game state.
     * </p>
     */
    public void loadGameState() {
        try {
            SerializableFileHandler handler = new SerializableFileHandler();
            GameUno loadedGame = (GameUno) handler.deserialize(SAVE_FILE_PATH);
            System.out.println("Juego cargado correctamente.");

            // Restore game state
            this.humanPlayer = loadedGame.getHumanPlayer();
            this.machinePlayer = loadedGame.getMachinePlayer();
            this.table = loadedGame.getTable();
            this.deck = loadedGame.getDeck();
            this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);

            // Reinitialize threads with updated references
            this.threadPlayMachine = new ThreadPlayMachine(
                    this.table,
                    this.machinePlayer,
                    this.tableImageView,
                    this,
                    this.deck
            );

            this.threadSingUNOMachine = new ThreadSingUNOMachine(
                    this.humanPlayer.getCardsPlayer(),
                    this.machinePlayer.getCardsPlayer(),
                    this,
                    this.threadPlayMachine
            );

            this.threadWinGame = new ThreadWinGame(this.humanPlayer, this.machinePlayer, this.deck, this);
            this.threadWinGame.init(this);

            // Start background threads
            new Thread(threadPlayMachine, "ThreadPlayMachine").start();
            new Thread(threadSingUNOMachine, "ThreadSingUNOMachine").start();
            new Thread(threadWinGame, "ThreadWinGame").start();

            updateGameUI();

        } catch (IOException | ClassNotFoundException | GameException.ThreadInitializationException e) {
            System.err.println("Error al cargar el estado del juego: " + e.getMessage());
        }
    }

    /**
     * Updates the game user interface based on the current game state.
     * <p>
     * This includes refreshing the human player's hand, the back of the machine's cards,
     * and the top card on the table. Exceptions are handled if the table is empty.
     * </p>
     */
    private void updateGameUI() {
        displayPlayerCards();
        updateMachineCardBack();

        try {
            Card topCard = table.getCurrentCardOnTheTable();
            updatePlayedCard(topCard);
            System.out.println("la carta es: " + topCard.getValue());
            for (Card c : humanPlayer.getCardsPlayer()) {
                System.out.println("Valor " + c.getValue());
            }
        } catch (GameException.EmptyTableException e) {
            System.err.println("No hay cartas sobre la mesa para mostrar.");
        }
    }

    /**
     * Sets up a hook to automatically save the game when the application window is closed.
     *
     * @param stage the primary stage of the JavaFX application
     */
    public void setupAutoSaveOnClose(Stage stage) {
        stage.setOnCloseRequest(event -> {
            saveGame();
            System.out.println("Juego guardado automaticamente al cerrar.");
        });
    }

    /**
     * Applies the effect of a special card and manages turn flow accordingly.
     *
     * @param card the played special card
     * @param currentPlayer the player who played the card
     * @param otherPlayer the opponent player
     * @throws GameException.EmptyTableException if the table is empty
     * @throws GameException.IllegalCardColor if an invalid color is applied
     * @throws GameException.OutOfCardsInDeck if the deck runs out of cards
     * @throws GameException.NullCardException if a null card is processed unexpectedly
     * @throws GameException.InvalidCardIndex if a hand index is invalid
     */
    public void specialCard(Card card, Player currentPlayer, Player otherPlayer)
            throws GameException.EmptyTableException, GameException.IllegalCardColor, GameException.OutOfCardsInDeck,
            GameException.NullCardException, GameException.InvalidCardIndex {

        String value = card.getValue();
        Card currentCard = table.getCurrentCardOnTheTable();

        switch (value) {
            case "WILD":
                if (currentPlayer.equals(humanPlayer)) {
                    this.card = card;
                    deckButton.setDisable(true);
                    setWaitingForColor(true);
                    setHumanTurn(true); // keep turn until the user chooses a color
                    showColorPicker();
                } else {
                    String randomColor = threadPlayMachine.getRandomColorFromHand();
                    table.setColorOnTheTable(randomColor);
                    currentCard.setColor(randomColor);
                    // pass turn to the opponent after auto color selection
                    setHumanTurn(!currentPlayer.equals(humanPlayer));
                    deckButton.setDisable(!isHumanTurn);
                }
                break;

            case "+2":
                for (int i = 0; i < 2; i++) {
                    otherPlayer.addCard(deck.takeCard());
                }
                // the player who plays +2 takes another turn
                setHumanTurn(currentPlayer.equals(humanPlayer));
                deckButton.setDisable(!isHumanTurn);
                break;

            case "+4":
                for (int i = 0; i < 4; i++) {
                    otherPlayer.addCard(deck.takeCard());
                }
                if (currentPlayer.equals(humanPlayer)) {
                    // the player who plays +4 takes another turn; color will be chosen via UI
                    setHumanTurn(currentPlayer.equals(humanPlayer));
                    deckButton.setDisable(!isHumanTurn);
                } else {
                    String randomColor = threadPlayMachine.getRandomColorFromHand();
                    table.setColorOnTheTable(randomColor);
                    // the player who plays +4 takes another turn
                    setHumanTurn(currentPlayer.equals(humanPlayer));
                    deckButton.setDisable(!isHumanTurn);
                }
                break;

            case "SKIP":
            case "RESERVE":
                // the player who plays SKIP/RESERVE takes another turn
                setHumanTurn(currentPlayer.equals(humanPlayer));
                deckButton.setDisable(!isHumanTurn);
                break;

            default:
                // normal card: pass turn to the opponent
                setHumanTurn(!currentPlayer.equals(humanPlayer));
                deckButton.setDisable(!isHumanTurn);
                break;
        }
    }

    /**
     * Indicates whether the provided value corresponds to a special card.
     *
     * @param value the card face value
     * @return true if the value matches a special card; false otherwise
     */
    public boolean isSpecial(String value) {
        return "SKIP".equals(value) || "+2".equals(value) || "+4".equals(value) || "RESERVE".equals(value) || "WILD".equals(value);
    }

    /**
     * Shows the color selection panel and prevents card plays until a color is chosen.
     *
     * @throws GameException.InvalidCardIndex if an invalid index is accessed while wiring events
     */
    public void showColorPicker() throws GameException.InvalidCardIndex {
        colorVBox.setVisible(true);
        colorVBox.setManaged(true);
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            Rectangle cardRectangle = card.getCard();
            cardRectangle.setOnMouseClicked((MouseEvent event) -> {
                // While choosing a color, do not allow further human plays
                if (isHumanTurn) return;
            });
        }
    }

    /**
     * Hides the color selection panel and restores layout spacing.
     */
    public void hideColorPicker() {
        colorVBox.setVisible(false);
        colorVBox.setManaged(false);
        updateGridPaneMargin();
    }

    /**
     * Updates the internal and UI state to reflect whose turn it is.
     *
     * @param humanTurn true if it is the human's turn; false for the machine
     * @throws GameException.EmptyTableException if updating the label requires the current table color and it is missing
     */
    public void setHumanTurn(boolean humanTurn) throws GameException.EmptyTableException {
        this.isHumanTurn = humanTurn;
        deckButton.setDisable(!humanTurn);
        updateTurnLabel();
    }

    /**
     * @return true if it is currently the human's turn; false otherwise
     */
    public boolean isHumanTurn() {
        return isHumanTurn;
    }

    /**
     * Sets whether the controller is waiting for a color selection
     * (e.g., after playing a WILD).
     *
     * @param waiting true to block plays until a color is selected
     */
    public void setWaitingForColor(boolean waiting) {
        this.waitingForColor = waiting;
    }

    /**
     * @return the human player reference
     */
    public Player getHumanPlayer(){
        return humanPlayer;
    }

    /**
     * @return the machine player reference
     */
    public Player getMachinePlayer(){
        return machinePlayer;
    }

    /**
     * Configures if the human is allowed to call "UNO".
     *
     * @param humanCanSayONE true to allow; false to block
     */
    public void setHumanCanSayONE(boolean humanCanSayONE) {
        this.humanCanSayONE = humanCanSayONE;
    }

    /**
     * Configures if the human is allowed to call "UNO" on the machine.
     *
     * @param humanCanSayONEToMachine true to allow; false to block
     */
    public void setHumanCanSayONEToMachine(boolean humanCanSayONEToMachine) {
        this.humanCanSayONEToMachine = humanCanSayONEToMachine;
    }

    /**
     * Records whether the machine has already called "UNO".
     *
     * @param machineSayOne true if already called; false otherwise
     */
    public void setMachineSayOne(boolean machineSayOne) {
        this.machineSayOne = machineSayOne;
    }

    /**
     * Enables or disables the human's ability to play.
     *
     * @param playHuman true to allow human to play; false to restrict
     */
    public void setPlayHuman(boolean playHuman) {
        this.playHuman = playHuman;
    }

    /**
     * Sets the text of the turn label in a thread-safe manner.
     *
     * @param text the text to display
     */
    public void setTurnLabel(String text) {
        Platform.runLater(() -> {
            this.turnLabel.setText(text);
        });
    }

    /**
     * @return true if the human is allowed to call "UNO"; false otherwise
     */
    public boolean isHumanCanSayONE() {
        return humanCanSayONE;
    }

    /**
     * @return true if the human can call "UNO" on the machine; false otherwise
     */
    public boolean isHumanCanSayONEToMachine() {
        return humanCanSayONEToMachine;
    }

    /**
     * @return true if the machine has called "UNO"; false otherwise
     */
    public boolean isMachineSayOne() {
        return machineSayOne;
    }

    /**
     * @return true if the human is currently allowed to play; false otherwise
     */
    public boolean isPlayHuman() {
        return playHuman;
    }

    /**
     * Enables or disables the UNO monitoring thread and forwards the state to it if present.
     *
     * @param runningOneThread true to keep it running; false to stop
     */
    public void setRunningOneThread(boolean runningOneThread) {
        this.runningOneThread = runningOneThread;
        if (threadSingUNOMachine != null) {
            threadSingUNOMachine.setRunning(runningOneThread);
        }
    }

    /**
     * Enables or disables the machine play thread and forwards the state to it if present.
     *
     * @param runningPlayMachineThread true to keep it running; false to stop
     */
    public void setRunningPlayMachineThread(boolean runningPlayMachineThread) {
        this.runningPlayMachineThread = runningPlayMachineThread;
        if (threadPlayMachine != null) {
            threadPlayMachine.setRunning(runningPlayMachineThread);
        }
    }

    /**
     * @return true if the UNO monitoring thread is marked as running; false otherwise
     */
    public boolean isRunningOneThread() {
        return runningOneThread;
    }

    /**
     * Updates the "turn" label with the current player and color on the table.
     *
     * @throws GameException.EmptyTableException if the table color cannot be obtained
     */
    private void updateTurnLabel() throws GameException.EmptyTableException {
        String turn = isHumanTurn ? "humano" : "maquina";
        String color = table.getColorOnTheTable();
        Platform.runLater(() -> {
            turnLabel.setText("Turno: " + turn + " | Color: " + color);
        });
    }

    /**
     * Schedules a UI refresh of both hands and the turn label on the JavaFX thread.
     */
    public void refreshGameView() {
        Platform.runLater(() -> {
            try {
                printCardsHumanPlayer();
            } catch (GameException.InvalidCardIndex | GameException.EmptyTableException e) {
                throw new RuntimeException(e);
            }
            printCardsMachinePlayer();
            try {
                updateTurnLabel();
            } catch (GameException.EmptyTableException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * Adjusts margins dynamically depending on whether the color picker is visible.
     * This keeps the layout compact when the color picker is hidden.
     */
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
     * Shows the previous group of human cards in the carousel-like navigation.
     *
     * @param event the mouse event
     * @throws GameException.InvalidCardIndex if an invalid card index is accessed while redrawing
     * @throws GameException.EmptyTableException if the table is unexpectedly empty
     */
    @FXML
    void onHandleBack(MouseEvent event) throws GameException.InvalidCardIndex, GameException.EmptyTableException {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            printCardsHumanPlayer();
        }
    }

    /**
     * Shows the next group of human cards in the carousel-like navigation.
     *
     * @param event the mouse event
     * @throws GameException.InvalidCardIndex if an invalid card index is accessed while redrawing
     * @throws GameException.EmptyTableException if the table is unexpectedly empty
     */
    @FXML
    void onHandleNext(MouseEvent event) throws GameException.InvalidCardIndex, GameException.EmptyTableException {
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printCardsHumanPlayer();
        }
    }

    /**
     * Handles a color selection triggered by the color buttons.
     * Applies the chosen color to the current table card and advances the turn.
     *
     * @param event the action event from the color button
     * @throws GameException.EmptyTableException if the table is unexpectedly empty
     * @throws GameException.IllegalCardColor if the selected color is invalid
     */
    @FXML
    private void onColorSelected(ActionEvent event) throws GameException.EmptyTableException, GameException.IllegalCardColor {
        Button source = (Button) event.getSource();
        String selectedColor = source.getText().toUpperCase();
        Card currentCard = table.getCurrentCardOnTheTable();

        // Update color on the table and on the current card
        table.setColorOnTheTable(selectedColor);
        currentCard.setColor(selectedColor);
        hideColorPicker();
        setWaitingForColor(false);
        setHumanTurn(false);

        if ("WILD".equals(card.getValue())) {
            deckButton.setDisable(true);
        }

        refreshGameView();
    }

    /**
     * Handles the "take card" action. The human draws one card at most per turn.
     * If the deck is empty, discards are recycled back into the deck.
     *
     * @param event the mouse event
     * @throws GameException.IllegalCardColor if an illegal color operation occurs
     * @throws GameException.OutOfCardsInDeck if the deck unexpectedly has no cards to draw
     * @throws GameException.NullCardException if a null card is encountered
     * @throws GameException.InvalidCardIndex if a hand index is invalid
     * @throws GameException.EmptyTableException if the table is unexpectedly empty
     */
    @FXML
    void onHandleTakeCard(MouseEvent event) throws GameException.IllegalCardColor, GameException.OutOfCardsInDeck, GameException.NullCardException, GameException.InvalidCardIndex, GameException.EmptyTableException {
        // Only allow if it's the human's turn, not disabled, and not waiting for a wild color
        if (!isHumanTurn) return;
        if (deckButton.isDisable()) return;
        if (waitingForColor) return;

        if (deck.isEmpty()) {
            // Recycle discards back into the deck; wild/+4 reset to black when requested
            List<Card> discards = table.collectDiscardsExceptTop(true); // true = resets wild/ +4 to black
            deck.reloadFrom(discards);
        }

        Card drawCard = deck.takeCard();
        humanPlayer.addCard(drawCard);
        printCardsHumanPlayer();

        if (!gridPaneCardsPlayer.getChildren().isEmpty()) {
            Node last = gridPaneCardsPlayer.getChildren()
                    .get(gridPaneCardsPlayer.getChildren().size() - 1);
            animations.cardAnimation(last);
        }

        // Only one draw per turn; then pass the turn
        deckButton.setDisable(true);
        setHumanTurn(false);
        refreshGameView();
    }

    /**
     * Closes the current window.
     *
     * @param event the mouse event from the exit icon
     */
    @FXML
    private void handleExitClick(MouseEvent event) {
        saveGame();
        System.out.println("Juego guardado automaticamente al cerrar.");
        Stage currentStage = (Stage) ((ImageView) event.getSource()).getScene().getWindow();
        currentStage.close();
    }

    /**
     * Handles the "UNO" action. Validates whether "UNO" can be called by the human
     * or against the machine, and updates the label accordingly.
     *
     * @param event the mouse event from the UNO button
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

    /**
     * Displays all the cards in the human player's hand on the game board.
     * <p>
     * This method clears the current card grid and repopulates it with
     * {@link ImageView} nodes representing each card.
     * </p>
     * <p><b>Note:</b> Card values and image paths are printed to the console for debugging purposes.</p>
     */
    private void displayPlayerCards() {
        for (Card c : humanPlayer.getCardsPlayer()) {
            System.out.println("Valor " + c.getValue());
        }

        gridPaneCardsPlayer.getChildren().clear();
        List<Card> playerCards = humanPlayer.getCardsPlayer();

        for (int i = 0; i < playerCards.size(); i++) {
            Card card = playerCards.get(i);
            Image image = new Image(card.getImagePath());
            System.out.println("ruta de la imagen: " + card.getImagePath());

            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(120);
            imageView.setFitWidth(80);

            gridPaneCardsPlayer.add(imageView, i, 0);
        }
    }

    /**
     * Updates the table to display the most recently played card.
     *
     * @param topCard the card currently on top of the discard pile; if {@code null}, no update is performed
     */
    private void updatePlayedCard(Card topCard) {
        if (topCard != null) {
            tableImageView.setImage(
                    new Image(getClass().getResourceAsStream("/org/example/unogame/cards-uno/" + topCard.getImageName()))
            );
        }
    }

    /**
     * Displays the back of the machine player's cards on the board.
     * <p>
     * This method visually represents the machine's hand by adding card backs
     * to the corresponding {@link GridPane}, hiding the actual card faces.
     * </p>
     */
    private void updateMachineCardBack() {
        gridPaneCardsMachine.getChildren().clear();
        int cardCount = machinePlayer.getCardsPlayer().size();

        for (int i = 0; i < cardCount; i++) {
            ImageView backView = new ImageView(
                    new Image(getClass().getResourceAsStream("/org/example/unogame/cards-uno/back.png"))
            );
            backView.setFitWidth(80);
            backView.setFitHeight(120);
            gridPaneCardsMachine.add(backView, i, 0);
        }
    }
}
