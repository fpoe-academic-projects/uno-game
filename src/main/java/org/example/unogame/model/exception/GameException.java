package org.example.unogame.model.exception;

/**
 * Base checked exception type for UNO game–specific errors.
 *
 * <p>All domain exceptions thrown by the model layer should extend this class.
 * Nested static subclasses represent common error categories (deck depletion,
 * illegal card attributes, invalid indices, etc.).</p>
 *
 * <h2>Usage</h2>
 * <ul>
 *   <li>Throw specific nested subclasses when possible (e.g., {@link OutOfCardsInDeck}).</li>
 *   <li>Use {@link GameException} itself for generic or unexpected error states
 *       that do not fit a dedicated subtype.</li>
 * </ul>
 */
public class GameException extends Exception {

    private static final long serialVersionUID = 1L;

    /** Creates a generic game exception with no message. */
    public GameException() {
        super();
    }

    /** Creates a generic game exception with a message. */
    public GameException(String message) {
        super(message);
    }

    /** Creates a generic game exception with a message and a cause. */
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }

    /** Creates a generic game exception with a cause. */
    public GameException(Throwable cause) {
        super(cause);
    }

    // ------------------------------------------------------------------
    // Nested exception types
    // ------------------------------------------------------------------

    /**
     * Thrown when the deck runs out of cards and a draw is attempted.
     */
    public static class OutOfCardsInDeck extends GameException {
        private static final long serialVersionUID = 2L;

        /** Creates the exception with a default message. */
        public OutOfCardsInDeck() {
            super("Deck has run out of cards.");
        }

        /** Creates the exception with a contextual suffix appended to the default message. */
        public OutOfCardsInDeck(String message) {
            super("Deck has run out of cards. " + message);
        }
    }

    /**
     * Thrown when a card has an illegal or unexpected color.
     */
    public static class IllegalCardColor extends GameException {
        private static final long serialVersionUID = 3L;

        /** Creates the exception with a default message. */
        public IllegalCardColor() {
            super("The card has an illegal or invalid color.");
        }

        /** Creates the exception with a contextual message. */
        public IllegalCardColor(String message) {
            super("Illegal card color: " + message);
        }
    }

    /**
     * Thrown when a card has an illegal or unexpected value.
     */
    public static class IllegalCardValue extends GameException {
        private static final long serialVersionUID = 4L;

        /** Creates the exception with a default message. */
        public IllegalCardValue() {
            super("The card has an illegal or invalid value.");
        }

        /** Creates the exception with a contextual message. */
        public IllegalCardValue(String message) {
            super("Illegal card value: " + message);
        }
    }

    /**
     * Thrown when an action is attempted after the game has already ended.
     */
    public static class GameEndedException extends GameException {
        private static final long serialVersionUID = 5L;

        /** Creates the exception with a default message. */
        public GameEndedException() {
            super("The game has already ended. Action not allowed.");
        }

        /** Creates the exception with a contextual message. */
        public GameEndedException(String message) {
            super("The game has already ended: " + message);
        }
    }

    /**
     * Thrown when accessing the top card on the table but there are no cards placed.
     */
    public static class EmptyTableException extends GameException {
        private static final long serialVersionUID = 6L;

        /** Creates the exception with a default message. */
        public EmptyTableException() {
            super("There are no cards on the table.");
        }

        /** Creates the exception with a contextual message. */
        public EmptyTableException(String message) {
            super("Empty table: " + message);
        }
    }

    /**
     * Thrown when attempting to access a card in a hand using an invalid index.
     */
    public static class InvalidCardIndex extends GameException {
        /** Creates the exception indicating the offending index. */
        public InvalidCardIndex(int index) {
            super("Invalid card index: " + index);
        }
    }

    /**
     * Thrown when a null {@code Card} is encountered where a valid instance is required.
     */
    public static class NullCardException extends GameException {
        /** Creates the exception without a message. */
        public NullCardException() {
            super();
        }

        /** Creates the exception with a contextual message. */
        public NullCardException(String message) {
            super(message);
        }
    }

    /**
     * Wraps an {@link InterruptedException} or similar interruption during thread operations
     * within the game model or controller logic.
     */
    public static class ThreadInterruptedException extends GameException {
        /** Creates the exception with a message and root cause. */
        public ThreadInterruptedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Thrown when a thread required by the game cannot be created or prepared correctly.
     */
    public static class ThreadInitializationException extends GameException {
        /** Creates the exception with a contextual message. */
        public ThreadInitializationException(String message) {
            super(message);
        }
    }

    /**
     * Thrown when a required player reference is missing or null.
     */
    public static class NullPlayerException extends GameException {
        /** Creates the exception with a contextual message. */
        public NullPlayerException(String message) {
            super(message);
        }
    }
}