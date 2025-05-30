package ui;

import java.awt.*;

/**
 * Represents a UI slider component for adjusting a value within a defined range.
 * Used, for example, to control game volume in the settings.
 */
public class Slider {
    private final int x;            // X-coordinate of the slider's top-left corner
    private final int y;            // Y-coordinate of the slider's top-left corner
    private final int width;        // Width of the slider track
    private final int height;       // Height of the slider track
    private final int minValue;     // Minimum value the slider can represent
    private final int maxValue;     // Maximum value the slider can represent
    private final String label;     // Text label displayed above the slider
    private final Rectangle sliderBounds; // Overall clickable area for the slider
    private final Rectangle knobBounds;   // Bounds of the draggable knob
    private final int knobWidth;    // Width of the knob
    private final int knobHeight;   // Height of the knob
    private int currentValue;       // Current value of the slider
    private boolean dragging = false; // True if the user is currently dragging the knob

    /**
     * Constructs a new Slider.
     *
     * @param label             The text label to display for the slider.
     * @param x                 The x-coordinate of the slider's top-left corner.
     * @param y                 The y-coordinate of the slider's top-left corner.
     * @param width             The width of the slider's track.
     * @param sliderTrackHeight The height of the slider's track.
     * @param minValue          The minimum value the slider can represent.
     * @param maxValue          The maximum value the slider can represent.
     * @param initialValue      The initial value of the slider.
     */
    public Slider(String label, int x, int y, int width, int sliderTrackHeight, int minValue, int maxValue, int initialValue) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = sliderTrackHeight;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.knobWidth = 10; // Fixed knob width
        this.knobHeight = sliderTrackHeight + 10; // Knob is slightly taller than the track
        // Define the broader clickable area for the slider, making it easier to hit with the mouse
        this.sliderBounds = new Rectangle(x, y + (height / 2) - (knobHeight / 2), width, knobHeight);
        this.knobBounds = new Rectangle(0, 0, this.knobWidth, this.knobHeight); // Initialize knob bounds
        setValue(initialValue); // Set initial value and update knob position
    }

    /**
     * Gets the current value of the slider.
     *
     * @return The current integer value.
     */
    public int getValue() {
        return currentValue;
    }

    /**
     * Sets the current value of the slider.
     * The value will be clamped to the slider's min and max values.
     * If the value changes, the knob position is updated.
     *
     * @param value The new value to set.
     */
    public void setValue(int value) {
        int oldValue = this.currentValue;
        this.currentValue = Math.max(minValue, Math.min(maxValue, value)); // Clamp value within bounds
        if (oldValue != this.currentValue) { // If value actually changed
            updateKnobPosition(); // Update the visual position of the knob
        }
    }

    /**
     * Updates the knob's screen position based on the current value.
     */
    private void updateKnobPosition() {
        if (maxValue == minValue) { // Avoid division by zero if min and max are the same
            knobBounds.x = x;
        } else {
            // Calculate percentage of value within the range
            float percentage = (float) (currentValue - minValue) / (maxValue - minValue);
            int availableTrackWidth = width - knobWidth; // The track width available for knob movement
            knobBounds.x = x + (int) (percentage * availableTrackWidth); // Calculate knob's X position
        }
        // Center the knob vertically relative to the slider track
        knobBounds.y = y + height / 2 - knobHeight / 2;
    }


    /**
     * Handles mouse input for the slider.
     * Updates the slider's value if the mouse is pressed within its bounds and dragged.
     *
     * @param mouseX         The current X-coordinate of the mouse.
     * @param mouseY         The current Y-coordinate of the mouse.
     * @param mouseIsPressed True if the mouse button is currently pressed, false otherwise.
     */
    public void handleMouseInput(int mouseX, int mouseY, boolean mouseIsPressed) {
        // Define a click/drag detection area that is slightly larger than the track for easier interaction
        Rectangle clickDetectionArea = new Rectangle(x, y + height / 2 - knobHeight / 2, width, knobHeight);

        if (mouseIsPressed) {
            // If mouse is pressed and not currently dragging, check if the press is within the slider's bounds
            if (!dragging && clickDetectionArea.contains(mouseX, mouseY)) {
                dragging = true; // Start dragging
                updateValueFromMouse(mouseX); // Update value based on initial click position
            }
        } else {
            dragging = false; // Stop dragging if mouse button is released
        }

        // If dragging, continue to update the slider's value based on mouse position
        if (dragging) {
            updateValueFromMouse(mouseX);
        }
    }

    /**
     * Updates the slider's current value based on the mouse's X-coordinate.
     *
     * @param mouseX The X-coordinate of the mouse.
     */
    private void updateValueFromMouse(int mouseX) {
        // Calculate mouse position relative to the start of the track, adjusted for knob width
        int effectiveMouseX = mouseX - (x + knobWidth / 2);
        int availableTrackWidth = width - knobWidth; // Actual width available for knob center to move

        float percentage;
        if (availableTrackWidth <= 0) { // Handle edge case of zero or negative track width
            percentage = 0;
        } else {
            // Clamp effectiveMouseX within the bounds of the available track width
            effectiveMouseX = Math.max(0, Math.min(effectiveMouseX, availableTrackWidth));
            percentage = (float) effectiveMouseX / availableTrackWidth; // Calculate percentage along the track
        }

        // Calculate the new value based on the percentage and the slider's range
        int newValue = minValue + (int) (percentage * (maxValue - minValue));
        setValue(newValue); // Set the new value (this will also clamp and update knob position)
    }

    /**
     * Renders the slider on the screen.
     *
     * @param g The Graphics2D context to draw on.
     */
    public void render(Graphics2D g) {
        // Save original rendering hint for text antialiasing
        Object originalTextAntialiasing = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // Enable text antialiasing

        // Draw the label and current value
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString(label + ": " + currentValue + "%", x, y - 5); // Display "Label: Value%"

        // Draw the slider track
        g.setColor(Color.GRAY);
        g.fillRect(x, y, width, height);
        g.setColor(Color.DARK_GRAY);
        g.drawRect(x, y, width, height); // Border for the track

        // Draw the slider knob
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(knobBounds.x, knobBounds.y, knobBounds.width, knobBounds.height);
        g.setColor(Color.WHITE);
        g.drawRect(knobBounds.x, knobBounds.y, knobBounds.width, knobBounds.height); // Border for the knob

        // Restore original text antialiasing hint
        if (originalTextAntialiasing != null) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, originalTextAntialiasing);
        }
    }

    /**
     * Checks if the slider knob is currently being dragged.
     *
     * @return True if dragging, false otherwise.
     */
    public boolean isDragging() {
        return dragging;
    }

    /**
     * Gets the x-coordinate of the slider.
     *
     * @return The x-coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * Gets the y-coordinate of the slider.
     *
     * @return The y-coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * Gets the height of the slider track.
     *
     * @return The height of the track.
     */
    public int getHeightProp() {
        return height;
    }

    /**
     * Gets the height of the slider knob.
     *
     * @return The height of the knob.
     */
    public int getKnobHeight() {
        return knobHeight;
    }

    /**
     * Gets the width of the slider track.
     *
     * @return The width of the track.
     */
    public int getWidthProp() {
        return width;
    }
}