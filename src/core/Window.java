package core;

import input.InputHandler; // Potřebujeme pro addKeyListener atd.

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

public class Window {
    private JFrame frame;
    private Canvas canvas; // Pro aktivní rendering s BufferStrategy

    private final int width;
    private final int height;
    // private final String title; // title je použit jen v konstruktoru frame

    private BufferStrategy bs;

    public Window(int width, int height, String title, Game game) {
        this.width = width;
        this.height = height;
        // this.title = title; // Není potřeba ukládat, pokud se nemění
        createDisplay(title, game.getInputHandler());
    }

    private void createDisplay(String title, InputHandler inputHandler) {
        frame = new JFrame(title);
        // frame.setSize(width, height); // Nahrazeno pack() po přidání canvasu
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // Centrovaní okna

        canvas = new Canvas();
        Dimension canvasSize = new Dimension(width, height);
        canvas.setPreferredSize(canvasSize);
        canvas.setMaximumSize(canvasSize);
        canvas.setMinimumSize(canvasSize);
        canvas.setFocusable(true); // Aby mohl přijímat vstup z klávesnice

        // Připojení InputHandleru ke Canvasu
        canvas.addKeyListener(inputHandler);
        canvas.addMouseListener(inputHandler);
        canvas.addMouseMotionListener(inputHandler);

        frame.add(canvas);
        frame.pack(); // Přizpůsobí velikost frame obsahu (canvas)
        frame.setVisible(true);
        canvas.requestFocusInWindow(); // <<--- PŘIDAT TENTO ŘÁDEK

        try {
            canvas.createBufferStrategy(2); // Double buffering
            bs = canvas.getBufferStrategy();
        } catch (IllegalStateException e) {
            System.err.println("Nelze vytvořit BufferStrategy: " + e.getMessage() + ". Komponenta nemusí být zobrazitelná.");
        }
        if (bs == null) {
            // Zkusit znovu po krátké pauze, pokud by setVisible nebylo dokončeno
            try {
                Thread.sleep(100); // Krátká pauza
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
            // Pokus o záchranu, pokud selhalo při inicializaci
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
        return (Graphics2D) bs.getDrawGraphics(); // Získá nový Graphics context pro kreslení
    }

    public void showGraphics() {
        if (bs != null && !bs.contentsLost()) {
            bs.show(); // Vymění buffery
        } else if (bs != null && bs.contentsLost()) {
            System.err.println("Obsah BufferStrategy byl ztracen.");
            // Zde by mohla být logika pro obnovu obsahu, pokud je to nutné
        }
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // getCanvas() a getFrame() mohou být užitečné pro pokročilejší operace nebo ladění
    public Canvas getCanvas() {
        return canvas;
    }
    // public JFrame getFrame() { return frame; }
}