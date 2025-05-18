package states;

import input.InputHandler;

import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;

public class PlayingState implements GameState {

    private StateManager stateManager;
    private InputHandler inputHandler;

    public PlayingState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
        init(stateManager);
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
        g.drawString("Stiskni ESC pro návrat do menu (zatím neimplementováno pro návrat)", 100, 350);
    }

    @Override
    public void handleInput() {
        if (inputHandler.escape) {
            // stateManager.setState(StateManager.StateType.MENU); // Později
            System.out.println("ESC v PlayingState - měl by být návrat do menu");
        }
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do PlayingState.");
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění PlayingState.");
    }
}