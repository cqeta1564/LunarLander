package states;

import core.Game; // Pro přístup k DEFAULT_WIDTH/HEIGHT
import input.GameAction;
import input.InputHandler;
import input.KeyBindings;
import audio.AudioManager;
import ui.Slider;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent; // Stále potřeba pro VK_ESCAPE v rawKeyCode
import java.util.ArrayList;
import java.util.List;

public class SettingsState implements GameState {

    private final StateManager stateManager;
    private final InputHandler inputHandler;
    private final AudioManager audioManager;
    private final Slider volumeSlider;
    private final KeyBindings keyBindings;

    private final List<Rectangle> keyBindClickAreas;
    private GameAction currentlySelectedActionToRebind = null;

    // Příznak escJustCancelledRebind JIŽ NENÍ POTŘEBA díky "isKeyJustPressed"
    // private boolean escJustCancelledRebind = false;

    private final int KEYBIND_START_Y = 280;
    private final int KEYBIND_ITEM_HEIGHT = 25;
    private final int KEYBIND_ITEM_SPACING = 15;
    private final int KEYBIND_AREA_WIDTH = 350;

    public SettingsState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
        this.audioManager = AudioManager.getInstance();
        this.keyBindings = inputHandler.getKeyBindings();

        int sliderX = Game.DEFAULT_WIDTH / 2 - 100;
        int sliderY = 180;
        this.volumeSlider = new Slider("Hlasitost", sliderX, sliderY, 200, 8, 0, 100, audioManager.getGlobalVolume());

        this.keyBindClickAreas = new ArrayList<>();
        recalculateClickAreas();
    }

    private void recalculateClickAreas() {
        keyBindClickAreas.clear();
        int currentY = KEYBIND_START_Y;
        for (GameAction action : GameAction.getAllActions()) {
            keyBindClickAreas.add(new Rectangle(
                    Game.DEFAULT_WIDTH / 2 - KEYBIND_AREA_WIDTH / 2,
                    currentY,
                    KEYBIND_AREA_WIDTH,
                    KEYBIND_ITEM_HEIGHT
            ));
            currentY += KEYBIND_ITEM_HEIGHT + KEYBIND_ITEM_SPACING;
        }
    }

    @Override
    public void init(StateManager manager) {
        volumeSlider.setValue(audioManager.getGlobalVolume());
        currentlySelectedActionToRebind = null;
        if (inputHandler.isListening()) {
            inputHandler.stopListeningForKey();
        }
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do SettingsState.");
        // Resetovat stav při vstupu
        volumeSlider.setValue(audioManager.getGlobalVolume()); // Aktualizovat slider
        currentlySelectedActionToRebind = null;
        if (inputHandler.isListening()) { // Pokud by nějakým způsobem zůstal viset
            inputHandler.stopListeningForKey();
        }
        // Není třeba explicitně "spotřebovávat" Esc, "isJustPressed" to řeší
    }

    @Override
    public void update(double deltaTime) {
        handleInput();
        // Aktualizace AudioManageru se děje až po handleInput, pokud slider změnil hodnotu
        // Je lepší, aby Slider přímo nevolal AudioManager, ale SettingsState to zprostředkoval
        int sliderVolume = volumeSlider.getValue();
        if (sliderVolume != audioManager.getGlobalVolume()) {
            audioManager.setGlobalVolume(sliderVolume);
        }
    }

    @Override
    public void handleInput() {
        volumeSlider.handleMouseInput(inputHandler.mouseX, inputHandler.mouseY, inputHandler.mouseLeftPressed);

        if (inputHandler.isListening()) {
            int rawKeyCode = inputHandler.consumeRawKeyCodeForRebind();

            if (rawKeyCode != KeyEvent.VK_UNDEFINED) { // Byla zachycena klávesa
                GameAction actionBeingRebound = inputHandler.getActionBeingRebound();
                if (rawKeyCode == KeyEvent.VK_ESCAPE) {
                    System.out.println("Změna klávesy pro " + (actionBeingRebound != null ? actionBeingRebound.getDisplayName() : "AKCI") + " zrušena (ESC).");
                    // Esc bylo "just pressed" a my jsme na to zareagovali zrušením rebindu.
                    // V tomto snímku už se `isEscJustPressed()` pro zavření menu nedostane ke slovu díky `return`.
                } else {
                    if (actionBeingRebound != null) {
                        keyBindings.setKey(actionBeingRebound, rawKeyCode);
                    }
                }
                inputHandler.stopListeningForKey();
                currentlySelectedActionToRebind = null; // Zrušíme i zde
                return; // DŮLEŽITÉ: Ukončíme handleInput, aby se zbytek neprovedl
            }
            // Pokud stále nasloucháme a žádná klávesa nebyla zachycena, nic neděláme
            // a čekáme na další cyklus. Esc pro zavření se neuplatní.

        } else { // inputHandler.isListening() == false
            // Zpracování kliknutí myši pro zahájení rebindu
            // Zde potřebujeme "isMouseLeftJustPressed" pro spolehlivou jednorázovou akci
            if (inputHandler.isMouseLeftJustPressed()) { // Předpokládáme, že tato metoda existuje a funguje
                for (int i = 0; i < GameAction.getAllActions().length; i++) {
                    if (keyBindClickAreas.get(i).contains(inputHandler.mouseX, inputHandler.mouseY)) {
                        currentlySelectedActionToRebind = GameAction.getAllActions()[i];
                        inputHandler.startListeningForKey(currentlySelectedActionToRebind);
                        // Po zahájení naslouchání je důležité ukončit handleInput,
                        // aby se např. Esc (pokud by bylo stisknuto současně s myší)
                        // nezpracovalo hned pro zrušení.
                        return;
                    }
                }
            }

            // Zpracování Esc pro zavření nastavení
            // Toto se provede, pouze pokud nejsme v režimu naslouchání A pokud Esc bylo právě stisknuto.
            if (inputHandler.isEscJustPressed()) {
                System.out.println("SettingsState: Esc JUST PRESSED (normální zavření), zavírám nastavení.");
                stateManager.setState(StateManager.StateType.MENU);
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        // Pozadí
        g.setColor(Color.DARK_GRAY.darker());
        g.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        // Titulek
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String titleText = "Nastavení"; // Přejmenováno pro srozumitelnost
        int titleWidth = g.getFontMetrics().stringWidth(titleText);
        g.drawString(titleText, (Game.DEFAULT_WIDTH - titleWidth) / 2, 80);

        // Slider
        volumeSlider.render(g);

        // Titulek pro Key Binding
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.CYAN);
        String keybindTitle = "Nastavení Ovládání";
        int keybindTitleWidth = g.getFontMetrics().stringWidth(keybindTitle);
        g.drawString(keybindTitle, (Game.DEFAULT_WIDTH - keybindTitleWidth) / 2, KEYBIND_START_Y - 40);

        // Vykreslení jednotlivých key bindings
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        for (int i = 0; i < GameAction.getAllActions().length; i++) {
            GameAction action = GameAction.getAllActions()[i];
            Rectangle clickArea = keyBindClickAreas.get(i);

            String actionDisplayName = action.getDisplayName() + ": ";
            String keyName;

            if (currentlySelectedActionToRebind == action && inputHandler.isListening()) {
                keyName = "[STISKNI KLÁVESU...]";
                g.setColor(Color.YELLOW);
            } else {
                keyName = keyBindings.getKeyTextForAction(action);
                if (clickArea.contains(inputHandler.mouseX, inputHandler.mouseY) && !inputHandler.isListening()) {
                    g.setColor(Color.ORANGE); // Zvýraznění při najetí myší
                } else {
                    g.setColor(Color.WHITE);
                }
            }
            g.drawString(actionDisplayName + keyName, clickArea.x + 10, clickArea.y + KEYBIND_ITEM_HEIGHT - 5);
        }

        // Zpráva pro zrušení naslouchání
        if (inputHandler.isListening()) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 14));
            g.drawString("Stiskni ESC pro zrušení změny klávesy.", Game.DEFAULT_WIDTH / 2 - 100,
                    KEYBIND_START_Y + (GameAction.getAllActions().length * (KEYBIND_ITEM_HEIGHT + KEYBIND_ITEM_SPACING)) + 20);
        }

        // Návrat zpět
        g.setColor(Color.WHITE); // Nastavit barvu znovu po případné změně výše
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String backText = "Zpět do menu (Esc)";
        int backTextWidth = g.getFontMetrics().stringWidth(backText);
        g.drawString(backText, (Game.DEFAULT_WIDTH - backTextWidth) / 2, Game.DEFAULT_HEIGHT - 60);
    }


    @Override
    public void onExit() {
        System.out.println("Opuštění SettingsState.");
        if (inputHandler.isListening()) { // Pokud by se stav opustil během naslouchání
            inputHandler.stopListeningForKey();
            currentlySelectedActionToRebind = null;
        }
    }
}