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

/**
 * Represents the main gameplay state where the player controls the lander.
 * Manages the lander, terrain, camera, HUD, and game logic during play.
 */
public class PlayingState implements GameState {
    // --- Camera and Scrolling Constants ---
    /**
     * Factor of screen width at which camera starts scrolling right. (e.g., 0.7 means lander at 70% of screen width from left)
     */
    private static final float SCROLL_THRESHOLD_RIGHT_FACTOR = 0.7f;
    /**
     * Target screen X position (as factor of screen width) for the lander after a scroll.
     */
    private static final float LANDER_SCREEN_TARGET_X_AFTER_SCROLL = 0.6f;
    /**
     * Speed for camera's linear interpolation (lerp) towards its target X position. Higher is faster.
     */
    private static final float CAMERA_SCROLL_LERP_SPEED = 4.0f;

    // --- Camera Zoom Constants ---
    /**
     * Zoom level when lander is close to terrain.
     */
    private static final float ZOOM_LEVEL_IN = 1.8f;
    /**
     * Default/ zoomed-out zoom level.
     */
    private static final float ZOOM_LEVEL_OUT = 1.0f;
    /**
     * Height above terrain (pixels) below which camera starts zooming in.
     */
    private static final float DISTANCE_TO_ZOOM_IN_THRESHOLD = 100.0f;
    /**
     * Height above terrain (pixels) above which camera starts zooming out.
     */
    private static final float DISTANCE_TO_ZOOM_OUT_THRESHOLD = 150.0f;
    /**
     * Speed for camera zoom interpolation. Higher is faster.
     */
    private static final float ZOOM_INTERPOLATION_SPEED = 2.0f;

    // --- HUD Constants ---
    /**
     * Horizontal margin for HUD elements.
     */
    private static final int HUD_MARGIN_X = 15;
    /**
     * Vertical margin for HUD elements from the top.
     */
    private static final int HUD_MARGIN_Y = 20;
    /**
     * Vertical spacing between lines of HUD text.
     */
    private static final int HUD_LINE_SPACING = 18;
    /**
     * Font for HUD text.
     */
    private static final Font HUD_FONT = new Font("Monospaced", Font.BOLD, 14);

    // --- Game Over/Message Constants ---
    /**
     * Large font for primary game over messages (e.g., "CRASHED!").
     */
    private static final Font GAME_OVER_FONT_BIG = new Font("Arial", Font.BOLD, 48);
    /**
     * Medium font for secondary messages (e.g., score).
     */
    private static final Font GAME_OVER_FONT_MEDIUM = new Font("Arial", Font.BOLD, 32);
    /**
     * Small font for instructional messages (e.g., "Press Enter to continue").
     */
    private static final Font GAME_OVER_FONT_SMALL = new Font("Arial", Font.PLAIN, 20);

    // --- State Management and Input ---
    private final StateManager stateManager; // Manages state transitions
    private final InputHandler inputHandler; // Handles player input

    // --- Game Entities and World ---
    private Terrain terrain; // The game terrain
    private Lander lander;   // The player's lander

    // --- Camera State ---
    private float cameraX = 0;         // Current X position of the camera's view in world coordinates
    private float targetCameraX = 0;   // Target X position for the camera (for smooth scrolling)
    private float currentZoom = 1.0f;  // Current camera zoom level
    private float targetZoom = 1.0f;   // Target camera zoom level (for smooth zooming)

    // --- Gameplay State Variables ---
    private boolean playerControlTakenSinceEnter = false; // True if player has made an input since this attempt started
    private boolean attemptConcluded = false;             // True if the current landing attempt has ended (landed/crashed)
    private String landingMessageDisplay = "";            // Message to display after landing/crashing
    private double totalTimePlayedInStateSeconds = 0;     // Time elapsed in the current landing attempt
    private float currentHeightAboveTerrain = 0;          // Lander's current height above the terrain directly below it
    private double fuelForNextAttempt = Lander.MAX_FUEL;  // Fuel to carry over to the next attempt (if any)
    private int cumulativeSessionScore = 0;               // Total score accumulated in the current game session (resets on menu exit/re-entry)


    /**
     * Constructs a PlayingState.
     *
     * @param stateManager The game's state manager.
     * @param inputHandler The game's input handler.
     */
    public PlayingState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
    }

    /**
     * Initializes the PlayingState. This method is called when the state is first created.
     *
     * @param manager The StateManager that manages this state.
     */
    @Override
    public void init(StateManager manager) {
        // One-time initialization for PlayingState if needed.
        // Terrain and Lander are typically created or reset in onEnter().
        System.out.println("PlayingState initialized.");
    }

    /**
     * Called every time this state becomes active.
     * Resets or initializes game elements for a new attempt or a new game session.
     * This includes resetting the lander, terrain, camera, and game variables.
     */
    @Override
    public void onEnter() {
        System.out.println("Entering PlayingState."); // Log state entry
        // Reset or initialize game elements for a new attempt or session start.

        // Initialize Terrain if it's the first time or needs full reset
        if (this.terrain == null) {
            this.terrain = new Terrain(Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);
        }

        // Reset camera position and regenerate visible terrain
        this.cameraX = 0;
        this.targetCameraX = 0;
        if (terrain != null) {
            this.terrain.populateVisibleTerrain(cameraX, Game.DEFAULT_WIDTH); // Populate terrain based on camera
        }

        // Determine start position for the lander (for fly-in)
        float startY = Game.DEFAULT_HEIGHT * Lander.INITIAL_FLYOVER_START_Y_RATIO; // Calculate initial Y position based on screen height
        float startX = this.cameraX - Lander.DISPLAY_LANDER_WIDTH; // Start off-screen to the left

        // Fuel for this attempt: either max fuel (new session) or carried-over fuel
        double fuelValueForThisNewAttempt = fuelForNextAttempt;

        // Initialize or reset Lander
        if (this.lander == null) {
            this.lander = new Lander(startX, startY); // Create new Lander instance if it doesn't exist
        } else {
            lander.reset(startX, startY); // Reset existing lander
        }
        this.lander.setFuel(fuelValueForThisNewAttempt); // Set fuel for this attempt

        // If starting with max fuel, it's a new game session, so reset cumulative score
        if (fuelValueForThisNewAttempt == Lander.MAX_FUEL) { //
            cumulativeSessionScore = 0; // Reset score for a new session
        }
        fuelForNextAttempt = Lander.MAX_FUEL; // Default for next attempt unless successfully landed with fuel

        // Reset per-attempt flags and state
        playerControlTakenSinceEnter = false;
        currentZoom = ZOOM_LEVEL_OUT; // Start zoomed out
        targetZoom = ZOOM_LEVEL_OUT;
        attemptConcluded = false;
        landingMessageDisplay = "";
        totalTimePlayedInStateSeconds = 0;
        currentHeightAboveTerrain = 0; // Will be calculated in update
    }

    /**
     * Updates the game logic for the playing state.
     * This includes handling input, updating the lander, managing camera movement and zoom,
     * and checking for game over conditions.
     *
     * @param deltaTime The time elapsed since the last update, in seconds.
     */
    @Override
    public void update(double deltaTime) {
        float previousCameraX = this.cameraX; // Store camera position for terrain update check

        if (!attemptConcluded) {
            // --- Active Gameplay Update ---
            handleInput(); // Process player controls
            totalTimePlayedInStateSeconds += deltaTime; // Increment attempt timer

            if (lander != null) {
                lander.update(deltaTime, terrain); // Update lander physics and state
                checkLanderStatusAndScore();       // Check if landed/crashed

                // Camera scrolling logic (only if lander is flying)
                if (terrain != null && (lander.getCurrentState() == Lander.State.PLAYER_CONTROL || lander.getCurrentState() == Lander.State.INITIAL_FLYOVER)) {
                    float landerScreenX = (float) lander.getX() - cameraX; // Lander's X position relative to camera
                    float scrollTriggerX = Game.DEFAULT_WIDTH * SCROLL_THRESHOLD_RIGHT_FACTOR; // Threshold for scrolling

                    if (landerScreenX > scrollTriggerX) {
                        // If lander moves past threshold, set target camera X to keep lander at target screen pos
                        this.targetCameraX = (float) lander.getX() - (Game.DEFAULT_WIDTH * LANDER_SCREEN_TARGET_X_AFTER_SCROLL);
                    }
                    // Prevent camera from scrolling too far left (world X < 0)
                    if (this.targetCameraX < 0) {
                        this.targetCameraX = 0;
                    }
                }
            }
        } else {
            // --- Attempt Concluded Update ---
            // Handle input for post-attempt options (retry, menu)
            if (inputHandler.isEscJustPressed()) {
                stateManager.setState(StateManager.StateType.MENU); // Go to menu
            } else if (inputHandler.isEnterJustPressed()) {
                // If landed successfully (or crashed with fuel remaining) and has fuel, allow another attempt
                if (lander != null && (lander.getCurrentState() == Lander.State.LANDED_GENTLE || lander.getCurrentState() == Lander.State.LANDED_HARD || lander.getCurrentState() == Lander.State.CRASHED) // Allow retry even on crash if fuel > 0
                        && lander.getFuel() > 0) { //

                    fuelForNextAttempt = lander.getFuel(); // Carry over remaining fuel
                    onEnter(); // Re-initialize for a new attempt
                }
                // If out of fuel, Enter does nothing here; Esc is the only option
            }
        }

        // Smoothly interpolate camera X to its target
        if (Math.abs(targetCameraX - cameraX) > 0.5f) { // Only lerp if significant difference
            cameraX += (targetCameraX - cameraX) * CAMERA_SCROLL_LERP_SPEED * deltaTime;
        } else if (targetCameraX != cameraX) { // Snap if very close
            cameraX = targetCameraX;
        }

        // If camera moved, regenerate the visible terrain
        if (Math.abs(cameraX - previousCameraX) > 0.1f && terrain != null) { // Check if camera has moved significantly
            terrain.populateVisibleTerrain(cameraX, Game.DEFAULT_WIDTH); // Update visible terrain based on new camera position
        }

        // --- Camera Zoom Logic ---
        if (lander != null && terrain != null && terrain.getTerrainSurfacePoints() != null && !terrain.getTerrainSurfacePoints().isEmpty() && (lander.getCurrentState() == Lander.State.PLAYER_CONTROL || lander.getCurrentState() == Lander.State.INITIAL_FLYOVER)) {
            // Calculate lander's effective screen X for height calculation
            double landerEffectiveScreenX = lander.getX() - cameraX;
            // Get lander's lowest point (approximated by a point at its feet, considering rotation)
            Point2D.Double landerFootPointLocal = new Point2D.Double(0, Lander.DISPLAY_LANDER_HEIGHT / 2.0); //
            Point2D.Double landerFootPointWorld = lander.getPointInWorldSpace(landerFootPointLocal); //
            double landerFeetScreenY = landerFootPointWorld.y; // World Y of lander's feet

            // Get terrain height directly below lander's screen X position
            float terrainYAtLanderX = calculateTerrainYAtScreenX((float) landerEffectiveScreenX, terrain.getTerrainSurfacePoints()); //

            if (terrainYAtLanderX != -1) { // If valid terrain height found
                this.currentHeightAboveTerrain = terrainYAtLanderX - (float) landerFeetScreenY; // Positive if above terrain
                // Adjust target zoom based on height
                if (currentHeightAboveTerrain < DISTANCE_TO_ZOOM_IN_THRESHOLD && currentHeightAboveTerrain >= -Lander.DISPLAY_LANDER_HEIGHT) { // Zoom in if close
                    targetZoom = ZOOM_LEVEL_IN;
                } else if (currentHeightAboveTerrain > DISTANCE_TO_ZOOM_OUT_THRESHOLD) { // Zoom out if far
                    targetZoom = ZOOM_LEVEL_OUT;
                }
            } else { // If no terrain below (e.g., off edge of current segment calculation), zoom out
                targetZoom = ZOOM_LEVEL_OUT;
                this.currentHeightAboveTerrain = Float.POSITIVE_INFINITY; // Indicate unknown/very high
            }
        } else if (lander != null && (lander.getCurrentState() == Lander.State.LANDED_GENTLE || lander.getCurrentState() == Lander.State.LANDED_HARD || lander.getCurrentState() == Lander.State.CRASHED)) { //
            targetZoom = ZOOM_LEVEL_OUT; // Zoom out when landed/crashed
        }

        // Smoothly interpolate current zoom to target zoom
        currentZoom += (targetZoom - currentZoom) * ZOOM_INTERPOLATION_SPEED * deltaTime;
        currentZoom = Math.max(ZOOM_LEVEL_OUT, Math.min(currentZoom, ZOOM_LEVEL_IN * 1.05f)); // Clamp zoom
    }

    /**
     * Calculates the Y-coordinate of the terrain surface at a given screen X-coordinate.
     * Linearly interpolates between the points of the terrain surface.
     *
     * @param screenX       The screen X-coordinate for which to find the terrain height.
     * @param surfacePoints A list of {@link Point} objects representing the terrain surface segments (screen coordinates).
     * @return The terrain's Y-coordinate at {@code screenX}, or -1 if not found or input is invalid.
     */
    private float calculateTerrainYAtScreenX(float screenX, List<Point> surfacePoints) {
        if (surfacePoints == null || surfacePoints.size() < 2) { // Check for sufficient points
            return -1; // Not enough points for a segment
        }
        Point p1 = null, p2 = null; // Points defining the segment
        // Find the segment of the terrain that contains the given screenX
        for (int i = 0; i < surfacePoints.size() - 1; i++) {
            if (surfacePoints.get(i).x <= screenX && surfacePoints.get(i + 1).x >= screenX) {
                p1 = surfacePoints.get(i);
                p2 = surfacePoints.get(i + 1);
                break;
            }
        }
        // Handle cases where screenX is outside the range of the provided surface points
        if (p1 == null && !surfacePoints.isEmpty()) {
            if (screenX < surfacePoints.get(0).x) return surfacePoints.get(0).y; // Before the first point
            if (screenX > surfacePoints.get(surfacePoints.size() - 1).x) // After the last point
                return surfacePoints.get(surfacePoints.size() - 1).y;
            return -1; // Should not happen if list is ordered and screenX is within overall bounds
        }

        if (p1 != null && p2 != null) {
            if (p2.x == p1.x) { // Vertical segment
                return Math.min(p1.y, p2.y); // Return the top-most Y of the vertical segment
            } else {
                // Linear interpolation for Y
                float t = (screenX - p1.x) / (float) (p2.x - p1.x);
                return p1.y + t * (p2.y - p1.y);
            }
        }
        return -1; // screenX not within any segment
    }

    /**
     * Checks the lander's status (landed, crashed) and updates the score and game messages accordingly.
     * Sets the {@code attemptConcluded} flag and {@code landingMessageDisplay}.
     * Also handles the "player dead" sound if landed successfully but out of fuel.
     */
    private void checkLanderStatusAndScore() {
        if (lander == null) return;
        if (attemptConcluded) return; // Don't re-evaluate if already concluded

        Lander.State lState = lander.getCurrentState(); // Get current lander state

        if (lState == Lander.State.LANDED_GENTLE || lState == Lander.State.LANDED_HARD) {
            cumulativeSessionScore += lander.getCalculatedScore(); // Add score to cumulative total
            attemptConcluded = true;
            landingMessageDisplay = (lState == Lander.State.LANDED_GENTLE) ? "Perfect Landing!" : "Hard Landing!"; // Set landing message
            if (lander.getFuel() <= 0) { // If out of fuel after landing
                landingMessageDisplay += " .. but out of fuel!";
            }
        } else if (lState == Lander.State.CRASHED) {
            attemptConcluded = true;
            landingMessageDisplay = "CRASHED!"; // Set crash message
            if (lander.getFuel() <= 0) { // If out of fuel after crashing
                landingMessageDisplay += " .. and out of fuel too!";
            }
        }
    }


    /**
     * Renders the playing state, including the terrain, lander, HUD, and game over messages.
     * Applies camera transformations (translation and zoom).
     *
     * @param g The Graphics2D context to draw on.
     */
    @Override
    public void render(Graphics2D g) {
        Graphics2D g2d = g; // Use g2d for clarity
        AffineTransform originalScreenTransform = g2d.getTransform(); // Save original transform to restore later

        // Set rendering hints for better quality
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON); //
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); //

        // Draw background
        g2d.setColor(new Color(10, 10, 20)); // Dark blue/black background
        g2d.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        // Create a new Graphics2D object for world rendering to isolate transformations
        Graphics2D worldG = (Graphics2D) g2d.create();

        // Apply zoom transformation, pivoting around the lander's screen position
        if (lander != null && currentZoom != 1.0f) {
            double landerScreenXForZoomPivot = lander.getX() - cameraX; // Lander's X position relative to the camera's current view
            double landerScreenYForZoomPivot = lander.getY(); // Lander's Y position (world space, but Y zoom is screen-centered)

            AffineTransform zoomTransform = new AffineTransform();
            // Translate to lander's position, scale, then translate back
            zoomTransform.translate(landerScreenXForZoomPivot, landerScreenYForZoomPivot);
            zoomTransform.scale(currentZoom, currentZoom);
            zoomTransform.translate(-landerScreenXForZoomPivot, -landerScreenYForZoomPivot);
            worldG.transform(zoomTransform); // Apply the zoom transformation
        }

        // Apply camera translation (scrolling)
        worldG.translate(-cameraX, 0);

        // Render terrain (it will be drawn in the transformed worldG context)
        if (terrain != null) {
            // Using a separate Graphics context for terrain rendering to ensure its transformations
            // are isolated if they were different from the lander's (though here they are the same due to worldG).
            Graphics2D terrainSpecificG = (Graphics2D) g2d.create(); // Create a new graphics context
            // Apply the same zoom and camera translation as worldG
            if (lander != null && currentZoom != 1.0f) {
                double landerScreenX = lander.getX() - cameraX;
                double landerScreenY = lander.getY();
                terrainSpecificG.translate(landerScreenX, landerScreenY);
                terrainSpecificG.scale(currentZoom, currentZoom);
                terrainSpecificG.translate(-landerScreenX, -landerScreenY);
            }
            terrainSpecificG.translate(-cameraX, 0); // Apply camera scroll
            terrain.render(terrainSpecificG); // Render the terrain
            terrainSpecificG.dispose(); // Dispose of the temporary graphics context
        }

        // Render lander (it will also be drawn in the transformed worldG context)
        if (lander != null) {
            Graphics2D landerSpecificG = (Graphics2D) g2d.create(); // Create another graphics context for the lander

            // Apply the same zoom transformation, pivoting around the lander
            if (currentZoom != 1.0f) {
                double landerScreenX = lander.getX() - cameraX; // Lander's X relative to the current camera view
                double landerScreenY = lander.getY();           // Lander's Y (world coordinate, for zoom pivot)
                landerSpecificG.translate(landerScreenX, landerScreenY);
                landerSpecificG.scale(currentZoom, currentZoom);
                landerSpecificG.translate(-landerScreenX, -landerScreenY);
            }
            // Apply the camera's X translation to the lander's graphics context
            landerSpecificG.translate(-cameraX, 0);

            lander.render(landerSpecificG); // Render the lander
            landerSpecificG.dispose(); // Dispose of the lander's graphics context
        }
        worldG.dispose(); // Dispose of the main transformed graphics context

        // Restore original transform for HUD and other screen-space elements
        g2d.setTransform(originalScreenTransform);

        // --- Render HUD (Heads-Up Display) ---
        g2d.setFont(HUD_FONT);
        g2d.setColor(Color.WHITE);

        int hudLX = HUD_MARGIN_X; // Left X position for HUD
        int hudLY = HUD_MARGIN_Y + HUD_FONT.getSize(); // Starting Y for HUD text


        g2d.drawString("SCORE: " + cumulativeSessionScore, hudLX, hudLY); // Display cumulative score
        hudLY += HUD_LINE_SPACING; // Move to next line

        long timeValSeconds = (long) totalTimePlayedInStateSeconds;
        String timeFormatted = String.format("%02d:%02d", timeValSeconds / 60, timeValSeconds % 60); // Format time as MM:SS
        g2d.drawString("TIME: " + timeFormatted, hudLX, hudLY); // Display time
        hudLY += HUD_LINE_SPACING;

        if (lander != null) {
            g2d.drawString(String.format("FUEL: %.0f", lander.getFuel()), hudLX, hudLY); // Display fuel
        } else {
            g2d.drawString("FUEL: ---", hudLX, hudLY); // Display placeholder if lander is null
        }

        // Right-aligned HUD elements
        int hudRX = Game.DEFAULT_WIDTH - HUD_MARGIN_X; // Right X position for HUD
        hudLY = HUD_MARGIN_Y + HUD_FONT.getSize(); // Reset Y for right side

        String heightString = "HEIGHT: N/A";
        if (lander != null && (lander.getCurrentState() == Lander.State.PLAYER_CONTROL || lander.getCurrentState() == Lander.State.INITIAL_FLYOVER) && currentHeightAboveTerrain != Float.POSITIVE_INFINITY) {
            heightString = String.format("HEIGHT: %.0f", currentHeightAboveTerrain);
        }
        int textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth(heightString); // Get width for right alignment
        g2d.drawString(heightString, hudRX - textWidth, hudLY); // Display height
        hudLY += HUD_LINE_SPACING;

        if (lander != null) {
            String horSpeedText = String.format("HOR.SPEED: %.0f", lander.getVx());
            textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth(horSpeedText);
            g2d.drawString(horSpeedText, hudRX - textWidth, hudLY); // Display horizontal speed
            hudLY += HUD_LINE_SPACING;

            String verSpeedText = String.format("VER.SPEED: %.0f", lander.getVy());
            textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth(verSpeedText);
            g2d.drawString(verSpeedText, hudRX - textWidth, hudLY); // Display vertical speed
        } else {
            String na = "N/A";
            textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth("HOR.SPEED: " + na);
            g2d.drawString("HOR.SPEED: " + na, hudRX - textWidth, hudLY);
            hudLY += HUD_LINE_SPACING; //
            textWidth = g2d.getFontMetrics(HUD_FONT).stringWidth("VER.SPEED: " + na);
            g2d.drawString("VER.SPEED: " + na, hudRX - textWidth, hudLY);
        }

        // --- Render Game Over / Landing Messages ---
        if (attemptConcluded) { // If the landing attempt has finished
            FontMetrics fm;
            // Main message (e.g., "CRASHED!", "Perfect Landing!")
            g2d.setColor(Color.YELLOW);
            g2d.setFont(GAME_OVER_FONT_BIG);
            fm = g2d.getFontMetrics();
            textWidth = fm.stringWidth(landingMessageDisplay);
            g2d.drawString(landingMessageDisplay, (Game.DEFAULT_WIDTH - textWidth) / 2, Game.DEFAULT_HEIGHT / 2 - 60);

            // Score message (if not crashed)
            if (lander != null && lander.getCurrentState() != Lander.State.CRASHED) {
                String scoreText = "Score: " + lander.getCalculatedScore();
                g2d.setFont(GAME_OVER_FONT_MEDIUM);
                fm = g2d.getFontMetrics();
                textWidth = fm.stringWidth(scoreText);
                g2d.setColor(Color.GREEN);
                g2d.drawString(scoreText, (Game.DEFAULT_WIDTH - textWidth) / 2, Game.DEFAULT_HEIGHT / 2 - 10);
            }

            // Continue/Menu message
            g2d.setFont(GAME_OVER_FONT_SMALL);
            fm = g2d.getFontMetrics();
            String continueMsg;
            // Check if player can continue (has fuel)
            boolean canContinueWithEnter = lander != null && (lander.getCurrentState() == Lander.State.LANDED_GENTLE || lander.getCurrentState() == Lander.State.LANDED_HARD || lander.getCurrentState() == Lander.State.CRASHED) && // Can retry even on crash if fuel is not zero
                    lander.getFuel() > 0;

            if (canContinueWithEnter) {
                continueMsg = "Press ENTER to continue | ESC for menu";
            } else {
                continueMsg = "Press ESC to return to menu";
            }
            textWidth = fm.stringWidth(continueMsg);
            g2d.setColor(Color.LIGHT_GRAY);
            g2d.drawString(continueMsg, (Game.DEFAULT_WIDTH - textWidth) / 2, Game.DEFAULT_HEIGHT / 2 + 40);
        }
    }

    /**
     * Handles player input for the playing state.
     * Controls lander thrust and rotation.
     * Allows returning to the menu via Escape key.
     * Transitions lander from INITIAL_FLYOVER to PLAYER_CONTROL on first input.
     */
    @Override
    public void handleInput() {
        if (attemptConcluded) return; // No input processing if attempt is over

        // Handle Escape key to return to menu
        if (inputHandler.isEscJustPressed()) { //
            stateManager.setState(StateManager.StateType.MENU);
            return;
        }

        if (lander == null) return; // No lander to control

        boolean actionKeyPressed = false; // Flag to check if any control key was pressed
        int rotation = 0; // -1 for left, 1 for right, 0 for no rotation

        // Handle rotation input
        if (inputHandler.isActionActive(GameAction.ROTATE_LEFT)) {
            rotation = -1;
            actionKeyPressed = true;
        } else if (inputHandler.isActionActive(GameAction.ROTATE_RIGHT)) {
            rotation = 1;
            actionKeyPressed = true;
        }
        lander.setRotation(rotation);

        // Handle thrust input
        boolean thrusting = inputHandler.isActionActive(GameAction.THRUST);
        if (thrusting) actionKeyPressed = true;
        lander.setPlayerRequestsThrust(thrusting);

        // Transition lander to PLAYER_CONTROL state on first input if it's in INITIAL_FLYOVER
        if (actionKeyPressed && !playerControlTakenSinceEnter) {
            lander.playerHasTakenControl();
            playerControlTakenSinceEnter = true;
        }
    }

    /**
     * Called when this state is exited.
     * Stops any ongoing sounds (like the lander's engine) and resets lander controls.
     */
    @Override
    public void onExit() {
        System.out.println("Exiting PlayingState."); // Log state exit
        AudioManager.getInstance().stopSound(AudioManager.SoundEffect.ENGINE); // Stop engine sound
        if (lander != null) {
            lander.setPlayerRequestsThrust(false); // Ensure thrust is off
            lander.setRotation(0); // Stop any rotation
        }
    }
}