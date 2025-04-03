package input;

import model.Lander;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Map;

//Mapuje klávesy na akce
public class InputHandler implements KeyListener {
    private final Map<Integer, Boolean> keyPressed = new HashMap<>();
    private final Lander lander;

    public InputHandler(Lander lander) {
        this.lander = lander;
        initializeKeys();
    }

    private void initializeKeys() {
        keyPressed.put(KeyEvent.VK_UP, false);
        keyPressed.put(KeyEvent.VK_LEFT, false);
        keyPressed.put(KeyEvent.VK_RIGHT, false);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        // Reset pouze při stisknutí R a pokud hra skončila
        if (keyCode == KeyEvent.VK_R && (lander.isLanded() || lander.isCrashed())) {
            lander.reset();
            return;
        }

        // Ovládání funguje pouze během letu
        if (!lander.isLanded() && !lander.isCrashed()) {
            if (keyPressed.containsKey(keyCode)) {
                keyPressed.put(keyCode, true);
                updateLanderState();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyPressed.containsKey(keyCode)) {
            keyPressed.put(keyCode, false);
            updateLanderState();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    } // Není potřeba

    private void updateLanderState() {
        // Pohon motoru
        lander.setThrusting(keyPressed.get(KeyEvent.VK_UP));

        // Rotace
        if (keyPressed.get(KeyEvent.VK_LEFT)) {
            lander.setAngle(lander.getAngle() - 2); // Otočení doleva
        }
        if (keyPressed.get(KeyEvent.VK_RIGHT)) {
            lander.setAngle(lander.getAngle() + 2); // Otočení doprava
        }
    }
}
