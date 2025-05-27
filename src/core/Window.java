package core;

import input.InputHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferStrategy;

public class Window {
    private final int width;
    private final int height;
    private JFrame frame;
    private Canvas canvas;
    private BufferStrategy bs;

    public Window(int width, int height, String title, Game game) {
        this.width = width;
        this.height = height;
        createDisplay(title, game.getInputHandler());
    }

    private void createDisplay(String title, InputHandler inputHandler) {
        frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        canvas = new Canvas();
        Dimension canvasSize = new Dimension(width, height);
        canvas.setPreferredSize(canvasSize);
        canvas.setMaximumSize(canvasSize);
        canvas.setMinimumSize(canvasSize);
        canvas.setFocusable(true);
        canvas.addKeyListener(inputHandler);
        canvas.addMouseListener(inputHandler);
        canvas.addMouseMotionListener(inputHandler);

        frame.add(canvas);
        frame.pack();
        frame.setVisible(true);
        canvas.requestFocusInWindow();

        try {
            canvas.createBufferStrategy(2);
            bs = canvas.getBufferStrategy();
        } catch (IllegalStateException e) {
            System.err.println("Nelze vytvořit BufferStrategy: " + e.getMessage() + ". Komponenta nemusí být zobrazitelná.");
        }
        if (bs == null) {
            try {
                Thread.sleep(100);
                canvas.createBufferStrategy(2);
                bs = canvas.getBufferStrategy();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                System.err.println("Vytváření BufferStrategy přerušeno.");
            } catch (IllegalStateException ise) {
                System.err.println("Stále nelze vytvořit BufferStrategy: " + ise.getMessage());
            }
        }
        if (bs == null) {
            System.err.println("BufferStrategy se nepodařilo vytvořit. Hra nemusí správně vykreslovat.");
        }
    }

    public Graphics2D getGraphicsContext() {
        if (bs == null) {
            try {
                canvas.createBufferStrategy(2);
                bs = canvas.getBufferStrategy();
                if (bs == null) {
                    System.err.println("Kritická chyba: BufferStrategy není dostupná v getGraphicsContext.");
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Chyba při nouzovém vytváření BufferStrategy: " + e.getMessage());
                return null;
            }
        }
        return (Graphics2D) bs.getDrawGraphics();
    }

    public void showGraphics() {
        if (bs != null && !bs.contentsLost()) {
            bs.show();
        } else if (bs != null && bs.contentsLost()) {
            System.err.println("Obsah BufferStrategy byl ztracen.");
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Canvas getCanvas() {
        return canvas;
    }
}