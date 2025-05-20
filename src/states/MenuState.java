package states;

import core.Game; // Pro přístup k DEFAULT_WIDTH/HEIGHT
import input.InputHandler;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
// KeyEvent zde již není potřeba pro consumeKey

public class MenuState implements GameState {

    private final StateManager stateManager;
    private final InputHandler inputHandler;

    // Příznaky "processedPress" již nejsou potřeba díky "isKeyJustPressed"
    // private boolean processedSPress = false;
    // private boolean processedEnterPress = false;

    public MenuState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
    }

    @Override
    public void init(StateManager manager) {
        // Případná jednorázová inicializace zdrojů pro menu
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do MenuState.");
        // Není třeba resetovat "processed" příznaky
    }

    @Override
    public void update(double deltaTime) {
        handleInput();
    }

    @Override
    public void handleInput() {
        if (inputHandler.isEnterJustPressed()) {
            stateManager.setState(StateManager.StateType.PLAYING);
        } else if (inputHandler.isSKeyJustPressed()) { // Používáme else if, aby se nezpracovalo více akcí najednou
            stateManager.setState(StateManager.StateType.SETTINGS);
        } else if (inputHandler.isEscJustPressed()) {
            System.out.println("Escape stisknut v Menu -> ukončení hry");
            System.exit(0); // Jednoduché ukončení, v reálné hře by se volalo Game.stopGame()
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