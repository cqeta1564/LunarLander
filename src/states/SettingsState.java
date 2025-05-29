package states;

import audio.AudioManager;
import core.Game;
import input.GameAction;
import input.InputHandler;
import input.KeyBindings;
import ui.Slider;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class SettingsState implements GameState {

    private final StateManager stateManager;
    private final InputHandler inputHandler;
    private final AudioManager audioManager;
    private final Slider volumeSlider;
    private final KeyBindings keyBindings;
    private final List<Rectangle> keyBindClickAreas;
    private final int KEYBIND_START_Y = 280;
    private final int KEYBIND_ITEM_HEIGHT = 25;
    private final int KEYBIND_ITEM_SPACING = 15;
    private final int KEYBIND_AREA_WIDTH = 350;
    private GameAction currentlySelectedActionToRebind = null;

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
            keyBindClickAreas.add(new Rectangle(Game.DEFAULT_WIDTH / 2 - KEYBIND_AREA_WIDTH / 2, currentY, KEYBIND_AREA_WIDTH, KEYBIND_ITEM_HEIGHT));
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
        volumeSlider.setValue(audioManager.getGlobalVolume());
        currentlySelectedActionToRebind = null;
        if (inputHandler.isListening()) {
            inputHandler.stopListeningForKey();
        }
    }

    @Override
    public void update(double deltaTime) {
        handleInput();

        if (stateManager.getCurrentState() == this && !inputHandler.isListening()) {
            int previousSliderDiscreteValue = volumeSlider.getValue();
            volumeSlider.handleMouseInput(inputHandler.mouseX, inputHandler.mouseY, inputHandler.mouseLeftPressed);
            int currentSliderDiscreteValue = volumeSlider.getValue();

            if (volumeSlider.isDragging() && inputHandler.mouseLeftPressed && currentSliderDiscreteValue != previousSliderDiscreteValue) {
                AudioManager.getInstance().playSound(AudioManager.SoundEffect.BUTTON_CLICK);
            }

            if (audioManager != null && currentSliderDiscreteValue != audioManager.getGlobalVolume()) {
                audioManager.setGlobalVolume(currentSliderDiscreteValue);
            }
        }
    }

    @Override
    public void handleInput() {
        volumeSlider.handleMouseInput(inputHandler.mouseX, inputHandler.mouseY, inputHandler.mouseLeftPressed);

        if (inputHandler.isListening()) {
            int rawKeyCode = inputHandler.consumeRawKeyCodeForRebind();
            if (rawKeyCode != KeyEvent.VK_UNDEFINED) {
                GameAction actionBeingRebound = inputHandler.getActionBeingRebound();
                if (rawKeyCode == KeyEvent.VK_ESCAPE) {
                } else {
                    if (actionBeingRebound != null) {
                        keyBindings.setKey(actionBeingRebound, rawKeyCode);
                    }
                }
                inputHandler.stopListeningForKey();
                currentlySelectedActionToRebind = null;
            }
        } else {
            if (inputHandler.isMouseLeftJustPressed()) {
                boolean clickedOnBindable = false;
                for (int i = 0; i < GameAction.getAllActions().length; i++) {
                    if (keyBindClickAreas.get(i).contains(inputHandler.mouseX, inputHandler.mouseY)) {
                        AudioManager.getInstance().playSound(AudioManager.SoundEffect.BUTTON_CLICK);
                        currentlySelectedActionToRebind = GameAction.getAllActions()[i];
                        inputHandler.startListeningForKey(currentlySelectedActionToRebind);
                        clickedOnBindable = true;
                        break;
                    }
                }
                if (clickedOnBindable) {
                    return;
                }
            }

            if (inputHandler.isEscJustPressed()) {
                AudioManager.getInstance().playSound(AudioManager.SoundEffect.BUTTON_CLICK);
                stateManager.setState(StateManager.StateType.MENU);
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g.setColor(Color.DARK_GRAY.darker());
        g.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        String titleText = "Nastavení";
        FontMetrics fmTitle = g.getFontMetrics();
        int titleWidth = fmTitle.stringWidth(titleText);
        g.drawString(titleText, (Game.DEFAULT_WIDTH - titleWidth) / 2, 80);

        volumeSlider.render(g);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.CYAN);
        String keybindTitle = "Nastavení Ovládání";
        FontMetrics fmKeybindTitle = g.getFontMetrics();
        int keybindTitleWidth = fmKeybindTitle.stringWidth(keybindTitle);
        g.drawString(keybindTitle, (Game.DEFAULT_WIDTH - keybindTitleWidth) / 2, KEYBIND_START_Y - 40);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        FontMetrics fmKeybindItem = g.getFontMetrics();
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
                    g.setColor(Color.ORANGE);
                } else {
                    g.setColor(Color.WHITE);
                }
            }
            g.drawString(actionDisplayName + keyName, clickArea.x + 10, clickArea.y + fmKeybindItem.getAscent() + (KEYBIND_ITEM_HEIGHT - fmKeybindItem.getHeight()) / 2);
        }

        if (inputHandler.isListening()) {
            g.setColor(Color.GRAY);
            g.setFont(new Font("Arial", Font.ITALIC, 14));
            g.drawString("Stiskni ESC pro zrušení změny klávesy.", Game.DEFAULT_WIDTH / 2 - 100, KEYBIND_START_Y + (GameAction.getAllActions().length * (KEYBIND_ITEM_HEIGHT + KEYBIND_ITEM_SPACING)) + 20);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 24));
        String backText = "Zpět do menu (Esc)";
        FontMetrics fmBack = g.getFontMetrics();
        int backTextWidth = fmBack.stringWidth(backText);
        g.drawString(backText, (Game.DEFAULT_WIDTH - backTextWidth) / 2, Game.DEFAULT_HEIGHT - 60);
    }

    @Override
    public void onExit() {
        if (inputHandler.isListening()) {
            inputHandler.stopListeningForKey();
            currentlySelectedActionToRebind = null;
        }
    }
}