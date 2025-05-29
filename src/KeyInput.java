// KeyInput.java
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer; // For listeners

public class KeyInput extends KeyAdapter {
    private Set<Integer> keysPressed = new HashSet<>();
    // For specific key down/up event listeners (like in JS)
    private Map<Integer, Runnable> keyDownListeners = new HashMap<>();
    private Map<Integer, Runnable> keyUpListeners = new HashMap<>();

    // Standard virtual key codes from KeyEvent
    public static final int UP = KeyEvent.VK_UP;
    public static final int LEFT = KeyEvent.VK_LEFT;
    public static final int RIGHT = KeyEvent.VK_RIGHT;
    public static final int DOWN = KeyEvent.VK_DOWN;
    // Add more key codes as needed, e.g., KeyEvent.VK_SPACE, KeyEvent.VK_S

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        keysPressed.add(keyCode);
        if (keyDownListeners.containsKey(keyCode)) {
            keyDownListeners.get(keyCode).run();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        keysPressed.remove(keyCode);
        if (keyUpListeners.containsKey(keyCode)) {
            keyUpListeners.get(keyCode).run();
        }
    }

    public boolean isKeyDown(int keyCode) {
        return keysPressed.contains(keyCode);
    }

    public boolean isKeyDown(char keyChar) {
        return isKeyDown(KeyEvent.getExtendedKeyCodeForChar(keyChar));
    }

    public void addKeyDownListener(int keyCode, Runnable action) {
        keyDownListeners.put(keyCode, action);
    }

    public void addKeyDownListener(char keyChar, Runnable action) {
        addKeyDownListener(KeyEvent.getExtendedKeyCodeForChar(keyChar), action);
    }

    public void addKeyUpListener(int keyCode, Runnable action) {
        keyUpListeners.put(keyCode, action);
    }

    public void addKeyUpListener(char keyChar, Runnable action) {
        addKeyUpListener(KeyEvent.getExtendedKeyCodeForChar(keyChar), action);
    }
}