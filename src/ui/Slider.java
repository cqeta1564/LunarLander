package ui;

import java.awt.*;

public class Slider {
    private final int x, y, width, height;
    private final int minValue, maxValue;
    private final String label;
    private final Rectangle sliderBounds;
    private final Rectangle knobBounds;
    private final int knobWidth;
    private final int knobHeight;
    private int currentValue;
    private boolean dragging = false;

    public Slider(String label, int x, int y, int width, int sliderTrackHeight, int minValue, int maxValue, int initialValue) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = sliderTrackHeight;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.knobWidth = 10;
        this.knobHeight = sliderTrackHeight + 10;
        this.sliderBounds = new Rectangle(x, y + (height / 2) - (knobHeight / 2), width, knobHeight);
        this.knobBounds = new Rectangle(0, 0, this.knobWidth, this.knobHeight);
        setValue(initialValue);
    }

    public int getValue() {
        return currentValue;
    }

    public void setValue(int value) {
        int oldValue = this.currentValue;
        this.currentValue = Math.max(minValue, Math.min(maxValue, value));
        if (oldValue != this.currentValue) {
            updateKnobPosition();
        }
    }

    private void updateKnobPosition() {
        if (maxValue == minValue) {
            knobBounds.x = x;
        } else {
            float percentage = (float) (currentValue - minValue) / (maxValue - minValue);
            int availableTrackWidth = width - knobWidth;
            knobBounds.x = x + (int) (percentage * availableTrackWidth);
        }
        knobBounds.y = y + height / 2 - knobHeight / 2;
    }


    public void handleMouseInput(int mouseX, int mouseY, boolean mouseIsPressed) {
        Rectangle clickDetectionArea = new Rectangle(x, y + height / 2 - knobHeight / 2, width, knobHeight);

        if (mouseIsPressed) {
            if (!dragging && clickDetectionArea.contains(mouseX, mouseY)) {
                dragging = true;
                updateValueFromMouse(mouseX);
            }
        } else {
            dragging = false;
        }

        if (dragging) {
            updateValueFromMouse(mouseX);
        }
    }

    private void updateValueFromMouse(int mouseX) {
        int effectiveMouseX = mouseX - (x + knobWidth / 2);
        int availableTrackWidth = width - knobWidth;

        float percentage;
        if (availableTrackWidth <= 0) {
            percentage = 0;
        } else {
            effectiveMouseX = Math.max(0, Math.min(effectiveMouseX, availableTrackWidth));
            percentage = (float) effectiveMouseX / availableTrackWidth;
        }

        int newValue = minValue + (int) (percentage * (maxValue - minValue));
        setValue(newValue);
    }

    public void render(Graphics2D g) {
        Object originalTextAntialiasing = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString(label + ": " + currentValue + "%", x, y - 5);

        g.setColor(Color.GRAY);
        g.fillRect(x, y, width, height);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, width, height);

        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(knobBounds.x, knobBounds.y, knobBounds.width, knobBounds.height);
        g.setColor(Color.WHITE);
        g.drawRect(knobBounds.x, knobBounds.y, knobBounds.width, knobBounds.height);

        if (originalTextAntialiasing != null) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, originalTextAntialiasing);
        }
    }

    public boolean isDragging() {
        return dragging;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getHeightProp() {
        return height;
    }

    public int getKnobHeight() {
        return knobHeight;
    }

    public int getWidthProp() {
        return width;
    }
}