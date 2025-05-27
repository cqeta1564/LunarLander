package input;

import java.awt.event.KeyEvent;
import java.util.EnumMap;
import java.util.Map;

public class KeyBindings {
    private final EnumMap<GameAction, Integer> keyMap;

    public KeyBindings() {
        keyMap = new EnumMap<>(GameAction.class);
        loadDefaults();
    }

    public static String getKeyTextStatic(int keyCode) {
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return "[NENASTAVENO]";
        }
        return KeyEvent.getKeyText(keyCode).toUpperCase();
    }

    public void loadDefaults() {
        keyMap.put(GameAction.ROTATE_LEFT, KeyEvent.VK_A);
        keyMap.put(GameAction.ROTATE_RIGHT, KeyEvent.VK_D);
        keyMap.put(GameAction.THRUST, KeyEvent.VK_W);
    }

    public int getKeyCode(GameAction action) {
        return keyMap.getOrDefault(action, KeyEvent.VK_UNDEFINED);
    }

    public GameAction setKey(GameAction actionToBind, int newKeyCode) {
        if (newKeyCode == KeyEvent.VK_UNDEFINED) {
            keyMap.remove(actionToBind);
            return null;
        }

        GameAction previouslyBoundAction = null;
        for (Map.Entry<GameAction, Integer> entry : keyMap.entrySet()) {
            if (entry.getValue() == newKeyCode && entry.getKey() != actionToBind) {
                previouslyBoundAction = entry.getKey();
                break;
            }
        }

        if (previouslyBoundAction != null) {
            keyMap.remove(previouslyBoundAction);
        }

        keyMap.put(actionToBind, newKeyCode);
        return previouslyBoundAction;
    }

    public String getKeyTextForAction(GameAction action) {
        return getKeyTextStatic(getKeyCode(action));
    }
}