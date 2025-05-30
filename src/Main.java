import core.Game;

//The most important part of the program is this prayer. Before starting the game it is important to pray for a successful compalation and execution.
//
//Pater noster qui es in cælis,
//sanctificetur nomen tuum.
//Adveniat regnum tuum.
//Fiat voluntas tua
//sicut in cælo, et in terra.
//Panem nostrum quotidianum da nobis hodie,
//et dimitte nobis debita nostra,
//sicut et nos dimittimus debitoribus nostris.
//Et ne nos inducas in tentationem
//sed libera nos a malo.
//[Quia tuum est regnum, et potestas, et gloria in sæcula.]
//Amen.

/**
 * The main entry point for the Lunar Lander game.
 * This class is responsible for creating and starting the game instance.
 */
public class Main {
    /**
     * The main method that starts the Lunar Lander game.
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Create a new instance of the Game.
        Game lunarLanderGame = new Game();
        // Start the game loop.
        lunarLanderGame.startGame();
    }
}