package states;

import input.InputHandler;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent; // Potřebujeme pro consumeKey

public class MenuState implements GameState {

    private StateManager stateManager;
    private InputHandler inputHandler;

    private boolean processedSPress = false; // Pro jednorázový stisk 'S'
    private boolean processedEnterPress = false; // Pro jednorázový stisk 'Enter'

    public MenuState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
    }

    @Override
    public void init(StateManager stateManager) {
        // System.out.println("MenuState initialized.");
    }

    @Override
    public void update(double deltaTime) {
        handleInput();
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, core.Game.DEFAULT_WIDTH, core.Game.DEFAULT_HEIGHT);

        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "Lunar Lander";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (core.Game.DEFAULT_WIDTH - titleWidth) / 2, 150);

        g.setFont(new Font("Arial", Font.PLAIN, 30));
        g.setColor(Color.WHITE);

        String playText = "Spustit hru (Enter)";
        int playTextWidth = g.getFontMetrics().stringWidth(playText);
        g.drawString(playText, (core.Game.DEFAULT_WIDTH - playTextWidth) / 2, 300);

        String settingsText = "Nastavení (S)";
        int settingsTextWidth = g.getFontMetrics().stringWidth(settingsText);
        g.drawString(settingsText, (core.Game.DEFAULT_WIDTH - settingsTextWidth) / 2, 350);

        String exitText = "Ukončit (Esc)";
        int exitTextWidth = g.getFontMetrics().stringWidth(exitText);
        g.drawString(exitText, (core.Game.DEFAULT_WIDTH - exitTextWidth) / 2, 400);
    }

    @Override
    public void handleInput() {
        // Přechod do hry
        if (inputHandler.enterPressed) {
            if(!processedEnterPress){
                stateManager.setState(StateManager.StateType.PLAYING);
                // Není třeba volat consumeKey pro Enter, pokud přecházíme do úplně jiného stavu,
                // kde Enter má jinou funkci nebo žádnou. Ale pro konzistenci můžeme:
                inputHandler.consumeKey(KeyEvent.VK_ENTER);
                processedEnterPress = true; // Aby se to nespustilo vícekrát, pokud by stav nepřepnul okamžitě
            }
        } else {
            processedEnterPress = false;
        }

        // Přechod do nastavení
        if (inputHandler.sPressed) { // Používáme nový příznak sPressed
            if (!processedSPress) {
                stateManager.setState(StateManager.StateType.SETTINGS);
                inputHandler.consumeKey(KeyEvent.VK_S); // "Spotřebovat" stisk S
                processedSPress = true;
            }
        } else {
            processedSPress = false;
        }

        // Ukončení hry
        if (inputHandler.escapePressed) {
            System.out.println("Escape stisknut v Menu -> ukončení hry");
            // Není třeba consumeKey, protože hra končí
            System.exit(0);
        }
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do MenuState.");
        // Resetovat stav zpracování stisku při vstupu do stavu
        processedSPress = false;
        processedEnterPress = false;
        // Případně "spotřebovat" klávesy, které mohly vést k tomuto stavu, pokud je to nutné
        inputHandler.consumeKey(KeyEvent.VK_S);
        inputHandler.consumeKey(KeyEvent.VK_ENTER);
        // Esc by měl být také spotřebován, pokud se vracíme z jiného stavu pomocí Esc
        inputHandler.consumeKey(KeyEvent.VK_ESCAPE);
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění MenuState.");
    }
}