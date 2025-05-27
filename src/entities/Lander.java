package entities;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

public class Lander {
    public static final double INITIAL_FLYOVER_SPEED_X_PIXELS_S = 60;
    public static final float INITIAL_FLYOVER_START_Y_RATIO = 0.15f;
    private static final double PIXELS_PER_METER = 20.0;
    private static final double LUNAR_GRAVITY_MS2 = 1.625;
    private static final double LUNAR_GRAVITY_PIXELS_S2 = LUNAR_GRAVITY_MS2 * PIXELS_PER_METER;
    private static final double MAIN_THRUSTER_ACCELERATION_MS2 = 3.0;
    private static final double MAIN_THRUSTER_ACCELERATION_PIXELS_S2 = MAIN_THRUSTER_ACCELERATION_MS2 * PIXELS_PER_METER;
    private static final double ROTATION_DEGREES_PER_SECOND = 90.0;
    private static final double ROTATION_RADIANS_PER_SECOND = Math.toRadians(ROTATION_DEGREES_PER_SECOND);
    private static final int LANDER_WIDTH = 20;
    private static final int LANDER_HEIGHT = 22;
    private static final double MAX_LANDING_SPEED_Y_PIXELS_S = 40;
    private static final double MAX_LANDING_SPEED_X_PIXELS_S = 30;
    private static final double MAX_LANDING_ANGLE_DEGREES = 8.0;
    private static final double MAX_LANDING_ANGLE_RADIANS = Math.toRadians(MAX_LANDING_ANGLE_DEGREES);
    private static final double LANDING_GEAR_Y_OFFSET = LANDER_HEIGHT / 2.0;
    private static final double LANDING_GEAR_WIDTH_FACTOR = 0.9;
    private State currentState;
    private double x, y;
    private double vx, vy;
    private double angle;
    private boolean thrusterActive = false;
    private int rotationDirection = 0;
    private final Polygon landerShape;
    private final Polygon flameShape;
    private final Point2D.Double[] collisionPointsLocal;
    public Lander(float startX, float startY) {
        this.landerShape = new Polygon();
        landerShape.addPoint(0, -LANDER_HEIGHT / 2);
        landerShape.addPoint(-LANDER_WIDTH / 2, LANDER_HEIGHT / 2);
        landerShape.addPoint(LANDER_WIDTH / 2, LANDER_HEIGHT / 2);

        this.flameShape = new Polygon();
        flameShape.addPoint(0, LANDER_HEIGHT / 2 + 2);
        flameShape.addPoint(-LANDER_WIDTH / 3, LANDER_HEIGHT / 2 + 15);
        flameShape.addPoint(LANDER_WIDTH / 3, LANDER_HEIGHT / 2 + 15);

        collisionPointsLocal = new Point2D.Double[]{new Point2D.Double(-LANDER_WIDTH / 2.0 * LANDING_GEAR_WIDTH_FACTOR, LANDING_GEAR_Y_OFFSET), new Point2D.Double(LANDER_WIDTH / 2.0 * LANDING_GEAR_WIDTH_FACTOR, LANDING_GEAR_Y_OFFSET)};

        reset(startX, startY);
    }

    public void reset(float startX, float startY) {
        this.x = startX;
        this.y = startY;
        this.vx = INITIAL_FLYOVER_SPEED_X_PIXELS_S;
        this.vy = 0;
        this.angle = 0;
        this.currentState = State.INITIAL_FLYOVER;
        this.thrusterActive = false;
        this.rotationDirection = 0;
        System.out.println("Lander reset. State: " + currentState);
    }

    public void playerHasTakenControl() {
        if (this.currentState == State.INITIAL_FLYOVER) {
            this.currentState = State.PLAYER_CONTROL;
            System.out.println("Player has taken control. State: " + currentState);
        }
    }

    public void setThrusterActive(boolean active) {
        if (currentState == State.PLAYER_CONTROL || currentState == State.INITIAL_FLYOVER) {
            this.thrusterActive = active;
        }
    }

    public void setRotation(int direction) {
        if (currentState == State.PLAYER_CONTROL) {
            this.rotationDirection = Integer.signum(direction);
        }
    }

    public void update(double deltaTime, Terrain terrain) {
        if (currentState == State.LANDED || currentState == State.CRASHED) {
            vx = 0;
            vy = 0;
            thrusterActive = false;
            rotationDirection = 0;
            return;
        }

        double ax = 0;
        double ay = 0;

        if (currentState == State.INITIAL_FLYOVER) {
            ay += LUNAR_GRAVITY_PIXELS_S2;
            if (x > terrain.getScreenWidth() + LANDER_WIDTH * 2) {
            }
        } else if (currentState == State.PLAYER_CONTROL) {
            ay += LUNAR_GRAVITY_PIXELS_S2;
            if (thrusterActive) {
                ax += Math.sin(angle) * MAIN_THRUSTER_ACCELERATION_PIXELS_S2;
                ay -= Math.cos(angle) * MAIN_THRUSTER_ACCELERATION_PIXELS_S2;
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
        if (terrain == null) return;

        Polygon terrainPolygon = terrain.getTerrainPolygon();
        boolean collisionDetected = false;
        Point2D.Double collisionPoint = null;

        for (Point2D.Double localP : collisionPointsLocal) {
            Point2D.Double worldP = getPointInWorldSpace(localP);
            if (terrainPolygon.contains(worldP)) {
                collisionDetected = true;
                collisionPoint = worldP;
                break;
            }
        }
        Point2D.Double tipWorld = getPointInWorldSpace(new Point2D.Double(0, -LANDER_HEIGHT / 2.0));
        if (!collisionDetected && terrainPolygon.contains(tipWorld)) {
            collisionDetected = true;
            collisionPoint = tipWorld;
        }


        if (collisionDetected) {
            System.out.println("Kolize detekována na: " + collisionPoint + " Rychlost (vx,vy): (" + String.format("%.2f", vx) + "," + String.format("%.2f", vy) + ") Uhel: " + String.format("%.1f", Math.toDegrees(angle)));

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

                    System.out.println("Na plošce " + pad.getMultiplier() + "x. SafeSpeedY: " + safeSpeedY + " (" + String.format("%.2f", vy) + ")" + ", SafeSpeedX: " + safeSpeedX + " (" + String.format("%.2f", vx) + ")" + ", SafeAngle: " + safeAngle + " (" + String.format("%.1f", angleDegrees) + ")");

                    if (safeSpeedY && safeSpeedX && safeAngle) {
                        onSafePad = true;
                        currentMultiplier = pad.getMultiplier();
                        this.y = pad.getY() - LANDING_GEAR_Y_OFFSET;
                        break;
                    } else {
                        onSafePad = false;
                        break;
                    }
                }
            }

            if (onSafePad) {
                currentState = State.LANDED;
                System.out.println("LANDED! Násobek: " + currentMultiplier + "x");
            } else {
                currentState = State.CRASHED;
                System.out.println("CRASHED!");
            }
            vx = 0;
            vy = 0;
            thrusterActive = false;
            rotationDirection = 0;
        }
    }

    public void render(Graphics2D g) {
        AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        g.rotate(angle);

        if (currentState == State.CRASHED) g.setColor(Color.RED);
        else if (currentState == State.LANDED) g.setColor(Color.GREEN);
        else g.setColor(Color.CYAN);
        g.fillPolygon(landerShape);
        g.setColor(Color.WHITE);
        g.drawPolygon(landerShape);

        if (thrusterActive && (currentState == State.PLAYER_CONTROL || currentState == State.INITIAL_FLYOVER)) {
            g.setColor(Color.ORANGE);
            int flameLength = 15 + (int) (Math.random() * 5);
            flameShape.ypoints[1] = LANDER_HEIGHT / 2 + flameLength;
            flameShape.ypoints[2] = LANDER_HEIGHT / 2 + flameLength;
            g.fillPolygon(flameShape);
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
    }

    private enum State {
        INITIAL_FLYOVER, PLAYER_CONTROL, LANDED, CRASHED
    }
}