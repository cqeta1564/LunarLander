package core;

import javax.swing.JFrame;
import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;

import input.InputHandler;

public class Window {
    private JFrame frame;
    private Canvas canvas;

    private int width, height;
    private String title;

    private BufferStrategy bs;
    // private Graphics2D g; // Graphics2D context budeme získávat čerstvý pro každý frame

    public Window(int width, int height, String title, Game game) {
        this.width = width;
        this.height = height;
        this.title = title;
        createDisplay(game.getInputHandler());
    }

    private void createDisplay(InputHandler inputHandler) {
        frame = new JFrame(title);
        frame.setSize(width, height);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        // frame.setIgnoreRepaint(true); // Pro aktivní rendering je to dobrá praxe

        canvas = new Canvas();
        canvas.setPreferredSize(new Dimension(width, height));
        canvas.setMaximumSize(new Dimension(width, height));
        canvas.setMinimumSize(new Dimension(width, height));
        canvas.setFocusable(true);

        canvas.addKeyListener(inputHandler);
        canvas.addMouseListener(inputHandler);
        canvas.addMouseMotionListener(inputHandler);

        frame.add(canvas);
        frame.pack();
        frame.setVisible(true); // Důležité volat před createBufferStrategy

        // Vytvoření BufferStrategy až poté, co je Canvas viditelný a přidaný do Frame
        try {
            canvas.createBufferStrategy(2); // Double buffering
            bs = canvas.getBufferStrategy();
        } catch (IllegalStateException e) {
            System.err.println("Nelze vytvořit BufferStrategy, komponenta není zobrazitelná: " + e.getMessage());
            // Pokud se stane, zkuste createBufferStrategy() zavolat později, např. při prvním renderu
        }
        if (bs == null) {
            System.err.println("BufferStrategy se nepodařilo vytvořit. Zkuste restartovat aplikaci.");
            // Možná bude potřeba fallback na jednodušší rendering nebo jiný přístup
        }
    }

    /**
     * Vrací Graphics2D kontext pro kreslení na aktuální buffer.
     * Tato metoda by měla být volána na začátku každého render cyklu.
     *
     * @return Graphics2D context, nebo null pokud BufferStrategy není dostupná.
     */
    public Graphics2D getGraphicsContext() {
        if (bs == null) {
            // Pokus o znovuvytvoření, pokud selhalo v createDisplay
            try {
                canvas.createBufferStrategy(2);
                bs = canvas.getBufferStrategy();
                if (bs == null) {
                    System.err.println("Stále se nedaří získat BufferStrategy v getGraphicsContext.");
                    return null;
                }
            } catch (Exception e) {
                System.err.println("Chyba při pokusu o znovuvytvoření BufferStrategy: " + e.getMessage());
                return null;
            }
        }
        return (Graphics2D) bs.getDrawGraphics();
    }

    /**
     * Zobrazí nakreslený buffer na obrazovce a uvolní grafický kontext.
     * Tato metoda by měla být volána na konci každého render cyklu.
     * Graphics2D context získaný z getGraphicsContext() by měl být uvolněn.
     */
    public void showGraphics() {
        if (bs != null) {
            // Graphics2D context by měl být uvolněn tím, kdo ho získal, typicky:
            // Graphics2D g = window.getGraphicsContext();
            // ... kreslení ...
            // g.dispose(); // <-- Uvolnění zde
            // window.showGraphics();

            // Nicméně, getDrawGraphics() vrací nový objekt pokaždé, takže dispose na něm je OK.
            // Samotné bs.show() provede výměnu bufferů.
            if (!bs.contentsLost()) { // Zkontrolujeme, jestli obsah bufferu nebyl ztracen
                bs.show();
            }
        }
    }

    // Gettery
    public Canvas getCanvas() {
        return canvas;
    }

    public JFrame getFrame() {
        return frame;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}