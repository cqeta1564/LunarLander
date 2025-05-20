package states;

import input.InputHandler;

public class StateManager {

    public enum StateType {
        MENU, PLAYING, SETTINGS // GAME_OVER zatím nepoužíváme
    }

    private GameState currentState;
    private final InputHandler inputHandler; // Může být final, pokud se nemění po konstrukci

    public StateManager(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void setState(StateType type) {
        if (currentState != null) {
            currentState.onExit();
        }

        // System.out.println("StateManager: Setting state to " + type); // Lze ponechat pro ladění

        switch (type) {
            case MENU:
                currentState = new MenuState(this, inputHandler);
                break;
            case PLAYING:
                currentState = new PlayingState(this, inputHandler);
                break;
            case SETTINGS:
                currentState = new SettingsState(this, inputHandler);
                break;
            // GAME_OVER case zatím není implementován
            default:
                System.err.println("Neznámý nebo neimplementovaný stav: " + type);
                currentState = null; // Nebo přejít na výchozí/error stav
        }

        if (currentState != null) {
            currentState.init(this); // Zavoláme init pro nový stav
            currentState.onEnter();  // A onEnter
        } else {
            // Toto by se nemělo stát, pokud všechny typy mají implementaci
            System.err.println("CurrentState je null po pokusu o nastavení typu: " + type + ". Pravděpodobně chybí case ve switchi.");
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }
}