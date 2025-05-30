import input.GameAction;
import input.KeyBindings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.event.KeyEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for the {@link KeyBindings} class.
 * This class verifies the functionality of managing game action to key code mappings,
 * including default bindings, setting new keys, and handling conflicts.
 */
class KeyBindingsTest {

    private KeyBindings keyBindings;

    /**
     * Sets up the test environment before each test.
     * Initializes a new KeyBindings instance.
     */
    @BeforeEach
    void setUp() {
        keyBindings = new KeyBindings(); // Create new KeyBindings for each test
    }

    /**
     * Tests if the default key bindings are loaded correctly upon initialization.
     */
    @Test
    void testDefaultKeyBindingsLoaded() {
        assertEquals(KeyEvent.VK_A, keyBindings.getKeyCode(GameAction.ROTATE_LEFT), "Default rotate left key is incorrect.");
        assertEquals(KeyEvent.VK_D, keyBindings.getKeyCode(GameAction.ROTATE_RIGHT), "Default rotate right key is incorrect.");
        assertEquals(KeyEvent.VK_W, keyBindings.getKeyCode(GameAction.THRUST), "Default thrust key is incorrect.");
    }

    /**
     * Tests if setting a new key for an action correctly updates the binding.
     */
    @Test
    void testSetKeyChangesBinding() {
        keyBindings.setKey(GameAction.THRUST, KeyEvent.VK_SPACE);
        assertEquals(KeyEvent.VK_SPACE, keyBindings.getKeyCode(GameAction.THRUST), "Key binding for thrust was not updated.");
    }

    /**
     * Tests that if a key is assigned to an action, and that key was previously assigned
     * to another action, the previous action becomes unbound.
     */
    @Test
    void testSetKeyResolvesConflict() {
        // Initially, ROTATE_LEFT is VK_A, THRUST is VK_W
        GameAction unboundAction = keyBindings.setKey(GameAction.THRUST, KeyEvent.VK_A); // Set THRUST to VK_A

        assertEquals(GameAction.ROTATE_LEFT, unboundAction, "ROTATE_LEFT should have been unbound because its key (A) was taken by THRUST.");
        assertEquals(KeyEvent.VK_A, keyBindings.getKeyCode(GameAction.THRUST), "THRUST should now be bound to A.");
        assertEquals(KeyEvent.VK_UNDEFINED, keyBindings.getKeyCode(GameAction.ROTATE_LEFT), "ROTATE_LEFT should be unassigned after its key was taken.");
    }

    /**
     * Tests unassigning a key from an action by setting its key code to VK_UNDEFINED.
     */
    @Test
    void testSetKeyToUnassigned() {
        keyBindings.setKey(GameAction.ROTATE_LEFT, KeyEvent.VK_UNDEFINED);
        assertEquals(KeyEvent.VK_UNDEFINED, keyBindings.getKeyCode(GameAction.ROTATE_LEFT), "ROTATE_LEFT should be unassigned.");
    }

    /**
     * Tests the {@link KeyBindings#getKeyTextForAction(GameAction)} method,
     * ensuring it returns the correct string representation for bound and unbound keys.
     * Note: The original test checked for "[NENASTAVENO]". This is updated to English.
     */
    @Test
    void testGetKeyTextForAction() {
        keyBindings.setKey(GameAction.THRUST, KeyEvent.VK_X);
        assertEquals("X", keyBindings.getKeyTextForAction(GameAction.THRUST).toUpperCase(), "Key text for X is incorrect.");

        keyBindings.setKey(GameAction.THRUST, KeyEvent.VK_UNDEFINED);
        // Assuming getKeyTextStatic is updated to return English text for VK_UNDEFINED
        assertEquals("[UNASSIGNED]", keyBindings.getKeyTextForAction(GameAction.THRUST), "Key text for unassigned is incorrect. Check KeyBindings.getKeyTextStatic method.");
    }
}