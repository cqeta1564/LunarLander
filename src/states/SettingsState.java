package states;

import input.InputHandler;
import audio.AudioManager;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyEvent; // Potřebujeme pro consumeKey

public class SettingsState implements GameState {

    private StateManager stateManager;
    private InputHandler inputHandler;
    private AudioManager audioManager;

    private boolean leftHeldLastUpdate = false; // Pro plynulou změnu při držení (zatím neimplementováno)
    private boolean rightHeldLastUpdate = false; // Pro plynulou změnu při držení (zatím neimplementováno)

    // Tyto flagy jsou pro jednorázovou reakci na stisk
    private boolean processedLeftPress = false;
    private boolean processedRightPress = false;


    public SettingsState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
        this.audioManager = AudioManager.getInstance();
    }

    @Override
    public void init(StateManager stateManager) {
        // System.out.println("SettingsState initialized/re-initialized via init().");
    }

    @Override
    public void update(double deltaTime) {
        handleInput();
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(Color.DARK_GRAY.darker());
        g.fillRect(0, 0, core.Game.DEFAULT_WIDTH, core.Game.DEFAULT_HEIGHT);

        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String title = "Nastavení";
        int titleWidth = g.getFontMetrics().stringWidth(title);
        g.drawString(title, (core.Game.DEFAULT_WIDTH - titleWidth) / 2, 100);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 28));
        String volumeText = "Hlasitost: " + audioManager.getGlobalVolume() + "%";
        int volumeTextWidth = g.getFontMetrics().stringWidth(volumeText);
        g.drawString(volumeText, (core.Game.DEFAULT_WIDTH - volumeTextWidth) / 2, 250);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String volumeControlText = "Použij šipky VLEVO / VPRAVO pro změnu";
        int volumeControlTextWidth = g.getFontMetrics().stringWidth(volumeControlText);
        g.drawString(volumeControlText, (core.Game.DEFAULT_WIDTH - volumeControlTextWidth) / 2, 290);

        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String backText = "Zpět do menu (Esc)";
        int backTextWidth = g.getFontMetrics().stringWidth(backText);
        g.drawString(backText, (core.Game.DEFAULT_WIDTH - backTextWidth) / 2, 450);
    }

    @Override
    public void handleInput() {
        // Šipka vlevo
        if (inputHandler.leftArrowPressed) {
            if (!processedLeftPress) { // Zareagovat pouze jednou na stisk, dokud není klávesa uvolněna a znovu stisknuta
                audioManager.decreaseVolume(5);
                processedLeftPress = true; // Označit, že stisk byl zpracován
            }
        } else {
            processedLeftPress = false; // Resetovat, když klávesa není stisknutá, umožní další reakci po novém stisku
        }

        // Šipka vpravo
        if (inputHandler.rightArrowPressed) {
            if (!processedRightPress) {
                audioManager.increaseVolume(5);
                processedRightPress = true;
            }
        } else {
            processedRightPress = false;
        }

        // Návrat do menu
        if (inputHandler.escapePressed) {
            stateManager.setState(StateManager.StateType.MENU);
            inputHandler.consumeKey(KeyEvent.VK_ESCAPE); // "Spotřebovat" stisk Esc, aby se hned neprojevil v menu
        }
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do SettingsState.");
        // Resetovat stav zpracování stisku při vstupu do stavu
        processedLeftPress = false; // nebo true, pokud chceme první update ignorovat
        processedRightPress = false; // nebo true
        // Zajistíme, že případný držený stisk klávesy z předchozího stavu nebude hned reagovat
        if(inputHandler.leftArrowPressed) processedLeftPress = true;
        if(inputHandler.rightArrowPressed) processedRightPress = true;

        inputHandler.consumeKey(KeyEvent.VK_ESCAPE); // Pro jistotu, kdyby Esc vedl sem
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění SettingsState.");
    }
}