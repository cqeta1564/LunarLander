import input.GameAction;
import input.KeyBindings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Testy pro KeyBindings.
 */
class KeyBindingsTest {

    private KeyBindings keyBindings;

    @BeforeEach
    void setUp() {
        keyBindings = new KeyBindings();
    }

    @Test
    void testDefaultKeyBindingsLoaded() {
        assertEquals(KeyEvent.VK_A, keyBindings.getKeyCode(GameAction.ROTATE_LEFT), "Default rotate left key is incorrect.");
        assertEquals(KeyEvent.VK_D, keyBindings.getKeyCode(GameAction.ROTATE_RIGHT), "Default rotate right key is incorrect.");
        assertEquals(KeyEvent.VK_W, keyBindings.getKeyCode(GameAction.THRUST), "Default thrust key is incorrect.");
    }

    @Test
    void testSetKeyChangesBinding() {
        keyBindings.setKey(GameAction.THRUST, KeyEvent.VK_SPACE);
        assertEquals(KeyEvent.VK_SPACE, keyBindings.getKeyCode(GameAction.THRUST), "Key binding for thrust was not updated.");
    }

    @Test
    void testSetKeyResolvesConflict() {
        GameAction unboundAction = keyBindings.setKey(GameAction.THRUST, KeyEvent.VK_A);

        assertEquals(GameAction.ROTATE_LEFT, unboundAction, "ROTATE_LEFT should have been unbound.");
        assertEquals(KeyEvent.VK_A, keyBindings.getKeyCode(GameAction.THRUST), "THRUST should now be A.");
        assertEquals(KeyEvent.VK_UNDEFINED, keyBindings.getKeyCode(GameAction.ROTATE_LEFT), "ROTATE_LEFT should be unassigned.");
    }

    @Test
    void testSetKeyToUnassigned() {
        keyBindings.setKey(GameAction.ROTATE_LEFT, KeyEvent.VK_UNDEFINED);
        assertEquals(KeyEvent.VK_UNDEFINED, keyBindings.getKeyCode(GameAction.ROTATE_LEFT), "ROTATE_LEFT should be unassigned.");
    }

    @Test
    void testGetKeyTextForAction() {
        keyBindings.setKey(GameAction.THRUST, KeyEvent.VK_X);
        assertEquals("X", keyBindings.getKeyTextForAction(GameAction.THRUST).toUpperCase(), "Key text for X is incorrect.");

        keyBindings.setKey(GameAction.THRUST, KeyEvent.VK_UNDEFINED);
        assertEquals("[NENASTAVENO]", keyBindings.getKeyTextForAction(GameAction.THRUST), "Key text for unassigned is incorrect.");
    }
}