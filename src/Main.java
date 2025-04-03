import core.GameEngine;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameEngine game = new GameEngine();
            game.start();
        });
    }
}