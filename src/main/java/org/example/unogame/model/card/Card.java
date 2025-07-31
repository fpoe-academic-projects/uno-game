package org.example.unogame.model.card;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.unoenum.UnoEnum;

import java.io.Serializable;

/**
 * Represents a single Uno card, including its value, color, image, and a
 * JavaFX {@link Rectangle} used for rendering in the UI.
 *
 * <p>This class is UI-aware (it creates a rectangle with the image as a fill)
 * so that controllers can directly place it in JavaFX layouts.</p>
 */
public class Card implements Serializable {
    private String url;
    private String value;
    private String color;

    private transient Image image;
    private transient Rectangle cardRectangle;

    /** Minimal internal defaults for the on-screen card view. */
    private static final class View {
        static final double W = 90;
        static final double H = 130;
        static final double ARC = 10;
        private View() {}
    }

    /**
     * Constructs a card with the specified image resource path, face value, and color.
     *
     * @param url   the classpath-relative URL to the card image resource
     * @param value the face value of the card (e.g., numbers, "SKIP", "+2", "+4", "WILD")
     * @param color the card color (e.g., "RED", "GREEN", "BLUE", "YELLOW", or "BLACK" for wilds)
     * @throws GameException.IllegalCardValue if {@code value} is {@code null} or empty
     * @throws GameException.IllegalCardColor if {@code color} is {@code null} or empty
     * @throws GameException                 if the image resource cannot be found
     */
    public Card(String url, String value, String color) throws GameException {
        if (value == null || value.isEmpty()) {
            throw new GameException.IllegalCardValue("Card value is null or empty.");
        }
        if (color == null || color.isEmpty()) {
            throw new GameException.IllegalCardColor("Card color is null or empty.");
        }
        this.url = url;
        this.value = value;
        this.color = color;

        loadImageResources();
    }

    /**
     * Convenience constructor primarily intended for tests,
     * where rendering is not required.
     *
     * <p>The {@code image} and {@code cardRectangle} fields are not initialized here.</p>
     *
     * @param value the face value for testing
     * @param color the color for testing
     */
    // Constructor para pruebas unitarias
    public Card(String value, String color) {
        this.value = value;
        this.color = color;
        this.url = null;
    }

    private void loadImageResources() {
        if (url != null) {
            var resource = getClass().getResource(url);
            if (resource != null) {
                this.image = new Image(resource.toString());
                this.cardRectangle = createCardRectangle();
            }
        }
    }

    /**
     * Creates and configures the JavaFX rectangle used to render this card,
     * applying the image as a fill and rounded corners for a card-like look.
     *
     * @return a configured {@link Rectangle} representing this card
     */
    private Rectangle createCardRectangle() {
        Rectangle card = new Rectangle(View.W, View.H);
        card.setFill(new ImagePattern(this.image));
        card.setArcWidth(View.ARC);
        card.setArcHeight(View.ARC);
        return card;
    }

    /**
     * Returns a back-face rectangle that represents a hidden Uno card,
     * typically used for the machine player's hand.
     *
     * @return a {@link Rectangle} filled with the Uno back image
     */
    public static Rectangle getBackCardRectangle() {
        Image backImage = new Image(Card.class.getResource(
                UnoEnum.CARD_UNO.getFilePath()
        ).toString());

        Rectangle cardBack = new Rectangle(View.W, View.H);
        cardBack.setFill(new ImagePattern(backImage));
        cardBack.setArcWidth(View.ARC);
        cardBack.setArcHeight(View.ARC);
        return cardBack;
    }

    /**
     * @return the JavaFX {@link Rectangle} representing this card on screen
     */
    public Rectangle getCard() {
        if (cardRectangle == null) {
            loadImageResources();
        }
        return cardRectangle;
    }

    /**
     * @return the {@link Image} associated with this card, or {@code null} if not loaded
     */
    public Image getImage() {
        if (image == null) {
            loadImageResources();
        }
        return image;
    }

    /**
     * @return the face value of the card
     */
    public String getImageName() {
        if (url == null) return null;
        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
    }

    public String getValue() {
        return value;
    }

    /**
     * @return the current color of the card
     */
    public String getColor() {
        return color;
    }

    /**
     * Changes the card color (commonly used after playing a wild).
     *
     * @param color the new color to apply
     * @throws GameException.IllegalCardColor if {@code color} is {@code null} or empty
     */
    public void setColor(String color) throws GameException.IllegalCardColor {
        if (color == null || color.isEmpty()) {
            throw new GameException.IllegalCardColor("Color cannot be null or empty when changing color.");
        }
        this.color = color;
    }

    /**
     * Returns the classpath-based image path for this card.
     * <p>
     * The path points to the image inside the {@code /cards-uno/} directory, using
     * the card's image file name as returned by {@link #getImageName()}.
     * </p>
     *
     * @return a string representing the relative path to the card image resource
     */
    public String getImagePath() {
        return "/org/example/unogame/cards-uno/" + getImageName();
    }

}
