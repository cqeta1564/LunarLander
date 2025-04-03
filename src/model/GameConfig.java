package model;

//(Singleton) – Nastavení hry (gravitace, obtížnost)
public class GameConfig {
    private static GameConfig instance;
    private double gravity = 1.62; // Měsíční gravitace

    private GameConfig() {
    } // Privátní konstruktor

    public static GameConfig getInstance() {
        if (instance == null) {
            instance = new GameConfig();
        }
        return instance;
    }

    public double getGravity() {
        return gravity;
    }

    public void setGravity(double gravity) {
        this.gravity = gravity;
    }
}
