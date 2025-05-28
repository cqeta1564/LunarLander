package states;

import core.Game;
import entities.Lander; // Ujistěte se, že tento import je zde
import entities.Terrain;
import input.GameAction;
import input.InputHandler;
import input.KeyBindings;

import java.awt.*;
import java.awt.geom.AffineTransform; // <<--- PŘIDAT IMPORT
import java.util.List;              // <<--- PŘIDAT IMPORT (pro java.util.List)
// KeyEvent zde není potřeba, pokud nepoužíváme consumeKey na specifické klávesy

public class PlayingState implements GameState {

    private final StateManager stateManager;
    private final InputHandler inputHandler;
    private Terrain terrain;
    private Lander lander;
    private float cameraX = 0;
    private boolean playerControlTakenSinceEnter = false;

    private float currentZoom = 1.0f;
    private float targetZoom = 1.0f;
    private static final float ZOOM_LEVEL_IN = 1.8f;
    private static final float ZOOM_LEVEL_OUT = 1.0f;
    private static final float DISTANCE_TO_ZOOM_IN_THRESHOLD = 100.0f;
    private static final float DISTANCE_TO_ZOOM_OUT_THRESHOLD = 150.0f;
    private static final float ZOOM_INTERPOLATION_SPEED = 2.0f;

    public PlayingState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
    }

    @Override
    public void init(StateManager manager) {
        // Zde může být jednorázová inicializace zdrojů, pokud je potřeba
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do PlayingState.");
        if (this.terrain == null) { // Vytvořit terén jen jednou
            this.terrain = new Terrain(Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);
        }
        this.cameraX = 0; // Nebo jiná logika pro startovní pozici kamery
        // PopulateVisibleTerrain se volá na základě cameraX a viewWidth (která může být ovlivněna zoomem)
        // Prozatím viewWidth = Game.DEFAULT_WIDTH, upravíme později pokud zoom mění rozsah dat pro populate
        this.terrain.populateVisibleTerrain(cameraX, Game.DEFAULT_WIDTH);


        float startY = Game.DEFAULT_HEIGHT * Lander.INITIAL_FLYOVER_START_Y_RATIO;
        float startX = -Lander.DISPLAY_LANDER_WIDTH; // Start mírně mimo obrazovku vlevo, použijeme DISPLAY_LANDER_WIDTH

        if (this.lander == null) {
            this.lander = new Lander(startX, startY);
        } else {
            lander.reset(startX, startY);
        }
        playerControlTakenSinceEnter = false;
        currentZoom = ZOOM_LEVEL_OUT; // Reset zoomu při vstupu do stavu
        targetZoom = ZOOM_LEVEL_OUT;
    }

    // --- JEDINÁ METODA UPDATE ---
    @Override
    public void update(double deltaTime) {
        handleInput();

        if (lander != null) {
            lander.update(deltaTime, terrain);

            // --- Logika pro Zoom Kamery ---
            // Používáme Lander.State, který je nyní public
            if (terrain != null && terrain.getTerrainSurfacePoints() != null && !terrain.getTerrainSurfacePoints().isEmpty() &&
                    (lander.getCurrentState() == Lander.State.PLAYER_CONTROL || lander.getCurrentState() == Lander.State.INITIAL_FLYOVER) ) {

                // Landerovy souřadnice jsou světové, cameraX je posun světa vůči levému okraji obrazovky
                double landerEffectiveScreenX = lander.getX() - cameraX;
                // Pro Y souřadnici nožiček použijeme DISPLAY_LANDER_HEIGHT, která je nyní public
                double landerFeetWorldY = lander.getY() + (Lander.DISPLAY_LANDER_HEIGHT / 2.0);

                float terrainYAtLanderX = -1;
                List<Point> surfacePoints = terrain.getTerrainSurfacePoints(); // Toto je java.util.List<java.awt.Point>

                if (surfacePoints.size() > 1) {
                    Point p1 = null, p2 = null;
                    for (int i = 0; i < surfacePoints.size() - 1; i++) {
                        // surfacePoints jsou již v screen-space pro daný cameraX
                        if (surfacePoints.get(i).x <= landerEffectiveScreenX && surfacePoints.get(i + 1).x >= landerEffectiveScreenX) {
                            p1 = surfacePoints.get(i);
                            p2 = surfacePoints.get(i + 1);
                            break;
                        }
                    }

                    if (p1 == null && landerEffectiveScreenX < surfacePoints.get(0).x) {
                        p1 = new Point((int)(landerEffectiveScreenX -10), surfacePoints.get(0).y);
                        p2 = surfacePoints.get(0);
                    } else if (p1 == null && landerEffectiveScreenX > surfacePoints.get(surfacePoints.size()-1).x) {
                        p1 = surfacePoints.get(surfacePoints.size()-1);
                        p2 = new Point((int)(landerEffectiveScreenX + 10), surfacePoints.get(surfacePoints.size()-1).y);
                    }

                    if (p1 != null && p2 != null) {
                        if (p2.x == p1.x) {
                            terrainYAtLanderX = Math.min(p1.y, p2.y);
                        } else {
                            float t = (float) (landerEffectiveScreenX - p1.x) / (float) (p2.x - p1.x);
                            terrainYAtLanderX = p1.y + t * (p2.y - p1.y);
                        }

                        // Vzdálenost od nožiček (jejich Y je landerFeetWorldY) k povrchu terénu
                        // landerFeetWorldY je "absolutní" Y, terrainYAtLanderX je Y povrchu na obrazovce
                        // Obě jsou v systému, kde Y roste dolů.
                        float distanceToSurface = terrainYAtLanderX - (float)landerFeetWorldY;

                        if (distanceToSurface < DISTANCE_TO_ZOOM_IN_THRESHOLD && distanceToSurface >= -Lander.DISPLAY_LANDER_HEIGHT) { // Zoom i pokud je mírně pod povrchem
                            targetZoom = ZOOM_LEVEL_IN;
                        } else if (distanceToSurface > DISTANCE_TO_ZOOM_OUT_THRESHOLD) {
                            targetZoom = ZOOM_LEVEL_OUT;
                        }
                    } else {
                        targetZoom = ZOOM_LEVEL_OUT;
                    }
                } else {
                    targetZoom = ZOOM_LEVEL_OUT;
                }
            } else if (lander.getCurrentState() == Lander.State.LANDED || lander.getCurrentState() == Lander.State.CRASHED) {
                targetZoom = ZOOM_LEVEL_OUT; // Oddálit po přistání/havárii
            }


            currentZoom += (targetZoom - currentZoom) * ZOOM_INTERPOLATION_SPEED * deltaTime;
            currentZoom = Math.max(ZOOM_LEVEL_OUT, Math.min(currentZoom, ZOOM_LEVEL_IN * 1.05f)); // Mírný overshoot povolen
        }
    }
    // --- KONEC JEDINÉ METODY UPDATE ---


    @Override
    public void handleInput() {
        if (inputHandler.isEscJustPressed()) {
            stateManager.setState(StateManager.StateType.MENU);
            return;
        }
        if (lander == null) return;

        boolean actionKeyPressed = false;
        int rotation = 0;
        if (inputHandler.isActionActive(GameAction.ROTATE_LEFT)) {
            rotation = -1;
            actionKeyPressed = true;
        } else if (inputHandler.isActionActive(GameAction.ROTATE_RIGHT)) {
            rotation = 1;
            actionKeyPressed = true;
        }
        lander.setRotation(rotation);

        boolean thrusting = inputHandler.isActionActive(GameAction.THRUST);
        if (thrusting) {
            actionKeyPressed = true;
        }
        lander.setPlayerRequestsThrust(thrusting);

        if (actionKeyPressed && !playerControlTakenSinceEnter) {
            lander.playerHasTakenControl();
            playerControlTakenSinceEnter = true;
        }
    }

    @Override
    public void render(Graphics2D g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform originalTransform = g2d.getTransform();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(10, 10, 20));
        g2d.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        // Aplikace transformace kamery a zoomu
        if (lander != null) {
            double focusX_world = lander.getX(); // Světová X pozice landeru
            double focusY_world = lander.getY(); // Světová Y pozice landeru

            // Chceme, aby bod (focusX_world, focusY_world) byl středem zoomu
            // a po zoomu se zobrazil uprostřed obrazovky.
            g2d.translate(Game.DEFAULT_WIDTH / 2.0, Game.DEFAULT_HEIGHT / 2.0); // 1. Posun do středu obrazovky
            g2d.scale(currentZoom, currentZoom);                                 // 2. Zoom kolem nového počátku (středu obr.)
            g2d.translate(-focusX_world, -focusY_world);                       // 3. Posun tak, aby lander byl v počátku (a tedy ve středu)

            // Nyní jsou souřadnice ve světě, kde lander je (0,0) a vše je zoomováno.
            // Musíme to posunout o cameraX, aby se zobrazila správná část světa.
            // Toto je složitější. Jednodušší je zoomovat kolem screen pozice landeru.

            // Vraťme se k jednoduššímu zoomu kolem screen pozice landeru:
            g2d.setTransform(originalTransform); // Reset na původní transformaci
            g2d.setColor(new Color(10, 10, 20)); // Znovu pozadí, protože reset transformace ho mohl ovlivnit
            g2d.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);


            double landerScreenX = lander.getX() - cameraX; // X landeru na nezoomované obrazovce
            double landerScreenY = lander.getY();           // Y landeru na nezoomované obrazovce

            g2d.translate(landerScreenX, landerScreenY);    // A. Posun na screen pozici landeru
            g2d.scale(currentZoom, currentZoom);            // B. Zoom kolem tohoto nového počátku
            g2d.translate(-landerScreenX, -landerScreenY);  // C. Posun zpět (efekt: zoom kolem [landerScreenX, landerScreenY])
        }
        // Konec transformace kamery a zoomu

        if (terrain != null) {
            // Terrain.render kreslí body z terrainSurfacePoints, které jsou již screen-space pro daný cameraX.
            // Globální transformace (A,B,C) se na ně aplikuje.
            terrain.render(g2d);
        }

        if (lander != null) {
            // Lander.render kreslí na this.x, this.y (world).
            // Potřebujeme, aby se kreslil s ohledem na cameraX a aplikovaný zoom.
            // Nejjednodušší je, pokud PlayingState aplikuje PLNÝ posun kamery PŘED zoomem.

            // Zkusme tedy tento přístup pro render:
            g2d.setTransform(originalTransform); // Začneme znovu s původní transformací
            g2d.setColor(new Color(10, 10, 20)); // Znovu pozadí
            g2d.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

            // 1. Globální posun kamery (cameraX)
            g2d.translate(-cameraX, 0);

            // 2. Zoom kolem landeru (jehož X je nyní this.x, Y je this.y v tomto transformovaném prostoru)
            if (lander != null && currentZoom != 1.0f) { // Zoom pouze pokud je aktivní a lander existuje
                g2d.translate(lander.getX(), lander.getY());    // Posun na světovou pozici landeru (která je teď relativní k posunuté kameře)
                g2d.scale(currentZoom, currentZoom);            // Zoom
                g2d.translate(-lander.getX(), -lander.getY());  // Posun zpět
            }

            // Kreslení světa
            if (terrain != null) {
                // Terrain.render musí nyní kreslit své body jako by byly světové souřadnice.
                // NEBO `terrain.populateVisibleTerrain` je voláno s `cameraX` a jeho body jsou screen-space.
                // Pokud `terrainSurfacePoints` jsou screen-space pro `cameraX=0` (a populate se stará o `cameraX`),
                // pak předchozí `terrain.render(g2d)` po zoomu bylo správnější.

                // Pro zachování stávajícího Terrain.render(), který kreslí screen-space body:
                // 1. Uložit transformaci PO cameraX posunu, ALE PŘED zoomem.
                AffineTransform transformBeforeZoom = g2d.getTransform();
                if (lander != null && currentZoom != 1.0f) {
                    g2d.translate(lander.getX() - cameraX, lander.getY()); // Fokus na screen pozici landeru
                    g2d.scale(currentZoom, currentZoom);
                    g2d.translate(-(lander.getX() - cameraX), -lander.getY());
                }
                terrain.render(g2d); // Kreslí screen-space body, nyní zoomované
                g2d.setTransform(transformBeforeZoom); // Vrátit transformaci bez zoomu, ale s cameraX posunem
            }


            if (lander != null) {
                // Lander se kreslí na svých světových souřadnicích (this.x, this.y)
                // a g2d je již posunuto o -cameraX, takže se vykreslí správně.
                // Pokud byl aplikován zoom, je také správně ovlivněn.
                lander.render(g2d);
            }
        }

        // Obnovíme původní transformaci pro HUD
        g2d.setTransform(originalTransform);

        g2d.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        if (lander != null) {
            g2d.drawString(String.format("Lander X: %.0f Y: %.0f", lander.getX(), lander.getY()), 10, 20);
            g2d.drawString(String.format("Zoom: %.2fx", currentZoom), 10, 40);
        }
        g2d.drawString("SKÓRE: 0", Game.DEFAULT_WIDTH - 150, 20);

        KeyBindings kb = inputHandler.getKeyBindings();
        if (kb != null) {
            // ... (zbytek HUD pro klávesy) ...
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2d.setColor(new Color(200, 200, 200, 180));
            int yPos = Game.DEFAULT_HEIGHT - 45;
            g2d.drawString("Ovládání:", 10, yPos); yPos += 12;
            g2d.drawString(" Tah: " + kb.getKeyTextForAction(GameAction.THRUST), 10, yPos); yPos += 12;
            g2d.drawString(" Vlevo: " + kb.getKeyTextForAction(GameAction.ROTATE_LEFT), 10, yPos); yPos += 12;
            g2d.drawString(" Vpravo: " + kb.getKeyTextForAction(GameAction.ROTATE_RIGHT), 10, yPos);
        }
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění PlayingState.");
        if (lander != null) {
            lander.setPlayerRequestsThrust(false);
            lander.setRotation(0);
        }
    }
}