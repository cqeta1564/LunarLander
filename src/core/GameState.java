package core;

import java.awt.*;

//Pro různé stavy (Menu, Playing, GameOver)
public interface GameState {
    void update();

    void render(Graphics2D g);
}
