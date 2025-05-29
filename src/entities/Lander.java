package entities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

public class Lander {
    public static final double INITIAL_FLYOVER_SPEED_X_PIXELS_S = 60;
    public static final float INITIAL_FLYOVER_START_Y_RATIO = 0.15f;

    private static final double PIXELS_PER_METER = 20.0;
    private static final double LUNAR_GRAVITY_MS2 = 1.625;
    private static final double LUNAR_GRAVITY_PIXELS_S2 = LUNAR_GRAVITY_MS2 * PIXELS_PER_METER;

    private static final double MAIN_THRUSTER_ACCELERATION_MS2 = 3.0;
    private static final double MAIN_THRUSTER_MAX_ACCELERATION_PIXELS_S2 = MAIN_THRUSTER_ACCELERATION_MS2 * PIXELS_PER_METER;

    private static final double ROTATION_DEGREES_PER_SECOND = 90.0;
    private static final double ROTATION_RADIANS_PER_SECOND = Math.toRadians(ROTATION_DEGREES_PER_SECOND);

    private static final int ORIGINAL_LANDER_IMAGE_WIDTH = 36;
    private static final int ORIGINAL_LANDER_IMAGE_HEIGHT = 31;

    private static final double IMAGE_SCALE_DIVISOR = 1.2;
    public static final int DISPLAY_LANDER_WIDTH = (int) (ORIGINAL_LANDER_IMAGE_WIDTH / IMAGE_SCALE_DIVISOR);
    public static final int DISPLAY_LANDER_HEIGHT = (int) (ORIGINAL_LANDER_IMAGE_HEIGHT / IMAGE_SCALE_DIVISOR);
    private static final double LANDING_GEAR_Y_OFFSET = DISPLAY_LANDER_HEIGHT / 2.0;
    private static final double MAX_LANDING_SPEED_Y_PIXELS_S = 40;
    private static final double MAX_LANDING_SPEED_X_PIXELS_S = 30;
    private static final double MAX_LANDING_ANGLE_DEGREES = 8.0;
    private static final double MAX_LANDING_ANGLE_RADIANS = Math.toRadians(MAX_LANDING_ANGLE_DEGREES);
    private static final double LANDING_GEAR_WIDTH_FACTOR = 0.7;

    private static final double THRUSTER_RAMP_UP_PER_SECOND = 0.75;
    private static final double THRUSTER_RAMP_DOWN_PER_SECOND = 1.5;

    private static final int FLAME_MIN_LENGTH = 5;
    private static final int FLAME_MAX_LENGTH = 20;
    private static final double FLAME_BASE_WIDTH_RATIO = 0.6;
    private final Point2D.Double[] collisionPointsLocal;
    private State currentState;

    private double x, y;
    private double vx, vy;
    private double angle;
    private boolean playerRequestsThrust = false;
    private double currentThrustOutput = 0.0;
    private int rotationDirection = 0;

    private BufferedImage landerImage;

    public Lander(float startX, float startY) {
        try {
            landerImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/pictures/ship.png")));
        } catch (IOException | NullPointerException e) {
            System.err.println("Chyba při načítání obrázku landeru: " + e.getMessage() + ". Použije se záložní tvar.");
            landerImage = null;
        }

        collisionPointsLocal = new Point2D.Double[]{new Point2D.Double(-DISPLAY_LANDER_WIDTH / 2.0 * LANDING_GEAR_WIDTH_FACTOR, LANDING_GEAR_Y_OFFSET), new Point2D.Double(DISPLAY_LANDER_WIDTH / 2.0 * LANDING_GEAR_WIDTH_FACTOR, LANDING_GEAR_Y_OFFSET)};
        reset(startX, startY);
    }

    public void reset(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.vx = INITIAL_FLYOVER_SPEED_X_PIXELS_S;
        this.vy = 0;
        this.angle = 0;
        this.currentState = State.INITIAL_FLYOVER;
        this.playerRequestsThrust = false;
        this.currentThrustOutput = 0.0;
        this.rotationDirection = 0;
    }

    public void playerHasTakenControl() {
        if (this.currentState == State.INITIAL_FLYOVER) {
            this.currentState = State.PLAYER_CONTROL;
        }
    }

    public void setPlayerRequestsThrust(boolean wantsThrust) {
        if (currentState == State.PLAYER_CONTROL || currentState == State.INITIAL_FLYOVER) {
            this.playerRequestsThrust = wantsThrust;
        } else {
            this.playerRequestsThrust = false;
        }
    }

    public void setRotation(int direction) {
        if (currentState == State.PLAYER_CONTROL) {
            this.rotationDirection = Integer.signum(direction);
        } else {
            this.rotationDirection = 0;
        }
    }

    public void update(double deltaTime, Terrain terrain) {
        if (currentState == State.LANDED || currentState == State.CRASHED) {
            vx = 0;
            vy = 0;
            currentThrustOutput = 0.0;
            playerRequestsThrust = false;
            rotationDirection = 0;
            return;
        }

        if (playerRequestsThrust) {
            currentThrustOutput += THRUSTER_RAMP_UP_PER_SECOND * deltaTime;
            if (currentThrustOutput > 1.0) currentThrustOutput = 1.0;
        } else {
            currentThrustOutput -= THRUSTER_RAMP_DOWN_PER_SECOND * deltaTime;
            if (currentThrustOutput < 0.0) currentThrustOutput = 0.0;
        }

        double ax = 0;
        double ay = 0;

        if (currentState == State.INITIAL_FLYOVER) {
            ay += LUNAR_GRAVITY_PIXELS_S2;
            if (terrain != null && x > terrain.getScreenWidth() + DISPLAY_LANDER_WIDTH * 2) {
            }
        } else if (currentState == State.PLAYER_CONTROL) {
            ay += LUNAR_GRAVITY_PIXELS_S2;
            if (currentThrustOutput > 0) {
                double actualThrustAcceleration = MAIN_THRUSTER_MAX_ACCELERATION_PIXELS_S2 * currentThrustOutput;
                ax += Math.sin(angle) * actualThrustAcceleration;
                ay -= Math.cos(angle) * actualThrustAcceleration;
            }
            if (rotationDirection != 0) {
                angle += rotationDirection * ROTATION_RADIANS_PER_SECOND * deltaTime;
                angle = (angle + 2 * Math.PI) % (2 * Math.PI);
            }
        }

        vx += ax * deltaTime;
        vy += ay * deltaTime;
        x += vx * deltaTime;
        y += vy * deltaTime;

        checkCollisions(terrain);
    }

    private Point2D.Double getPointInWorldSpace(Point2D.Double localPoint) {
        double worldX = x + (localPoint.x * Math.cos(angle) - localPoint.y * Math.sin(angle));
        double worldY = y + (localPoint.x * Math.sin(angle) + localPoint.y * Math.cos(angle));
        return new Point2D.Double(worldX, worldY);
    }

    private void checkCollisions(Terrain terrain) {
        if (terrain == null || currentState == State.LANDED || currentState == State.CRASHED) return;

        Polygon terrainPolygon = terrain.getTerrainPolygon();
        boolean collisionDetected = false;

        for (Point2D.Double localP : collisionPointsLocal) {
            Point2D.Double worldP = getPointInWorldSpace(localP);
            if (terrainPolygon.contains(worldP)) {
                collisionDetected = true;
                break;
            }
        }
        Point2D.Double tipWorld = getPointInWorldSpace(new Point2D.Double(0, -DISPLAY_LANDER_HEIGHT / 2.0));
        if (!collisionDetected && terrainPolygon.contains(tipWorld)) {
            collisionDetected = true;
        }

        if (collisionDetected) {
            boolean onSafePad = false;
            int currentMultiplier = 0;
            for (LandingPad pad : terrain.getLandingPads()) {
                Point2D.Double worldFootLeft = getPointInWorldSpace(collisionPointsLocal[0]);
                Point2D.Double worldFootRight = getPointInWorldSpace(collisionPointsLocal[1]);
                boolean leftFootOnPadX = worldFootLeft.x >= pad.getStartX() && worldFootLeft.x <= pad.getEndX();
                boolean rightFootOnPadX = worldFootRight.x >= pad.getStartX() && worldFootRight.x <= pad.getEndX();
                boolean yAlignment = Math.abs(worldFootLeft.y - pad.getY()) < 5 && Math.abs(worldFootRight.y - pad.getY()) < 5;

                if (leftFootOnPadX && rightFootOnPadX && yAlignment) {
                    boolean safeSpeedY = Math.abs(vy) <= MAX_LANDING_SPEED_Y_PIXELS_S;
                    boolean safeSpeedX = Math.abs(vx) <= MAX_LANDING_SPEED_X_PIXELS_S;
                    double angleDegrees = (Math.toDegrees(angle) % 360 + 360) % 360;
                    boolean safeAngle = (angleDegrees <= MAX_LANDING_ANGLE_DEGREES || angleDegrees >= (360 - MAX_LANDING_ANGLE_DEGREES));

                    if (safeSpeedY && safeSpeedX && safeAngle) {
                        onSafePad = true;
                        currentMultiplier = pad.getMultiplier();
                        this.y = pad.getY() - LANDING_GEAR_Y_OFFSET;
                        this.angle = 0;
                        break;
                    } else {
                        onSafePad = false;
                        break;
                    }
                }
            }
            if (onSafePad) currentState = State.LANDED;
            else currentState = State.CRASHED;

            vx = 0;
            vy = 0;
            playerRequestsThrust = false;
            currentThrustOutput = 0.0;
            rotationDirection = 0;
        }
    }

    public void render(Graphics2D g) {
        AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        g.rotate(angle);

        if (landerImage != null) {
            int imgX = -DISPLAY_LANDER_WIDTH / 2;
            int imgY = -DISPLAY_LANDER_HEIGHT / 2;
            g.drawImage(landerImage, imgX, imgY, DISPLAY_LANDER_WIDTH, DISPLAY_LANDER_HEIGHT, null);
        } else {
            Color fallbackColor;
            if (currentState == State.CRASHED) fallbackColor = Color.RED;
            else if (currentState == State.LANDED) fallbackColor = Color.GREEN;
            else fallbackColor = Color.CYAN;
            g.setColor(fallbackColor);
            Polygon fallbackShape = new Polygon();
            fallbackShape.addPoint(0, -DISPLAY_LANDER_HEIGHT / 2);
            fallbackShape.addPoint(-DISPLAY_LANDER_WIDTH / 2, DISPLAY_LANDER_HEIGHT / 2);
            fallbackShape.addPoint(DISPLAY_LANDER_WIDTH / 2, DISPLAY_LANDER_HEIGHT / 2);
            g.fillPolygon(fallbackShape);
            g.setColor(Color.WHITE);
            g.drawPolygon(fallbackShape);
        }

        if (currentThrustOutput > 0.05 && (currentState == State.PLAYER_CONTROL || currentState == State.INITIAL_FLYOVER)) {
            float flameBaseY = DISPLAY_LANDER_HEIGHT / 2.0f;
            float flameBaseHalfWidth = (DISPLAY_LANDER_WIDTH * (float) FLAME_BASE_WIDTH_RATIO) / 2.0f;
            float flameTipLength = (float) (FLAME_MIN_LENGTH + (FLAME_MAX_LENGTH - FLAME_MIN_LENGTH) * currentThrustOutput);
            flameTipLength += (Math.random() * 5.0f - 2.5f) * currentThrustOutput;
            flameTipLength = Math.max(0, flameTipLength);

            Polygon dynamicFlame = new Polygon();
            dynamicFlame.addPoint((int) -flameBaseHalfWidth, (int) flameBaseY);
            dynamicFlame.addPoint((int) flameBaseHalfWidth, (int) flameBaseY);
            dynamicFlame.addPoint(0, (int) (flameBaseY + flameTipLength));

            g.setColor(new Color(255, 255, 255, 255));
            g.drawPolygon(dynamicFlame);
        }
        g.setTransform(oldTransform);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public State getCurrentState() {
        return currentState;
    } // Nyní je State public

    public enum State {
        INITIAL_FLYOVER, PLAYER_CONTROL, LANDED, CRASHED
    }
}