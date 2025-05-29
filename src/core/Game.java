package core;

import audio.AudioManager;
import input.InputHandler;
import states.StateManager;

import java.awt.*;

public class Game implements Runnable {

    public static final int DEFAULT_WIDTH = 1200;
    public static final int DEFAULT_HEIGHT = 675;
    public static final String TITLE = "Lunar Lander";
    public static final int TARGET_FPS = 60;
    public static final int TARGET_UPS = 60;
    private final Window window;
    private final StateManager stateManager;
    private final InputHandler inputHandler;
    private Thread gameThread;
    private volatile boolean running = false;

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
        AudioManager.getInstance().shutdown();
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

            inputHandler.update();

            while (unprocessedUpdates >= 1.0) {
                updateGameLogic(1.0 / TARGET_UPS);
                updates++;
                unprocessedUpdates -= 1.0;
            }

            renderGame();
            frames++;

            inputHandler.finishFrame();

            if (System.currentTimeMillis() - timer >= 1000) {
                System.out.println("FPS: " + frames + ", UPS: " + updates);
                frames = 0;
                updates = 0;
                timer += 1000;
            }

            long loopCycleTime = System.nanoTime() - currentTime;
            long sleepTime = (long) ((1000000000.0 / TARGET_FPS) - loopCycleTime);

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

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public Window getWindow() {
        return window;
    }
}