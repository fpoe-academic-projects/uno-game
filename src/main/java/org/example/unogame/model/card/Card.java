package org.example.unogame.model.card;

import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Rectangle;
import org.example.unogame.model.exception.GameException;
import org.example.unogame.model.unoenum.UnoEnum;

import java.io.Serializable;

/**
 * Represents a card in the Uno game.
 */
public class Card implements Serializable {
    private String url;
    private String value;
    private String color;

    private transient Image image;
    private transient Rectangle cardRectangle;

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

    private Rectangle createCardRectangle() {
        Rectangle card = new Rectangle(90, 130);
        card.setFill(new ImagePattern(this.image));
        card.setArcWidth(10);
        card.setArcHeight(10);
        return card;
    }

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

    public Rectangle getCard() {
        if (cardRectangle == null) {
            loadImageResources();
        }
        return cardRectangle;
    }

    public Image getImage() {
        if (image == null) {
            loadImageResources();
        }
        return image;
    }

    public String getImageName() {
        if (url == null) return null;
        int lastSlash = url.lastIndexOf('/');
        return lastSlash >= 0 ? url.substring(lastSlash + 1) : url;
    }

    public String getValue() {
        return value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) throws GameException.IllegalCardColor {
        if (color == null || color.isEmpty()) {
            throw new GameException.IllegalCardColor("Color cannot be null or empty when changing color.");
        }
        this.color = color;
    }

    public String getImagePath() {
        return "/org/example/unogame/cards-uno/" + getImageName();
    }


}
