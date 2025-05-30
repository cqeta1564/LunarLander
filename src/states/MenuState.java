package states;

import audio.AudioManager;
import core.Game;
import input.InputHandler;

import java.awt.*;

/**
 * Represents the main menu state of the game.
 * Allows the player to start the game, go to settings, or exit.
 */
public class MenuState implements GameState {

    private final StateManager stateManager; // Manages transitions between states
    private final InputHandler inputHandler; // Handles user input

    /**
     * Constructs a MenuState.
     *
     * @param stateManager The game's state manager.
     * @param inputHandler The game's input handler.
     */
    public MenuState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
    }

    @Override
    public void init(StateManager manager) {
        // Initialization specific to this state, if needed when first created.
        // For MenuState, often onEnter is sufficient.
        System.out.println("MenuState initialized.");
    }

    @Override
    public void onEnter() {
        // Called every time this state becomes active.
        System.out.println("Entered MenuState.");
        // Reset any per-entry state if necessary (e.g., animation timers, selected option)
    }

    @Override
    public void update(double deltaTime) {
        // Update logic for the menu (e.g., animations, blinking text).
        // For this menu, input handling is the primary update task.
        handleInput();
    }

    @Override
    public void handleInput() {
        if (inputHandler.isEnterJustPressed()) { // Player presses Enter
            AudioManager.getInstance().playSound(AudioManager.SoundEffect.BUTTON_CLICK);
            stateManager.setState(StateManager.StateType.PLAYING); // Switch to Playing state
        } else if (inputHandler.isSKeyJustPressed()) { // Player presses 'S'
            AudioManager.getInstance().playSound(AudioManager.SoundEffect.BUTTON_CLICK);
            stateManager.setState(StateManager.StateType.SETTINGS); // Switch to Settings state
        } else if (inputHandler.isEscJustPressed()) { // Player presses Escape
            AudioManager.getInstance().playSound(AudioManager.SoundEffect.BUTTON_CLICK);
            // Brief pause to allow sound to play before exiting
            try {
                Thread.sleep(100); // Small delay
            } catch (InterruptedException ignored) {
                // Restore interrupt status if thread is interrupted during sleep
                Thread.currentThread().interrupt();
            }
            System.exit(0); // Exit the game
        }
    }

    @Override
    public void render(Graphics2D g) {
        // Enable text anti-aliasing for smoother text
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Background
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        // Game Title
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "Lunar Lander";
        FontMetrics fmTitle = g.getFontMetrics(); // Get font metrics for centering
        int titleWidth = fmTitle.stringWidth(title);
        g.drawString(title, (Game.DEFAULT_WIDTH - titleWidth) / 2, 150); // Centered horizontally

        // Menu Options
        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.setColor(Color.WHITE);
        FontMetrics fmOptions = g.getFontMetrics();

        String playText = "Start Game (Enter)";
        int playTextWidth = fmOptions.stringWidth(playText);
        g.drawString(playText, (Game.DEFAULT_WIDTH - playTextWidth) / 2, 300);

        String settingsText = "Settings (S)";
        int settingsTextWidth = fmOptions.stringWidth(settingsText);
        g.drawString(settingsText, (Game.DEFAULT_WIDTH - settingsTextWidth) / 2, 350);

        String exitText = "Exit (Esc)";
        int exitTextWidth = fmOptions.stringWidth(exitText);
        g.drawString(exitText, (Game.DEFAULT_WIDTH - exitTextWidth) / 2, 400);
    }

    @Override
    public void onExit() {
        // Called when this state is no longer active.
        System.out.println("Exited MenuState.");
        // Clean up resources or save state if necessary.
    }
}