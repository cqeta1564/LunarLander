// TouchInput.java (Adapting for mouse, conceptual for touch)
// For actual touch in Java, JavaFX or a mobile framework (Android) would be used.
// This example will simulate two "touch" regions with mouse buttons.
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics2D; // For rendering debug info
import java.awt.Color;

public class TouchInput extends MouseAdapter {
    public TouchData leftTouch = new TouchData();  // Simulates left half of screen touch
    public TouchData rightTouch = new TouchData(); // Simulates right half of screen touch
    public boolean active = false; // Has there been any "touch" (mouse) activity

    private int screenWidth; // Needed to determine left/right half

    public TouchInput(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public void updateScreenWidth(int newScreenWidth) {
        this.screenWidth = newScreenWidth;
    }

    @Override
    public void mousePressed(MouseEvent e) {
        active = true;
        int x = e.getX();
        int y = e.getY();

        if (x < screenWidth / 2) { // Left half
            if (!leftTouch.touching) { // Only start if not already touching
                leftTouch.startTouch(x, y, e.getButton()); // Use mouse button as ID
            }
        } else { // Right half
            if (!rightTouch.touching) {
                rightTouch.startTouch(x, y, e.getButton());
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (leftTouch.touching && leftTouch.id == e.getModifiersEx()) { // Check if this is the "touch" being dragged
            leftTouch.updateTouch(e.getX(), e.getY());
        }
        if (rightTouch.touching && rightTouch.id == e.getModifiersEx()) { // Crude check for button
            rightTouch.updateTouch(e.getX(), e.getY());
        }
        // More robust ID check would be needed if multiple mouse buttons simulate different touches.
        // For simplicity, assume primary mouse button drag updates the active touch.
        // Or, better: in mousePressed, store which touch (left/right) became active.
        // MouseEvent.getButton() is only reliable on press/release.
        // During drag, getModifiersEx() can check button state.

        // Simplified: if a touch is active, update it.
        // This won't distinguish between left/right drags if both are pressed.
        if (leftTouch.touching) { // If left mouse button started this "touch"
            if ((e.getModifiersEx() & MouseEvent.BUTTON1_DOWN_MASK) != 0) { // Check if button 1 is still down
                leftTouch.updateTouch(e.getX(), e.getY());
            }
        }
        if (rightTouch.touching) { // If e.g. right mouse button started this
            if ((e.getModifiersEx() & MouseEvent.BUTTON3_DOWN_MASK) != 0) { // Check if button 3 is still down
                rightTouch.updateTouch(e.getX(), e.getY());
            }
        }

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Check which touch corresponds to the released button/area
        // This is simplified; proper ID tracking from press is better.
        if (leftTouch.id == e.getButton()) { // Check if this touch was initiated by this button
            leftTouch.endTouch();
        }
        if (rightTouch.id == e.getButton()) {
            rightTouch.endTouch();
        }
    }

    // For debug rendering, similar to JS
    public void render(Graphics2D g) {
        if (leftTouch.touching) {
            g.setColor(Color.RED);
            g.drawOval((int)leftTouch.getX() - 20, (int)leftTouch.getY() - 20, 40, 40);
        }
        if (rightTouch.touching) {
            g.setColor(Color.BLUE);
            g.drawOval((int)rightTouch.getX() - 20, (int)rightTouch.getY() - 20, 40, 40);
        }
    }
}