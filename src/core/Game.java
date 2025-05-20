package core;

import input.InputHandler;
import states.StateManager;
import java.awt.Color;
import java.awt.Graphics2D;

public class Game implements Runnable {

    private Window window;
    private StateManager stateManager;
    private InputHandler inputHandler;

    private Thread gameThread;
    private volatile boolean running = false;

    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final String TITLE = "Lunar Lander";

    public static final int TARGET_FPS = 60;
    public static final int TARGET_UPS = 60;

    public Game() {
        inputHandler = new InputHandler();
        window = new Window(DEFAULT_WIDTH, DEFAULT_HEIGHT, TITLE, this);
        stateManager = new StateManager(inputHandler);
    }

    public synchronized void startGame() {
        if (running) return;
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }

    public synchronized void stopGame() {
        if (!running) return;
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            System.err.println("Game thread interrupted during stop: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run() {
        init();

        long lastTime = System.nanoTime();
        double timePerUpdate = 1000000000.0 / TARGET_UPS;
        double unprocessedUpdates = 0;

        long timer = System.currentTimeMillis();
        int frames = 0;
        int updates = 0;

        while (running) {
            long currentTime = System.nanoTime();
            unprocessedUpdates += (currentTime - lastTime) / timePerUpdate;
            lastTime = currentTime;

            // Zavoláme inputHandler.update() jednou na začátku cyklu,
            // aby se vypočítaly "justPressed" stavy pro tento "snímek" zpracování.
            inputHandler.update(); // <<--- ZMĚNA Z preUpdate()

            // Logické updaty s pevným krokem
            // Smyčka se postará o to, aby se logika aktualizovala správným počtem kroků, pokud hra zaostává.
            while (unprocessedUpdates >= 1.0) {
                updateGameLogic(1.0 / TARGET_UPS); // Předání fixního delta času
                updates++;
                unprocessedUpdates -= 1.0;
            }

            renderGame();
            frames++;

            // Zavoláme inputHandler.finishFrame() jednou na konci cyklu, po renderování.
            // Tím se připraví previousKeys pro výpočet "justPressed" v příštím volání inputHandler.update().
            inputHandler.finishFrame(); // <<--- ZMĚNA Z postUpdate()


            if (System.currentTimeMillis() - timer >= 1000) {
                System.out.println("FPS: " + frames + ", UPS: " + updates);
                frames = 0;
                updates = 0;
                timer += 1000;
            }

            // Omezení FPS
            long loopCycleTime = System.nanoTime() - currentTime; // Jak dlouho trval aktuální cyklus
            long sleepTime = (long)((1000000000.0 / TARGET_FPS) - loopCycleTime);

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime / 1000000, (int) (sleepTime % 1000000));
                } catch (InterruptedException e) {
                    System.err.println("Game thread interrupted during sleep: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void init() {
        if (stateManager != null) {
            stateManager.setState(StateManager.StateType.MENU);
        } else {
            System.err.println("StateManager nebyl inicializován před init() v Game!");
        }
    }

    private void updateGameLogic(double deltaTime) {
        if (stateManager.getCurrentState() != null) {
            stateManager.getCurrentState().update(deltaTime);
        }
    }

    private void renderGame() {
        Graphics2D g = window.getGraphicsContext();
        if (g != null) {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, window.getWidth(), window.getHeight());
            if (stateManager.getCurrentState() != null) {
                stateManager.getCurrentState().render(g);
            }
            g.dispose();
            window.showGraphics();
        } else {
            System.err.println("Nepodařilo se získat Graphics context pro renderování.");
        }
    }

    public InputHandler getInputHandler() { return inputHandler; }
    public Window getWindow() { return window; }
}