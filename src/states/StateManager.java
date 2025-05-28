package states;

import input.InputHandler;

public class StateManager {

    private final InputHandler inputHandler;
    private GameState currentState;

    public StateManager(InputHandler inputHandler) {
        this.inputHandler = inputHandler;
    }

    public void setState(StateType type) {
        if (currentState != null) {
            currentState.onExit();
        }
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
            default:
                System.err.println("Neznámý nebo neimplementovaný stav: " + type);
                currentState = null;
        }

        if (currentState != null) {
            currentState.init(this);
            currentState.onEnter();
        } else {
            System.err.println("CurrentState je null po pokusu o nastavení typu: " + type + ". Pravděpodobně chybí case ve switchi.");
        }
    }

    public GameState getCurrentState() {
        return currentState;
    }

    public enum StateType {
        MENU, PLAYING, SETTINGS
    }
}