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
            // System.out.println(actionToBind.getDisplayName() + " odmapováno."); // Ladící výpis
            return null;
        }

        GameAction previouslyBoundAction = null;
        // Zjistíme, zda nová klávesa již není použita jinou akcí
        for (Map.Entry<GameAction, Integer> entry : keyMap.entrySet()) {
            if (entry.getValue() == newKeyCode && entry.getKey() != actionToBind) {
                previouslyBoundAction = entry.getKey();
                break;
            }
        }

        if (previouslyBoundAction != null) {
            keyMap.remove(previouslyBoundAction); // Odmapujeme starou akci od této klávesy
            // System.out.println("Klávesa " + getKeyTextStatic(newKeyCode) +
            //                    " byla odmapována od akce: " + previouslyBoundAction.getDisplayName());
        }

        keyMap.put(actionToBind, newKeyCode);
        // System.out.println("Akce " + actionToBind.getDisplayName() +
        //                    " namapována na klávesu: " + getKeyTextStatic(newKeyCode));
        return previouslyBoundAction;
    }

    public String getKeyTextForAction(GameAction action) {
        return getKeyTextStatic(getKeyCode(action));
    }

    public static String getKeyTextStatic(int keyCode) {
        if (keyCode == KeyEvent.VK_UNDEFINED) {
            return "[NENASTAVENO]";
        }
        // KeyEvent.getKeyText může vrátit "Unknown keyCode: 0x0" pro některé systémové klávesy,
        // nebo názvy jako "Caps Lock". Pro písmena vrací písmena.
        return KeyEvent.getKeyText(keyCode).toUpperCase();
    }
}