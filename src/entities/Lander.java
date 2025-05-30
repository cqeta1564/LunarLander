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

/**
 * Represents the player-controlled Lunar Lander.
 * Handles its physics, state, fuel, collision detection, and rendering.
 */
public class Lander {
    /**
     * Initial horizontal speed during the flyover state, in pixels per second.
     */
    public static final double INITIAL_FLYOVER_SPEED_X_PIXELS_S = 60;
    /**
     * Initial Y position ratio relative to screen height for the flyover state.
     */
    public static final float INITIAL_FLYOVER_START_Y_RATIO = 0.15f;
    /**
     * Conversion factor from meters to pixels.
     */
    public static final double PIXELS_PER_METER = 20.0;
    /**
     * Original width of the lander image resource.
     */
    public static final int ORIGINAL_LANDER_IMAGE_WIDTH = 36;
    /**
     * Original height of the lander image resource.
     */
    public static final int ORIGINAL_LANDER_IMAGE_HEIGHT = 31;
    /**
     * Maximum fuel capacity of the lander.
     */
    public static final double MAX_FUEL = 1000.0;

    // Physics Constants
    private static final double LUNAR_GRAVITY_MS2 = 1.625; // Lunar gravity in m/s^2
    private static final double LUNAR_GRAVITY_PIXELS_S2 = LUNAR_GRAVITY_MS2 * PIXELS_PER_METER; // Lunar gravity in pixels/s^2
    private static final double MAIN_THRUSTER_ACCELERATION_MS2 = 3.0; // Main thruster acceleration in m/s^2
    private static final double MAIN_THRUSTER_MAX_ACCELERATION_PIXELS_S2 = MAIN_THRUSTER_ACCELERATION_MS2 * PIXELS_PER_METER; // pixels/s^2
    private static final double ROTATION_DEGREES_PER_SECOND = 90.0; // Rotation speed in degrees per second
    private static final double ROTATION_RADIANS_PER_SECOND = Math.toRadians(ROTATION_DEGREES_PER_SECOND); // Rotation speed in radians/s

    // Display and Collision Constants
    private static final double IMAGE_SCALE_DIVISOR = 1.2; // Divisor to scale down the original image
    /**
     * Display width of the lander on screen.
     */
    public static final int DISPLAY_LANDER_WIDTH = (int) (ORIGINAL_LANDER_IMAGE_WIDTH / IMAGE_SCALE_DIVISOR);
    /**
     * Display height of the lander on screen.
     */
    public static final int DISPLAY_LANDER_HEIGHT = (int) (ORIGINAL_LANDER_IMAGE_HEIGHT / IMAGE_SCALE_DIVISOR);

    private static final double LANDING_GEAR_Y_OFFSET = DISPLAY_LANDER_HEIGHT / 2.0; // Y offset for landing gear collision points from center
    private static final double LANDING_GEAR_WIDTH_FACTOR = 0.7; // Factor to adjust width of landing gear collision points

    // Landing/Crash Thresholds (velocities in pixels/s, angle in degrees)
    private static final double CRASH_LIMIT_SPEED_Y = 45.0; // Max vertical speed for non-crash
    private static final double CRASH_LIMIT_SPEED_X = 35.0; // Max horizontal speed for non-crash
    private static final double CRASH_LIMIT_ANGLE_DEG = 10.0;   // Max angle (degrees from vertical) for non-crash

    private static final double PERFECT_LANDING_SPEED_Y = 5.0;  // Max vertical speed for perfect landing
    private static final double PERFECT_LANDING_SPEED_X = 5.0;  // Max horizontal speed for perfect landing
    private static final double PERFECT_LANDING_ANGLE_DEG = 1.0;    // Max angle for perfect landing

    // Scoring and Fuel Constants
    private static final int MAX_BASE_SCORE_FOR_QUALITY_1 = 30; // Max base score for a perfect quality (1.0) landing
    private static final int MIN_SCORE_FOR_SUCCESSFUL_LANDING = 10; // Minimum score awarded for any successful landing
    private static final int MAX_SCORE_OVERALL_CAP = 50; // Overall cap for score from a single landing, regardless of multiplier

    private static final double THRUSTER_RAMP_UP_PER_SECOND = 0.75;  // Rate at which thruster output increases (0 to 1)
    private static final double THRUSTER_RAMP_DOWN_PER_SECOND = 1.5; // Rate at which thruster output decreases (1 to 0)
    private static final int FLAME_MIN_LENGTH = 5;    // Min length of thruster flame
    private static final int FLAME_MAX_LENGTH = 20;   // Max length of thruster flame at full thrust
    private static final double FLAME_BASE_WIDTH_RATIO = 0.6; // Ratio of flame base width to lander width
    private static final double FUEL_CONSUMPTION_RATE_PER_SECOND_AT_FULL_THRUST = 50.0; // Fuel units consumed per second at full thrust
    private static final double FUEL_PENALTY_CRASH_BASE = 100.0; // Base fuel penalty on crash
    private static final double FUEL_PENALTY_CRASH_VELOCITY_FACTOR = 1.5; // Additional fuel penalty based on impact speed

    /**
     * Debug flag for infinite landing tolerance (easy landings).
     */
    public static boolean DEBUG_INFINITE_TOLERANCE_LANDING = false;

    // Collision points relative to the lander's center, before rotation
    private final Point2D.Double[] collisionPointsLocalFeet; // Two points for landing gear
    private final Point2D.Double collisionPointLocalTip;   // One point for the tip/nose

    private boolean engineSoundPlaying = false; // Tracks if the engine sound is currently playing
    private State currentState;                 // Current state of the lander (e.g., flying, landed, crashed)
    private int calculatedScore = 0;            // Score calculated for the last landing attempt
    private double fuel;                        // Current fuel amount
    private double x, y;                        // Position (center of the lander)
    private double vx, vy;                      // Velocity in pixels per second
    private double angle;                       // Angle in radians (0 is upright)
    private boolean playerRequestsThrust = false; // True if player is holding the thrust key
    private double currentThrustOutput = 0.0;   // Current thruster output (0.0 to 1.0), ramps up/down
    private int rotationDirection = 0;          // -1 for left, 1 for right, 0 for no rotation
    private BufferedImage landerImage;          // Image of the lander

    /**
     * Constructs a new Lander.
     *
     * @param startX The initial X position of the lander.
     * @param startY The initial Y position of the lander.
     */
    public Lander(float startX, float startY) {
        try {
            // Load lander image from resources
            landerImage = ImageIO.read(Objects.requireNonNull(getClass().getResourceAsStream("/pictures/ship.png")));
        } catch (IOException | NullPointerException e) {
            System.err.println("Error loading lander image: " + e.getMessage() + ". Fallback shape will be used.");
            landerImage = null; // Use fallback drawing if image fails to load
        }

        // Define local collision points (relative to lander's center [0,0] before rotation)
        // Feet points are at the bottom corners
        collisionPointsLocalFeet = new Point2D.Double[]{new Point2D.Double(-DISPLAY_LANDER_WIDTH / 2.0 * LANDING_GEAR_WIDTH_FACTOR, LANDING_GEAR_Y_OFFSET), new Point2D.Double(DISPLAY_LANDER_WIDTH / 2.0 * LANDING_GEAR_WIDTH_FACTOR, LANDING_GEAR_Y_OFFSET)};
        // Tip point is at the top center
        collisionPointLocalTip = new Point2D.Double(0, -DISPLAY_LANDER_HEIGHT / 2.0);

        this.fuel = MAX_FUEL; // Start with maximum fuel
        reset(startX, startY); // Set initial state and position
    }

    /**
     * Resets the lander to its initial state for a new attempt.
     *
     * @param startX The X position to reset to.
     * @param startY The Y position to reset to.
     */
    public void reset(float startX, float startY) {
        this.engineSoundPlaying = false;
        AudioManager.getInstance().stopSound(AudioManager.SoundEffect.ENGINE); // Ensure engine sound is off

        this.x = startX;
        this.y = startY;
        this.vx = INITIAL_FLYOVER_SPEED_X_PIXELS_S; // Initial speed for fly-in
        this.vy = 0;
        this.angle = 0; // Upright
        this.currentState = State.INITIAL_FLYOVER;
        this.playerRequestsThrust = false;
        this.currentThrustOutput = 0.0;
        this.rotationDirection = 0;
        this.calculatedScore = 0; // Reset score for the new attempt
        // Fuel is typically handled by PlayingState before calling reset, but can be set here too
        // this.fuel = MAX_FUEL; // Or use a value passed from PlayingState
    }

    /**
     * Called when the player first provides input, transitioning from INITIAL_FLYOVER to PLAYER_CONTROL.
     */
    public void playerHasTakenControl() {
        if (this.currentState == State.INITIAL_FLYOVER) {
            this.currentState = State.PLAYER_CONTROL;
        }
    }

    /**
     * Sets whether the player is requesting thrust.
     *
     * @param wantsThrust True if player wants to apply thrust, false otherwise.
     */
    public void setPlayerRequestsThrust(boolean wantsThrust) {
        // Allow thrust input only in controllable states
        if (currentState == State.PLAYER_CONTROL || currentState == State.INITIAL_FLYOVER) {
            this.playerRequestsThrust = wantsThrust;
        } else {
            this.playerRequestsThrust = false; // No thrust if landed/crashed
        }
    }

    /**
     * Sets the direction of rotation.
     *
     * @param direction -1 for left, 1 for right, 0 for no rotation.
     */
    public void setRotation(int direction) {
        // Allow rotation only in PLAYER_CONTROL state
        if (currentState == State.PLAYER_CONTROL) {
            this.rotationDirection = Integer.signum(direction); // Ensure direction is -1, 0, or 1
        } else {
            this.rotationDirection = 0; // No rotation if not in player control
        }
    }

    /**
     * Updates the lander's state, physics, and checks for collisions.
     *
     * @param deltaTime The time elapsed since the last update, in seconds.
     * @param terrain   The Terrain object for collision detection.
     */
    public void update(double deltaTime, Terrain terrain) {
        // If landed or crashed, no further physics updates are needed for movement
        if (currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD || currentState == State.CRASHED) {
            vx = 0; // Stop movement
            vy = 0;
            currentThrustOutput = 0.0; // Thruster off
            playerRequestsThrust = false;
            rotationDirection = 0; // Stop rotation
            return;
        }

        // --- Thruster Output Ramp ---
        if (playerRequestsThrust && fuel > 0) {
            // Ramp up thruster output
            currentThrustOutput += THRUSTER_RAMP_UP_PER_SECOND * deltaTime;
            if (currentThrustOutput > 1.0) currentThrustOutput = 1.0; // Cap at 100%
        } else {
            // Ramp down thruster output (or if out of fuel)
            currentThrustOutput -= THRUSTER_RAMP_DOWN_PER_SECOND * deltaTime;
            if (currentThrustOutput < 0.0) currentThrustOutput = 0.0; // Cap at 0%
            if (fuel <= 0) playerRequestsThrust = false; // Force thrust request off if out of fuel
        }

        // --- Fuel Consumption ---
        if (currentThrustOutput > 0 && fuel > 0) {
            fuel -= FUEL_CONSUMPTION_RATE_PER_SECOND_AT_FULL_THRUST * currentThrustOutput * deltaTime;
            if (fuel < 0) {
                fuel = 0;
                currentThrustOutput = 0; // Stop thruster if fuel just ran out
                playerRequestsThrust = false;
                System.out.println("FUEL DEPLETED!");
            }
        }

        // --- Acceleration Calculation ---
        double ax = 0; // Acceleration in X
        double ay = 0; // Acceleration in Y

        if (currentState == State.INITIAL_FLYOVER) {
            ay += LUNAR_GRAVITY_PIXELS_S2; // Only gravity applies during flyover
            // (Optional: could add logic here if flyover needs to end or change behavior)
            if (terrain != null && x > terrain.getScreenWidth() + DISPLAY_LANDER_WIDTH * 2) {
                // Lander has flown off-screen during flyover without player input (e.g., if it never lands)
                // This case might not be reachable if player always takes control or lands.
            }
        } else if (currentState == State.PLAYER_CONTROL) {
            // Apply gravity
            ay += LUNAR_GRAVITY_PIXELS_S2;

            // Apply thruster acceleration if active
            if (currentThrustOutput > 0) {
                double actualThrustAcceleration = MAIN_THRUSTER_MAX_ACCELERATION_PIXELS_S2 * currentThrustOutput;
                // Thrust is applied opposite to lander's orientation (upwards relative to lander)
                ax += Math.sin(angle) * actualThrustAcceleration; // Horizontal component of thrust
                ay -= Math.cos(angle) * actualThrustAcceleration; // Vertical component of thrust (negative is up)
            }

            // Apply rotation
            if (rotationDirection != 0) {
                angle += rotationDirection * ROTATION_RADIANS_PER_SECOND * deltaTime;
                angle = (angle + 2 * Math.PI) % (2 * Math.PI); // Normalize angle to 0 - 2PI
            }
        }

        // --- Euler Integration for Velocity and Position ---
        vx += ax * deltaTime;
        vy += ay * deltaTime;
        x += vx * deltaTime;
        y += vy * deltaTime;

        checkCollisions(terrain); // Check for collisions with terrain or landing pads

        // --- Audio Management for Engine Sound ---
        AudioManager audioManager = AudioManager.getInstance();
        // Condition for engine sound: player requests thrust, has fuel, and thruster output is significant
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
        } else { // Landed, crashed, etc.
            if (engineSoundPlaying) {
                audioManager.stopSound(AudioManager.SoundEffect.ENGINE);
                engineSoundPlaying = false;
            }
        }
    }

    /**
     * Transforms a local point (relative to lander's center and orientation) to world coordinates.
     *
     * @param localPoint The Point2D.Double in local lander space.
     * @return The corresponding Point2D.Double in world space.
     */
    public Point2D.Double getPointInWorldSpace(Point2D.Double localPoint) {
        double cosAngle = Math.cos(angle);
        double sinAngle = Math.sin(angle);
        // Apply rotation and then translation
        double worldX = x + (localPoint.x * cosAngle - localPoint.y * sinAngle);
        double worldY = y + (localPoint.x * sinAngle + localPoint.y * cosAngle);
        return new Point2D.Double(worldX, worldY);
    }

    /**
     * Checks for collisions between the lander and the terrain.
     * Updates lander state and score based on collision outcome.
     *
     * @param terrain The Terrain object to check against.
     */
    private void checkCollisions(Terrain terrain) {
        if (terrain == null) return;

        // Collision detection is primarily for PLAYER_CONTROL state
        if (currentState != State.PLAYER_CONTROL) {
            // Handle off-screen crash during flyover (simple Y check)
            if (currentState == State.INITIAL_FLYOVER && y > terrain.getScreenHeight() + DISPLAY_LANDER_HEIGHT) {
                currentState = State.CRASHED;
                calculatedScore = 0;
                // AudioManager.getInstance().playSound(AudioManager.SoundEffect.LUNAR_EXPLOSION); // Or some other sound
            }
            return;
        }

        Polygon terrainPolygon = terrain.getTerrainPolygon(); // Get the collision shape of the terrain
        boolean anyPointInTerrainPolygon = false;

        // Get all collision points in world space
        List<Point2D.Double> allCollisionPoints = new ArrayList<>();
        for (Point2D.Double localP : collisionPointsLocalFeet) {
            allCollisionPoints.add(getPointInWorldSpace(localP));
        }
        allCollisionPoints.add(getPointInWorldSpace(collisionPointLocalTip));

        // Check if any of the lander's collision points are inside the terrain polygon
        for (Point2D.Double worldP : allCollisionPoints) {
            if (terrainPolygon.contains(worldP)) {
                anyPointInTerrainPolygon = true;
                break;
            }
        }

        if (anyPointInTerrainPolygon) {
            // --- Collision Occurred ---
            double impactVx = this.vx;
            double impactVy = this.vy;

            // Check if collision was with a landing pad
            LandingPad contactPad = null;
            for (LandingPad pad : terrain.getLandingPads()) {
                boolean padContactFound = false;
                for (Point2D.Double worldP : allCollisionPoints) { // Check all lander points against this pad
                    // Check if point is within horizontal bounds of the pad
                    boolean xMatch = worldP.x >= pad.getStartX() && worldP.x <= pad.getEndX();
                    // Check if point is close enough vertically to the pad's surface
                    double yTolerance = DEBUG_INFINITE_TOLERANCE_LANDING ? DISPLAY_LANDER_HEIGHT : DISPLAY_LANDER_HEIGHT * 0.5;
                    boolean yMatch = Math.abs(worldP.y - pad.getY()) < yTolerance;

                    if (xMatch && yMatch) {
                        contactPad = pad;
                        padContactFound = true;
                        break;
                    }
                }
                if (padContactFound) break; // Found a contact pad
            }

            // Get angle in degrees, normalized between -180 and 180
            double currentAngleDeg = (Math.toDegrees(this.angle % (2 * Math.PI)) + 360) % 360;
            if (currentAngleDeg > 180) currentAngleDeg -= 360; // Normalize to +/- 180 range
            double absAngleDeg = Math.abs(currentAngleDeg);
            double absVx = Math.abs(impactVx);
            double absVy = Math.abs(impactVy);

            // --- Determine Landing Outcome ---
            if (DEBUG_INFINITE_TOLERANCE_LANDING && contactPad != null) {
                // Easy mode: perfect landing if on a pad
                currentState = State.LANDED_GENTLE;
                calculatedScore = Math.min(MAX_BASE_SCORE_FOR_QUALITY_1 * contactPad.getMultiplier(), MAX_SCORE_OVERALL_CAP);
                this.y = contactPad.getY() - LANDING_GEAR_Y_OFFSET; // Snap to pad surface
                this.angle = 0; // Straighten lander
                // AudioManager.getInstance().playSound(AudioManager.SoundEffect.PLAYER_DEAD); // Placeholder for landing sound
            } else if (absAngleDeg > CRASH_LIMIT_ANGLE_DEG || absVx > CRASH_LIMIT_SPEED_X || absVy > CRASH_LIMIT_SPEED_Y || contactPad == null) {
                // Crash conditions: too much angle, too fast, or hit terrain (not a pad)
                currentState = State.CRASHED;
                calculatedScore = 0;
                AudioManager.getInstance().playSound(AudioManager.SoundEffect.LUNAR_EXPLOSION);
                // Apply fuel penalty for crashing
                double impactSpeed = Math.sqrt(impactVx * impactVx + impactVy * impactVy);
                fuel -= (FUEL_PENALTY_CRASH_BASE + impactSpeed * FUEL_PENALTY_CRASH_VELOCITY_FACTOR);
                if (fuel < 0) fuel = 0;
            } else {
                // Successful landing on a pad
                this.angle = 0; // Straighten lander

                // Calculate landing quality (0.0 to 1.0 for each component)
                float qualityVy = 1.0f - (float) Math.max(0, Math.min(1, (absVy - PERFECT_LANDING_SPEED_Y) / (CRASH_LIMIT_SPEED_Y - PERFECT_LANDING_SPEED_Y)));
                float qualityVx = 1.0f - (float) Math.max(0, Math.min(1, (absVx - PERFECT_LANDING_SPEED_X) / (CRASH_LIMIT_SPEED_X - PERFECT_LANDING_SPEED_X)));
                float qualityAngle = 1.0f - (float) Math.max(0, Math.min(1, (absAngleDeg - PERFECT_LANDING_ANGLE_DEG) / (CRASH_LIMIT_ANGLE_DEG - PERFECT_LANDING_ANGLE_DEG)));

                float overallQuality = (qualityVy + qualityVx + qualityAngle) / 3.0f; // Average quality
                if (Float.isNaN(overallQuality) || Float.isInfinite(overallQuality)) overallQuality = 0f; // Sanitize

                float baseScore = overallQuality * MAX_BASE_SCORE_FOR_QUALITY_1;
                int multiplier = (contactPad != null) ? contactPad.getMultiplier() : 1; // Pad multiplier (should always be != null here)

                int finalScore = Math.round(baseScore * multiplier);
                finalScore = Math.max(MIN_SCORE_FOR_SUCCESSFUL_LANDING, finalScore); // Ensure minimum score
                finalScore = Math.min(finalScore, MAX_SCORE_OVERALL_CAP); // Cap maximum score
                this.calculatedScore = finalScore;

                if (contactPad != null) {
                    this.y = contactPad.getY() - LANDING_GEAR_Y_OFFSET; // Snap to pad surface
                }

                // Determine if landing was "gentle" or "hard" based on score
                // Threshold is somewhat arbitrary, e.g., 60% of the score range above minimum.
                if (this.calculatedScore >= (MIN_SCORE_FOR_SUCCESSFUL_LANDING + (MAX_SCORE_OVERALL_CAP - MIN_SCORE_FOR_SUCCESSFUL_LANDING) * 0.6f)) {
                    currentState = State.LANDED_GENTLE;
                } else {
                    currentState = State.LANDED_HARD;
                }
                // AudioManager.getInstance().playSound(AudioManager.SoundEffect.PLAYER_DEAD); // Placeholder for landing sound
            }

            // Stop movement and controls after any collision resolution
            vx = 0;
            vy = 0;
            playerRequestsThrust = false;
            currentThrustOutput = 0.0;
            rotationDirection = 0;
        }

        // --- Final Audio Adjustments Post-Collision ---
        if (engineSoundPlaying && (currentState == State.CRASHED || currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD)) {
            AudioManager.getInstance().stopSound(AudioManager.SoundEffect.ENGINE);
            engineSoundPlaying = false;
        }

        // Play "player dead" sound if landed successfully but out of fuel (game over condition)
        if (currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD) {
            if (this.fuel <= 0) {
                AudioManager.getInstance().playSound(AudioManager.SoundEffect.PLAYER_DEAD);
            }
        }
    }

    /**
     * Renders the lander on the screen.
     *
     * @param g The Graphics2D context to draw on.
     */
    public void render(Graphics2D g) {
        AffineTransform oldTransform = g.getTransform(); // Save current transform

        // Translate and rotate graphics context to lander's position and orientation
        g.translate(x, y);
        g.rotate(angle);

        // --- Draw Lander ---
        Color baseColor = Color.CYAN; // Default color
        if (currentState == State.CRASHED) baseColor = Color.RED;
        else if (currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD) baseColor = Color.GREEN;

        if (landerImage != null) {
            int imgX = -DISPLAY_LANDER_WIDTH / 2; // Center image
            int imgY = -DISPLAY_LANDER_HEIGHT / 2;
            g.drawImage(landerImage, imgX, imgY, DISPLAY_LANDER_WIDTH, DISPLAY_LANDER_HEIGHT, null);

            // Optional: Overlay color tint if landed/crashed
            if (currentState == State.CRASHED || currentState == State.LANDED_GENTLE || currentState == State.LANDED_HARD) {
                g.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 70)); // Semi-transparent
                g.fillRect(imgX, imgY, DISPLAY_LANDER_WIDTH, DISPLAY_LANDER_HEIGHT);
            }
        } else {
            // Fallback drawing if image is not loaded: a simple triangle
            g.setColor(baseColor);
            Polygon fallbackShape = new Polygon();
            fallbackShape.addPoint(0, -DISPLAY_LANDER_HEIGHT / 2); // Tip
            fallbackShape.addPoint(-DISPLAY_LANDER_WIDTH / 2, DISPLAY_LANDER_HEIGHT / 2); // Bottom-left
            fallbackShape.addPoint(DISPLAY_LANDER_WIDTH / 2, DISPLAY_LANDER_HEIGHT / 2);  // Bottom-right
            g.fillPolygon(fallbackShape);
            g.setColor(Color.WHITE); // Outline
            g.drawPolygon(fallbackShape);
        }

        // --- Draw Thruster Flame ---
        if (currentThrustOutput > 0.05 && (currentState == State.PLAYER_CONTROL || currentState == State.INITIAL_FLYOVER)) {
            float flameBaseY = DISPLAY_LANDER_HEIGHT / 2.0f; // Flame starts at the bottom of the lander
            float flameBaseHalfWidth = (DISPLAY_LANDER_WIDTH * (float) FLAME_BASE_WIDTH_RATIO) / 3.5f; // Adjusted for visual preference
            // Flame length depends on thrust output, with some randomness for flicker effect
            float flameTipLength = (float) (FLAME_MIN_LENGTH + (FLAME_MAX_LENGTH - FLAME_MIN_LENGTH) * currentThrustOutput);
            flameTipLength += (Math.random() * 5.0f - 2.5f) * currentThrustOutput; // Random flicker
            flameTipLength = Math.max(0, flameTipLength); // Ensure non-negative length

            Polygon dynamicFlame = new Polygon();
            dynamicFlame.addPoint((int) -flameBaseHalfWidth, (int) flameBaseY); // Left base of flame
            dynamicFlame.addPoint((int) flameBaseHalfWidth, (int) flameBaseY);  // Right base of flame
            dynamicFlame.addPoint(0, (int) (flameBaseY + flameTipLength));      // Tip of flame

            g.setColor(Color.WHITE); // Flame color (could be orange/yellow too)
            // g.fillPolygon(dynamicFlame); // For a filled flame
            g.drawPolygon(dynamicFlame); // For an outlined flame
        }

        g.setTransform(oldTransform); // Restore original transform
    }

    // --- Getters ---

    /**
     * @return The current X position of the lander.
     */
    public double getX() {
        return x;
    }

    /**
     * @return The current Y position of the lander.
     */
    public double getY() {
        return y;
    }

    /**
     * @return The current horizontal velocity (vx) of the lander.
     */
    public double getVx() {
        return vx;
    }

    /**
     * @return The current vertical velocity (vy) of the lander.
     */
    public double getVy() {
        return vy;
    }

    /**
     * @return The current angle (in radians) of the lander.
     */
    public double getAngle() {
        return angle;
    }

    /**
     * @return The current state of the lander.
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * @return The score calculated from the last landing attempt.
     */
    public int getCalculatedScore() {
        return calculatedScore;
    }

    /**
     * @return The current fuel amount.
     */
    public double getFuel() {
        return fuel;
    }

    // --- Setters (mainly for testing or specific game logic needs) ---

    /**
     * Sets the lander's fuel amount. Clamped between 0 and MAX_FUEL.
     *
     * @param fuelAmount The new fuel amount.
     */
    public void setFuel(double fuelAmount) {
        this.fuel = Math.max(0, Math.min(fuelAmount, MAX_FUEL));
    }

    /**
     * Public setter for vertical velocity (vy), primarily for testing.
     *
     * @param v The new vertical velocity.
     */
    public void setVyPublic(double v) {
        this.vy = v;
    }

    /**
     * Public setter for horizontal velocity (vx), primarily for testing.
     *
     * @param v The new horizontal velocity.
     */
    public void setVxPublic(double v) {
        this.vx = v;
    }

    /**
     * Public setter for angle, primarily for testing.
     *
     * @param v The new angle in radians.
     */
    public void setAnglePublic(double v) {
        this.angle = v;
    }


    /**
     * Enum representing the various states the lander can be in.
     */
    public enum State {
        /**
         * Initial state where the lander flies in from the side. Limited player control.
         */
        INITIAL_FLYOVER,
        /**
         * Player has full control over thrust and rotation.
         */
        PLAYER_CONTROL,
        /**
         * Successfully landed with good parameters.
         */
        LANDED_GENTLE,
        /**
         * Successfully landed, but parameters were not optimal (e.g., a bit too fast).
         */
        LANDED_HARD,
        /**
         * Lander crashed due to excessive speed, angle, or impact with rough terrain.
         */
        CRASHED
    }
}