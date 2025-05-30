package input;

/**
 * Enumerates the possible game actions that can be mapped to input keys.
 * Each action has a display name for UI purposes.
 */
public enum GameAction {
    /**
     * Action for rotating the lander to the left.
     */
    ROTATE_LEFT("Rotate Left"),
    /**
     * Action for rotating the lander to the right.
     */
    ROTATE_RIGHT("Rotate Right"),
    /**
     * Action for applying thrust to the lander's main engine.
     */
    THRUST("Engine Thrust");

    private final String displayName; // User-friendly name for the action

    /**
     * Constructor for GameAction.
     *
     * @param displayName The user-friendly name for this action (e.g., for settings screen).
     */
    GameAction(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Gets an array of all available game actions.
     *
     * @return An array containing all GameAction enum constants.
     */
    public static GameAction[] getAllActions() {
        return values(); // Returns all enum constants
    }

    /**
     * Gets the display name of the game action.
     * This is typically used for UI elements like key binding settings.
     *
     * @return The user-friendly display name of the action.
     */
    public String getDisplayName() {
        return displayName;
    }
}