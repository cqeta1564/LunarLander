package states;

import input.InputHandler;

/**
 * Manages the different states of the game (e.g., Menu, Playing, Settings).
 * Handles transitions between states and ensures that state lifecycle methods (init, onEnter, onExit) are called.
 */
public class StateManager {

    private final InputHandler inputHandler; // Handler for user input, passed to states
    private GameState currentState; // The currently active game state

    /**
     * Constructs a StateManager.
     *
     * @param inputHandler The InputHandler to be used by the states.
     */
    public StateManager(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        this.currentState = null; // Initialize with no state
    }

    /**
     * Sets the current game state.
     * If there's an existing current state, its {@code onExit} method is called.
     * The new state's {@code init} and {@code onEnter} methods are then called.
     *
     * @param type The type of state to switch to (e.g., MENU, PLAYING, SETTINGS).
     */
    public void setState(StateType type) {
        // Call onExit for the current state if it exists
        if (currentState != null) {
            currentState.onExit();
        }

        // Create the new state based on the type
        switch (type) {
            case MENU:
                currentState = new MenuState(this, inputHandler);
                break;
            case PLAYING:
                currentState = new PlayingState(this, inputHandler);
                break;
            case SETTINGS:
                currentState = new SettingsState(this, inputHandler);
                break;
            default:
                System.err.println("Unknown or unimplemented state: " + type); // Error message for unknown state type
                currentState = null;
        }

        // Initialize and enter the new state if it was successfully created
        if (currentState != null) {
            currentState.init(this); // Initialize the new state
            currentState.onEnter();  // Call onEnter for the new state
        } else {
            System.err.println("CurrentState is null after attempting to set type: " + type + ". A case might be missing in the switch."); // Error if state creation failed
        }
    }

    /**
     * Gets the currently active game state.
     *
     * @return The current GameState, or null if no state is active.
     */
    public GameState getCurrentState() {
        return currentState;
    }

    /**
     * Enum representing the different types of game states available.
     */
    public enum StateType {
        MENU,       // Main menu state
        PLAYING,    // Gameplay state
        SETTINGS    // Settings screen state
    }
}