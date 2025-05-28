package states;

import core.Game;
import entities.Lander;
import entities.Terrain;
import input.GameAction;
import input.InputHandler;
import input.KeyBindings;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.List;

public class PlayingState implements GameState {

    private static final float ZOOM_LEVEL_IN = 1.8f;
    private static final float ZOOM_LEVEL_OUT = 1.0f;
    private static final float DISTANCE_TO_ZOOM_IN_THRESHOLD = 100.0f;
    private static final float DISTANCE_TO_ZOOM_OUT_THRESHOLD = 150.0f;
    private static final float ZOOM_INTERPOLATION_SPEED = 2.0f;
    private final StateManager stateManager;
    private final InputHandler inputHandler;
    private Terrain terrain;
    private Lander lander;
    private float cameraX = 0;
    private boolean playerControlTakenSinceEnter = false;
    private float currentZoom = 1.0f;
    private float targetZoom = 1.0f;

    public PlayingState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
    }

    @Override
    public void init(StateManager manager) {
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do PlayingState.");
        if (this.terrain == null) { // Vytvořit terén jen jednou
            this.terrain = new Terrain(Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);
        }
        this.cameraX = 0;

        this.terrain.populateVisibleTerrain(cameraX, Game.DEFAULT_WIDTH);

        float startY = Game.DEFAULT_HEIGHT * Lander.INITIAL_FLYOVER_START_Y_RATIO;
        float startX = -Lander.DISPLAY_LANDER_WIDTH;

        if (this.lander == null) {
            this.lander = new Lander(startX, startY);
        } else {
            lander.reset(startX, startY);
        }
        playerControlTakenSinceEnter = false;
        currentZoom = ZOOM_LEVEL_OUT;
        targetZoom = ZOOM_LEVEL_OUT;
    }

    @Override
    public void update(double deltaTime) {
        handleInput();

        if (lander != null) {
            lander.update(deltaTime, terrain);

            if (terrain != null && terrain.getTerrainSurfacePoints() != null && !terrain.getTerrainSurfacePoints().isEmpty() && (lander.getCurrentState() == Lander.State.PLAYER_CONTROL || lander.getCurrentState() == Lander.State.INITIAL_FLYOVER)) {

                double landerEffectiveScreenX = lander.getX() - cameraX;
                double landerFeetWorldY = lander.getY() + (Lander.DISPLAY_LANDER_HEIGHT / 2.0);

                float terrainYAtLanderX = -1;
                List<Point> surfacePoints = terrain.getTerrainSurfacePoints();

                if (surfacePoints.size() > 1) {
                    Point p1 = null, p2 = null;
                    for (int i = 0; i < surfacePoints.size() - 1; i++) {
                        if (surfacePoints.get(i).x <= landerEffectiveScreenX && surfacePoints.get(i + 1).x >= landerEffectiveScreenX) {
                            p1 = surfacePoints.get(i);
                            p2 = surfacePoints.get(i + 1);
                            break;
                        }
                    }

                    if (p1 == null && landerEffectiveScreenX < surfacePoints.get(0).x) {
                        p1 = new Point((int) (landerEffectiveScreenX - 10), surfacePoints.get(0).y);
                        p2 = surfacePoints.get(0);
                    } else if (p1 == null && landerEffectiveScreenX > surfacePoints.get(surfacePoints.size() - 1).x) {
                        p1 = surfacePoints.get(surfacePoints.size() - 1);
                        p2 = new Point((int) (landerEffectiveScreenX + 10), surfacePoints.get(surfacePoints.size() - 1).y);
                    }

                    if (p1 != null && p2 != null) {
                        if (p2.x == p1.x) {
                            terrainYAtLanderX = Math.min(p1.y, p2.y);
                        } else {
                            float t = (float) (landerEffectiveScreenX - p1.x) / (float) (p2.x - p1.x);
                            terrainYAtLanderX = p1.y + t * (p2.y - p1.y);
                        }

                        float distanceToSurface = terrainYAtLanderX - (float) landerFeetWorldY;

                        if (distanceToSurface < DISTANCE_TO_ZOOM_IN_THRESHOLD && distanceToSurface >= -Lander.DISPLAY_LANDER_HEIGHT) {
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
                targetZoom = ZOOM_LEVEL_OUT;
            }


            currentZoom += (targetZoom - currentZoom) * ZOOM_INTERPOLATION_SPEED * deltaTime;
            currentZoom = Math.max(ZOOM_LEVEL_OUT, Math.min(currentZoom, ZOOM_LEVEL_IN * 1.05f));
        }
    }

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
        Graphics2D g2d = g;
        AffineTransform originalTransform = g2d.getTransform();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(10, 10, 20));
        g2d.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        if (lander != null) {
            double focusX_world = lander.getX();
            double focusY_world = lander.getY();

            g2d.translate(Game.DEFAULT_WIDTH / 2.0, Game.DEFAULT_HEIGHT / 2.0);
            g2d.scale(currentZoom, currentZoom);
            g2d.translate(-focusX_world, -focusY_world);

            g2d.setTransform(originalTransform);
            g2d.setColor(new Color(10, 10, 20));
            g2d.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

            double landerScreenX = lander.getX() - cameraX;
            double landerScreenY = lander.getY();

            g2d.translate(landerScreenX, landerScreenY);
            g2d.scale(currentZoom, currentZoom);
            g2d.translate(-landerScreenX, -landerScreenY);
        }

        if (terrain != null) {
            terrain.render(g2d);
        }

        if (lander != null) {
            g2d.setTransform(originalTransform);
            g2d.setColor(new Color(10, 10, 20));
            g2d.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

            g2d.translate(-cameraX, 0);

            if (lander != null && currentZoom != 1.0f) {
                g2d.translate(lander.getX(), lander.getY());
                g2d.scale(currentZoom, currentZoom);
                g2d.translate(-lander.getX(), -lander.getY());
            }

            // Kreslení světa
            if (terrain != null) {
                AffineTransform transformBeforeZoom = g2d.getTransform();
                if (lander != null && currentZoom != 1.0f) {
                    g2d.translate(lander.getX() - cameraX, lander.getY());
                    g2d.scale(currentZoom, currentZoom);
                    g2d.translate(-(lander.getX() - cameraX), -lander.getY());
                }
                terrain.render(g2d);
                g2d.setTransform(transformBeforeZoom);
            }


            if (lander != null) {
                lander.render(g2d);
            }
        }

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
            g2d.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g2d.setColor(new Color(200, 200, 200, 180));
            int yPos = Game.DEFAULT_HEIGHT - 45;
            g2d.drawString("Ovládání:", 10, yPos);
            yPos += 12;
            g2d.drawString(" Tah: " + kb.getKeyTextForAction(GameAction.THRUST), 10, yPos);
            yPos += 12;
            g2d.drawString(" Vlevo: " + kb.getKeyTextForAction(GameAction.ROTATE_LEFT), 10, yPos);
            yPos += 12;
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