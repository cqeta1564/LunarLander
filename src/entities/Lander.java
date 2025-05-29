package entities;

import audio.AudioManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Lander {
    public static final double INITIAL_FLYOVER_SPEED_X_PIXELS_S = 60;
    public static final float INITIAL_FLYOVER_START_Y_RATIO = 0.15f;
    public static final double PIXELS_PER_METER = 20.0;
    public static final int ORIGINAL_LANDER_IMAGE_WIDTH = 36;
    public static final int ORIGINAL_LANDER_IMAGE_HEIGHT = 31;
    public static final double MAX_FUEL = 1000.0;
    private static final double LUNAR_GRAVITY_MS2 = 1.625;
    private static final double LUNAR_GRAVITY_PIXELS_S2 = LUNAR_GRAVITY_MS2 * PIXELS_PER_METER;
    private static final double MAIN_THRUSTER_ACCELERATION_MS2 = 3.0;
    private static final double MAIN_THRUSTER_MAX_ACCELERATION_PIXELS_S2 = MAIN_THRUSTER_ACCELERATION_MS2 * PIXELS_PER_METER;
    private static final double ROTATION_DEGREES_PER_SECOND = 90.0;
    private static final double ROTATION_RADIANS_PER_SECOND = Math.toRadians(ROTATION_DEGREES_PER_SECOND);
    private static final double IMAGE_SCALE_DIVISOR = 1.2;
    public static final int DISPLAY_LANDER_WIDTH = (int) (ORIGINAL_LANDER_IMAGE_WIDTH / IMAGE_SCALE_DIVISOR);
    public static final int DISPLAY_LANDER_HEIGHT = (int) (ORIGINAL_LANDER_IMAGE_HEIGHT / IMAGE_SCALE_DIVISOR);

    private static final double LANDING_GEAR_Y_OFFSET = DISPLAY_LANDER_HEIGHT / 2.0;
    private static final double LANDING_GEAR_WIDTH_FACTOR = 0.7;

    private static final double CRASH_LIMIT_SPEED_Y = 45.0;
    private static final double CRASH_LIMIT_SPEED_X = 35.0;
    private static final double CRASH_LIMIT_ANGLE_DEG = 10.0;

    private static final double PERFECT_LANDING_SPEED_Y = 5.0;
    private static final double PERFECT_LANDING_SPEED_X = 5.0;
    private static final double PERFECT_LANDING_ANGLE_DEG = 1.0;

    private static final int MAX_BASE_SCORE_FOR_QUALITY_1 = 30;
    private static final int MIN_SCORE_FOR_SUCCESSFUL_LANDING = 10;
    private static final int MAX_SCORE_OVERALL_CAP = 50;

    private static final double THRUSTER_RAMP_UP_PER_SECOND = 0.75;
    private static final double THRUSTER_RAMP_DOWN_PER_SECOND = 1.5;
    private static final int FLAME_MIN_LENGTH = 5;
    private static final int FLAME_MAX_LENGTH = 20;
    private static final double FLAME_BASE_WIDTH_RATIO = 0.6;
    private static final double FUEL_CONSUMPTION_RATE_PER_SECOND_AT_FULL_THRUST = 50.0;
    private static final double FUEL_PENALTY_CRASH_BASE = 100.0;
    private static final double FUEL_PENALTY_CRASH_VELOCITY_FACTOR = 1.5;
    public static boolean DEBUG_INFINITE_TOLERANCE_LANDING = false;
    private final Point2D.Double[] collisionPointsLocalFeet;
    private final Point2D.Double collisionPointLocalTip;
    private boolean engineSoundPlaying = false;
    private State currentState;
    private int calculatedScore = 0;
    private double fuel;
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

        collisionPointsLocalFeet = new Point2D.Double[]{new Point2D.Double(-DISPLAY_LANDER_WIDTH / 2.0 * LANDING_GEAR_WIDTH_FACTOR, LANDING_GEAR_Y_OFFSET), new Point2D.Double(DISPLAY_LANDER_WIDTH / 2.0 * LANDING_GEAR_WIDTH_FACTOR, LANDING_GEAR_Y_OFFSET)};
        collisionPointLocalTip = new Point2D.Double(0, -DISPLAY_LANDER_HEIGHT / 2.0);
        reset(startX, startY);
    }

    public void reset(float startX, float startY) {
        this.engineSoundPlaying = false;
        AudioManager.getInstance().stopSound(AudioManager.SoundEffect.ENGINE);
        this.x = startX;
        this.y = startY;
        this.vx = INITIAL_FLYOVER_SPEED_X_PIXELS_S;
        this.vy = 0;
        this.angle = 0;
        this.currentState = State.INITIAL_FLYOVER;
        this.playerRequestsThrust = false;
        this.currentThrustOutput = 0.0;
        this.rotationDirection = 0;
        this.calculatedScore = 0;
        this.fuel = MAX_FUEL;
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
        if (currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD || currentState == State.CRASHED) {
            vx = 0;
            vy = 0;
            currentThrustOutput = 0.0;
            playerRequestsThrust = false;
            rotationDirection = 0;
            return;
        }

        if (playerRequestsThrust && fuel > 0) {
            currentThrustOutput += THRUSTER_RAMP_UP_PER_SECOND * deltaTime;
            if (currentThrustOutput > 1.0) currentThrustOutput = 1.0;
        } else {
            currentThrustOutput -= THRUSTER_RAMP_DOWN_PER_SECOND * deltaTime;
            if (currentThrustOutput < 0.0) currentThrustOutput = 0.0;
            if (fuel <= 0) playerRequestsThrust = false;
        }

        if (currentThrustOutput > 0 && fuel > 0) {
            fuel -= FUEL_CONSUMPTION_RATE_PER_SECOND_AT_FULL_THRUST * currentThrustOutput * deltaTime;
            if (fuel < 0) {
                fuel = 0;
                currentThrustOutput = 0;
                playerRequestsThrust = false;
                System.out.println("PALIVO DOŠLO!");
            }
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

        AudioManager audioManager = AudioManager.getInstance();
        boolean thrustShouldBeActive = playerRequestsThrust && fuel > 0 && currentThrustOutput > 0.05;

        if (currentState == State.PLAYER_CONTROL || currentState == State.INITIAL_FLYOVER) {
            if (thrustShouldBeActive) {
                if (!engineSoundPlaying) {
                    audioManager.loopSound(AudioManager.SoundEffect.ENGINE);
                    engineSoundPlaying = true;
                }
            } else {
                if (engineSoundPlaying) {
                    audioManager.stopSound(AudioManager.SoundEffect.ENGINE);
                    engineSoundPlaying = false;
                }
            }
        } else {
            if (engineSoundPlaying) {
                audioManager.stopSound(AudioManager.SoundEffect.ENGINE);
                engineSoundPlaying = false;
            }
        }
    }

    public Point2D.Double getPointInWorldSpace(Point2D.Double localPoint) {
        double c = Math.cos(angle);
        double s = Math.sin(angle);
        double worldX = x + (localPoint.x * c - localPoint.y * s);
        double worldY = y + (localPoint.x * s + localPoint.y * c);
        return new Point2D.Double(worldX, worldY);
    }

    private void checkCollisions(Terrain terrain) {
        if (terrain == null || currentState != State.PLAYER_CONTROL) {
            if (currentState == State.INITIAL_FLYOVER && y > terrain.getScreenHeight() + DISPLAY_LANDER_HEIGHT) {
                currentState = State.CRASHED;
                calculatedScore = 0;
            }
            return;
        }

        Polygon terrainPolygon = terrain.getTerrainPolygon();
        boolean anyPointInTerrainPolygon = false;

        List<Point2D.Double> allCollisionPoints = new ArrayList<>();
        for (Point2D.Double localP : collisionPointsLocalFeet) {
            allCollisionPoints.add(getPointInWorldSpace(localP));
        }
        allCollisionPoints.add(getPointInWorldSpace(collisionPointLocalTip));

        for (Point2D.Double worldP : allCollisionPoints) {
            if (terrainPolygon.contains(worldP)) {
                anyPointInTerrainPolygon = true;
                break;
            }
        }

        if (anyPointInTerrainPolygon) {
            double impactVx = this.vx;
            double impactVy = this.vy;

            LandingPad contactPad = null;
            for (LandingPad pad : terrain.getLandingPads()) {
                boolean padContactFound = false;
                for (Point2D.Double worldP : allCollisionPoints) {
                    boolean xMatch = worldP.x >= pad.getStartX() && worldP.x <= pad.getEndX();
                    double yTolerance = DEBUG_INFINITE_TOLERANCE_LANDING ? DISPLAY_LANDER_HEIGHT : DISPLAY_LANDER_HEIGHT * 0.5;
                    boolean yMatch = Math.abs(worldP.y - pad.getY()) < yTolerance;
                    if (xMatch && yMatch) {
                        contactPad = pad;
                        padContactFound = true;
                        break;
                    }
                }
                if (padContactFound) break;
            }

            double currentAngleDeg = (Math.toDegrees(this.angle % (2 * Math.PI)) + 360) % 360;
            if (currentAngleDeg > 180) currentAngleDeg -= 360;
            double absAngleDeg = Math.abs(currentAngleDeg);
            double absVx = Math.abs(impactVx);
            double absVy = Math.abs(impactVy);

            if (DEBUG_INFINITE_TOLERANCE_LANDING && contactPad != null) {
                currentState = State.LANDED_GENTLE;
                calculatedScore = Math.min(MAX_BASE_SCORE_FOR_QUALITY_1 * contactPad.getMultiplier(), MAX_SCORE_OVERALL_CAP);
                this.y = contactPad.getY() - LANDING_GEAR_Y_OFFSET;
                this.angle = 0;
            } else if (absAngleDeg > CRASH_LIMIT_ANGLE_DEG || absVx > CRASH_LIMIT_SPEED_X || absVy > CRASH_LIMIT_SPEED_Y) {
                currentState = State.CRASHED;
                calculatedScore = 0;
                double impactSpeed = Math.sqrt(impactVx * impactVx + impactVy * impactVy);
                fuel -= (FUEL_PENALTY_CRASH_BASE + impactSpeed * FUEL_PENALTY_CRASH_VELOCITY_FACTOR);
                if (fuel < 0) fuel = 0;
            } else {
                this.angle = 0;
                float qualityVy = 1.0f - (float) Math.max(0, Math.min(1, (absVy - PERFECT_LANDING_SPEED_Y) / (CRASH_LIMIT_SPEED_Y - PERFECT_LANDING_SPEED_Y)));
                float qualityVx = 1.0f - (float) Math.max(0, Math.min(1, (absVx - PERFECT_LANDING_SPEED_X) / (CRASH_LIMIT_SPEED_X - PERFECT_LANDING_SPEED_X)));
                float qualityAngle = 1.0f - (float) Math.max(0, Math.min(1, (absAngleDeg - PERFECT_LANDING_ANGLE_DEG) / (CRASH_LIMIT_ANGLE_DEG - PERFECT_LANDING_ANGLE_DEG)));

                float overallQuality = (qualityVy + qualityVx + qualityAngle) / 3.0f;
                if (Float.isNaN(overallQuality) || Float.isInfinite(overallQuality)) overallQuality = 0f;

                float baseScore = overallQuality * MAX_BASE_SCORE_FOR_QUALITY_1;
                int multiplier = (contactPad != null) ? contactPad.getMultiplier() : 1;

                int finalScore = Math.round(baseScore * multiplier);
                finalScore = Math.max(MIN_SCORE_FOR_SUCCESSFUL_LANDING, finalScore);
                finalScore = Math.min(finalScore, MAX_SCORE_OVERALL_CAP);
                this.calculatedScore = finalScore;

                if (contactPad != null) {
                    this.y = contactPad.getY() - LANDING_GEAR_Y_OFFSET;
                }
                if (this.calculatedScore >= (MIN_SCORE_FOR_SUCCESSFUL_LANDING + (MAX_SCORE_OVERALL_CAP - MIN_SCORE_FOR_SUCCESSFUL_LANDING) * 0.6f)) {
                    currentState = State.LANDED_GENTLE;
                } else {
                    currentState = State.LANDED_HARD;
                }
            }

            vx = 0;
            vy = 0;
            playerRequestsThrust = false;
            currentThrustOutput = 0.0;
            rotationDirection = 0;
        }

        if (engineSoundPlaying && (currentState == State.CRASHED || currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD)) {
            AudioManager.getInstance().stopSound(AudioManager.SoundEffect.ENGINE);
            engineSoundPlaying = false;
        }

        if (currentState == State.CRASHED) {
            AudioManager.getInstance().playSound(AudioManager.SoundEffect.LUNAR_EXPLOSION);
        } else if (currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD) {
            if (this.fuel <= 0) {
                AudioManager.getInstance().playSound(AudioManager.SoundEffect.PLAYER_DEAD);
            }
        }
    }

    public void render(Graphics2D g) {
        AffineTransform oldTransform = g.getTransform();
        g.translate(x, y);
        g.rotate(angle);

        Color baseColor = Color.CYAN;
        if (currentState == State.CRASHED) baseColor = Color.RED;
        else if (currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD) baseColor = Color.GREEN;

        if (landerImage != null) {
            int imgX = -DISPLAY_LANDER_WIDTH / 2;
            int imgY = -DISPLAY_LANDER_HEIGHT / 2;
            g.drawImage(landerImage, imgX, imgY, DISPLAY_LANDER_WIDTH, DISPLAY_LANDER_HEIGHT, null);
            if (currentState == State.CRASHED || currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD) {
                g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 70));
                g.fillRect(imgX, imgY, DISPLAY_LANDER_WIDTH, DISPLAY_LANDER_HEIGHT);
            }
        } else {
            g.setColor(baseColor);
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
            float flameBaseHalfWidth = (DISPLAY_LANDER_WIDTH * (float) FLAME_BASE_WIDTH_RATIO) / 3.5f;
            float flameTipLength = (float) (FLAME_MIN_LENGTH + (FLAME_MAX_LENGTH - FLAME_MIN_LENGTH) * currentThrustOutput);
            flameTipLength += (Math.random() * 5.0f - 2.5f) * currentThrustOutput;
            flameTipLength = Math.max(0, flameTipLength);

            Polygon dynamicFlame = new Polygon();
            dynamicFlame.addPoint((int) -flameBaseHalfWidth, (int) flameBaseY);
            dynamicFlame.addPoint((int) flameBaseHalfWidth, (int) flameBaseY);
            dynamicFlame.addPoint(0, (int) (flameBaseY + flameTipLength));

            g.setColor(Color.WHITE);
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

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public double getAngle() {
        return angle;
    }

    public State getCurrentState() {
        return currentState;
    }

    public int getCalculatedScore() {
        return calculatedScore;
    }

    public double getFuel() {
        return fuel;
    }

    public enum State {
        INITIAL_FLYOVER, PLAYER_CONTROL, LANDED_GENTLE, LANDED_HARD, CRASHED
    }
}