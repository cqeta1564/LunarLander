import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ui.Slider;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy pro třídu Slider.
 */
class SliderTest {
    private Slider slider;

    @BeforeEach
    void setUp() {
        slider = new Slider("TestSlider", 50, 50, 200, 8, 0, 100, 50);
    }

    @Test
    void testInitialValue() {
        assertEquals(50, slider.getValue(), "Initial slider value is incorrect.");
    }

    @Test
    void testSetValueClampsToMax() {
        slider.setValue(150);
        assertEquals(100, slider.getValue(), "Slider value should be clamped to maximum.");
    }

    @Test
    void testSetValueClampsToMin() {
        slider.setValue(-50);
        assertEquals(0, slider.getValue(), "Slider value should be clamped to minimum.");
    }

    @Test
    void testSetValueWithinBounds() {
        slider.setValue(75);
        assertEquals(75, slider.getValue(), "Slider value should be set correctly within bounds.");
    }

    @Test
    void testUpdateValueFromMouseCalculatesCorrectly() {
        slider.handleMouseInput(50 + 10 / 2, 50, true);
        slider.handleMouseInput(50 + 10 / 2, 50, true);
        assertEquals(0, slider.getValue(), "Slider value should be 0 when mouse is at the beginning.");
        slider.handleMouseInput(50 + 10 / 2, 50, false);

        slider.handleMouseInput(50 + 200 - 10 / 2, 50, true);
        slider.handleMouseInput(50 + 200 - 10 / 2, 50, true);
        assertEquals(100, slider.getValue(), "Slider value should be 100 when mouse is at the end.");
        slider.handleMouseInput(50 + 200 - 10 / 2, 50, false);

        slider.handleMouseInput(150, 50, true);
        slider.handleMouseInput(150, 50, true);
        assertTrue(Math.abs(50 - slider.getValue()) <= 1, "Slider value should be around 50 when mouse is in the middle.");
        slider.handleMouseInput(150, 50, false);
    }

    @Test
    void testDraggingState() {
        Rectangle clickDetectionArea = new Rectangle(slider.getX(), slider.getY() + slider.getHeightProp() / 2 - slider.getKnobHeight() / 2, slider.getWidthProp(), slider.getKnobHeight());

        assertFalse(slider.isDragging(), "Slider should not be dragging initially.");

        slider.handleMouseInput(slider.getX() + slider.getWidthProp() / 2, slider.getY() + slider.getHeightProp() / 2, true);
        assertTrue(slider.isDragging(), "Slider should be dragging after mouse press inside.");

        slider.handleMouseInput(slider.getX() + slider.getWidthProp() / 2, slider.getY() + slider.getHeightProp() / 2, false);
        assertFalse(slider.isDragging(), "Slider should not be dragging after mouse release.");

        slider.handleMouseInput(0, 0, true); // Mimo slider
        assertFalse(slider.isDragging(), "Slider should not start dragging if press is outside.");
    }
}