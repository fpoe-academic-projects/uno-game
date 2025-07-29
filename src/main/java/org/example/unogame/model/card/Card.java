package org.example.unogame.model.card;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.example.unogame.model.unoenum.UnoEnum;

/**
 * Represents a card in the Uno game.
 */
public class Card {
    private String url;
    private String value;
    private String color;
    private Image image;
    private Rectangle cardRectangle;

    /**
     * Constructs a Card with the specified image URL and name.
     *
     * @param url the URL of the card image
     * @param value of the card
     */
    public Card(String url, String value, String color) {
        this.url = url;
        this.value = value;
        this.color = color;
        this.image = new Image(String.valueOf(getClass().getResource(url)));
        this.cardRectangle = createCardRectangle();
    }

    // Constructor for testing
    public Card(String value, String color) {
        this.image = null;
        this.value = value;
        this.color = color;
    }

    /**
     * Creates and configures the ImageView for the card.
     *
     * @return the configured ImageView of the card
     */
    private Rectangle createCardRectangle() {
        Rectangle card = new Rectangle(90, 130);
        card.setFill(new ImagePattern(this.image));
        card.setArcWidth(10); // Bordes redondeados
        card.setArcHeight(10);
        return card;
    }

    // este metodo se usa para que la maquina obtenga la imagen de la parte de atras de la carta del uno
    public static Rectangle getBackCardRectangle() {
        Image backImage = new Image(Card.class.getResource(
                UnoEnum.CARD_UNO.getFilePath()
        ).toString());

        Rectangle cardBack = new Rectangle(90, 130);
        cardBack.setFill(new ImagePattern(backImage));
        cardBack.setArcWidth(10);
        cardBack.setArcHeight(10);
        return cardBack;
    }

    /**
     * Gets the ImageView representation of the card.
     *
     * @return the ImageView of the card
     */
    public Rectangle getCard() {
        return cardRectangle;
    }

    /**
     * Gets the image of the card.
     *
     * @return the Image of the card
     */
    public Image getImage() {
        return image;
    }

    public String getValue() {
        return value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

}
