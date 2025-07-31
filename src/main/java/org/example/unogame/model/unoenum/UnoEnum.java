package org.example.unogame.model.unoenum;

import java.io.Serializable;

/**
 * Enumerates all image asset keys used by the Uno game and resolves each to a
 * classpath resource path.
 *
 * <p>Each enum constant represents a single image file (icons, UI elements, and
 * card faces). The stored path is the absolute, classpath-based location obtained
 * by prefixing a shared base path to the relative file name.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * // Load an image using the resolved classpath resource:
 * Image img = new Image(getClass().getResource(UnoEnum.RED_5.getFilePath()).toString());
 * }</pre>
 *
 * <h2>Notes</h2>
 * <ul>
 *   <li>The <em>base path</em> is {@code /org/example/unogame/} and is applied to all entries.</li>
 *   <li>Numeric card assets follow the pattern {@code <digit>_<color>.png}.</li>
 *   <li>Special cards (e.g., SKIP/RESERVE, wilds) have descriptive file names.</li>
 * </ul>
 */
public enum UnoEnum implements Serializable {
    FAVICON("favicon.png"),
    UNO("images/uno.png"),
    BACKGROUND_UNO("images/background_uno.gif"),
    BUTTON_UNO("images/button_uno.png"),
    CARD_UNO("cards-uno/card_uno.png"),
    DECK_OF_CARDS("cards-uno/deck_of_cards.png"),
    WILD("cards-uno/wild.png"),
    TWO_WILD_DRAW_BLUE("cards-uno/2_wild_draw_blue.png"),
    TWO_WILD_DRAW_GREEN("cards-uno/2_wild_draw_green.png"),
    TWO_WILD_DRAW_YELLOW("cards-uno/2_wild_draw_yellow.png"),
    TWO_WILD_DRAW_RED("cards-uno/2_wild_draw_red.png"),
    FOUR_WILD_DRAW("cards-uno/4_wild_draw.png"),
    SKIP_BLUE("cards-uno/skip_blue.png"),
    SKIP_YELLOW("cards-uno/skip_yellow.png"),
    SKIP_GREEN("cards-uno/skip_green.png"),
    SKIP_RED("cards-uno/skip_red.png"),
    RESERVE_BLUE("cards-uno/reserve_blue.png"),
    RESERVE_YELLOW("cards-uno/reserve_yellow.png"),
    RESERVE_GREEN("cards-uno/reserve_green.png"),
    RESERVE_RED("cards-uno/reserve_red.png"),
    GREEN_0("cards-uno/0_green.png"),
    GREEN_1("cards-uno/1_green.png"),
    GREEN_2("cards-uno/2_green.png"),
    GREEN_3("cards-uno/3_green.png"),
    GREEN_4("cards-uno/4_green.png"),
    GREEN_5("cards-uno/5_green.png"),
    GREEN_6("cards-uno/6_green.png"),
    GREEN_7("cards-uno/7_green.png"),
    GREEN_8("cards-uno/8_green.png"),
    GREEN_9("cards-uno/9_green.png"),
    YELLOW_0("cards-uno/0_yellow.png"),
    YELLOW_1("cards-uno/1_yellow.png"),
    YELLOW_2("cards-uno/2_yellow.png"),
    YELLOW_3("cards-uno/3_yellow.png"),
    YELLOW_4("cards-uno/4_yellow.png"),
    YELLOW_5("cards-uno/5_yellow.png"),
    YELLOW_6("cards-uno/6_yellow.png"),
    YELLOW_7("cards-uno/7_yellow.png"),
    YELLOW_8("cards-uno/8_yellow.png"),
    YELLOW_9("cards-uno/9_yellow.png"),
    BLUE_0("cards-uno/0_blue.png"),
    BLUE_1("cards-uno/1_blue.png"),
    BLUE_2("cards-uno/2_blue.png"),
    BLUE_3("cards-uno/3_blue.png"),
    BLUE_4("cards-uno/4_blue.png"),
    BLUE_5("cards-uno/5_blue.png"),
    BLUE_6("cards-uno/6_blue.png"),
    BLUE_7("cards-uno/7_blue.png"),
    BLUE_8("cards-uno/8_blue.png"),
    BLUE_9("cards-uno/9_blue.png"),
    RED_0("cards-uno/0_red.png"),
    RED_1("cards-uno/1_red.png"),
    RED_2("cards-uno/2_red.png"),
    RED_3("cards-uno/3_red.png"),
    RED_4("cards-uno/4_red.png"),
    RED_5("cards-uno/5_red.png"),
    RED_6("cards-uno/6_red.png"),
    RED_7("cards-uno/7_red.png"),
    RED_8("cards-uno/8_red.png"),
    RED_9("cards-uno/9_red.png");

    /** Absolute classpath prefix applied to all asset file names. */
    private static final String PATH = "/org/example/unogame/";

    /** Full classpath-resolved resource path for this asset. */
    private final String filePath;

    /**
     * Associates the enum constant with its relative asset path and resolves it
     * to an absolute classpath location by prefixing the shared base path.
     *
     * @param filePath relative file path of the image within the project resources
     */
    UnoEnum(String filePath) {
        this.filePath = PATH + filePath;
    }

    /**
     * Returns the absolute classpath resource path for this asset.
     *
     * <p>This path is suitable for {@code getResource(...)} calls.</p>
     *
     * @return the absolute classpath path of the image resource
     */
    public String getFilePath() {
        return filePath;
    }
}
