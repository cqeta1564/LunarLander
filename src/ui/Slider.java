package ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class Slider {
    private final int x, y, width, height; // Rozměry dráhy
    private final int minValue, maxValue;
    private int currentValue;
    private final String label;

    private final Rectangle sliderBounds; // Celková interaktivní oblast
    private final Rectangle knobBounds;   // Vizuální jezdec

    private boolean dragging = false;
    private final int knobWidth;
    private final int knobHeight;

    public Slider(String label, int x, int y, int width, int sliderTrackHeight, int minValue, int maxValue, int initialValue) {
        this.label = label;
        this.x = x;
        this.y = y; // y pozice horního okraje dráhy slideru
        this.width = width; // Celková šířka dráhy, po které se jezdec může pohybovat
        this.height = sliderTrackHeight; // Výška samotné viditelné dráhy
        this.minValue = minValue;
        this.maxValue = maxValue;

        this.knobWidth = 10; // Pevná šířka jezdce
        this.knobHeight = sliderTrackHeight + 10; // Jezdec je o něco vyšší než dráha

        // sliderBounds je oblast, na kterou lze kliknout pro interakci se sliderem
        // Měla by pokrývat dráhu i jezdce, aby bylo snadné chytit.
        // Vertikálně centrujeme knobHeight kolem středu dráhy pro výpočet sliderBounds.
        int sliderBoundsY = y + sliderTrackHeight / 2 - knobHeight / 2;
        this.sliderBounds = new Rectangle(x, sliderBoundsY, width, knobHeight);
        this.knobBounds = new Rectangle(0, 0, this.knobWidth, this.knobHeight); // Pozice se aktualizuje

        setValue(initialValue);
    }

    public void setValue(int value) {
        int oldValue = this.currentValue;
        this.currentValue = Math.max(minValue, Math.min(maxValue, value));
        if (oldValue != this.currentValue) { // Aktualizovat pozici jen pokud se hodnota změnila
            updateKnobPosition();
        }
    }

    public int getValue() {
        return currentValue;
    }

    private void updateKnobPosition() {
        if (maxValue == minValue) { // Vyhnout se dělení nulou
            knobBounds.x = x;
        } else {
            float percentage = (float) (currentValue - minValue) / (maxValue - minValue);
            // Jezdec se pohybuje v rámci šířky dráhy, jeho levý okraj.
            // Aby byl jezdec vizuálně "na" hodnotě, posuneme ho tak, aby jeho střed byl na dané procentuální pozici.
            // Nebo jednodušeji, levý okraj jezdce se pohybuje od x do x + width - knobWidth.
            int availableTrackWidth = width - knobWidth;
            knobBounds.x = x + (int) (percentage * availableTrackWidth);
        }
        // Vertikální centrování jezdce vůči dráze
        knobBounds.y = y + height / 2 - knobHeight / 2;
    }

    public void handleMouseInput(int mouseX, int mouseY, boolean mouseIsPressed) {
        if (mouseIsPressed) {
            // Pokud je myš stisknuta a ještě netáhneme, zkontrolujeme, zda stisk byl na slideru
            if (!dragging && sliderBounds.contains(mouseX, mouseY)) {
                dragging = true;
                // Při prvním kliknutí okamžitě aktualizujeme hodnotu
                updateValueFromMouse(mouseX);
            }
        } else {
            dragging = false; // Pokud tlačítko myši není stisknuté, určitě netáhneme
        }

        // Pokud táhneme, aktualizujeme hodnotu slideru
        if (dragging) {
            updateValueFromMouse(mouseX);
        }
    }

    private void updateValueFromMouse(int mouseX) {
        // Omezíme relativní pozici myši na šířku dráhy
        int relativeMouseX = Math.max(0, Math.min(mouseX - this.x, this.width));
        float percentage;
        if (this.width == 0) { // Vyhnout se dělení nulou, pokud je šířka 0
            percentage = 0;
        } else {
            percentage = (float) relativeMouseX / this.width;
        }
        int newValue = minValue + (int) (percentage * (maxValue - minValue));
        setValue(newValue);
    }

    public void render(Graphics2D g) {
        // Popisek
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        // y - 10 posune popisek nad dráhu slideru
        g.drawString(label + ": " + currentValue + "%", x, y - 5);

        // Dráha slideru
        g.setColor(Color.GRAY);
        g.fillRect(x, y, width, height);
        g.setColor(Color.DARK_GRAY); // Okraj dráhy
        g.drawRect(x, y, width, height);

        // Jezdec (knob)
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(knobBounds.x, knobBounds.y, knobBounds.width, knobBounds.height);
        g.setColor(Color.WHITE); // Okraj jezdce
        g.drawRect(knobBounds.x, knobBounds.y, knobBounds.width, knobBounds.height);
    }
}