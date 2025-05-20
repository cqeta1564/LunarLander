package input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {

    private final boolean[] currentKeys = new boolean[256];
    private final boolean[] previousKeys = new boolean[256];

    // Příznaky "právě stisknuto"
    private boolean escapeJustPressed;
    private boolean enterJustPressed;
    private boolean sKeyJustPressed;
    private boolean mouseLeftJustPressed;

    // Stavy myši
    public int mouseX, mouseY;
    public boolean mouseLeftPressed;  // true, pokud je levé tlačítko aktuálně drženo
    public boolean mouseRightPressed;
    private boolean previousMouseLeftState = false; // Pro výpočet mouseLeftJustPressed

    // Remapování kláves
    private final KeyBindings keyBindings;
    private boolean isListeningForKey = false;
    private GameAction actionToRebind = null;
    private int rawKeyCodeForRebind = KeyEvent.VK_UNDEFINED;

    public InputHandler() {
        this.keyBindings = new KeyBindings();
        Arrays.fill(currentKeys, false);
        Arrays.fill(previousKeys, false);
    }

    /**
     * Voláno JEDNOU za herní cyklus, na jeho začátku (např. v Game.run() nebo Game.updateGameLogic()).
     * Vypočítá "just pressed" stavy pro tento snímek na základě změn od předchozího snímku.
     * Přejmenováno z preUpdate().
     */
    public void update() {
        escapeJustPressed = isKeyDown(KeyEvent.VK_ESCAPE) && !wasKeyDown(KeyEvent.VK_ESCAPE);
        enterJustPressed = isKeyDown(KeyEvent.VK_ENTER) && !wasKeyDown(KeyEvent.VK_ENTER);
        sKeyJustPressed = isKeyDown(KeyEvent.VK_S) && !wasKeyDown(KeyEvent.VK_S);

        mouseLeftJustPressed = mouseLeftPressed && !previousMouseLeftState;
    }

    /**
     * Voláno na konci každého herního cyklu.
     * Uloží aktuální stav kláves a myši pro použití v příštím cyklu jako "předchozí stav".
     * Přejmenováno z postUpdate().
     */
    public void finishFrame() {
        System.arraycopy(currentKeys, 0, previousKeys, 0, currentKeys.length);
        previousMouseLeftState = mouseLeftPressed;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < currentKeys.length) {
            currentKeys[keyCode] = true;
        }

        if (isListeningForKey) {
            if (keyCode == KeyEvent.VK_ESCAPE || isValidKeyForBinding(keyCode)) {
                rawKeyCodeForRebind = keyCode;
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >= 0 && keyCode < currentKeys.length) {
            currentKeys[keyCode] = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { /* Nepoužíváno */ }

    private boolean isValidKeyForBinding(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_SHIFT:
            case KeyEvent.VK_CONTROL:
            case KeyEvent.VK_ALT:
            case KeyEvent.VK_META:
            case KeyEvent.VK_CAPS_LOCK:
            case KeyEvent.VK_NUM_LOCK:
            case KeyEvent.VK_SCROLL_LOCK:
            case KeyEvent.VK_PRINTSCREEN:
            case KeyEvent.VK_PAUSE:
                return false;
            default:
                return true;
        }
    }

    public boolean isKeyDown(int keyCode) {
        return keyCode >= 0 && keyCode < currentKeys.length && currentKeys[keyCode];
    }

    private boolean wasKeyDown(int keyCode) { // private, protože stavy by měly používat "justPressed" nebo "isDown"
        return keyCode >= 0 && keyCode < previousKeys.length && previousKeys[keyCode];
    }

    public boolean isEscJustPressed() {
        return escapeJustPressed;
    }

    public boolean isEnterJustPressed() {
        return enterJustPressed;
    }

    public boolean isSKeyJustPressed() {
        return sKeyJustPressed;
    }

    public boolean isMouseLeftJustPressed() {
        return mouseLeftJustPressed;
    }


    public boolean isActionActive(GameAction action) {
        int keyCode = keyBindings.getKeyCode(action);
        return isKeyDown(keyCode);
    }

    public void startListeningForKey(GameAction action) {
        isListeningForKey = true;
        actionToRebind = action;
        rawKeyCodeForRebind = KeyEvent.VK_UNDEFINED;
    }

    public void stopListeningForKey() {
        isListeningForKey = false;
        // actionToRebind se nemaže zde, ale až ve stavu po úspěšném rebindu/zrušení
    }

    public boolean isListening() {
        return isListeningForKey;
    }

    public GameAction getActionBeingRebound() {
        return actionToRebind;
    }

    public int consumeRawKeyCodeForRebind() {
        int code = rawKeyCodeForRebind;
        rawKeyCodeForRebind = KeyEvent.VK_UNDEFINED;
        return code;
    }

    public KeyBindings getKeyBindings() {
        return keyBindings;
    }

    @Override
    public void mouseClicked(MouseEvent e) { /* Zatím explicitně nepoužíváme */ }

    @Override
    public void mousePressed(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) mouseLeftPressed = true;
        else if (e.getButton() == MouseEvent.BUTTON3) mouseRightPressed = true;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
        if (e.getButton() == MouseEvent.BUTTON1) mouseLeftPressed = false;
        else if (e.getButton() == MouseEvent.BUTTON3) mouseRightPressed = false;
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }
}