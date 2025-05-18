package states;

import input.InputHandler;
// import graphics.Renderer;
import java.awt.Graphics2D;
import java.awt.Color; // Jen pro příklad
import java.awt.Font;  // Jen pro příklad

public class MenuState implements GameState {

    private StateManager stateManager;
    private InputHandler inputHandler;

    // Příklad: tlačítka
    // private ui.Button playButton;
    // private ui.Button settingsButton;
    // private ui.Button exitButton;
    // int selectedOption = 0;


    public MenuState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
        init(stateManager);
    }

    @Override
    public void init(StateManager stateManager) {
        // Inicializace tlačítek, načtení obrázků pro menu atd.
        // playButton = new ui.Button("Start Game", 200, 200, ...);
        System.out.println("MenuState initialized.");
    }

    @Override
    public void update(double deltaTime) {
        handleInput(); // Zpracování vstupu specifického pro menu
        // Aktualizace animací v menu, efektů na tlačítkách atd.

        // Příklad logiky výběru:
        // if (inputHandler.down && !previousDownState) { selectedOption++; }
        // if (inputHandler.enter) { /* ... aktivuj vybranou možnost ... */ }
    }

    @Override
    public void render(Graphics2D g) { // Dočasně Graphics2D
        // Vykreslení pozadí menu
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, core.Game.DEFAULT_WIDTH, core.Game.DEFAULT_HEIGHT); // Předpokládáme přístup k rozměrům

        // Vykreslení názvu hry
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        String title = "Lunar Lander";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (core.Game.DEFAULT_WIDTH - titleWidth) / 2, 150);

        // Vykreslení tlačítek/položek menu
        g.setFont(new Font("Arial", Font.PLAIN, 30));
        // Příklad:
        // if (selectedOption == 0) g.setColor(Color.RED); else g.setColor(Color.WHITE);
        g.setColor(Color.WHITE); // Dočasně vždy bílá
        String playText = "Spustit hru (Enter)";
        int playTextWidth = g.getFontMetrics().stringWidth(playText);
        g.drawString(playText, (core.Game.DEFAULT_WIDTH - playTextWidth) / 2, 300);

        String settingsText = "Nastavení (S)";
        int settingsTextWidth = g.getFontMetrics().stringWidth(settingsText);
        g.drawString(settingsText, (core.Game.DEFAULT_WIDTH - settingsTextWidth) / 2, 350);

        String exitText = "Ukončit (Esc)";
        int exitTextWidth = g.getFontMetrics().stringWidth(exitText);
        g.drawString(exitText, (core.Game.DEFAULT_WIDTH - exitTextWidth) / 2, 400);

        // playButton.render(g);
        // settingsButton.render(g);
        // exitButton.render(g);
    }

    @Override
    public void handleInput() {
        // Zde budeme reagovat na stisky kláves pro navigaci v menu
        if (inputHandler.enter) {
            // stateManager.setState(StateManager.StateType.PLAYING);
            System.out.println("Enter stisknut v Menu -> přechod do PlayingState (zatím neimplementováno)");
            // Prozatím jen výpis, PlayingState ještě nemáme
            stateManager.setState(StateManager.StateType.PLAYING);
        }
        if (inputHandler.escape) {
            System.out.println("Escape stisknut v Menu -> ukončení hry");
            System.exit(0); // Jednoduché ukončení
        }
        // if (inputHandler.s) { // 'S' pro Settings
        //     stateManager.setState(StateManager.StateType.SETTINGS);
        // }
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do MenuState.");
        // Resetovat stav menu, např. vybranou položku
        // selectedOption = 0;
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění MenuState.");
        // Uvolnit zdroje specifické pro menu, pokud je potřeba
    }
}