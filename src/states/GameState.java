package states;

import java.awt.*;

/**
 * Interface defining the contract for all game states (e.g., Menu, Playing, Settings).
 * Each game state must implement methods for initialization, handling entry/exit,
 * updating game logic, rendering, and processing input.
 */
public interface GameState {

    /**
     * Initializes the game state.
     * This is called once when the state is first created or set by the StateManager.
     * It can be used for one-time setup that requires the StateManager.
     *
     * @param stateManager The StateManager controlling this state.
     */
    void init(StateManager stateManager);

    /**
     * Called when this game state becomes the active state.
     * Use this for setup that needs to occur each time the state is entered
     * (e.g., resetting scores, positions, UI elements).
     */
    void onEnter();

    /**
     * Updates the game logic for this state.
     * This method is called repeatedly by the game loop.
     *
     * @param deltaTime The time elapsed since the last update, in seconds.
     *                  Used for time-dependent calculations (e.g., physics).
     */
    void update(double deltaTime);

    /**
     * Renders the game state to the screen.
     * This method is called repeatedly by the game loop after updates.
     *
     * @param g The Graphics2D context to draw on.
     */
    void render(Graphics2D g);

    /**
     * Handles user input for this state.
     * This is typically called within the update method or directly by the StateManager
     * before or after the update.
     * Implementation might involve querying an InputHandler.
     */
    void handleInput();

    /**
     * Called when this game state is being exited (i.e., when a new state becomes active).
     * Use this for cleanup or saving state before switching away.
     */
    void onExit();
}