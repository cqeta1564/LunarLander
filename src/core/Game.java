package core;

import states.StateManager;
import states.MenuState; // Přidán import pro nastavení počátečního stavu
import input.InputHandler;

import java.awt.*;

// Další importy podle potřeby

public class Game implements Runnable {

    private Window window;
    private StateManager stateManager;
    private InputHandler inputHandler;
    // private AudioManager audioManager;

    private Thread gameThread;
    private volatile boolean running = false;

    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    public static final String TITLE = "Lunar Lander";
    public static final int TARGET_FPS = 60; // <--- Ujistěte se, že je zde
    public static final long OPTIMAL_TIME = 1000000000 / TARGET_FPS;

    public Game() {
        inputHandler = new InputHandler();
        window = new Window(DEFAULT_WIDTH, DEFAULT_HEIGHT, TITLE, this); // Předáme this pro KeyListener
        stateManager = new StateManager(inputHandler); // InputHandler předán do StateManageru
        // audioManager = AudioManager.getInstance();
        // Config.load();
    }

    public synchronized void startGame() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public synchronized void stopGame() {
        if (!running) return;
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        // Zde se TARGET_FPS používá korektně, pokud je definováno výše
        final double ns = 1000000000.0 / TARGET_FPS;
        double delta = 0;
        int frames = 0;
        int updates = 0;

        init(); // Inicializace po vytvoření okna a před smyčkou

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            boolean shouldRender = false; // Renderovat pouze pokud proběhl update

            while (delta >= 1) {
                update(delta); // Předáme delta pro plynulejší fyziku
                updates++;
                delta--;
                shouldRender = true;
            }

            // Renderovat pouze pokud proběhl alespoň jeden update,
            // nebo pokud chceme renderovat co nejčastěji (pak tuto podmínku odstranit)
            if (shouldRender) {
                render();
                frames++;
            }


            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames + ", UPS: " + updates);
                frames = 0;
                updates = 0;
            }

            // Omezení FPS, pokud smyčka běží příliš rychle (volitelné)
            // Pro jednoduchost můžeme vynechat, pokud update a render nejsou náročné
            // long loopTook = System.nanoTime() - now;
            // if (loopTook < OPTIMAL_TIME / 2) { // Jen příklad, jak by se to dalo
            //     try {
            //         Thread.sleep((OPTIMAL_TIME - loopTook) / 2000000); // Spát zlomek času
            //     } catch (InterruptedException e) {
            //         e.printStackTrace();
            //     }
            // }
        }
        // stopGame(); // stopGame() se volá po skončení smyčky, pokud je to žádoucí
        // nebo z externího požadavku (např. zavření okna)
    }

    private void init() {
        // window.createDisplay() se volá v konstruktoru Window
        // Nastavení počátečního stavu hry
        if (stateManager != null) {
            // Zde se nastaví výchozí stav, např. MENU
            // Ujistěte se, že MenuState (nebo jakýkoliv první stav) je vytvořen
            // a StateManager ho může nastavit.
            // V mém předchozím návrhu StateManager vytváří instance stavů v setState.
            stateManager.setState(StateManager.StateType.MENU);
        } else {
            System.err.println("StateManager nebyl inicializován před init() v Game!");
        }
        // Načtení zdrojů atd.
    }

    private void update(double deltaTime) {
        inputHandler.update(); // Aktualizace stavů kláves z InputHandleru
        if (stateManager.getCurrentState() != null) {
            stateManager.getCurrentState().update(deltaTime);
        }
    }

    private void render() {
        Graphics2D g = window.getGraphicsContext(); // Získání Graphics2D z Window
        if (g != null) {
            // Vyčištění obrazovky (může být součástí Rendereru nebo zde)
            g.setColor(Color.BLACK); // Příklad barvy pozadí
            g.fillRect(0, 0, window.getWidth(), window.getHeight());

            if (stateManager.getCurrentState() != null) {
                stateManager.getCurrentState().render(g); // Předáme Graphics2D
            }

            g.dispose(); // Uvolnění grafického kontextu PO všem kreslení
            window.showGraphics(); // Zobrazení bufferu
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
    // public AudioManager getAudioManager() { return audioManager; }
}