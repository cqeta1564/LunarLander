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
            System.exit(0);
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "Lunar Lander";
        FontMetrics fmTitle = g.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(title);
        g.drawString(title, (Game.DEFAULT_WIDTH - titleWidth) / 2, 150);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.setColor(Color.WHITE);
        FontMetrics fmOptions = g.getFontMetrics();

        String playText = "Spustit hru (Enter)";
        int playTextWidth = fmOptions.stringWidth(playText);
        g.drawString(playText, (Game.DEFAULT_WIDTH - playTextWidth) / 2, 300);

        String settingsText = "Nastavení (S)";
        int settingsTextWidth = fmOptions.stringWidth(settingsText);
        g.drawString(settingsText, (Game.DEFAULT_WIDTH - settingsTextWidth) / 2, 350);

        String exitText = "Ukončit (Esc)";
        int exitTextWidth = fmOptions.stringWidth(exitText);
        g.drawString(exitText, (Game.DEFAULT_WIDTH - exitTextWidth) / 2, 400);
    }

    @Override
    public void onExit() {
    }
}