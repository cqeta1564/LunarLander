package states;

import core.Game;
import input.InputHandler;

import java.awt.*;

public class MenuState implements GameState {

    private final StateManager stateManager;
    private final InputHandler inputHandler;

    public MenuState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
    }

    @Override
    public void init(StateManager manager) {
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do MenuState.");
    }

    @Override
    public void update(double deltaTime) {
        handleInput();
    }

    @Override
    public void handleInput() {
        if (inputHandler.isEnterJustPressed()) {
            stateManager.setState(StateManager.StateType.PLAYING);
        } else if (inputHandler.isSKeyJustPressed()) {
            stateManager.setState(StateManager.StateType.SETTINGS);
        } else if (inputHandler.isEscJustPressed()) {
            System.out.println("Escape stisknut v Menu -> ukončení hry");
            System.exit(0);
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "Lunar Lander";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (Game.DEFAULT_WIDTH - titleWidth) / 2, 150);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.setColor(Color.WHITE);

        String playText = "Spustit hru (Enter)";
        int playTextWidth = g.getFontMetrics().stringWidth(playText);
        g.drawString(playText, (Game.DEFAULT_WIDTH - playTextWidth) / 2, 300);

        String settingsText = "Nastavení (S)";
        int settingsTextWidth = g.getFontMetrics().stringWidth(settingsText);
        g.drawString(settingsText, (Game.DEFAULT_WIDTH - settingsTextWidth) / 2, 350);

        String exitText = "Ukončit (Esc)";
        int exitTextWidth = g.getFontMetrics().stringWidth(exitText);
        g.drawString(exitText, (Game.DEFAULT_WIDTH - exitTextWidth) / 2, 400);
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění MenuState.");
    }
}