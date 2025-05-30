package core;

import input.InputHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

/**
 * Manages the game window (JFrame) and the drawing surface (Canvas).
 * It sets up the window properties, creates a buffer strategy for smooth rendering,
 * and provides access to the graphics context.
 */
public class Window {
    // Width of the window.
    private final int width;
    // Height of the window.
    private final int height;
    // The main window frame.
    private JFrame frame;
    // The canvas used for drawing game graphics.
    private Canvas canvas;
    // The buffer strategy for double or triple buffering.
    private BufferStrategy bs;

    /**
     * Constructs a new Window.
     *
     * @param width  The width of the window in pixels.
     * @param height The height of the window in pixels.
     * @param title  The title of the window.
     * @param game   The main Game instance, used to access the input handler.
     */
    public Window(int width, int height, String title, Game game) {
        this.width = width;
        this.height = height;
        createDisplay(title, game.getInputHandler());
    }

    /**
     * Creates and configures the JFrame and Canvas.
     *
     * @param title        The title for the JFrame.
     * @param inputHandler The InputHandler to register with the canvas for event listening.
     */
    private void createDisplay(String title, InputHandler inputHandler) {
        // Create the main game window.
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Exit application when window is closed.
        frame.setResizable(false); // Window cannot be resized by the user.
        frame.setLocationRelativeTo(null); // Center the window on the screen.

        // Create the canvas for drawing.
        canvas = new Canvas();
        Dimension canvasSize = new Dimension(width, height);
        canvas.setPreferredSize(canvasSize);
        canvas.setMaximumSize(canvasSize);
        canvas.setMinimumSize(canvasSize);
        canvas.setFocusable(true); // Canvas can receive focus for keyboard input.

        // Register input listeners with the canvas.
        canvas.addKeyListener(inputHandler);
        canvas.addMouseListener(inputHandler);
        canvas.addMouseMotionListener(inputHandler);

        // Add the canvas to the frame.
        frame.add(canvas);
        frame.pack(); // Adjust frame size to fit the preferred size of its components (the canvas).
        frame.setVisible(true); // Make the window visible.
        canvas.requestFocusInWindow(); // Request focus for the canvas to receive input immediately.

        // Attempt to create a double buffer strategy.
        try {
            canvas.createBufferStrategy(2); // Use 2 buffers (double buffering).
            bs = canvas.getBufferStrategy();
        } catch (IllegalStateException e) {
            // This can happen if the component is not yet displayable.
            System.err.println("Cannot create BufferStrategy: " + e.getMessage() + ". Component might not be displayable yet."); // Nelze vytvořit BufferStrategy... Komponenta nemusí být zobrazitelná.
        }
        // Retry creating buffer strategy if the first attempt failed (e.g., component wasn't displayable).
        if (bs == null) {
            try {
                Thread.sleep(100); // Wait briefly for the component to become displayable.
                canvas.createBufferStrategy(2);
                bs = canvas.getBufferStrategy();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.err.println("BufferStrategy creation interrupted."); // Vytváření BufferStrategy přerušeno.
            } catch (IllegalStateException ise) {
                System.err.println("Still cannot create BufferStrategy: " + ise.getMessage()); // Stále nelze vytvořit BufferStrategy
            }
        }
        if (bs == null) {
            // If buffer strategy still couldn't be created, log an error.
            // Rendering might be flickery or not work correctly.
            System.err.println("BufferStrategy could not be created. Game may not render correctly."); // BufferStrategy se nepodařilo vytvořit. Hra nemusí správně vykreslovat.
        }
    }

    /**
     * Gets the graphics context for drawing on the back buffer.
     * If the buffer strategy is lost or not initialized, it attempts to re-create it.
     *
     * @return The {@link Graphics2D} context, or null if it cannot be obtained.
     */
    public Graphics2D getGraphicsContext() {
        // If buffer strategy is somehow null, try to recreate it.
        if (bs == null) {
            try {
                canvas.createBufferStrategy(2);
                bs = canvas.getBufferStrategy();
                if (bs == null) {
                    System.err.println("Critical error: BufferStrategy is not available in getGraphicsContext."); // Kritická chyba: BufferStrategy není dostupná v getGraphicsContext.
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Error during emergency BufferStrategy creation: " + e.getMessage()); // Chyba při nouzovém vytváření BufferStrategy
                return null;
            }
        }
        return (Graphics2D) bs.getDrawGraphics(); // Get the graphics object for the current back buffer.
    }

    /**
     * Shows the contents of the back buffer on the screen.
     * This effectively swaps the front and back buffers if using double buffering.
     * Handles cases where the buffer contents might have been lost.
     */
    public void showGraphics() {
        if (bs != null && !bs.contentsLost()) {
            bs.show(); // Flip/show the buffer.
        } else if (bs != null && bs.contentsLost()) {
            // This can happen due to OS events (e.g., screen mode change).
            // The contents of the drawing buffer are lost and need to be redrawn.
            System.err.println("BufferStrategy contents were lost."); // Obsah BufferStrategy byl ztracen.
        }
    }

    /**
     * Gets the width of the window.
     *
     * @return The window width in pixels.
     */
    public int getWidth() {
        return width;
    }

    /**
     * Gets the height of the window.
     *
     * @return The window height in pixels.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Gets the canvas used for drawing.
     *
     * @return The {@link Canvas} object.
     */
    public Canvas getCanvas() {
        return canvas;
    }
}