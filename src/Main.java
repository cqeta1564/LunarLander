import core.Game;

public class Main {
    public static void main(String[] args) {
        // Vytvoření instance hlavní herní třídy
        Game lunarLanderGame = new Game();
        lunarLanderGame.startGame(); // Nebo jen game.run(), záleží na implementaci GameLoop
    }
}