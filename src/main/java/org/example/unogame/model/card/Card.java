package org.example.unogame.model.card;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.example.unogame.model.unoenum.UnoEnum;

/**
 * Represents a card in the Uno game.
 */
public class Card {
    private String url;
    private String value;
    private String color;
    private Image image;
    private ImageView cardImageView;
    private UnoEnum cardEnum;

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
        this.cardImageView = createCardImageView();
    }

    /**
     * Creates and configures the ImageView for the card.
     *
     * @return the configured ImageView of the card
     */
    private ImageView createCardImageView() {
        ImageView card = new ImageView(this.image);
        card.setY(16);
        card.setFitHeight(130);
        card.setFitWidth(90);
        return card;
    }

    // este metodo se usa para que la maquina obtenga la imagen de la parte de atras de la carta del uno
    public static ImageView getBackCardImageView() {
        Image backImage = new Image(Card.class.getResource(
                org.example.unogame.model.unoenum.UnoEnum.CARD_UNO.getFilePath()
        ).toString());

        ImageView cardBackView = new ImageView(backImage);
        cardBackView.setFitWidth(90);
        cardBackView.setFitHeight(130);
        cardBackView.setY(16);
        return cardBackView;
    }

    /**
     * Gets the ImageView representation of the card.
     *
     * @return the ImageView of the card
     */
    public ImageView getCard() {
        return cardImageView;
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
}
