package states;

import core.Game; // Pro přístup k DEFAULT_WIDTH/HEIGHT
import input.GameAction;
import input.InputHandler;
import input.KeyBindings; // Pro debug zobrazení

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
// KeyEvent zde již není potřeba pro consumeKey

public class PlayingState implements GameState {

    private final StateManager stateManager;
    private final InputHandler inputHandler;
    // private Lander lander; // Instance lodi

    public PlayingState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
        // this.lander = new Lander(Game.DEFAULT_WIDTH / 2, 100); // Příklad inicializace
    }

    @Override
    public void init(StateManager manager) {
        System.out.println("PlayingState initialized.");
        // if (lander != null) lander.reset();
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do PlayingState.");
        // Není třeba "spotřebovávat" klávesy Enter/Esc z předchozího stavu,
        // protože "justPressed" je platné jen pro snímek, kdy byl stav změněn.
    }

    @Override
    public void update(double deltaTime) {
        handleInput();
        // if (lander != null) lander.update(deltaTime);
        // Zde další herní logika: kolize, palivo, atd.
    }

    @Override
    public void handleInput() {
        if (inputHandler.isEscJustPressed()) {
            stateManager.setState(StateManager.StateType.MENU);
            return; // Ukončíme handleInput, abychom nezpracovali i pohyb lodi
        }

        // Příklad ovládání lodi (kontinuální akce)
        // if (lander != null) {
        //     boolean thrusting = inputHandler.isActionActive(GameAction.THRUST);
        //     lander.setThrusting(thrusting);
        //
        //     if (inputHandler.isActionActive(GameAction.ROTATE_LEFT)) {
        //         lander.rotate(-1 * Lander.ROTATION_SPEED); // Předpokládáme konstantu rychlosti rotace
        //     } else if (inputHandler.isActionActive(GameAction.ROTATE_RIGHT)) {
        //         lander.rotate(1 * Lander.ROTATION_SPEED);
        //     } else {
        //         lander.stopRotation();
        //     }
        // }

        // Ladící výpisy pro aktivní akce
        if (inputHandler.isActionActive(GameAction.THRUST)) {
            System.out.println("HRA: Akce TAH MOTORU");
        }
        if (inputHandler.isActionActive(GameAction.ROTATE_LEFT)) {
            System.out.println("HRA: Akce ROTACE VLEVO");
        }
        if (inputHandler.isActionActive(GameAction.ROTATE_RIGHT)) {
            System.out.println("HRA: Akce ROTACE VPRAVO");
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        // if (lander != null) lander.render(g);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("HRA BĚŽÍ - OVLÁDÁNÍ DLE NASTAVENÍ", 50, 50);
        g.drawString("Stiskni ESC pro návrat do menu", 50, 80);

        // Debug info pro klávesy
        KeyBindings kb = inputHandler.getKeyBindings();
        if (kb != null) {
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.drawString("Tah: " + kb.getKeyTextForAction(GameAction.THRUST), 10, Game.DEFAULT_HEIGHT - 60);
            g.drawString("Vlevo: " + kb.getKeyTextForAction(GameAction.ROTATE_LEFT), 10, Game.DEFAULT_HEIGHT - 45);
            g.drawString("Vpravo: " + kb.getKeyTextForAction(GameAction.ROTATE_RIGHT), 10, Game.DEFAULT_HEIGHT - 30);
        }
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění PlayingState.");
        // if (lander != null) lander.setThrusting(false); // Např. vypnout motor při opuštění
    }
}