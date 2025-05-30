package entities;

import java.awt.*;

/**
 * Represents a landing pad on the terrain.
 * Landing pads have a position, a score multiplier, and a visual representation.
 */
public class LandingPad {
    private final float screenStartX;    // Starting X-coordinate on the screen
    private final float screenEndX;      // Ending X-coordinate on the screen
    private final float screenY;         // Y-coordinate on the screen (surface of the pad)
    private final int multiplier;        // Score multiplier for landing on this pad
    private final Color color;           // Color of the landing pad

    /**
     * Constructs a new LandingPad.
     *
     * @param screenStartX The starting X-coordinate of the pad on the screen.
     * @param screenEndX   The ending X-coordinate of the pad on the screen.
     * @param screenY      The Y-coordinate of the pad's surface on the screen.
     * @param multiplier   The score multiplier associated with this pad.
     */
    public LandingPad(float screenStartX, float screenEndX, float screenY, int multiplier) {
        this.screenStartX = screenStartX;
        this.screenEndX = screenEndX;
        this.screenY = screenY;
        this.multiplier = multiplier;
        this.color = new Color(255, 255, 255, 200); // Semi-transparent white
    }

    /**
     * Gets the starting X-coordinate of the landing pad.
     *
     * @return The starting X-coordinate.
     */
    public float getStartX() {
        return screenStartX;
    }

    /**
     * Gets the ending X-coordinate of the landing pad.
     *
     * @return The ending X-coordinate.
     */
    public float getEndX() {
        return screenEndX;
    }

    /**
     * Gets the Y-coordinate of the landing pad's surface.
     *
     * @return The Y-coordinate.
     */
    public float getY() {
        return screenY;
    }

    /**
     * Gets the score multiplier for this landing pad.
     *
     * @return The score multiplier.
     */
    public int getMultiplier() {
        return multiplier;
    }

    /**
     * Gets the length of the landing pad.
     *
     * @return The length (width) of the pad.
     */
    public float getLength() {
        return screenEndX - screenStartX;
    }

    /**
     * Gets the color of the landing pad.
     *
     * @return The color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Defines a landing pad within the base terrain pattern data.
     * This class stores coordinates relative to the pattern and other properties
     * before they are transformed into actual screen LandingPad objects.
     */
    public static class PatternPadDefinition {
        /**
         * Starting X-coordinate within the normalized terrain pattern.
         */
        public final float patternStartX;
        /**
         * Ending X-coordinate within the normalized terrain pattern.
         */
        public final float patternEndX;
        /**
         * Original Y-coordinate from the raw terrain data (used for reference or specific logic).
         */
        public final int rawDataY;
        /**
         * Score multiplier for this pad type.
         */
        public final int multiplier;
        /**
         * Y-coordinate after transformation to screen space, relative to the pattern's viewport mapping.
         */
        public final float transformedScreenY;

        /**
         * Constructs a PatternPadDefinition.
         *
         * @param patternStartX      The start X-coordinate in the pattern space.
         * @param patternEndX        The end X-coordinate in the pattern space.
         * @param rawDataY           The original Y data value.
         * @param multiplier         The score multiplier.
         * @param transformedScreenY The Y-coordinate after initial transformation (mapping to viewport).
         */
        public PatternPadDefinition(float patternStartX, float patternEndX, int rawDataY, int multiplier, float transformedScreenY) {
            this.patternStartX = patternStartX;
            this.patternEndX = patternEndX;
            this.rawDataY = rawDataY;
            this.multiplier = multiplier;
            this.transformedScreenY = transformedScreenY;
        }
    }
}