package core;

import graphics.Renderer;
import input.InputHandler;
import model.Lander;
import model.Terrain;
import physics.PhysicsEngine;
import sound.SoundManager;

import javax.swing.*;

//Hlavní herní smyčka (update & render)
public class GameEngine {
    private JFrame frame;
    private Renderer renderer;
    private Lander lander;
    private Terrain terrain;
    private PhysicsEngine physicsEngine;
    private InputHandler inputHandler;
    private SoundManager soundManager;

    public GameEngine() {
        // 1. Nejprve inicializujeme modelové třídy
        this.terrain = Terrain.generateRandom(800, 600); // Inicializace TERRAINU
        this.lander = new Lander(100, 100);

        // 2. Až poté vytváříme Renderer
        this.soundManager = new SoundManager();
        this.renderer = new Renderer(lander, terrain, soundManager);
        this.physicsEngine = new PhysicsEngine();

        setupFrame();
    }

    private void setupFrame() {
        frame = new JFrame("Lunar Lander (Swing)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.add(renderer);
        frame.addKeyListener(inputHandler); // Registrace ovládání
        frame.setVisible(true);
    }

    public void start() {
        // Herní smyčka pomocí Swing Timer
        Timer timer = new Timer(16, e -> {
            physicsEngine.update(lander, terrain, 0.016); // 60 FPS
            renderer.repaint(); // Překreslí scénu
        });
        timer.start();
    }

    private void update(double deltaTime) {
        if (!lander.isLanded() && !lander.isCrashed()) {
            physicsEngine.update(lander, terrain, deltaTime);

            // Ovládání zvuku motoru
            soundManager.playEngine(lander.isThrusting() && lander.getFuel() > 0);
        }

        renderer.repaint();
    }

    // V metodě, kde detekujeme přistání/náraz:
    private void checkGameState() {
        if (lander.isLanded()) {
            soundManager.playLanding();
            soundManager.playEngine(false);
        } else if (lander.isCrashed()) {
            soundManager.playCrash();
            soundManager.playEngine(false);
        }
    }
}
