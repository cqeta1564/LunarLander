package states;

import audio.AudioManager;
import core.Game;
import input.GameAction;
import input.InputHandler;
import input.KeyBindings;
import ui.Slider;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the settings state of the game.
 * Allows the player to adjust game volume and rebind controls.
 */
public class SettingsState implements GameState {

    private final StateManager stateManager; // Manages transitions between states
    private final InputHandler inputHandler; // Handles user input
    private final AudioManager audioManager; // Manages audio playback and volume
    private final Slider volumeSlider;       // UI slider for volume control
    private final KeyBindings keyBindings;   // Manages game action key mappings

    // UI layout constants for key binding display
    private final List<Rectangle> keyBindClickAreas; // Clickable areas for rebinding keys
    private final int KEYBIND_START_Y = 280;         // Initial Y position for key binding options
    private final int KEYBIND_ITEM_HEIGHT = 25;      // Height of each key binding item
    private final int KEYBIND_ITEM_SPACING = 15;     // Spacing between key binding items
    private final int KEYBIND_AREA_WIDTH = 350;      // Width of the key binding area

    private GameAction currentlySelectedActionToRebind = null; // The action currently being rebound

    /**
     * Constructs a SettingsState.
     *
     * @param stateManager The game's state manager.
     * @param inputHandler The game's input handler.
     */
    public SettingsState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
        this.audioManager = AudioManager.getInstance(); // Get the singleton AudioManager instance
        this.keyBindings = inputHandler.getKeyBindings(); // Get key bindings from input handler

        // Initialize the volume slider
        int sliderX = Game.DEFAULT_WIDTH / 2 - 100; // Center slider horizontally
        int sliderY = 180; // Vertical position for the slider
        this.volumeSlider = new Slider("Volume", sliderX, sliderY, 200, 8, 0, 100, audioManager.getGlobalVolume()); // Create slider

        this.keyBindClickAreas = new ArrayList<>(); // Initialize list for key binding clickable areas
        recalculateClickAreas(); // Calculate positions for key binding UI elements
    }

    /**
     * Recalculates the clickable areas for the key binding options.
     * This is useful if the number of game actions changes or if layout needs to be dynamic.
     */
    private void recalculateClickAreas() {
        keyBindClickAreas.clear(); // Clear existing areas
        int currentY = KEYBIND_START_Y; // Start Y position for the first item
        for (GameAction action : GameAction.getAllActions()) { // Iterate through all game actions
            keyBindClickAreas.add(new Rectangle(Game.DEFAULT_WIDTH / 2 - KEYBIND_AREA_WIDTH / 2, currentY, KEYBIND_AREA_WIDTH, KEYBIND_ITEM_HEIGHT)); //
            currentY += KEYBIND_ITEM_HEIGHT + KEYBIND_ITEM_SPACING; // Increment Y for the next item
        }
    }

    /**
     * Initializes the SettingsState. Called when the state is first created.
     * Sets the volume slider to the current global volume and ensures key listening is off.
     *
     * @param manager The StateManager that manages this state.
     */
    @Override
    public void init(StateManager manager) {
        volumeSlider.setValue(audioManager.getGlobalVolume()); // Sync slider with current volume
        currentlySelectedActionToRebind = null; // No action is being rebound initially
        if (inputHandler.isListening()) { // If input handler was left in listening state
            inputHandler.stopListeningForKey(); // Stop listening
        }
    }

    /**
     * Called every time this state becomes active.
     * Resets the volume slider and key rebinding state.
     */
    @Override
    public void onEnter() {
        volumeSlider.setValue(audioManager.getGlobalVolume()); // Sync slider with current volume
        currentlySelectedActionToRebind = null; // No action is being rebound
        if (inputHandler.isListening()) { // If input handler was left in listening state
            inputHandler.stopListeningForKey(); // Stop listening
        }
    }

    /**
     * Updates the settings state logic.
     * Handles input and updates the volume slider's value and effect.
     *
     * @param deltaTime The time elapsed since the last update, in seconds.
     */
    @Override
    public void update(double deltaTime) {
        handleInput(); // Process user inputs

        // Update volume slider and audio manager if not currently waiting for a key bind
        if (stateManager.getCurrentState() == this && !inputHandler.isListening()) {
            int previousSliderDiscreteValue = volumeSlider.getValue(); // Get slider value before handling mouse input
            volumeSlider.handleMouseInput(inputHandler.mouseX, inputHandler.mouseY, inputHandler.mouseLeftPressed); // Update slider based on mouse
            int currentSliderDiscreteValue = volumeSlider.getValue(); // Get slider value after handling mouse input

            // Play a sound if the slider value changes due to dragging
            if (volumeSlider.isDragging() && inputHandler.mouseLeftPressed && currentSliderDiscreteValue != previousSliderDiscreteValue) {
                AudioManager.getInstance().playSound(AudioManager.SoundEffect.BUTTON_CLICK);
            }

            // If the slider value changed, update the global audio volume
            if (audioManager != null && currentSliderDiscreteValue != audioManager.getGlobalVolume()) {
                audioManager.setGlobalVolume(currentSliderDiscreteValue);
            }
        }
    }

    /**
     * Handles user input for the settings screen.
     * Manages key rebinding logic and navigation.
     */
    @Override
    public void handleInput() {
        volumeSlider.handleMouseInput(inputHandler.mouseX, inputHandler.mouseY, inputHandler.mouseLeftPressed); // Always allow slider interaction

        if (inputHandler.isListening()) { // If currently waiting for a key press for rebinding
            int rawKeyCode = inputHandler.consumeRawKeyCodeForRebind(); // Get the pressed key
            if (rawKeyCode != KeyEvent.VK_UNDEFINED) { // If a key was pressed (or Escape)
                GameAction actionBeingRebound = inputHandler.getActionBeingRebound(); // Get the action being rebound
                if (rawKeyCode == KeyEvent.VK_ESCAPE) {
                    // User pressed Escape to cancel rebinding, do nothing with the key
                } else {
                    // A valid key was pressed for rebinding
                    if (actionBeingRebound != null) {
                        keyBindings.setKey(actionBeingRebound, rawKeyCode); // Set the new key for the action
                    }
                }
                inputHandler.stopListeningForKey(); // Stop listening for keys
                currentlySelectedActionToRebind = null; // Clear the action being rebound
            }
        } else { // Not currently listening for a key press
            if (inputHandler.isMouseLeftJustPressed()) { // Check for mouse click
                boolean clickedOnBindable = false;
                // Check if the click was on any key binding area
                for (int i = 0; i < GameAction.getAllActions().length; i++) {
                    if (keyBindClickAreas.get(i).contains(inputHandler.mouseX, inputHandler.mouseY)) { // Check if click is within a bindable area
                        AudioManager.getInstance().playSound(AudioManager.SoundEffect.BUTTON_CLICK);
                        currentlySelectedActionToRebind = GameAction.getAllActions()[i]; // Set the action to rebind
                        inputHandler.startListeningForKey(currentlySelectedActionToRebind); // Start listening for a key press
                        clickedOnBindable = true;
                        break;
                    }
                }
                if (clickedOnBindable) {
                    return; // Don't process other inputs if a rebind action was initiated
                }
            }

            // Handle Escape key to return to menu
            if (inputHandler.isEscJustPressed()) {
                AudioManager.getInstance().playSound(AudioManager.SoundEffect.BUTTON_CLICK);
                stateManager.setState(StateManager.StateType.MENU); // Change state to Menu
            }
        }
    }

    /**
     * Renders the settings screen.
     * Draws the title, volume slider, key binding options, and navigation hints.
     *
     * @param g The Graphics2D context to draw on.
     */
    @Override
    public void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); // Enable anti-aliasing for text

        // Draw background
        g.setColor(Color.DARK_GRAY.darker());
        g.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        // Draw title
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String titleText = "Settings"; // Title text in English
        FontMetrics fmTitle = g.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(titleText);
        g.drawString(titleText, (Game.DEFAULT_WIDTH - titleWidth) / 2, 80);

        // Render volume slider
        volumeSlider.render(g);

        // Draw key binding section title
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.CYAN);
        String keybindTitle = "Controls"; // Section title in English
        FontMetrics fmKeybindTitle = g.getFontMetrics();
        int keybindTitleWidth = fmKeybindTitle.stringWidth(keybindTitle);
        g.drawString(keybindTitle, (Game.DEFAULT_WIDTH - keybindTitleWidth) / 2, KEYBIND_START_Y - 40);

        // Draw key binding options
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        FontMetrics fmKeybindItem = g.getFontMetrics();
        for (int i = 0; i < GameAction.getAllActions().length; i++) {
            GameAction action = GameAction.getAllActions()[i];
            Rectangle clickArea = keyBindClickAreas.get(i);
            String actionDisplayName = action.getDisplayName() + ": "; // Get action's display name
            String keyName;
            if (currentlySelectedActionToRebind == action && inputHandler.isListening()) { // If this action is being rebound
                keyName = "[PRESS A KEY...]"; // Prompt for key press
                g.setColor(Color.YELLOW); // Highlight in yellow
            } else {
                keyName = keyBindings.getKeyTextForAction(action); // Get current key for the action
                if (clickArea.contains(inputHandler.mouseX, inputHandler.mouseY) && !inputHandler.isListening()) { // If mouse hovers over it
                    g.setColor(Color.ORANGE); // Highlight in orange
                } else {
                    g.setColor(Color.WHITE); // Default color
                }
            }
            // Draw the action name and its bound key
            g.drawString(actionDisplayName + keyName, clickArea.x + 10, clickArea.y + fmKeybindItem.getAscent() + (KEYBIND_ITEM_HEIGHT - fmKeybindItem.getHeight()) / 2);
        }

        // Display instruction if listening for a key
        if (inputHandler.isListening()) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 14));
            g.drawString("Press ESC to cancel key change.", Game.DEFAULT_WIDTH / 2 - 100, KEYBIND_START_Y + (GameAction.getAllActions().length * (KEYBIND_ITEM_HEIGHT + KEYBIND_ITEM_SPACING)) + 20);
        }

        // Draw "Back to Menu" instruction
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String backText = "Back to Menu (Esc)"; // Back text in English
        FontMetrics fmBack = g.getFontMetrics();
        int backTextWidth = fmBack.stringWidth(backText);
        g.drawString(backText, (Game.DEFAULT_WIDTH - backTextWidth) / 2, Game.DEFAULT_HEIGHT - 60);
    }

    /**
     * Called when this state is exited.
     * Ensures that key listening is stopped if it was active.
     */
    @Override
    public void onExit() {
        // If we were listening for a key, stop it to avoid issues in other states
        if (inputHandler.isListening()) {
            inputHandler.stopListeningForKey();
            currentlySelectedActionToRebind = null;
        }
    }
}