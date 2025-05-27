package entities;

import java.awt.*;

public class LandingPad {
    private final float screenStartX;
    private final float screenEndX;
    private final float screenY;
    private final int multiplier;
    private final Color color;

    public LandingPad(float screenStartX, float screenEndX, float screenY, int multiplier) {
        this.screenStartX = screenStartX;
        this.screenEndX = screenEndX;
        this.screenY = screenY;
        this.multiplier = multiplier;

        if (multiplier >= 8) this.color = new Color(255, 100, 100, 200);
        else if (multiplier >= 5) this.color = new Color(255, 180, 100, 200);
        else this.color = new Color(100, 255, 100, 200);
    }

    public float getStartX() {
        return screenStartX;
    }

    public float getEndX() {
        return screenEndX;
    }

    public float getY() {
        return screenY;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public float getLength() {
        return screenEndX - screenStartX;
    }

    public Color getColor() {
        return color;
    }

    public static class PatternPadDefinition {
        public final float patternStartX;
        public final float patternEndX;
        public final int rawDataY;
        public final int multiplier;
        public final float transformedScreenY;

        public PatternPadDefinition(float patternStartX, float patternEndX, int rawDataY, int multiplier, float transformedScreenY) {
            this.patternStartX = patternStartX;
            this.patternEndX = patternEndX;
            this.rawDataY = rawDataY;
            this.multiplier = multiplier;
            this.transformedScreenY = transformedScreenY;
        }
    }
}