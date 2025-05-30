package core;

import audio.AudioManager;
import input.InputHandler;
import states.StateManager;

import java.awt.*;

/**
 * The main class for the Lunar Lander game.
 * It implements the Runnable interface to create the game loop in a separate thread.
 * This class manages the game window, state transitions, input handling,
 * and the core update/render cycle.
 */
public class Game implements Runnable {

    /**
     * Default width of the game window in pixels.
     */
    public static final int DEFAULT_WIDTH = 1200;
    /**
     * Default height of the game window in pixels.
     */
    public static final int DEFAULT_HEIGHT = 675;
    /**
     * Title of the game window.
     */
    public static final String TITLE = "Lunar Lander"; // Title of the game.
    /**
     * Target frames per second (FPS) for rendering.
     */
    public static final int TARGET_FPS = 60;
    /**
     * Target updates per second (UPS) for game logic.
     */
    public static final int TARGET_UPS = 60;

    // The game window.
    private final Window window;
    // Manages game states (e.g., menu, playing, settings).
    private final StateManager stateManager;
    // Handles user input (keyboard and mouse).
    private final InputHandler inputHandler;
    // The main game thread.
    private Thread gameThread;
    // Flag to control the game loop's execution. Marked volatile for thread safety.
    private volatile boolean running = false;

    /**
     * Constructs a new Game instance.
     * Initializes the input handler, window, and state manager.
     */
    public Game() {
        inputHandler = new InputHandler();
        window = new Window(DEFAULT_WIDTH, DEFAULT_HEIGHT, TITLE, this);
        stateManager = new StateManager(inputHandler);
    }

    /**
     * Starts the game.
     * If the game is not already running, it sets the running flag to true
     * and starts a new game thread.
     */
    public synchronized void startGame() {
        if (running) return; // Prevent starting if already running.
        running = true;
        gameThread = new Thread(this, "GameThread"); // Create a new thread for the game loop.
        gameThread.start(); // Start the thread.
    }

    /**
     * Stops the game.
     * If the game is running, it sets the running flag to false and waits for
     * the game thread to join (terminate).
     * Also shuts down the AudioManager.
     */
    public synchronized void stopGame() {
        if (!running) return; // Prevent stopping if not running.
        running = false;
        try {
            gameThread.join(); // Wait for the game thread to finish.
        } catch (InterruptedException e) {
            System.err.println("Game thread interrupted during stop: " + e.getMessage());
            Thread.currentThread().interrupt(); // Re-interrupt the current thread.
        }
        AudioManager.getInstance().shutdown(); // Clean up audio resources.
    }

    /**
     * The main game loop, executed by the game thread.
     * Manages game timing, updates game logic, and renders the game.
     * Aims for {@link #TARGET_UPS} updates and {@link #TARGET_FPS} frames per second.
     */
    @Override
    public void run() {
        init(); // Initialize game components.

        long lastTime = System.nanoTime(); // Time of the last loop iteration.
        double timePerUpdate = 1000000000.0 / TARGET_UPS; // Time allocated for each game update.
        double unprocessedUpdates = 0; // Accumulator for unprocessed update time.

        long timer = System.currentTimeMillis(); // Timer for tracking FPS and UPS.
        int frames = 0;  // Frame counter for the current second.
        int updates = 0; // Update counter for the current second.

        while (running) {
            long currentTime = System.nanoTime(); // Current time at the start of the loop.
            // Calculate how much time has passed since the last loop, and add it to unprocessedUpdates.
            unprocessedUpdates += (currentTime - lastTime) / timePerUpdate;
            lastTime = currentTime;

            // Process input before updates.
            inputHandler.update();

            // Process all accumulated updates.
            // This loop ensures game logic updates at a consistent rate (TARGET_UPS).
            while (unprocessedUpdates >= 1.0) {
                updateGameLogic(1.0 / TARGET_UPS); // Update game logic with a fixed delta time.
                updates++;
                unprocessedUpdates -= 1.0;
            }

            renderGame(); // Render the current game state.
            frames++;

            // Finalize input states for the next frame.
            inputHandler.finishFrame();

            // Print FPS and UPS once per second.
            if (System.currentTimeMillis() - timer >= 1000) {
                System.out.println("FPS: " + frames + ", UPS: " + updates); // Output FPS and UPS to console
                frames = 0;
                updates = 0;
                timer += 1000;
            }

            // Calculate time taken by the loop cycle.
            long loopCycleTime = System.nanoTime() - currentTime;
            // Calculate sleep time to achieve TARGET_FPS.
            // This yields CPU time to other processes if the loop is running faster than needed.
            long sleepTime = (long) ((1000000000.0 / TARGET_FPS) - loopCycleTime);

            if (sleepTime > 0) {
                try {
                    // Sleep for the calculated duration.
                    // Thread.sleep takes milliseconds and nanoseconds.
                    Thread.sleep(sleepTime / 1000000, (int) (sleepTime % 1000000));
                } catch (InterruptedException e) {
                    System.err.println("Game thread interrupted during sleep: " + e.getMessage());
                    Thread.currentThread().interrupt(); // Re-interrupt the current thread.
                }
            }
        }
        // stopGame(); // Potentially call stopGame here if not called externally.
    }

    /**
     * Initializes the game, primarily by setting the initial game state.
     */
    private void init() {
        if (stateManager != null) {
            // Set the initial game state to MENU.
            stateManager.setState(StateManager.StateType.MENU);
        } else {
            System.err.println("StateManager was not initialized before init() in Game!"); // StateManager nebyl inicializován před init() v Game!
        }
    }

    /**
     * Updates the game logic for the current active state.
     *
     * @param deltaTime The time elapsed since the last update, in seconds.
     *                  This is a fixed value based on TARGET_UPS.
     */
    private void updateGameLogic(double deltaTime) {
        if (stateManager.getCurrentState() != null) {
            stateManager.getCurrentState().update(deltaTime);
        }
    }

    /**
     * Renders the current game state to the window.
     * Uses a buffer strategy for smooth rendering.
     */
    private void renderGame() {
        // Get the graphics context from the window's buffer strategy.
        Graphics2D g = window.getGraphicsContext();
        if (g != null) {
            // Clear the screen with black color.
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, window.getWidth(), window.getHeight());

            // Render the current game state.
            if (stateManager.getCurrentState() != null) {
                stateManager.getCurrentState().render(g);
            }

            // Dispose of the graphics context to release system resources.
            g.dispose();
            // Show the contents of the back buffer.
            window.showGraphics();
        } else {
            System.err.println("Failed to get Graphics context for rendering."); // Nepodařilo se získat Graphics context pro renderování.
        }
    }

    /**
     * Returns the input handler.
     *
     * @return The game's {@link InputHandler}.
     */
    public InputHandler getInputHandler() {
        return inputHandler;
    }

    /**
     * Returns the game window.
     *
     * @return The game's {@link Window}.
     */
    public Window getWindow() {
        return window;
    }
}