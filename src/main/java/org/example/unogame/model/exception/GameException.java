package org.example.unogame.model.exception;

/**
 * Base exception class for UNO game-specific errors.
 */
public class GameException extends Exception {

    private static final long serialVersionUID = 1L;

    public GameException() {
        super();
    }

    public GameException(String message) {
        super(message);
    }

    public GameException(String message, Throwable cause) {
        super(message, cause);
    }

    public GameException(Throwable cause) {
        super(cause);
    }

    /**
     * Thrown when the deck runs out of cards.
     */
    public static class OutOfCardsInDeck extends GameException {
        private static final long serialVersionUID = 2L;

        public OutOfCardsInDeck() {
            super("Deck has run out of cards.");
        }

        public OutOfCardsInDeck(String message) {
            super("Deck has run out of cards. " + message);
        }
    }

    /**
     * Thrown when a card has an illegal or unexpected color.
     */
    public static class IllegalCardColor extends GameException {
        private static final long serialVersionUID = 3L;

        public IllegalCardColor() {
            super("The card has an illegal or invalid color.");
        }

        public IllegalCardColor(String message) {
            super("Illegal card color: " + message);
        }
    }

    /**
     * Thrown when a card has an illegal or unexpected value.
     */
    public static class IllegalCardValue extends GameException {
        private static final long serialVersionUID = 4L;

        public IllegalCardValue() {
            super("The card has an illegal or invalid value.");
        }

        public IllegalCardValue(String message) {
            super("Illegal card value: " + message);
        }
    }

    /**
     * Thrown when an action is attempted after the game has ended.
     */
    public static class GameEndedException extends GameException {
        private static final long serialVersionUID = 5L;

        public GameEndedException() {
            super("The game has already ended. Action not allowed.");
        }

        public GameEndedException(String message) {
            super("The game has already ended: " + message);
        }
    }

    /**
     * Thrown when trying to access a card on the table, but the table is empty.
     */
    public static class EmptyTableException extends GameException {
        private static final long serialVersionUID = 6L;

        public EmptyTableException() {
            super("There are no cards on the table.");
        }

        public EmptyTableException(String message) {
            super("Empty table: " + message);
        }
    }

    public static class InvalidCardIndex extends GameException {
        public InvalidCardIndex(int index) {
            super("Invalid card index: " + index);
        }
    }

    public static class NullCardException extends GameException {
        public NullCardException() {
            super();
        }

        public NullCardException(String message) {
            super(message);
        }
    }


    public static class ThreadInterruptedException extends GameException {
        public ThreadInterruptedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ThreadInitializationException extends GameException {
        public ThreadInitializationException(String message) {
            super(message);
        }
    }

    public static class NullPlayerException extends GameException {
        public NullPlayerException(String message) {
            super(message);
        }
    }


}

