package entities;

import java.awt.Color;

public class LandingPad {
    private final float screenStartX; // X souřadnice na obrazovce
    private final float screenEndX;   // X souřadnice na obrazovce
    private final float screenY;      // Y souřadnice na obrazovce
    private final int multiplier;
    private final Color color;

    // Tento konstruktor bude volán z Terrain.populateVisibleTerrain
    public LandingPad(float screenStartX, float screenEndX, float screenY, int multiplier) {
        this.screenStartX = screenStartX;
        this.screenEndX = screenEndX;
        this.screenY = screenY;
        this.multiplier = multiplier;

        if (multiplier >= 8) this.color = new Color(255, 100, 100, 200); // Částečně průhledná pro lepší vzhled
        else if (multiplier >= 5) this.color = new Color(255, 180, 100, 200);
        else this.color = new Color(100, 255, 100, 200);
    }

    public float getStartX() { return screenStartX; }
    public float getEndX() { return screenEndX; }
    public float getY() { return screenY; }
    public int getMultiplier() { return multiplier; }
    public float getLength() { return screenEndX - screenStartX; }
    public Color getColor() { return color; }

    /**
     * Pomocná struktura pro definici plošky v rámci jednoho cyklu vzoru.
     */
    public static class PatternPadDefinition {
        public final float patternStartX; // X souřadnice v rámci 0...patternWidth
        public final float patternEndX;
        public final int rawDataY;       // Původní Y souřadnice z dat (pro referenci)
        public final int multiplier;
        public final float transformedScreenY; // Y souřadnice již transformovaná na obrazovku

        public PatternPadDefinition(float patternStartX, float patternEndX, int rawDataY, int multiplier, float transformedScreenY) {
            this.patternStartX = patternStartX;
            this.patternEndX = patternEndX;
            this.rawDataY = rawDataY;
            this.multiplier = multiplier;
            this.transformedScreenY = transformedScreenY;
        }
    }
}