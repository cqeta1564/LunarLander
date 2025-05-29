// Game.java (Conceptual Outline)
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer; // For game loop
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Color;
import java.awt.event.*;
import java.util.List;


enum GameState {
    WAITING, PLAYING, LANDED, CRASHED, GAMEOVER
}

public class Game extends JPanel implements ActionListener {

    // Screen dimensions - will be dynamic from JFrame
    private int screenWidth = 800; // Initial default
    private int screenHeight = 600; // Initial default
    // private int halfWidth, halfHeight; // Can be calculated

    // Touchable detection is different in Java. Assume mouse/keyboard for now.
    // private TouchInput touchInput; // If using the conceptual TouchInput

    private static final int FPS = 60;
    private static final int MPF = 1000 / FPS; // Milliseconds per frame
    private long gameStartTime;
    private long gameLoopCounter; // Equivalent to 'counter' in JS loop for timing/frames

    private KeyInput keyInput;
    // Mouse input listeners can be added directly to the JPanel

    private String startMessage = "INSERT COINS<br><br>CLICK TO PLAY<br>ARROW KEYS TO MOVE";
    // singlePlayMode would be a boolean flag

    private GameState gameState;
    private int score;
    private long timeElapsedMs; // Game time in milliseconds

    private Lander lander;
    private Landscape landscape;
    private InfoDisplay infoDisplay;
    // private List<Vector2D> testPoints = new ArrayList<>(); // If needed

    // Viewport related
    private Viewport view;
    private boolean zoomedIn;
    private static final double ZOOM_FACTOR = 4.0; // Check usage in JS, might be dynamic

    private Timer timer; // Game loop timer

    //WebSocket related (conceptual)
    // private WebSocketClient webSocketClient;
    // private String wsID;


    public Game() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(Color.BLACK);

        this.keyInput = new KeyInput();
        addKeyListener(keyInput);
        // this.touchInput = new TouchInput(screenWidth); // Init if used
        // addMouseListener(touchInput);
        // addMouseMotionListener(touchInput);

        // For mouse position tracking, similar to JS onMouseMove
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // mouseX = e.getX(); // Store if needed globally
                // mouseY = e.getY();
                // Logic for showing/hiding cursor
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameState == GameState.WAITING) {
                    newGame();
                }
            }
        });


        setFocusable(true); // Important for KeyListener to work on JPanel
        requestFocusInWindow();

        // Component listener to handle window resizing
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                resizeGamePanel();
            }
        });

        initGameObjects();

        // Start game state
        gameState = GameState.GAMEOVER; // As per JS initial state
        restartLevel(); // This will set to WAITING and show message

        gameStartTime = System.currentTimeMillis();
        timer = new Timer(MPF, this);
        timer.start();
    }

    private void resizeGamePanel() {
        screenWidth = getWidth();
        screenHeight = getHeight();
        // halfWidth = screenWidth / 2;
        // halfHeight = screenHeight / 2;

        if (view != null) {
            // Re-initialize or update viewport with new screen dimensions
            // This ensures view calculations are correct after resize
            // The original JS setZoom also re-calculates view based on SCREEN_WIDTH/HEIGHT
            // So, create a new viewport or update its screen dimensions and then call setZoom.
            view = new Viewport(screenWidth, screenHeight); // Recreate or update
            setZoom(zoomedIn); // Re-apply zoom with new screen dimensions
        }
        if (infoDisplay != null) {
            infoDisplay.arrangeBoxes(screenWidth, screenHeight);
        }
        // if (touchInput != null) touchInput.updateScreenWidth(screenWidth);

        // Stats display position update if using one
    }


    private void initGameObjects() {
        this.lander = new Lander();
        this.landscape = new Landscape();
        this.view = new Viewport(screenWidth, screenHeight); // Initialize viewport
        this.infoDisplay = new InfoDisplay(screenWidth, screenHeight);
        // Add infoDisplay as a component if it's a JPanel, or handle its drawing in paintComponent
        // this.add(infoDisplay); // If InfoDisplay extends JPanel and uses JLabels
        // infoDisplay.setBounds(0,0, screenWidth, screenHeight); // If using null layout for main Game panel
        // For custom drawing InfoDisplay, no need to add as component.

        // Key listeners for lander control (original JS way)
        keyInput.addKeyDownListener(KeyInput.UP, () -> { if(gameState == GameState.PLAYING) lander.thrust(1); });
        keyInput.addKeyUpListener(KeyInput.UP, () -> lander.thrust(0));

        setZoom(false); // Initial zoom state
    }

    // Game Loop - called by Timer
    @Override
    public void actionPerformed(ActionEvent e) {
        gameLoopCounter++;
        // Skipped frames logic from JS is complex and related to browser rendering.
        // In Java Swing with Timer, each tick is an attempt to update and render.
        // If updates take too long, rendering might stutter but frames aren't "skipped"
        // in the same way. For very high-precision timing, a manual thread-based loop
        // with System.nanoTime() is often preferred over javax.swing.Timer.

        updateGame();
        repaint(); // Triggers paintComponent
    }

    private void updateGame() {
        // Update game time
        // timeElapsedMs = System.currentTimeMillis() - gameStartTime; // If using absolute time
        // Or, if 'counter' * MPF is the canonical game time:
        timeElapsedMs = gameLoopCounter * MPF;


        if (gameState == GameState.PLAYING) {
            checkKeys(); // Handle continuous key presses for rotation
            // Touch controls would be processed here if implemented
            // if (touchInput != null && touchInput.active) {
            //    handleTouchInput();
            // }
        }

        lander.update();
        Landscape.incrementCounter(); // For landscape animations/flicker

        // if ((gameLoopCounter % 6) == 0 && webSocketClient != null && gameState == GameState.PLAYING) {
        //    sendPosition();
        // }

        if ((gameState == GameState.WAITING) && (lander.altitude < 100)) {
            // gameState = GameState.GAMEOVER; // This logic might be too aggressive
            // restartLevel();
        }

        if (gameState == GameState.PLAYING || gameState == GameState.WAITING) {
            checkCollisions();
        }

        updateView();
        updateTextInfo();

        // Mouse hide logic (can be done with setCursor)
    }

    private void checkKeys() {
        if (keyInput.isKeyDown(KeyInput.LEFT)) {
            lander.rotate(-1);
        } else if (keyInput.isKeyDown(KeyInput.RIGHT)) {
            lander.rotate(1);
        }
        // if (keyInput.isKeyDown(KeyEvent.VK_A)) { // AbortKey from JS
        //    lander.abort();
        // }
        // if (keyInput.isKeyDown(KeyEvent.VK_S)) { // Speed mode
        //    for(int i=0; i<3;i++) lander.update();
        // }
    }

    // private void handleTouchInput() {
    //     if (touchInput.rightTouch.touching) {
    //         double thrustPower = GameUtils.map(
    //             touchInput.rightTouch.getY(),
    //             screenHeight * 0.9, // touchThrustBottom from JS
    //             screenHeight * 0.25, // touchThrustTop from JS
    //             0, 1, true);
    //         lander.thrust(thrustPower);
    //     } else {
    //         lander.thrust(0);
    //     }

    //     if (touchInput.leftTouch.touching) {
    //         // Simplified rotation, JS has more complex startAngle logic
    //         // touchRotateStartAngle = lander.rotation when touch begins
    //         // This is a placeholder for direct control:
    //         double touchAngle = GameUtils.map(
    //             touchInput.leftTouch.getXOffset(),
    //             screenWidth * 0.2 * -0.5, // touchRotateRange
    //             screenWidth * 0.2 * 0.5,
    //             -90, 90, false); // false: don't clamp intermediate angle yet
    //         // lander.setRotation(touchRotateStartAngle + touchAngle); // Needs startAngle
    //         // Temporary:
    //         if(touchInput.leftTouch.getXOffset() < -10) lander.rotate(-1);
    //         if(touchInput.leftTouch.getXOffset() > 10) lander.rotate(1);


    //     }
    // }


    private void updateView() {
        double marginX = screenWidth * 0.2;
        double marginTop = screenHeight * 0.2;
        double marginBottom = screenHeight * 0.3;

        if ((!zoomedIn) && (lander.altitude < 70)) {
            setZoom(true);
        } else if ((zoomedIn) && (lander.altitude > 160)) {
            setZoom(false);
        }

        double currentViewScale = view.scale; // view.scale should be authoritative

        // Lander's screen position without view adjustment: lander.pos.x * currentViewScale
        // View's current X translation: view.xTranslate
        // Lander's effective screen X: lander.pos.x * currentViewScale + view.xTranslate

        if (((lander.pos.x * currentViewScale) + view.xTranslate < marginX)) {
            view.xTranslate = -(lander.pos.x * currentViewScale) + marginX;
        } else if (((lander.pos.x * currentViewScale) + view.xTranslate > screenWidth - marginX)) {
            view.xTranslate = -(lander.pos.x * currentViewScale) + screenWidth - marginX;
        }

        if (((lander.pos.y * currentViewScale) + view.yTranslate < marginTop)) {
            view.yTranslate = -(lander.pos.y * currentViewScale) + marginTop;
        } else if (((lander.pos.y * currentViewScale) + view.yTranslate > screenHeight - marginBottom)) {
            view.yTranslate = -(lander.pos.y * currentViewScale) + screenHeight - marginBottom;
        }

        view.updateBounds(); // Recalculate view's world coordinates based on new translation/scale
    }

    private void setZoom(boolean zoom) {
        if (zoom) {
            // Target a fixed world height (e.g., 700 units in JS was scaled to screen height,
            // then zoomed by 5x).
            // So, effective world height to show is 700/5 = 140 units.
            // view.scale = screenHeight / 140.0; // Example, match JS: SCREEN_HEIGHT/700*5
            view.scale = (double)screenHeight / 700.0 * 5.0;


            zoomedIn = true;
            // Center lander with some offset
            view.xTranslate = -lander.pos.x * view.scale + (screenWidth / 2.0);
            view.yTranslate = -lander.pos.y * view.scale + (screenHeight * 0.25); // Top 25% for lander
            lander.scale = 0.25;
        } else {
            // view.scale = screenHeight / 700.0; // Show 700 world units vertically
            view.scale = (double)screenHeight / 700.0;
            zoomedIn = false;
            lander.scale = 0.6; // Default lander visual scale
            view.xTranslate = 0; // Or center initial view if desired
            view.yTranslate = 0;
        }
        view.updateBounds(); // Crucial after changing scale or translation
    }

    private void checkCollisions() {
        List<LandscapeLine> landscapeLines = landscape.lines;
        // Lander's world coordinates for its feet (bottom corners)
        // Need to consider lander's rotation for accurate collision points if it's not upright.
        // The JS code uses lander.left, lander.right, lander.bottom which are AABB.
        // For precise collision with rotated lander, you'd transform lander's corner vertices.
        // JS approach (using AABB 'left' and 'right' for horizontal check):

        // Effective lander collision points (bottom corners) in world space
        // This simplified version assumes lander.bottomLeft/Right are already in world space.
        // If lander graphic is rotated, these points need to be calculated based on pos, rotation, and scaled size.
        // For now, let's assume lander.left/right/bottom are world AABB from lander.update()

        double landerLeftEdge = lander.pos.x - (10 * lander.scale); // A simple AABB approach
        double landerRightEdge = lander.pos.x + (10 * lander.scale);
        double landerBottomEdge = lander.pos.y + (14 * lander.scale);


        // Tiling: adjust lander's horizontal position for collision checking if landscape tiles
        double effectiveLanderLeft = landerLeftEdge;
        double effectiveLanderRight = landerRightEdge;
        if (landscape.tileWidth > 0) {
            effectiveLanderLeft = landerLeftEdge % landscape.tileWidth;
            effectiveLanderRight = landerRightEdge % landscape.tileWidth;
            while (effectiveLanderRight < 0) { // Ensure positive for modulo consistency with JS
                effectiveLanderRight += landscape.tileWidth;
                effectiveLanderLeft += landscape.tileWidth;
            }
            // Adjust so that effectiveLanderLeft is also in the primary tile range if right wrapped
            if (effectiveLanderRight < effectiveLanderLeft && landerRightEdge > landerLeftEdge) { // Wrapped around
                // This case means the lander spans the tile boundary.
                // Collision check needs to be done twice or with adjusted coordinates.
                // Simpler: check against lines in current tile and potentially next one.
                // For now, this simplified modulo might not be perfectly robust for objects wider than tile gaps.
            }
        }


        lander.altitude = Double.POSITIVE_INFINITY; // Reset altitude

        for (LandscapeLine line : landscapeLines) {
            line.checked = false; // Reset for debug or multi-pass checks

            // Broad phase: Check if lander's AABB x-range overlaps line's x-range
            // This uses the modulo-adjusted effectiveLanderLeft/Right for tiled landscape
            if (!((effectiveLanderRight < line.p1.x && effectiveLanderRight < line.p2.x) ||
                    (effectiveLanderLeft > line.p1.x && effectiveLanderLeft > line.p2.x))) {
                // More precise horizontal overlap:
                double lineMinX = Math.min(line.p1.x, line.p2.x);
                double lineMaxX = Math.max(line.p1.x, line.p2.x);
                if (!(effectiveLanderRight < lineMinX || effectiveLanderLeft > lineMaxX)) {


                    // Calculate altitude relative to this line segment
                    // This is simplified; true altitude is vertical distance to line segment below.
                    // For horizontal lines, it's simpler:
                    if (line.landable) { // Horizontal line
                        lander.altitude = Math.min(lander.altitude, line.p1.y - landerBottomEdge);
                    }
                    line.checked = true;


                    if (line.landable) {
                        if (landerBottomEdge >= line.p1.y - 1.0) { // -1.0 tolerance for landing
                            // Check if lander is horizontally within the landing pad
                            // Using lander's original AABB for this check, not modulo.
                            if ((landerLeftEdge > line.p1.x) && (landerRightEdge < line.p2.x)) {
                                if ((Math.abs(lander.rotation) < 1.0) && (lander.vel.y < 0.15 && lander.vel.y >= 0)) { // Rotation check (near 0)
                                    setLanded(line);
                                    return; // Collision processed
                                } else {
                                    setCrashed();
                                    return; // Collision processed
                                }
                            } else { // Not fully within the horizontal extent of the pad
                                setCrashed();
                                return; // Collision processed
                            }
                        }
                    } else { // Sloped line
                        // Check collision with sloped line if lander's bottom is below it
                        // This requires Point-Line collision test.
                        // JS uses pointIsLessThanLine for lander's bottom corners.
                        // Lander's bottom corners in world space:
                        // Vector2D bL = new Vector2D(landerLeftEdge, landerBottomEdge);
                        // Vector2D bR = new Vector2D(landerRightEdge, landerBottomEdge);
                        // These are AABB corners. For rotated lander, calculate actual rotated foot positions.
                        // For simplicity, using the AABB corners:
                        Vector2D worldBottomLeft = lander.bottomLeft; // Assuming these are correctly updated world coords
                        Vector2D worldBottomRight = lander.bottomRight;

                        // Adjust worldBottomLeft/Right for tiling if using them with line.p1/p2 directly
                        Vector2D checkBottomLeft = worldBottomLeft.clone();
                        Vector2D checkBottomRight = worldBottomRight.clone();
                        if (landscape.tileWidth > 0) {
                            // This is complex. If the line is in tile X, but lander feet are in tile X-1 due to modulo.
                            // The original JS seems to compare modulo-ed lander X with non-modulo line X,
                            // which implies lines are considered only in the first tile, and lander wraps.
                            // Let's assume line coordinates are in the 'base' tile (0 to tileWidth).
                            // And lander's effectiveLeft/Right are for that base tile.
                            // So, the bottomLeft/Right for collision need to be consistent.
                            checkBottomLeft.x = effectiveLanderLeft; // This is likely incorrect logic mixing.
                            checkBottomRight.x = effectiveLanderRight; // This maps AABB to the tile segment.

                            // A more robust way: iterate landscape tiles that overlap the lander.
                            // For now, stick to the simpler JS logic as close as possible.
                            // The JS logic: `lander.bottomRight.x = right; lander.bottomLeft.x = left;`
                            // where `right` and `left` are modulo-adjusted. So the `pointIsLessThanLine`
                            // gets these modulo-adjusted X values.
                            worldBottomLeft.x = effectiveLanderLeft; // Correctly mirroring JS logic
                            worldBottomRight.x = effectiveLanderRight;
                        }


                        if (landerBottomEdge > Math.min(line.p1.y, line.p2.y) - 1.0) { // Quick check: is lander generally low enough?
                            if (pointIsLessThanLine(worldBottomLeft, line.p1, line.p2) ||
                                    pointIsLessThanLine(worldBottomRight, line.p1, line.p2)) {
                                setCrashed();
                                return; // Collision processed
                            }
                        }
                    }
                }
            }
        }
        // If altitude still infinity, it means no ground detected below (e.g. very high up)
        if (Double.isInfinite(lander.altitude)) {
            // Handle case where lander is above all terrain, altitude could be distance to nearest point,
            // or simply capped. JS seems to set it if overlapping a line horizontally.
            // If no overlap, altitude isn't explicitly set further.
        }
    }

    // From JS: checks if point.y is at or below the line segment at point.x
    private boolean pointIsLessThanLine(Vector2D point, Vector2D lineP1, Vector2D lineP2) {
        // If line is vertical
        if (Math.abs(lineP2.x - lineP1.x) < 0.001) {
            if (point.x >= Math.min(lineP1.x, lineP2.x) - 0.5 && point.x <= Math.max(lineP1.x, lineP2.x) + 0.5) { // Within x-range of vertical line
                return point.y >= Math.min(lineP1.y, lineP2.y) && point.y <= Math.max(lineP1.y, lineP2.y); // Check if point is within y-range
            }
            return false;
        }

        // Calculate how far 'point.x' is along the line segment in terms of x-proportion
        double dist = (point.x - lineP1.x) / (lineP2.x - lineP1.x);

        // If point.x is within the horizontal span of the line segment
        if (dist >= 0 && dist <= 1) {
            // Calculate the y-coordinate of the line at point.x
            double yHitPoint = lineP1.y + ((lineP2.y - lineP1.y) * dist);
            return point.y >= yHitPoint - 0.5; // -0.5 for a small tolerance
        }
        return false; // Point.x is outside the line segment's horizontal span
    }


    private void setLanded(LandscapeLine line) {
        if (gameState == GameState.PLAYING) { // Prevent multiple land/crash calls
            lander.land();
            int points = 0;
            String msg;
            if (lander.vel.y < 0.075) { // Gentle landing
                points = 50 * line.multiplier;
                msg = "CONGRATULATIONS<br>A PERFECT LANDING<br>" + points + " POINTS";
                lander.fuel += 50; // Bonus fuel
            } else { // Hard landing
                points = 15 * line.multiplier;
                msg = "YOU LANDED HARD<br>YOU ARE HOPELESSLY MAROONED<br>" + points + " POINTS";
                lander.makeBounce();
            }
            score += points;
            infoDisplay.showGameInfo(msg.replace("<br>", "\n")); // Replace <br> for JLabel if it doesn't support HTML
            gameState = GameState.LANDED;
            // if (singlePlayMode) setGameOver();
            // sendLanded(); // WebSocket
            scheduleRestart();
        }
    }

    private void setCrashed() {
        if (gameState == GameState.PLAYING) { // Prevent multiple land/crash calls
            lander.crash();
            // SoundManager.playExplosion(); // Placeholder

            int fuelLost = (int) GameUtils.randomRange(200, 400);
            lander.fuel -= fuelLost;
            String msg;

            if (lander.fuel < 1) {
                setGameOver(); // This will change gameState
                msg = "OUT OF FUEL<br><br>GAME OVER";
            } else {
                double rnd = Math.random();
                String crashMsg;
                if (rnd < 0.3) crashMsg = "YOU JUST DESTROYED A 100 MEGABUCK LANDER";
                else if (rnd < 0.6) crashMsg = "DESTROYED";
                else crashMsg = "YOU CREATED A TWO MILE CRATER";
                msg = "AUXILIARY FUEL TANKS DESTROYED<br>" + fuelLost + " FUEL UNITS LOST<br><br>" + crashMsg;
                gameState = GameState.CRASHED;
                // if (singlePlayMode) setGameOver();
            }
            infoDisplay.showGameInfo(msg.replace("<br>", "\n"));
            // sendCrashed(); // WebSocket
            scheduleRestart();
        }
    }

    private void setGameOver() {
        gameState = GameState.GAMEOVER;
        // sendGameOver(); // WebSocket
        // Potentially show a different message or wait for restart command
        // In JS, restartLevel is called which might show start message if GAMEOVER
    }

    private void newGame() {
        lander.fuel = 1000;
        timeElapsedMs = 0;
        gameLoopCounter = 0; // Reset game loop counter for time
        score = 0;
        gameStartTime = System.currentTimeMillis(); // Reset overall game timer if used

        restartLevel(); // This will set gameState to PLAYING (or WAITING then PLAYING)
    }

    private Timer restartTimer;
    private void scheduleRestart() {
        if (restartTimer != null && restartTimer.isRunning()) {
            restartTimer.stop();
        }
        restartTimer = new Timer(4000, ae -> restartLevel());
        restartTimer.setRepeats(false);
        restartTimer.start();
    }

    private void restartLevel() {
        lander.reset();
        landscape.setZones(); // Reset landing zone multipliers
        setZoom(false);       // Reset zoom

        if (gameState == GameState.GAMEOVER || gameState == GameState.WAITING) { // If restarting from game over or initial wait
            gameState = GameState.WAITING;
            showStartMessage();
            lander.vel.reset(0.2,0); // Slower initial drift for WAITING
        } else { // Restarting after a crash or successful landing
            gameState = GameState.PLAYING;
            // sendRestart(); // WebSocket
            infoDisplay.hideGameInfo();
        }
    }

    private void updateTextInfo() {
        infoDisplay.updateBoxIntByName("score", score, 4);
        infoDisplay.updateBoxIntByName("fuel", (int)Math.floor(lander.fuel), 4);

        if (gameState == GameState.PLAYING) {
            infoDisplay.updateBoxTimeByName("time", timeElapsedMs);
        }

        infoDisplay.updateBoxIntByName("alt", (lander.altitude < 0 || Double.isInfinite(lander.altitude)) ? 0 : (int)Math.floor(lander.altitude), 4);
        infoDisplay.updateBoxIntByName("horizSpeed", (int)Math.floor(lander.vel.x * 200), 0); // No padding needed as per JS
        infoDisplay.updateBoxIntByName("vertSpeed", (int)Math.floor(lander.vel.y * 200), 0);

        if ((lander.fuel < 300) && (gameState == GameState.PLAYING)) {
            if ((gameLoopCounter % 50) < 30) { // Flashing message
                if (lander.fuel <= 0) {
                    infoDisplay.showGameInfo("Out of fuel");
                } else {
                    infoDisplay.showGameInfo("Low on fuel");
                }
                // if (playBeep) SoundManager.playBeep(); // Placeholder
            } else {
                infoDisplay.hideGameInfo();
            }
        }
    }

    private void showStartMessage() {
        infoDisplay.showGameInfo(startMessage.replace("<br>", "\n"));
    }

    // Rendering - called by repaint()
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g); // Clears the panel
        Graphics2D g2d = (Graphics2D) g.create(); // Work on a copy

        // Apply view transformations (translation and scale)
        g2d.translate(view.xTranslate, view.yTranslate);
        g2d.scale(view.scale, view.scale);

        // Render landscape first
        if (landscape != null) {
            landscape.render(g2d, view); // Pass the transformed g2d and view
        }

        // Render lander
        if (lander != null) {
            // Lander's render method should save/restore transform or apply its own on top of g2d's
            // Create a new graphics context for the lander to isolate transformations
            Graphics2D landerG2d = (Graphics2D) g2d.create();
            lander.render(landerG2d, view.scale); // Pass view.scale for line thickness adjustments
            landerG2d.dispose();
        }

        // Render other game objects (e.g., other players if multiplayer)

        g2d.dispose(); // Dispose of the graphics copy for view transform

        // Render UI elements (InfoDisplay) on top, without view transformations
        // If InfoDisplay is a JPanel added to this Game panel, it will be drawn automatically.
        // If InfoDisplay draws itself directly:
        if (infoDisplay != null && !(infoDisplay.getParent() == this)) { // Only if not already a child component
            // This is tricky if InfoDisplay uses JLabels. Better to add it as a child.
            // For custom paint:
            // Graphics2D uiG2d = (Graphics2D) g.create();
            // infoDisplay.customRenderMethod(uiG2d, screenWidth, screenHeight);
            // uiG2d.dispose();
        }

        // Render touch controller debug if implemented
        // if (touchInput != null && touchInput.active && gameState == GameState.PLAYING) {
        //    Graphics2D touchG2d = (Graphics2D) g.create();
        //    touchInput.render(touchG2d);
        //    touchG2d.dispose();
        // }
    }

    // Main method to run the game
    public static void main(String[] args) {
        JFrame frame = new JFrame("Lander Game Java");
        Game gamePanel = new Game();
        frame.add(gamePanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack(); // Sizes frame to panel's preferred size
        frame.setLocationRelativeTo(null); // Center on screen
        frame.setVisible(true);
        gamePanel.requestFocusInWindow(); // Ensure panel has focus for input
    }

    // WebSocket methods (conceptual)
    // private void initWebSocket() { /* ... */ }
    // private void sendObject(Object obj) { /* ... */ }
    // private void sendPosition() { /* ... */ }
    // ... other send methods ...
}