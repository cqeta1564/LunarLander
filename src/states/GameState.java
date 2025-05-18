package states;

// import graphics.Renderer; // Až budeme mít třídu Renderer

import java.awt.Graphics2D; // Prozatím

public interface GameState {
    void init(StateManager stateManager); // Pro inicializaci stavu, předání StateManageru

    void update(double deltaTime);       // Aktualizace logiky stavu

    // void render(graphics.Renderer renderer); // Vykreslení stavu
    void render(Graphics2D g); // Prozatímní verze s Graphics2D

    void handleInput();                  // Zpracování vstupu specifické pro daný stav

    void onEnter();                      // Voláno při vstupu do stavu

    void onExit();                       // Voláno při opuštění stavu
}