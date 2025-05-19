package states;

import input.InputHandler;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent; // Přidáno pro consumeKey, pokud bychom ho zde potřebovali

public class PlayingState implements GameState {

    private StateManager stateManager;
    private InputHandler inputHandler;

    public PlayingState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
        // init(stateManager); // init se obvykle volá ze StateManageru nebo zde v konstruktoru
    }

    @Override
    public void init(StateManager stateManager) {
        System.out.println("PlayingState initialized.");
    }

    @Override
    public void update(double deltaTime) {
        handleInput();
        // Zde bude logika hry: pohyb lodi, fyzika, kolize...
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, core.Game.DEFAULT_WIDTH, core.Game.DEFAULT_HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("HRA BĚŽÍ - SEM PŘIJDE LANDER", 100, 300);
        g.drawString("Stiskni ESC pro návrat do menu", 100, 350); // Text upraven
    }

    @Override
    public void handleInput() {
        // Použijeme nový název proměnné z InputHandler
        if (inputHandler.escapePressed) {
            stateManager.setState(StateManager.StateType.MENU);
            // "Spotřebujeme" klávesu, aby se neprojevila hned v menu
            inputHandler.consumeKey(KeyEvent.VK_ESCAPE);
        }
        // Zde přijde ovládání lodi pomocí inputHandler.up, inputHandler.left atd.
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do PlayingState.");
        // "Spotřebujeme" klávesu Enter nebo jinou, která mohla vést k tomuto stavu,
        // aby se hned neaktivovala nějaká akce ve hře.
        inputHandler.consumeKey(KeyEvent.VK_ENTER);
        inputHandler.consumeKey(KeyEvent.VK_ESCAPE); // Pro jistotu
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění PlayingState.");
    }
}