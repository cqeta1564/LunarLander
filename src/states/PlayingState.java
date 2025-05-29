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

    private static final float SCROLL_THRESHOLD_RIGHT_FACTOR = 0.7f;
    private static final float LANDER_SCREEN_TARGET_X_AFTER_SCROLL = 0.6f;
    private static final float CAMERA_SCROLL_LERP_SPEED = 4.0f;
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
    private float targetCameraX = 0;
    private boolean playerControlTakenSinceEnter = false;
    private float currentZoom = 1.0f;
    private float targetZoom = 1.0f;
    private boolean attemptConcluded = false;
    private String landingMessageDisplay = "";

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
        if (this.terrain == null) {
            this.terrain = new Terrain(Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);
        }
        this.cameraX = 0;
        this.targetCameraX = 0;

        float startY = Game.DEFAULT_HEIGHT * Lander.INITIAL_FLYOVER_START_Y_RATIO;
        float startX = this.cameraX - Lander.DISPLAY_LANDER_WIDTH;

        if (this.lander == null) {
            this.lander = new Lander(startX, startY);
        } else {
            lander.reset(startX, startY);
        }
        playerControlTakenSinceEnter = false;
        currentZoom = ZOOM_LEVEL_OUT;
        targetZoom = ZOOM_LEVEL_OUT;
        attemptConcluded = false;
        landingMessageDisplay = "";

        if (terrain != null) {
            terrain.populateVisibleTerrain(this.cameraX, Game.DEFAULT_WIDTH);
        }
    }

    @Override
    public void update(double deltaTime) {
        float previousCameraX = this.cameraX;

        if (!attemptConcluded) {
            handleInput();
            if (lander != null) {
                lander.update(deltaTime, terrain);
                checkLanderStatusAndScore();

                if (lander.getCurrentState() == Lander.State.PLAYER_CONTROL || lander.getCurrentState() == Lander.State.INITIAL_FLYOVER) {
                    float landerScreenX = (float) lander.getX() - this.cameraX;
                    float scrollTriggerX = Game.DEFAULT_WIDTH * SCROLL_THRESHOLD_RIGHT_FACTOR;

                    if (landerScreenX > scrollTriggerX) {
                        this.targetCameraX = (float) lander.getX() - (Game.DEFAULT_WIDTH * LANDER_SCREEN_TARGET_X_AFTER_SCROLL);
                    }
                    if (this.targetCameraX < 0) {
                        this.targetCameraX = 0;
                    }
                }
            }
        } else {
            if (inputHandler.isEscJustPressed()) {
                stateManager.setState(StateManager.StateType.MENU);
            } else if (lander != null && (lander.getCurrentState() == Lander.State.LANDED_GENTLE || lander.getCurrentState() == Lander.State.LANDED_HARD) && lander.getFuel() > 0 && inputHandler.isEnterJustPressed()) {
                onEnter();
            }
        }

        if (Math.abs(targetCameraX - cameraX) > 0.5f) {
            cameraX += (targetCameraX - cameraX) * CAMERA_SCROLL_LERP_SPEED * deltaTime;
        } else if (targetCameraX != cameraX) {
            cameraX = targetCameraX;
        }

        if (Math.abs(cameraX - previousCameraX) > 0.1f && terrain != null) {
            terrain.populateVisibleTerrain(cameraX, Game.DEFAULT_WIDTH);
        }

        if (lander != null && terrain != null && terrain.getTerrainSurfacePoints() != null && !terrain.getTerrainSurfacePoints().isEmpty() && (lander.getCurrentState() == Lander.State.PLAYER_CONTROL || lander.getCurrentState() == Lander.State.INITIAL_FLYOVER)) {
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
                if (p1 == null && !surfacePoints.isEmpty() && landerEffectiveScreenX < surfacePoints.get(0).x) {
                    p1 = new Point((int) (landerEffectiveScreenX - 10), surfacePoints.get(0).y);
                    p2 = surfacePoints.get(0);
                } else if (p1 == null && !surfacePoints.isEmpty() && landerEffectiveScreenX > surfacePoints.get(surfacePoints.size() - 1).x) {
                    p1 = surfacePoints.get(surfacePoints.size() - 1);
                    p2 = new Point((int) (landerEffectiveScreenX + 10), surfacePoints.get(surfacePoints.size() - 1).y);
                }

                if (p1 != null && p2 != null) {
                    if (p2.x == p1.x) {
                        terrainYAtLanderX = Math.min(p1.y, p2.y);
                    } else {
                        float t = (p2.x - p1.x == 0) ? 0 : (float) (landerEffectiveScreenX - p1.x) / (float) (p2.x - p1.x);
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
        } else if (lander != null && (lander.getCurrentState() == Lander.State.LANDED_GENTLE || lander.getCurrentState() == Lander.State.LANDED_HARD || lander.getCurrentState() == Lander.State.CRASHED)) {
            targetZoom = ZOOM_LEVEL_OUT;
        }
        currentZoom += (targetZoom - currentZoom) * ZOOM_INTERPOLATION_SPEED * deltaTime;
        currentZoom = Math.max(ZOOM_LEVEL_OUT, Math.min(currentZoom, ZOOM_LEVEL_IN * 1.05f));
    }

    private void checkLanderStatusAndScore() {
        if (lander == null || attemptConcluded) return;
        Lander.State lState = lander.getCurrentState();
        if (lState == Lander.State.LANDED_GENTLE || lState == Lander.State.LANDED_HARD || lState == Lander.State.CRASHED) {
            attemptConcluded = true;
            switch (lState) {
                case LANDED_GENTLE:
                    landingMessageDisplay = "Perfektní přistání!";
                    break;
                case LANDED_HARD:
                    landingMessageDisplay = "Tvrdé přistání!";
                    break;
                case CRASHED:
                    landingMessageDisplay = "Havárie!";
                    break;
                default:
                    landingMessageDisplay = "Pokus ukončen.";
                    break;
            }
            if (lander.getFuel() <= 0 && lState != Lander.State.CRASHED) {
                landingMessageDisplay += " .. ale došlo palivo!";
            }
        }
    }

    @Override
    public void handleInput() {
        if (attemptConcluded) {
            return;
        }
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
        if (thrusting) actionKeyPressed = true;
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

        AffineTransform worldTransform = new AffineTransform();
        if (lander != null && currentZoom != 1.0f) {
            double landerScreenX = lander.getX() - cameraX;
            double landerScreenY = lander.getY();

            worldTransform.translate(landerScreenX, landerScreenY);
            worldTransform.scale(currentZoom, currentZoom);
            worldTransform.translate(-landerScreenX, -landerScreenY);
        }

        worldTransform.translate(-cameraX, 0);

        AffineTransform g2dOriginalForWorld = g2d.getTransform();
        g2d.transform(worldTransform);

        if (terrain != null) {
            Graphics2D terrainGraphics = (Graphics2D) g.create();
            terrainGraphics.setTransform(originalTransform);

            if (lander != null && currentZoom != 1.0f) {
                double landerScreenX = lander.getX() - cameraX;
                double landerScreenY = lander.getY();
                AffineTransform terrainZoom = new AffineTransform();
                terrainZoom.translate(landerScreenX, landerScreenY);
                terrainZoom.scale(currentZoom, currentZoom);
                terrainZoom.translate(-landerScreenX, -landerScreenY);
                terrainGraphics.transform(terrainZoom);
            }
            terrain.render(terrainGraphics);
            terrainGraphics.dispose();
        }

        if (lander != null) {
            lander.render(g2d);
        }

        g2d.setTransform(originalTransform);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Monospaced", Font.BOLD, 16));
        if (lander != null) {
            g2d.drawString(String.format("Lander X:%.0f Y:%.0f V_X:%.1f V_Y:%.1f Angle:%.0f", lander.getX(), lander.getY(), lander.getVx(), lander.getVy(), Math.toDegrees(lander.getAngle())), 10, 20);
            g2d.drawString(String.format("Zoom:%.2fx Palivo:%.0f Stav: %s CamX:%.0f", currentZoom, lander.getFuel(), lander.getCurrentState(), cameraX), 10, 40);
        }
        g2d.drawString("SKÓRE: " + (attemptConcluded && lander != null ? lander.getCalculatedScore() : "---"), Game.DEFAULT_WIDTH - 150, 20);

        if (attemptConcluded) {
            g2d.setFont(new Font("Arial", Font.BOLD, 40));
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(landingMessageDisplay);
            g2d.setColor(Color.YELLOW);
            g2d.drawString(landingMessageDisplay, (Game.DEFAULT_WIDTH - textWidth) / 2, Game.DEFAULT_HEIGHT / 2 - 60);

            if (lander != null && lander.getCurrentState() != Lander.State.CRASHED) {
                String scoreText = "Body: " + lander.getCalculatedScore();
                g2d.setFont(new Font("Arial", Font.BOLD, 32));
                fm = g2d.getFontMetrics();
                textWidth = fm.stringWidth(scoreText);
                g2d.setColor(Color.GREEN);
                g2d.drawString(scoreText, (Game.DEFAULT_WIDTH - textWidth) / 2, Game.DEFAULT_HEIGHT / 2 - 10);
            }

            g2d.setFont(new Font("Arial", Font.PLAIN, 20));
            fm = g2d.getFontMetrics();
            String continueMsg;
            if (lander != null && (lander.getCurrentState() == Lander.State.LANDED_GENTLE || lander.getCurrentState() == Lander.State.LANDED_HARD) && lander.getFuel() > 0) {
                continueMsg = "Stiskni ENTER pro pokračování | ESC pro menu";
            } else {
                continueMsg = "Stiskni ESC pro návrat do menu";
            }
            textWidth = fm.stringWidth(continueMsg);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString(continueMsg, (Game.DEFAULT_WIDTH - textWidth) / 2, Game.DEFAULT_HEIGHT / 2 + 40);
        }

        KeyBindings kb = inputHandler.getKeyBindings();
        if (kb != null && !attemptConcluded) {
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