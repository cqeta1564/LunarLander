package input;

import java.awt.event.*;
import java.util.Arrays;

/**
 * Handles all keyboard and mouse input for the game.
 * It tracks current and previous key states to detect key presses ("just pressed"),
 * manages key bindings for game actions, and processes mouse events.
 * It also supports a mode for listening to a key press to rebind an action.
 */
public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {

    // Keyboard state arrays
    private final boolean[] currentKeys = new boolean[256];  // Tracks if a key is currently held down
    private final boolean[] previousKeys = new boolean[256]; // Tracks key state from the previous frame

    private final KeyBindings keyBindings; // Manages mapping from GameAction to key codes

    // Mouse state
    /**
     * Current X-coordinate of the mouse cursor.
     */
    public int mouseX;
    /**
     * Current Y-coordinate of the mouse cursor.
     */
    public int mouseY;
    /**
     * True if the left mouse button is currently pressed.
     */
    public boolean mouseLeftPressed;
    /**
     * True if the right mouse button is currently pressed.
     */
    public boolean mouseRightPressed;

    // "Just pressed" flags for common keys (updated each frame)
    private boolean escapeJustPressed;
    private boolean enterJustPressed;
    private boolean sKeyJustPressed; // Commonly used for 'Settings'
    private boolean mouseLeftJustPressed;
    private boolean previousMouseLeftState = false; // Tracks mouse left button state from previous frame

    // State for key rebinding UI
    private boolean isListeningForKey = false;      // True if currently waiting for a key press to rebind
    private GameAction actionToRebind = null;       // The GameAction being rebound
    private int rawKeyCodeForRebind = KeyEvent.VK_UNDEFINED; // Key code captured for rebinding

    /**
     * Constructs an InputHandler.
     * Initializes key state arrays and loads default key bindings.
     */
    public InputHandler() {
        this.keyBindings = new KeyBindings(); // Initialize with default bindings
        Arrays.fill(currentKeys, false);
        Arrays.fill(previousKeys, false);
    }

    /**
     * Updates the "just pressed" state for specific keys and mouse buttons.
     * This should be called once per game loop, before processing input for game logic.
     */
    public void update() {
        escapeJustPressed = isKeyDown(KeyEvent.VK_ESCAPE) && !wasKeyDown(KeyEvent.VK_ESCAPE);
        enterJustPressed = isKeyDown(KeyEvent.VK_ENTER) && !wasKeyDown(KeyEvent.VK_ENTER);
        sKeyJustPressed = isKeyDown(KeyEvent.VK_S) && !wasKeyDown(KeyEvent.VK_S);

        mouseLeftJustPressed = mouseLeftPressed && !previousMouseLeftState;
    }

    /**
     * Finalizes input processing for the current frame.
     * Copies current key states to previous key states and updates previous mouse state.
     * This should be called after all input has been processed for the frame.
     */
    public void finishFrame() {
        System.arraycopy(currentKeys, 0, previousKeys, 0, currentKeys.length);
        previousMouseLeftState = mouseLeftPressed;
    }

    // --- KeyListener Methods ---
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < currentKeys.length) {
            currentKeys[keyCode] = true; // Mark key as currently pressed
        }

        // If in key rebinding mode, capture the pressed key
        if (isListeningForKey) {
            if (keyCode == KeyEvent.VK_ESCAPE || isValidKeyForBinding(keyCode)) {
                rawKeyCodeForRebind = keyCode; // Store the key code (or ESC to cancel)
            }
            // Note: The actual rebinding logic happens when this rawKeyCodeForRebind is consumed.
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < currentKeys.length) {
            currentKeys[keyCode] = false; // Mark key as released
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Not used for game actions, but required by KeyListener interface.
        // keyTyped is for character input, keyPressed/Released are for physical key presses.
    }

    /**
     * Checks if a given keyCode is valid for binding to a game action.
     * Excludes modifier keys and system keys that might be problematic.
     *
     * @param keyCode The KeyEvent keyCode to check.
     * @return True if the key is valid for binding, false otherwise.
     */
    private boolean isValidKeyForBinding(int keyCode) {
        switch (keyCode) {
            // Disallow binding of common modifier keys on their own,
            // and keys that might interfere with OS or have unusual behavior.
            case KeyEvent.VK_SHIFT:
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_META: // Command/Windows key
            case KeyEvent.VK_CAPS_LOCK:
            case KeyEvent.VK_NUM_LOCK:
            case KeyEvent.VK_SCROLL_LOCK:
            case KeyEvent.VK_PRINTSCREEN:
            case KeyEvent.VK_PAUSE:
                return false; // These keys are not typically used as primary action keys
            default:
                return true; // All other keys are considered valid
        }
    }

    // --- Public Input State Accessors ---

    /**
     * Checks if a specific key is currently held down.
     *
     * @param keyCode The KeyEvent keyCode to check.
     * @return True if the key is currently pressed, false otherwise.
     */
    public boolean isKeyDown(int keyCode) {
        return keyCode >= 0 && keyCode < currentKeys.length && currentKeys[keyCode];
    }

    /**
     * Checks if a specific key was held down in the previous frame.
     *
     * @param keyCode The KeyEvent keyCode to check.
     * @return True if the key was pressed in the previous frame, false otherwise.
     */
    private boolean wasKeyDown(int keyCode) {
        return keyCode >= 0 && keyCode < previousKeys.length && previousKeys[keyCode];
    }

    /**
     * Checks if the Escape key was just pressed in this frame.
     *
     * @return True if Escape was just pressed, false otherwise.
     */
    public boolean isEscJustPressed() {
        return escapeJustPressed;
    }

    /**
     * Checks if the Enter key was just pressed in this frame.
     *
     * @return True if Enter was just pressed, false otherwise.
     */
    public boolean isEnterJustPressed() {
        return enterJustPressed;
    }

    /**
     * Checks if the 'S' key was just pressed in this frame.
     *
     * @return True if 'S' was just pressed, false otherwise.
     */
    public boolean isSKeyJustPressed() {
        return sKeyJustPressed;
    }

    /**
     * Checks if the left mouse button was just pressed in this frame.
     *
     * @return True if the left mouse button was just pressed, false otherwise.
     */
    public boolean isMouseLeftJustPressed() {
        return mouseLeftJustPressed;
    }


    /**
     * Checks if a game action (e.g., THRUST, ROTATE_LEFT) is currently active,
     * based on its bound key being held down.
     *
     * @param action The GameAction to check.
     * @return True if the action's key is pressed, false otherwise.
     */
    public boolean isActionActive(GameAction action) {
        int keyCode = keyBindings.getKeyCode(action);
        return isKeyDown(keyCode);
    }

    // --- Key Rebinding Methods ---

    /**
     * Starts listening for a key press to rebind the specified game action.
     *
     * @param action The GameAction to rebind.
     */
    public void startListeningForKey(GameAction action) {
        isListeningForKey = true;
        actionToRebind = action;
        rawKeyCodeForRebind = KeyEvent.VK_UNDEFINED; // Reset any previously captured key
        System.out.println("InputHandler: Started listening for key to rebind " + action.name());
    }

    /**
     * Stops listening for a key press for rebinding.
     */
    public void stopListeningForKey() {
        isListeningForKey = false;
        // actionToRebind is typically cleared by the state managing the rebind UI
        System.out.println("InputHandler: Stopped listening for key.");
    }

    /**
     * Checks if the InputHandler is currently in key listening mode for rebinding.
     *
     * @return True if listening for a key, false otherwise.
     */
    public boolean isListening() {
        return isListeningForKey;
    }

    /**
     * Gets the game action that is currently being rebound.
     *
     * @return The GameAction being rebound, or null if not in listening mode.
     */
    public GameAction getActionBeingRebound() {
        return actionToRebind;
    }

    /**
     * Consumes the raw key code captured during key listening mode.
     * This method should be called by the UI managing the rebinding process
     * to get the key pressed by the user. It resets the internal raw key code.
     *
     * @return The KeyEvent keyCode of the pressed key, or KeyEvent.VK_UNDEFINED if no valid key was captured.
     */
    public int consumeRawKeyCodeForRebind() {
        int code = rawKeyCodeForRebind;
        rawKeyCodeForRebind = KeyEvent.VK_UNDEFINED; // Reset after consumption
        return code;
    }

    /**
     * Gets the KeyBindings object associated with this input handler.
     *
     * @return The KeyBindings instance.
     */
    public KeyBindings getKeyBindings() {
        return keyBindings;
    }

    // --- MouseListener Methods ---
    @Override
    public void mouseClicked(MouseEvent e) {
        // Not typically used for continuous game input, but available.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX(); // Update mouse position
        mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) mouseLeftPressed = true;   // Left button
        else if (e.getButton() == MouseEvent.BUTTON3) mouseRightPressed = true; // Right button
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseX = e.getX(); // Update mouse position
        mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) mouseLeftPressed = false;  // Left button released
        else if (e.getButton() == MouseEvent.BUTTON3) mouseRightPressed = false; // Right button released
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Called when mouse cursor enters the component.
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Called when mouse cursor exits the component.
    }

    // --- MouseMotionListener Methods ---
    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX(); // Update mouse position while dragging
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX(); // Update mouse position when moved (no buttons pressed)
        mouseY = e.getY();
    }
}