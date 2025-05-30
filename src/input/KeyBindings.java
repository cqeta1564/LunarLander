package input;

import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;

/**
 * Manages the mapping between game actions ({@link GameAction}) and keyboard key codes ({@link KeyEvent}).
 * Allows loading default bindings, setting new bindings, and resolving conflicts if a key is reassigned.
 */
public class KeyBindings {
    /**
     * Stores the mapping from GameAction to its corresponding KeyEvent.VK_ keyCode.
     */
    private final EnumMap<GameAction, Integer> keyMap;

    /**
     * Constructs a KeyBindings object and loads the default key mappings.
     */
    public KeyBindings() {
        keyMap = new EnumMap<>(GameAction.class);
        loadDefaults();
    }

    /**
     * Gets a human-readable text representation of a given key code.
     * For example, KeyEvent.VK_W becomes "W".
     *
     * @param keyCode The {@link KeyEvent} virtual key code.
     * @return A string representation of the key, or "[UNASSIGNED]" if the key code is VK_UNDEFINED.
     */
    public static String getKeyTextStatic(int keyCode) {
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return "[UNASSIGNED]";
        }
        return KeyEvent.getKeyText(keyCode).toUpperCase(); // Standard Java method for key text
    }

    /**
     * Loads the default key bindings for all game actions.
     * This is called during construction and can be used to reset bindings.
     */
    public void loadDefaults() {
        keyMap.clear(); // Clear any existing bindings before loading defaults
        keyMap.put(GameAction.ROTATE_LEFT, KeyEvent.VK_A);
        keyMap.put(GameAction.ROTATE_RIGHT, KeyEvent.VK_D);
        keyMap.put(GameAction.THRUST, KeyEvent.VK_W);
        // Add other default bindings here if new GameActions are created
    }

    /**
     * Gets the key code currently bound to the specified game action.
     *
     * @param action The {@link GameAction} to query.
     * @return The {@link KeyEvent} virtual key code, or {@link KeyEvent#VK_UNDEFINED} if the action is not bound.
     */
    public int getKeyCode(GameAction action) {
        return keyMap.getOrDefault(action, KeyEvent.VK_UNDEFINED);
    }

    /**
     * Sets or changes the key binding for a game action.
     * If the new key code is already bound to another action, that other action will become unbound.
     *
     * @param actionToBind The {@link GameAction} to bind or rebind.
     * @param newKeyCode   The new {@link KeyEvent} virtual key code to assign.
     *                     If {@link KeyEvent#VK_UNDEFINED}, the action will be unbound.
     * @return The {@link GameAction} that was previously bound to {@code newKeyCode},
     * or {@code null} if {@code newKeyCode} was not previously bound or if the binding was cleared.
     */
    public GameAction setKey(GameAction actionToBind, int newKeyCode) {
        if (newKeyCode == KeyEvent.VK_UNDEFINED) {
            // If unbinding, simply remove the action from the map
            keyMap.remove(actionToBind);
            return null; // No conflict to report
        }

        GameAction previouslyBoundAction = null;
        // Check if the newKeyCode is already used by another action
        for (Map.Entry<GameAction, Integer> entry : keyMap.entrySet()) {
            if (entry.getValue() == newKeyCode && entry.getKey() != actionToBind) {
                previouslyBoundAction = entry.getKey(); // Found a conflict
                break;
            }
        }

        // If there was a conflict, unbind the action that was using newKeyCode
        if (previouslyBoundAction != null) {
            keyMap.remove(previouslyBoundAction);
            System.out.println("Key " + getKeyTextStatic(newKeyCode) + " was previously bound to " + previouslyBoundAction.getDisplayName() + ". It is now unbound.");
        }

        // Assign the new key code to the specified action
        keyMap.put(actionToBind, newKeyCode);
        System.out.println("Action " + actionToBind.getDisplayName() + " is now bound to key " + getKeyTextStatic(newKeyCode));

        return previouslyBoundAction; // Return the action that was displaced, if any
    }

    /**
     * Gets a human-readable text representation of the key currently bound to the specified game action.
     *
     * @param action The {@link GameAction} to query.
     * @return A string representation of the bound key (e.g., "W", "SPACE"),
     * or "[UNASSIGNED]" if the action is not bound.
     */
    public String getKeyTextForAction(GameAction action) {
        return getKeyTextStatic(getKeyCode(action));
    }
}