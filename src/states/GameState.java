package states;

import java.awt.*;

public interface GameState {
    void init(StateManager stateManager);

    void onEnter();

    void update(double deltaTime);

    void render(Graphics2D g);

    void handleInput();

    void onExit();
}