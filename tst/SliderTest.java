import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.Slider;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link Slider} UI component class.
 * This class verifies the functionality of the slider, including value setting,
 * clamping, mouse interaction, and dragging state.
 */
class SliderTest {
    private Slider slider; // The Slider instance to be tested

    /**
     * Sets up the test environment before each test.
     * Initializes a new Slider instance with predefined parameters.
     */
    @BeforeEach
    void setUp() {
        // Initialize a slider for testing: Label, x, y, width, trackHeight, minVal, maxVal, initialVal
        slider = new Slider("TestSlider", 50, 50, 200, 8, 0, 100, 50);
    }

    /**
     * Tests that the slider's initial value is correctly set by the constructor.
     */
    @Test
    void testInitialValue() {
        assertEquals(50, slider.getValue(), "Initial slider value is incorrect.");
    }

    /**
     * Tests that setting a value greater than the maximum clamps the value to the maximum.
     */
    @Test
    void testSetValueClampsToMax() {
        slider.setValue(150); // Attempt to set value above max
        assertEquals(100, slider.getValue(), "Slider value should be clamped to maximum.");
    }

    /**
     * Tests that setting a value less than the minimum clamps the value to the minimum.
     */
    @Test
    void testSetValueClampsToMin() {
        slider.setValue(-50); // Attempt to set value below min
        assertEquals(0, slider.getValue(), "Slider value should be clamped to minimum.");
    }

    /**
     * Tests that setting a value within the valid range updates the slider's value correctly.
     */
    @Test
    void testSetValueWithinBounds() {
        slider.setValue(75); // Set a valid value
        assertEquals(75, slider.getValue(), "Slider value should be set correctly within bounds.");
    }

    /**
     * Tests if the slider's value is correctly updated based on mouse input/position.
     * Checks values at the beginning, end, and middle of the slider track.
     */
    @Test
    void testUpdateValueFromMouseCalculatesCorrectly() {
        // Test clicking at the beginning of the slider
        // The mouse X is set to the slider's start X + half knob width for accurate targeting
        slider.handleMouseInput(50 + slider.getKnobHeight() / 2, 50, true); // Simulate mouse press
        // handleMouseInput might need to be called twice if the first only sets dragging=true
        // and the second (while dragging) updates the value.
        // Based on current Slider.java, first press already updates.
        assertEquals(0, slider.getValue(), "Slider value should be 0 when mouse is at the beginning.");
        slider.handleMouseInput(50 + slider.getKnobHeight() / 2, 50, false); // Release mouse

        // Test clicking at the end of the slider
        // Mouse X at slider's start X + track width - half knob width
        slider.handleMouseInput(50 + 200 - slider.getKnobHeight() / 2, 50, true);
        assertEquals(100, slider.getValue(), "Slider value should be 100 when mouse is at the end.");
        slider.handleMouseInput(50 + 200 - slider.getKnobHeight() / 2, 50, false);

        // Test clicking at the middle of the slider
        // Mouse X at slider's start X + half track width
        slider.handleMouseInput(50 + 200 / 2, 50, true);
        // Due to integer rounding, the value might not be exactly 50. Allow a small tolerance.
        assertTrue(Math.abs(50 - slider.getValue()) <= 1, "Slider value should be around 50 when mouse is in the middle. Actual: " + slider.getValue());
        slider.handleMouseInput(50 + 200 / 2, 50, false);
    }

    /**
     * Tests the dragging state of the slider.
     * Verifies that dragging starts on mouse press within the slider and stops on release.
     * Also ensures dragging doesn't start if the press is outside the slider area.
     */
    @Test
    void testDraggingState() {
        // The click detection area is based on knobHeight for vertical detection,
        // centered on the track.
        // Recalculate based on Slider.java for clarity, though not strictly needed for the test logic itself.
        // Rectangle clickDetectionArea = new Rectangle(slider.getX(), slider.getY() + slider.getHeightProp() / 2 - slider.getKnobHeight() / 2, slider.getWidthProp(), slider.getKnobHeight());

        assertFalse(slider.isDragging(), "Slider should not be dragging initially.");

        // Simulate mouse press inside the slider's clickable area
        slider.handleMouseInput(slider.getX() + slider.getWidthProp() / 2, slider.getY() + slider.getHeightProp() / 2, true);
        assertTrue(slider.isDragging(), "Slider should be dragging after mouse press inside.");

        // Simulate mouse release
        slider.handleMouseInput(slider.getX() + slider.getWidthProp() / 2, slider.getY() + slider.getHeightProp() / 2, false);
        assertFalse(slider.isDragging(), "Slider should not be dragging after mouse release.");

        // Simulate mouse press outside the slider
        slider.handleMouseInput(0, 0, true); // Click far outside the slider
        assertFalse(slider.isDragging(), "Slider should not start dragging if press is outside.");
    }
}