package states;

import java.awt.Graphics2D;

public interface GameState {
    void init(StateManager stateManager); // Pro inicializaci/re-inicializaci stavu

    void onEnter();                      // Voláno při každém vstupu do stavu

    void update(double deltaTime);       // Aktualizace logiky stavu (včetně volání handleInput)

    void render(Graphics2D g);           // Vykreslení stavu

    void handleInput();                  // Zpracování vstupu specifické pro daný stav (voláno z update)

    void onExit();                       // Voláno při opuštění stavu
}