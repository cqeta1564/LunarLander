package states;

import input.InputHandler;

public class StateManager {

    public enum StateType {
        MENU, PLAYING, SETTINGS, GAME_OVER
    }

    private GameState currentState;
    private InputHandler inputHandler;
    // Instance stavů pro možné cachování (volitelné)
    // private MenuState menuState;
    // private PlayingState playingState;
    // private SettingsState settingsState;

    public StateManager(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        // Počáteční stav se nastavuje z Game.init()
    }

    public void setState(StateType type) {
        if (currentState != null) {
            currentState.onExit();
        }

        System.out.println("StateManager: Setting state to " + type); // Ladící výpis

        switch (type) {
            case MENU:
                // if (menuState == null) menuState = new MenuState(this, inputHandler);
                // currentState = menuState;
                currentState = new MenuState(this, inputHandler); // Vždy nová instance nebo z cache
                break;
            case PLAYING:
                // if (playingState == null) playingState = new PlayingState(this, inputHandler);
                // currentState = playingState;
                currentState = new PlayingState(this, inputHandler);
                break;
            case SETTINGS: // <<-- NOVÁ ČÁST
                // if (settingsState == null) settingsState = new SettingsState(this, inputHandler);
                // currentState = settingsState;
                currentState = new SettingsState(this, inputHandler);
                break;
            default:
                System.err.println("Neznámý stav: " + type);
                currentState = null;
        }

        if (currentState != null) {
            // currentState.init(this); // init() voláme zde, pokud není volán v konstruktoru stavu
            // nebo pokud chceme explicitní re-inicializaci
            currentState.onEnter();
        } else {
            System.err.println("CurrentState je null po pokusu o nastavení typu: " + type);
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }
}