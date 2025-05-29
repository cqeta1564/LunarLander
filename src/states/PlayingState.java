package states;

import audio.AudioManager;
import core.Game;
import entities.Lander;
import entities.Terrain;
import input.GameAction;
import input.InputHandler;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
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
    private static final int HUD_MARGIN_X = 15;
    private static final int HUD_MARGIN_Y = 20;
    private static final int HUD_LINE_SPACING = 18;
    private static final Font HUD_FONT = new Font("Monospaced", Font.BOLD, 14);
    private static final Font GAME_OVER_FONT_BIG = new Font("Arial", Font.BOLD, 48);
    private static final Font GAME_OVER_FONT_MEDIUM = new Font("Arial", Font.BOLD, 32);
    private static final Font GAME_OVER_FONT_SMALL = new Font("Arial", Font.PLAIN, 20);
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
    private double totalTimePlayedInStateSeconds = 0;
    private float currentHeightAboveTerrain = 0;
    private double fuelForNextAttempt = Lander.MAX_FUEL;
    private int cumulativeSessionScore = 0;

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
        if (terrain != null) {
            this.terrain.populateVisibleTerrain(cameraX, Game.DEFAULT_WIDTH);
        }

        float startY = Game.DEFAULT_HEIGHT * Lander.INITIAL_FLYOVER_START_Y_RATIO;
        float startX = this.cameraX - Lander.DISPLAY_LANDER_WIDTH;

        double fuelValueForThisNewAttempt = fuelForNextAttempt;

        if (this.lander == null) {
            this.lander = new Lander(startX, startY);
        } else {
            lander.reset(startX, startY);
        }
        this.lander.setFuel(fuelValueForThisNewAttempt);

        if (fuelValueForThisNewAttempt == Lander.MAX_FUEL) {
            cumulativeSessionScore = 0;
        }
        fuelForNextAttempt = Lander.MAX_FUEL;

        playerControlTakenSinceEnter = false;
        currentZoom = ZOOM_LEVEL_OUT;
        targetZoom = ZOOM_LEVEL_OUT;
        attemptConcluded = false;
        landingMessageDisplay = "";
        totalTimePlayedInStateSeconds = 0;
        currentHeightAboveTerrain = 0;
    }

    @Override
    public void update(double deltaTime) {
        float previousCameraX = this.cameraX;

        if (!attemptConcluded) {
            handleInput();
            totalTimePlayedInStateSeconds += deltaTime;
            if (lander != null) {
                lander.update(deltaTime, terrain);
                checkLanderStatusAndScore();

                if (terrain != null && (lander.getCurrentState() == Lander.State.PLAYER_CONTROL || lander.getCurrentState() == Lander.State.INITIAL_FLYOVER)) {
                    float landerScreenX = (float) lander.getX() - cameraX;
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
            } else if (inputHandler.isEnterJustPressed()) {
                if (lander != null && (lander.getCurrentState() == Lander.State.LANDED_GENTLE || lander.getCurrentState() == Lander.State.LANDED_HARD || lander.getCurrentState() == Lander.State.CRASHED) && lander.getFuel() > 0) {

                    fuelForNextAttempt = lander.getFuel();
                    onEnter();
                }
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
            Point2D.Double landerFootPointLocal = new Point2D.Double(0, Lander.DISPLAY_LANDER_HEIGHT / 2.0);
            Point2D.Double landerFootPointWorld = lander.getPointInWorldSpace(landerFootPointLocal);
            double landerFeetScreenY = landerFootPointWorld.y;

            float terrainYAtLanderX = calculateTerrainYAtScreenX((float) landerEffectiveScreenX, terrain.getTerrainSurfacePoints());

            if (terrainYAtLanderX != -1) {
                this.currentHeightAboveTerrain = terrainYAtLanderX - (float) landerFeetScreenY;
                if (currentHeightAboveTerrain < DISTANCE_TO_ZOOM_IN_THRESHOLD && currentHeightAboveTerrain >= -Lander.DISPLAY_LANDER_HEIGHT) {
                    targetZoom = ZOOM_LEVEL_IN;
                } else if (currentHeightAboveTerrain > DISTANCE_TO_ZOOM_OUT_THRESHOLD) {
                    targetZoom = ZOOM_LEVEL_OUT;
                }
            } else {
                targetZoom = ZOOM_LEVEL_OUT;
                this.currentHeightAboveTerrain = Float.POSITIVE_INFINITY;
            }
        } else if (lander != null && (lander.getCurrentState() == Lander.State.LANDED_GENTLE || lander.getCurrentState() == Lander.State.LANDED_HARD || lander.getCurrentState() == Lander.State.CRASHED)) {
            targetZoom = ZOOM_LEVEL_OUT;
        }
        currentZoom += (targetZoom - currentZoom) * ZOOM_INTERPOLATION_SPEED * deltaTime;
        currentZoom = Math.max(ZOOM_LEVEL_OUT, Math.min(currentZoom, ZOOM_LEVEL_IN * 1.05f));
    }

    private float calculateTerrainYAtScreenX(float screenX, List<Point> surfacePoints) {
        if (surfacePoints == null || surfacePoints.size() < 2) {
            return -1;
        }
        Point p1 = null, p2 = null;
        for (int i = 0; i < surfacePoints.size() - 1; i++) {
            if (surfacePoints.get(i).x <= screenX && surfacePoints.get(i + 1).x >= screenX) {
                p1 = surfacePoints.get(i);
                p2 = surfacePoints.get(i + 1);
                break;
            }
        }
        if (p1 == null && !surfacePoints.isEmpty()) {
            if (screenX < surfacePoints.get(0).x) return surfacePoints.get(0).y;
            if (screenX > surfacePoints.get(surfacePoints.size() - 1).x)
                return surfacePoints.get(surfacePoints.size() - 1).y;
            return -1;
        }

        if (p1 != null && p2 != null) {
            if (p2.x == p1.x) {
                return Math.min(p1.y, p2.y);
            } else {
                float t = (screenX - p1.x) / (float) (p2.x - p1.x);
                return p1.y + t * (p2.y - p1.y);
            }
        }
        return -1;
    }

    private void checkLanderStatusAndScore() {
        if (lander == null) return;
        if (attemptConcluded) return;

        Lander.State lState = lander.getCurrentState();

        if (lState == Lander.State.LANDED_GENTLE || lState == Lander.State.LANDED_HARD) {
            cumulativeSessionScore += lander.getCalculatedScore();
            attemptConcluded = true;
            landingMessageDisplay = (lState == Lander.State.LANDED_GENTLE) ? "Perfektní přistání!" : "Tvrdé přistání!";
            if (lander.getFuel() <= 0) {
                landingMessageDisplay += " .. ale došlo palivo!";
            }
        } else if (lState == Lander.State.CRASHED) {
            attemptConcluded = true;
            landingMessageDisplay = "Havárie!";
            if (lander.getFuel() <= 0) {
                landingMessageDisplay += " .. a palivo také došlo!";
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        Graphics2D g2d = g;
        AffineTransform originalScreenTransform = g2d.getTransform();

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(10, 10, 20));
        g2d.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        Graphics2D worldG = (Graphics2D) g2d.create();

        if (lander != null && currentZoom != 1.0f) {
            double landerScreenXForZoomPivot = lander.getX() - cameraX;
            double landerScreenYForZoomPivot = lander.getY();

            AffineTransform zoomTransform = new AffineTransform();
            zoomTransform.translate(landerScreenXForZoomPivot, landerScreenYForZoomPivot);
            zoomTransform.scale(currentZoom, currentZoom);
            zoomTransform.translate(-landerScreenXForZoomPivot, -landerScreenYForZoomPivot);
            worldG.transform(zoomTransform);
        }

        worldG.translate(-cameraX, 0);

        if (terrain != null) {
            Graphics2D terrainSpecificG = (Graphics2D) g2d.create();
            if (lander != null && currentZoom != 1.0f) {
                double landerScreenX = lander.getX() - cameraX;
                double landerScreenY = lander.getY();
                terrainSpecificG.translate(landerScreenX, landerScreenY);
                terrainSpecificG.scale(currentZoom, currentZoom);
                terrainSpecificG.translate(-landerScreenX, -landerScreenY);
            }
            terrain.render(terrainSpecificG);
            terrainSpecificG.dispose();
        }

        if (lander != null) {
            Graphics2D landerSpecificG = (Graphics2D) g2d.create();

            if (currentZoom != 1.0f) {
                double landerScreenX = lander.getX() - cameraX;
                double landerScreenY = lander.getY();
                landerSpecificG.translate(landerScreenX, landerScreenY);
                landerSpecificG.scale(currentZoom, currentZoom);
                landerSpecificG.translate(-landerScreenX, -landerScreenY);
            }
            landerSpecificG.translate(-cameraX, 0);

            lander.render(landerSpecificG);
            landerSpecificG.dispose();
        }
        worldG.dispose();

        g2d.setTransform(originalScreenTransform);

        g2d.setFont(HUD_FONT);
        g2d.setColor(Color.WHITE);

        int hudLX = HUD_MARGIN_X;
        int hudLY = HUD_MARGIN_Y + HUD_FONT.getSize();


        g2d.drawString("SKÓRE: " + cumulativeSessionScore, hudLX, hudLY);
        hudLY += HUD_LINE_SPACING;

        long timeValSeconds = (long) totalTimePlayedInStateSeconds;
        String timeFormatted = String.format("%02d:%02d", timeValSeconds / 60, timeValSeconds % 60);
        g2d.drawString("ČAS: " + timeFormatted, hudLX, hudLY);
        hudLY += HUD_LINE_SPACING;

        if (lander != null) {
            g2d.drawString(String.format("PALIVO: %.0f", lander.getFuel()), hudLX, hudLY);
        } else {
            g2d.drawString("PALIVO: ---", hudLX, hudLY);
        }

        int hudRX = Game.DEFAULT_WIDTH - HUD_MARGIN_X;
        hudLY = HUD_MARGIN_Y + HUD_FONT.getSize();

        String heightString = "VÝŠKA: N/A";
        if (lander != null && (lander.getCurrentState() == Lander.State.PLAYER_CONTROL || lander.getCurrentState() == Lander.State.INITIAL_FLYOVER) && currentHeightAboveTerrain != Float.POSITIVE_INFINITY) {
            heightString = String.format("VÝŠKA: %.0f", currentHeightAboveTerrain);
        }
        int textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth(heightString);
        g2d.drawString(heightString, hudRX - textWidth, hudLY);
        hudLY += HUD_LINE_SPACING;

        if (lander != null) {
            String horSpeedText = String.format("HOR.RYCHL: %.1f", lander.getVx());
            textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth(horSpeedText);
            g2d.drawString(horSpeedText, hudRX - textWidth, hudLY);
            hudLY += HUD_LINE_SPACING;

            String verSpeedText = String.format("VER.RYCHL: %.1f", lander.getVy());
            textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth(verSpeedText);
            g2d.drawString(verSpeedText, hudRX - textWidth, hudLY);
        } else {
            String na = "N/A";
            textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth("HOR.RYCHL: " + na);
            g2d.drawString("HOR.RYCHL: " + na, hudRX - textWidth, hudLY);
            hudLY += HUD_LINE_SPACING;
            textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth("VER.RYCHL: " + na);
            g2d.drawString("VER.RYCHL: " + na, hudRX - textWidth, hudLY);
        }

        if (attemptConcluded) {
            FontMetrics fm;
            g2d.setColor(Color.YELLOW);
            g2d.setFont(GAME_OVER_FONT_BIG);
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(landingMessageDisplay);
            g2d.drawString(landingMessageDisplay, (Game.DEFAULT_WIDTH - textWidth) / 2, Game.DEFAULT_HEIGHT / 2 - 60);

            if (lander != null && lander.getCurrentState() != Lander.State.CRASHED) {
                String scoreText = "Body: " + lander.getCalculatedScore();
                g2d.setFont(GAME_OVER_FONT_MEDIUM);
                fm = g2d.getFontMetrics();
                textWidth = fm.stringWidth(scoreText);
                g2d.setColor(Color.GREEN);
                g2d.drawString(scoreText, (Game.DEFAULT_WIDTH - textWidth) / 2, Game.DEFAULT_HEIGHT / 2 - 10);
            }

            g2d.setFont(GAME_OVER_FONT_SMALL);
            fm = g2d.getFontMetrics();
            String continueMsg;
            boolean canContinueWithEnter = lander != null && (lander.getCurrentState() == Lander.State.LANDED_GENTLE || lander.getCurrentState() == Lander.State.LANDED_HARD || lander.getCurrentState() == Lander.State.CRASHED) && lander.getFuel() > 0;

            if (canContinueWithEnter) {
                continueMsg = "Stiskni ENTER pro pokračování | ESC pro menu";
            } else {
                continueMsg = "Stiskni ESC pro návrat do menu";
            }
            textWidth = fm.stringWidth(continueMsg);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString(continueMsg, (Game.DEFAULT_WIDTH - textWidth) / 2, Game.DEFAULT_HEIGHT / 2 + 40);
        }
    }

    @Override
    public void handleInput() {
        if (attemptConcluded) return;
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
    public void onExit() {
        System.out.println("Opuštění PlayingState.");
        AudioManager.getInstance().stopSound(AudioManager.SoundEffect.ENGINE);
        if (lander != null) {
            lander.setPlayerRequestsThrust(false);
            lander.setRotation(0);
        }
    }
}