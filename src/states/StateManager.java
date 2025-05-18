package states;

import input.InputHandler;

public class StateManager {

    public enum StateType {
        MENU, PLAYING, SETTINGS, GAME_OVER // Další podle potřeby
    }

    private GameState currentState;
    private InputHandler inputHandler;
    // Zde můžeme držet instance všech stavů, pokud je chceme cachovat
    // private MenuState menuState;
    // private PlayingState playingState;
    // ...

    public StateManager(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
        // Inicializovat a nastavit počáteční stav
        // Např. menuState = new MenuState(this, inputHandler);
        // setState(StateType.MENU); // Nebo to udělat z Game třídy po inicializaci
    }

    public void setState(StateType type) {
        if (currentState != null) {
            currentState.onExit();
        }

        switch (type) {
            case MENU:
                // if (menuState == null) menuState = new MenuState(this, inputHandler);
                currentState = new MenuState(this, inputHandler); // Nebo získat z cache
                break;
            case PLAYING:
                // if (playingState == null) playingState = new PlayingState(this, inputHandler);
                currentState = new PlayingState(this, inputHandler); // Dočasně, později vytvoříme
                break;
            // case SETTINGS:
            //     currentState = new SettingsState(this, inputHandler);
            //     break;
            default:
                System.err.println("Neznámý stav: " + type);
                currentState = null; // Nebo defaultní stav
        }

        if (currentState != null) {
            // currentState.init(); // init() by se mělo volat jednou, možná v konstruktoru stavu
            currentState.onEnter();
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    // update() a render() metody StateManageru by jen volaly metody aktuálního stavu
    // Tyto jsou již volány přímo z Game třídy na getCurrentState().update() / .render()
}