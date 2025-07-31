package org.example.unogame.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.example.unogame.model.card.Card;
import org.example.unogame.model.deck.Deck;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.fileHanldlers.ISerializableFileHandler;
import org.example.unogame.model.fileHanldlers.SerializableFileHandler;
import org.example.unogame.model.game.GameUno;
import org.example.unogame.model.machine.ThreadPlayMachine;
import org.example.unogame.model.machine.ThreadSingUNOMachine;
import org.example.unogame.model.machine.ThreadWinGame;
import org.example.unogame.model.machine.observers.observable;
import org.example.unogame.model.machine.observers.observableClass;
import org.example.unogame.model.player.Player;
import org.example.unogame.model.table.Table;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controller for the Uno game screen. It coordinates user interactions,
 * machine actions, game rules enforcement, and UI updates.
 * 
 * <p>This controller manages the complete game flow including:</p>
 * <ul>
 *   <li>Player turn management</li>
 *   <li>Card validation and play logic</li>
 *   <li>Special card effects (WILD, +2, +4, SKIP, RESERVE)</li>
 *   <li>UNO calling mechanics with timers</li>
 *   <li>Game state persistence</li>
 *   <li>Multi-threaded machine player behavior</li>
 *   <li>Observer pattern for game events</li>
 * </ul>
 * 
 * <p>The controller uses multiple threads to handle concurrent game events:</p>
 * <ul>
 *   <li>ThreadPlayMachine: Handles machine player decisions</li>
 *   <li>ThreadSingUNOMachine: Monitors UNO calling opportunities</li>
 *   <li>ThreadWinGame: Checks for win conditions</li>
 * </ul>
 * 
 * @author Uno Game Team
 * @version 1.0
 * @since 2024
 */
public class GameUnoController {

    /** Grid pane for displaying machine player's cards */
    @FXML private GridPane gridPaneCardsMachine;
    
    /** Grid pane for displaying human player's cards */
    @FXML private GridPane gridPaneCardsPlayer;
    
    /** Image view for the current card on the table */
    @FXML private ImageView tableImageView;
    
    /** Vertical box containing color selection buttons */
    @FXML private VBox colorVBox;
    
    /** Exit button image view */
    @FXML private ImageView exitButton;
    
    /** Deck button image view for drawing cards */
    @FXML private ImageView deckButton;
    
    /** Next button image view for card navigation */
    @FXML private ImageView nextButton;
    
    /** Back button image view for card navigation */
    @FXML private ImageView backButton;
    
    /** UNO button image view for calling UNO */
    @FXML private ImageView unoButton;

    /** Label displaying current turn and table color information */
    @FXML public Label turnLabel;

    /** The human player instance */
    private Player humanPlayer;
    
    /** The machine player instance */
    private Player machinePlayer;
    
    /** The game deck containing all cards */
    private Deck deck;
    
    /** The game table where cards are played */
    private Table table;
    
    /** The main game logic controller */
    private GameUno gameUno;
    
    /** Temporary card reference for special operations */
    private Card card;
    
    /** Starting position for displaying human player cards in carousel view */
    private int posInitCardToShow;
    
    /** Animation controller for UI effects */
    private IAnimations animations;
    
    /** Flag indicating if it's currently the human player's turn */
    private volatile boolean isHumanTurn;
    
    /** Flag indicating if the system is waiting for color selection after WILD/+4 */
    private volatile boolean waitingForColor = false;

    // UNO calling state variables
    /** Flag indicating if human player can call UNO */
    private boolean humanCanSayONE = true;
    
    /** Flag indicating if human player can call UNO against machine */
    private boolean humanCanSayONEToMachine = true;
    
    /** Flag indicating if machine has called UNO */
    private boolean machineSayOne = false;
    
    /** Flag indicating if human player is allowed to play */
    private boolean playHuman = true;
    
    /** Flag indicating if system is waiting for human to call UNO */
    private boolean waitingForHumanUNO = false;
    
    /** Flag indicating if system is waiting for human to call UNO against machine */
    private boolean waitingForHumanUNOAgainstMachine = false;
    
    /** Flag for canceling UNO timer */
    public volatile boolean cancelTimer = false;
    
    /** Timeline for UNO countdown timer */
    private Timeline unoTimer;

    // Thread control variables
    /** Flag controlling UNO monitoring thread execution */
    private boolean runningOneThread = true;
    
    /** Flag controlling machine play thread execution */
    private boolean runningPlayMachineThread = true;

    /** Thread for machine UNO calling logic */
    private ThreadSingUNOMachine threadSingUNOMachine;
    
    /** Thread for machine player decision making */
    private ThreadPlayMachine threadPlayMachine;
    
    /** Thread for win condition monitoring */
    private ThreadWinGame threadWinGame;
    
    /** Observer pattern implementation for game events */
    private observable gameEvents = new observableClass();
    
    /** File path for game state persistence */
    private static final String SAVE_FILE_PATH = "uno_saved_game.ser";
    
    /** File handler for serialization operations */
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
     * Sets the animation controller for UI effects.
     *
     * @param animations the animation controller to use
     */
    public void setAnimations(IAnimations animations) {
        this.animations = animations;
    }

    /**
     * Initializes a new match or loads an existing game state.
     * 
     * @param game the game instance to initialize with, or null for new game
     * @throws GameException if game initialization fails
     */
    public void initmatch(GameUno game) throws GameException {
        initialize();
        if(game == null){
            newGame();
        }
        else{
            loadGameState();
        }
    }

    /**
     * Starts a new game by initializing all game components and starting worker threads.
     * 
     * @throws GameException if game initialization fails
     */
    public void newGame() throws GameException {
        initVariables();
        this.gameUno.startGame();
        updateGridPaneMargin();

        tableImageView.setImage(this.table.getCurrentCardOnTheTable().getImage()); // mostrar visualmente a carta inciial en la mesa
        refreshGameView();

        // Start machine behavior thread
        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.tableImageView, this, this.deck);
        threadPlayMachine.start();

        // Start UNO monitoring thread
        threadSingUNOMachine = new ThreadSingUNOMachine(
                this.humanPlayer.getCardsPlayer(),
                this.machinePlayer.getCardsPlayer(),
                this,
                this.threadPlayMachine
        );
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();
        
        // Register ThreadSingUNOMachine as observer
        addGameObserver(threadSingUNOMachine);

        // Start win-condition monitoring thread
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
     * Saves the current game state to a file for later restoration.
     * Uses serialization to persist all game objects.
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
     * Loads a previously saved game state from file and restores all game components.
     * Recreates worker threads with the loaded state.
     */
    public void loadGameState() {
        try {
            SerializableFileHandler handler = new SerializableFileHandler();
            GameUno loadedGame = (GameUno) handler.deserialize(SAVE_FILE_PATH);
            System.out.println("Juego cargado correctamente.");

            this.humanPlayer = loadedGame.getHumanPlayer();
            this.machinePlayer = loadedGame.getMachinePlayer();
            this.table = loadedGame.getTable();
            this.deck = loadedGame.getDeck();
            this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer,this.deck, this.table);

            // Recrear hilos con los datos cargados y referencias actuales
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

            // Iniciar los hilos
            new Thread(threadPlayMachine, "ThreadPlayMachine").start();
            new Thread(threadSingUNOMachine, "ThreadSingUNOMachine").start();
            new Thread(threadWinGame, "ThreadWinGame").start();

            updateGameUI();

        } catch (IOException | ClassNotFoundException | GameException.ThreadInitializationException e) {
            System.err.println("Error al cargar el estado del juego: " + e.getMessage());
        }
    }

    /**
     * Updates the game UI to reflect the current state after loading a saved game.
     * Displays player cards, machine card backs, and the current table card.
     */
    private void updateGameUI() {
        // Mostrar cartas del jugador humano
        displayPlayerCards();

        // Mostrar reverso de la carta de la máquina (cantidad de cartas boca abajo)
        updateMachineCardBack();

        // Mostrar la carta superior en la mesa
        try {
            Card topCard = table.getCurrentCardOnTheTable();
            updatePlayedCard(topCard);
            System.out.println("la carta es: " + topCard.getValue());
            for(Card c: humanPlayer.getCardsPlayer()){
                System.out.println("Valor " + c.getValue() );
            }
        } catch (GameException.EmptyTableException e) {
            System.err.println("No hay cartas sobre la mesa para mostrar.");
        }
    }

    /**
     * Sets up automatic game saving when the window is closed.
     * 
     * @param stage the stage to monitor for close events
     */
    public void setupAutoSaveOnClose(Stage stage) {
        stage.setOnCloseRequest(event -> {
            saveGame();
            System.out.println("Juego guardado automáticamente al cerrar.");
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
     * Sets whether the system is waiting for the human to call UNO.
     *
     * @param waitingForHumanUNO true if waiting for human UNO call; false otherwise
     */
    public void setWaitingForHumanUNO(boolean waitingForHumanUNO) {
        this.waitingForHumanUNO = waitingForHumanUNO;
    }
    
    /**
     * @return true if the system is waiting for the human to call UNO; false otherwise
     */
    public boolean isWaitingForHumanUNO() {
        return waitingForHumanUNO;
    }
    
    /**
     * Sets whether the system is waiting for the human to call UNO against the machine.
     *
     * @param waitingForHumanUNOAgainstMachine true if waiting for human UNO call against machine; false otherwise
     */
    public void setWaitingForHumanUNOAgainstMachine(boolean waitingForHumanUNOAgainstMachine) {
        this.waitingForHumanUNOAgainstMachine = waitingForHumanUNOAgainstMachine;
    }
    
    /**
     * @return true if the system is waiting for the human to call UNO against the machine; false otherwise
     */
    public boolean isWaitingForHumanUNOAgainstMachine() {
        return waitingForHumanUNOAgainstMachine;
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
        String turn = isHumanTurn ? "humano" : "máquina";
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
        System.out.println("Juego guardado automáticamente al cerrar.");
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
        if (humanPlayer.getCardsPlayer().size() == 1) {
            // Cancelar timer inmediatamente
            cancelUNOTimer();
            
            // Notificar evento usando Observer pattern
            gameEvents.notification("HUMAN_SAID_UNO");
            setTurnLabel("¡UNO cantado correctamente!");
        } else if (machinePlayer.getCardsPlayer().size() == 1) {
            // Cancelar timer inmediatamente
            cancelUNOTimer();
            
            // Notificar evento usando Observer pattern
            gameEvents.notification("HUMAN_SAID_UNO_TO_MACHINE");
            setTurnLabel("¡UNO cantado contra la máquina!");
        } else {
            setTurnLabel("No se puede cantar UNO en este momento");
        }
    }
    
    /**
     * Registers an observer to listen for game events.
     *
     * @param observer the observer to register
     */
    public void addGameObserver(org.example.unogame.model.machine.observers.observer observer) {
        gameEvents.addObserver(observer);
    }
    
    /**
     * Removes an observer from listening for game events.
     *
     * @param observer the observer to remove
     */
    public void removeGameObserver(org.example.unogame.model.machine.observers.observer observer) {
        gameEvents.deleteObserver(observer);
    }
    
    /**
     * Notifies all observers of a game event.
     *
     * @param event the event to notify
     */
    public void notifyGameEvent(String event) {
        gameEvents.notification(event);
    }
    
    /**
     * Penalizes the human player for not calling UNO on time by making them draw a card.
     * This method is called when the machine successfully calls UNO against the human.
     *
     * @throws GameException.OutOfCardsInDeck if the deck is empty and cannot be reloaded
     * @throws GameException.NullCardException if a null card is encountered
     * @throws GameException.IllegalCardColor if an illegal color operation occurs
     * @throws GameException.InvalidCardIndex if a hand index is invalid
     * @throws GameException.EmptyTableException if the table is unexpectedly empty
     */
    public void penalizeHumanForNotCallingUNO() throws GameException.OutOfCardsInDeck, GameException.NullCardException, GameException.IllegalCardColor, GameException.InvalidCardIndex, GameException.EmptyTableException {
        if (deck.isEmpty()) {
            // recicla las cartas descartadas excepto la última
            List<Card> discards = table.collectDiscardsExceptTop(true);
            deck.reloadFrom(discards);
        }
        
        Card penaltyCard = deck.takeCard();
        humanPlayer.addCard(penaltyCard);

        Platform.runLater(() -> {
            try {
                printCardsHumanPlayer();
                
                // actualiza el turno y el label para mostrar la penalización - JUGADOR
                setTurnLabel("¡Penalización! Jugador toma una carta por no cantar UNO");
                
                // muestra una nueva carta
                refreshGameView();
            } catch (Exception e) {
                System.err.println("Error al actualizar UI después de penalización: " + e.getMessage());
            }
        });
    }
    
    /**
     * Shows a countdown timer for UNO declaration.
     * This method updates the turn label with a countdown from the specified seconds.
     *
     * @param seconds the number of seconds for the countdown
     */
    public void showUNOTimer(int seconds) {
        // Cancel any existing timer
        if (unoTimer != null) {
            unoTimer.stop();
        }
        
        cancelTimer = false; // Reset cancellation flag
        final int[] countdown = {seconds};
        
        // Verificar si ya se canceló antes de empezar
        if (cancelTimer) {
            return;
        }
        
        unoTimer = new Timeline();
        unoTimer.getKeyFrames().add(
            new KeyFrame(Duration.seconds(1), event -> {
                if (cancelTimer) {
                    unoTimer.stop();
                    return;
                }
                
                if (countdown[0] > 0) {
                    setTurnLabel("¡Canta UNO!");
                    countdown[0]--;
                } else {
                    // Timer finished
                    if (!cancelTimer) {
                        setTurnLabel("¡Tiempo agotado!");
                    }
                    unoTimer.stop();
                }
            })
        );
        unoTimer.setCycleCount(seconds + 1); // +1 for the final "Tiempo agotado" message
        unoTimer.play();
    }
    
    /**
     * Cancels the current UNO timer.
     */
    public void cancelUNOTimer() {
        cancelTimer = true;
        if (unoTimer != null) {
            unoTimer.stop();
            unoTimer = null; // Limpiar referencia
        }
    }
    
    /**
     * Penalizes the machine player for not calling UNO on time by making it draw a card.
     * This method is called when the human successfully calls UNO against the machine.
     *
     * @throws GameException.OutOfCardsInDeck if the deck is empty and cannot be reloaded
     * @throws GameException.NullCardException if a null card is encountered
     * @throws GameException.IllegalCardColor if an illegal color operation occurs
     */
    public void penalizeMachineForNotCallingUNO() throws GameException.OutOfCardsInDeck, GameException.NullCardException, GameException.IllegalCardColor {
        if (deck.isEmpty()) {
            // Recycle discards back into the deck
            List<Card> discards = table.collectDiscardsExceptTop(true);
            deck.reloadFrom(discards);
        }
        
        Card penaltyCard = deck.takeCard();
        machinePlayer.addCard(penaltyCard);

        Platform.runLater(() -> {
            // actualiza el turno y el label para mostrar la penalización - MAQUINA
            setTurnLabel("¡Penalización!");
            
            // mostrar una nueva carta
            refreshGameView();
        });
    }

    /**
     * Displays the human player's cards in the UI grid.
     * This method is used when loading a saved game state.
     */
    private void displayPlayerCards() {
        for(Card c: humanPlayer.getCardsPlayer()){
            System.out.println("Valor " + c.getValue() );
        }
        gridPaneCardsPlayer.getChildren().clear();
        List<Card> playerCards = humanPlayer.getCardsPlayer();

        for (int i = 0; i < playerCards.size(); i++) {
            Card card = playerCards.get(i);
            javafx.scene.image.Image image = new javafx.scene.image.Image(card.getImagePath());
            System.out.println("ruta de la imagen: " + card.getImagePath());
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(120);
            imageView.setFitWidth(80);
            gridPaneCardsPlayer.add(imageView, i, 0);
        }
    }

    /**
     * Updates the table image view to show the current top card.
     * 
     * @param topCard the card to display on the table
     */
    private void updatePlayedCard(Card topCard) {
        if (topCard != null) {
            tableImageView.setImage(new javafx.scene.image.Image(getClass().getResourceAsStream("/org/example/unogame/cards-uno/" + topCard.getImageName())));
        }
    }

    /**
     * Updates the machine player's card display to show the correct number of back cards.
     * This method is used when loading a saved game state.
     */
    private void updateMachineCardBack() {
        gridPaneCardsMachine.getChildren().clear();
        int cardCount = machinePlayer.getCardsPlayer().size();
        for (int i = 0; i < cardCount; i++) {
            ImageView backView = new ImageView(new javafx.scene.image.Image(getClass().getResourceAsStream("/org/example/unogame/cards-uno/back.png")));
            backView.setFitWidth(80);
            backView.setFitHeight(120);
            gridPaneCardsMachine.add(backView, i, 0);
        }
    }
}
