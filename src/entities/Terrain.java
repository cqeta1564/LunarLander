package entities;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages the generation, transformation, and rendering of the game's terrain.
 * The terrain is based on a repeating pattern defined by raw data points and
 * can include randomly placed landing pads.
 */
public class Terrain {
    // --- Terrain Generation Parameters ---
    /**
     * Factor to shrink the terrain pattern along the X-axis. A value of 1 means no shrink.
     */
    private static final float X_AXIS_SHRINK_FACTOR = 1f;
    /**
     * Factor to shrink the terrain pattern along the Y-axis (historically, but now effectively
     * used in scaling calculations). A value of 2.0 means the raw Y range will be mapped
     * into a smaller screen Y range if not for TERRAIN_VIEWPORT ratios.
     * Current implementation maps raw Y data to a specific viewport, so this factor's
     * direct meaning as a "shrink" is less prominent.
     */
    private static final float Y_AXIS_SHRINK_FACTOR = 2.0f; // Less direct impact due to viewport mapping
    /**
     * Probability (0.0 to 1.0) that a predefined landing pad will spawn in a pattern cycle.
     */
    private static final double PAD_SPAWN_PROBABILITY = 0.6;
    /**
     * Ratio of screen height defining the top Y-coordinate of the terrain's display area.
     */
    private static final float TERRAIN_VIEWPORT_TOP_Y_RATIO = 0.40f;
    /**
     * Ratio of screen height defining the bottom Y-coordinate of the terrain's display area.
     */
    private static final float TERRAIN_VIEWPORT_BOTTOM_Y_RATIO = 0.95f;

    /**
     * Raw string data representing the X,Y coordinates of the base terrain pattern.
     * Format: "x1,y1 x2,y2 ..."
     */
    private static final String RAW_TERRAIN_DATA = "0,1429 45,1429 60,1367 60,1340 76,1340 83,1315 98,1299 105,1287 108,1276 118,1261 134,1261 148,1204 163,1198 178,1211 207,1211 222,1198 228,1169 237,1140 253,1134 254,1122 266,1108 280,1096 298,1096 301,1108 316,1109 318,1122 327,1122 330,1160 341,1197 342,1213 348,1236 372,1249 379,1279 385,1289 399,1300 416,1300 429,1289 437,1277 444,1266 455,1254 465,1244 475,1236 481,1224 496,1210 501,1198 517,1192 532,1174 548,1169 560,1159 577,1159 592,1163 606,1173 609,1184 620,1198 625,1211 635,1224 637,1236 649,1250 666,1249 676,1225 686,1218 698,1224 709,1236 716,1262 737,1275 742,1316 752,1351 752,1366 766,1377 796,1378 812,1366 826,1353 833,1339 848,1327 854,1315 869,1308 884,1251 899,1244 909,1234 917,1225 924,1216 930,1207 936,1196 943,1185 951,1159 965,1147 972,1134 976,1123 986,1109 1003,1108 1017,1096 1025,1083 1031,1073 1045,1058 1060,997 1061,962 1064,941 1073,916 1074,910 1089,903 1095,889 1103,877 1119,877 1122,890 1138,890 1142,904 1149,904 1159,909 1165,916 1170,922 1178,947 1201,962 1221,1017 1237,1022 1252,1032 1254,1044 1266,1058 1269,1069 1279,1082 1296,1083 1298,1123 1309,1158 1310,1173 1324,1173 1328,1186 1339,1196 1342,1209 1344,1216 1346,1223 1349,1228 1353,1235 1369,1249 1372,1289 1382,1325 1382,1340 1386,1351 1397,1365 1413,1365 1419,1390 1425,1399 1456,1428 1573,1428 1582,1390 1596,1366 1603,1339 1611,1339 1622,1314 1634,1304 1647,1288 1663,1283 1675,1274 1691,1274 1714,1333 1716,1345 1734,1362 1738,1364 1748,1376 1756,1403 1778,1417 1840,1419 1867,1428 1879,1429";

    // --- Instance Variables ---
    /**
     * List of points defining the base terrain pattern after scaling and transformation to screen coordinates.
     */
    private final List<Point> basePatternScreenPoints;
    /**
     * List of landing pad definitions within the base terrain pattern.
     */
    private final List<LandingPad.PatternPadDefinition> basePatternPadDefinitions;
    /**
     * List of current terrain surface points visible on screen (after camera offset).
     */
    private final List<Point> terrainSurfacePoints;
    /**
     * List of currently active LandingPad objects on screen.
     */
    private final List<LandingPad> landingPads;
    /**
     * Polygon representing the current visible terrain surface for collision detection.
     */
    private final Polygon terrainPolygon;
    /**
     * Width of the game screen.
     */
    private final int screenWidth;
    /**
     * Height of the game screen.
     */
    private final int screenHeight;
    /**
     * The width of one full cycle of the terrain pattern after all transformations (in screen pixels).
     */
    private float finalPatternCycleWidth;
    /**
     * Minimum Y screen coordinate of the transformed base pattern (for debugging/info).
     */
    private float finalPatternMinScreenY_DEBUG;
    /**
     * Maximum Y screen coordinate of the transformed base pattern (for debugging/info).
     */
    private float finalPatternMaxScreenY_DEBUG;

    /**
     * Constructs a new Terrain object.
     *
     * @param screenWidth  The width of the game screen.
     * @param screenHeight The height of the game screen.
     */
    public Terrain(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;

        this.basePatternScreenPoints = new ArrayList<>();
        this.basePatternPadDefinitions = new ArrayList<>();
        this.terrainSurfacePoints = new ArrayList<>();
        this.landingPads = new ArrayList<>();
        this.terrainPolygon = new Polygon();

        System.out.println("--- Terrain Constructor (Y-inverted for screen, X-Shrink: " + X_AXIS_SHRINK_FACTOR + ") ---");
        System.out.println("ScreenW: " + screenWidth + ", ScreenH: " + screenHeight);

        parseAndTransformBasePattern(); // Parse raw data and create the base transformed pattern
        populateVisibleTerrain(0, this.screenWidth); // Initial population of visible terrain

        System.out.println("Final pattern cycle width on screen: " + String.format("%.2f", finalPatternCycleWidth));
        System.out.println("Final pattern Y range on screen (DEBUG): " + String.format("%.2f", finalPatternMinScreenY_DEBUG) + " to " + String.format("%.2f", finalPatternMaxScreenY_DEBUG));
        System.out.println("--- Terrain Constructor END ---");
    }

    /**
     * Parses the {@link #RAW_TERRAIN_DATA}, normalizes it, and transforms it into
     * the {@link #basePatternScreenPoints} and {@link #basePatternPadDefinitions}.
     * This method establishes the fundamental shape and properties of the repeating terrain pattern.
     * Y-coordinates are transformed to fit within the defined screen viewport.
     */
    private void parseAndTransformBasePattern() {
        System.out.println("--- parseAndTransformBasePattern START (Y inverted for screen) ---");
        List<PointData> tempParsedData = new ArrayList<>(); // Temporary list for raw parsed points
        String[] pairs = RAW_TERRAIN_DATA.split(" "); // Split into "x,y" pairs
        float minRawX = Float.MAX_VALUE, maxRawX = Float.MIN_VALUE;
        float minRawY = Float.MAX_VALUE, maxRawY = Float.MIN_VALUE;

        // Parse raw data and find min/max for normalization
        for (String pair : pairs) {
            String[] coords = pair.split(",");
            if (coords.length == 2) {
                try {
                    int x = Integer.parseInt(coords[0]);
                    int y = Integer.parseInt(coords[1]);
                    tempParsedData.add(new PointData(x, y));
                    if (x < minRawX) minRawX = x;
                    if (x > maxRawX) maxRawX = x;
                    if (y < minRawY) minRawY = y;
                    if (y > maxRawY) maxRawY = y;
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing terrain data: " + pair);
                }
            }
        }

        if (tempParsedData.isEmpty()) {
            System.err.println("Error: No data loaded for terrain!");
            this.finalPatternCycleWidth = 0;
            return;
        }

        // Normalize X coordinates of raw data to start from 0
        for (PointData pd : tempParsedData) {
            pd.x -= (int) minRawX;
        }
        maxRawX -= minRawX; // This is now the original pattern width in raw units
        // minRawX = 0; // Already effectively 0

        float originalPatternWidthRaw = maxRawX;
        this.finalPatternCycleWidth = originalPatternWidthRaw / X_AXIS_SHRINK_FACTOR; // Scaled width on screen
        System.out.println("Original pattern raw width (normalized X): " + originalPatternWidthRaw + ", Scaled screen width: " + this.finalPatternCycleWidth);
        System.out.println("Original raw Y range (minRawY to maxRawY): " + minRawY + " to " + maxRawY);

        // Calculate target Y screen coordinates for the terrain viewport
        float targetScreenTopY = screenHeight * TERRAIN_VIEWPORT_TOP_Y_RATIO;
        float targetScreenBottomY = screenHeight * TERRAIN_VIEWPORT_BOTTOM_Y_RATIO;
        float targetScreenDisplayHeight = targetScreenBottomY - targetScreenTopY;
        if (targetScreenDisplayHeight <= 0) targetScreenDisplayHeight = 1; // Avoid division by zero
        float rawYRange = maxRawY - minRawY;
        if (rawYRange <= 0) rawYRange = 1; // Avoid division by zero

        System.out.println("Target Y band on screen: " + targetScreenTopY + " to " + targetScreenBottomY + " (height: " + targetScreenDisplayHeight + ")");

        basePatternScreenPoints.clear();
        this.finalPatternMinScreenY_DEBUG = Float.MAX_VALUE;
        this.finalPatternMaxScreenY_DEBUG = Float.MIN_VALUE;

        // Transform raw points to base screen pattern points
        for (PointData rawPD : tempParsedData) {
            int screenX = (int) (rawPD.x / X_AXIS_SHRINK_FACTOR); // Apply X scaling

            // Normalize raw Y to 0-1 range, then map to screen viewport
            // Note: Raw Y data usually has smaller values higher up (like cartesian),
            // screen Y has smaller values at the top. This transformation handles it.
            // If raw Y is inverted (larger Y is lower), then (maxRawY - rawPD.y) might be needed.
            // Assuming raw Y is like screen Y (larger Y is lower on image/data source):
            float normalizedRawY = (rawPD.y - minRawY) / rawYRange;
            // Screen Y: top of viewport + scaled position within viewport height
            int screenY = (int) (targetScreenTopY + (normalizedRawY * targetScreenDisplayHeight));

            basePatternScreenPoints.add(new Point(screenX, screenY));

            // Track min/max Y for debugging
            if (screenY < this.finalPatternMinScreenY_DEBUG) this.finalPatternMinScreenY_DEBUG = screenY;
            if (screenY > this.finalPatternMaxScreenY_DEBUG) this.finalPatternMaxScreenY_DEBUG = screenY;
        }

        // --- Define Landing Pad patterns within the base terrain data ---
        // For each pad, its Y coordinate also needs to be transformed like terrain points.
        basePatternPadDefinitions.clear();
        float pA_normY = (1096f - minRawY) / rawYRange; // Raw Y of pad A, normalized
        float pA_screenY = targetScreenTopY + (pA_normY * targetScreenDisplayHeight);
        basePatternPadDefinitions.add(new LandingPad.PatternPadDefinition((280 / X_AXIS_SHRINK_FACTOR), (298 / X_AXIS_SHRINK_FACTOR), // Scaled X coords
                1096, 10, pA_screenY)); // Raw Y, Multiplier, Transformed Screen Y

        float pB_normY = (877f - minRawY) / rawYRange;
        float pB_screenY = targetScreenTopY + (pB_normY * targetScreenDisplayHeight);
        basePatternPadDefinitions.add(new LandingPad.PatternPadDefinition((1103 / X_AXIS_SHRINK_FACTOR), (1119 / X_AXIS_SHRINK_FACTOR), 877, 10, pB_screenY));

        float pC_normY = (1428f - minRawY) / rawYRange;
        float pC_screenY = targetScreenTopY + (pC_normY * targetScreenDisplayHeight);
        basePatternPadDefinitions.add(new LandingPad.PatternPadDefinition((1456 / X_AXIS_SHRINK_FACTOR), (1573 / X_AXIS_SHRINK_FACTOR), 1428, 2, pC_screenY));

        float rawY_P2 = 1211f;
        float normY_P2 = (rawY_P2 - minRawY) / rawYRange;
        float screenY_P2 = targetScreenTopY + (normY_P2 * targetScreenDisplayHeight);
        basePatternPadDefinitions.add(new LandingPad.PatternPadDefinition((178 / X_AXIS_SHRINK_FACTOR), (207 / X_AXIS_SHRINK_FACTOR), (int) rawY_P2, 4, screenY_P2));

        float rawY_P6 = 890f;
        float normY_P6 = (rawY_P6 - minRawY) / rawYRange;
        float screenY_P6 = targetScreenTopY + (normY_P6 * targetScreenDisplayHeight);
        basePatternPadDefinitions.add(new LandingPad.PatternPadDefinition((1122 / X_AXIS_SHRINK_FACTOR), (1138 / X_AXIS_SHRINK_FACTOR), (int) rawY_P6, 6, screenY_P6));

        float rawY_P4 = 1300f;
        float normY_P4 = (rawY_P4 - minRawY) / rawYRange;
        float screenY_P4 = targetScreenTopY + (normY_P4 * targetScreenDisplayHeight);
        basePatternPadDefinitions.add(new LandingPad.PatternPadDefinition((399 / X_AXIS_SHRINK_FACTOR), (416 / X_AXIS_SHRINK_FACTOR), (int) rawY_P4, 3, screenY_P4));

        System.out.println("Pad A (screen Y - transformed): " + pA_screenY);
        System.out.println("Pad B (screen Y - transformed, should be higher in band): " + pB_screenY);
        System.out.println("Pad C (screen Y - transformed, should be lower in band): " + pC_screenY);
        System.out.println("--- parseAndTransformBasePattern END ---");
    }


    /**
     * Populates the {@link #terrainSurfacePoints} and {@link #landingPads} lists
     * with actual screen elements based on the current camera position (worldXOffset)
     * and view width. This method generates the visible portion of the repeating terrain.
     *
     * @param worldXOffset The current X offset of the camera in world coordinates.
     *                     This determines which part of the infinite terrain is visible.
     * @param viewWidth    The width of the visible screen area (typically screenWidth).
     */
    public void populateVisibleTerrain(float worldXOffset, int viewWidth) {
        terrainSurfacePoints.clear();
        landingPads.clear();
        terrainPolygon.reset(); // Clear the old collision polygon

        if (basePatternScreenPoints.isEmpty() || finalPatternCycleWidth <= 0) {
            System.err.println("populateVisibleTerrain: Base pattern is empty or has zero width.");
            return;
        }

        // Determine which repetitions of the base pattern are visible or nearby
        // Add some buffer repetitions to ensure smooth scrolling and avoid pop-ins
        int startRepetitionIndex = (int) Math.floor(worldXOffset / finalPatternCycleWidth);
        int endRepetitionIndex = (int) Math.ceil((worldXOffset + viewWidth) / finalPatternCycleWidth);
        startRepetitionIndex--; // One extra repetition to the left
        endRepetitionIndex++;   // One extra repetition to the right

        Point lastOverallScreenPoint = null; // To avoid adding duplicate consecutive points

        for (int i = startRepetitionIndex; i <= endRepetitionIndex; i++) {
            float currentPatternCycleWorldXStart = i * finalPatternCycleWidth; // X start of this repetition in world coords

            // Add terrain points from this repetition
            for (Point patternPoint : basePatternScreenPoints) {
                int pointWorldX = (int) (currentPatternCycleWorldXStart + patternPoint.x);
                int pointScreenX = pointWorldX - (int) worldXOffset; // X relative to current camera view
                Point currentScreenPoint = new Point(pointScreenX, patternPoint.y); // Y is from base pattern

                // Add point if it's within a wider view range (including off-screen buffers)
                if (pointScreenX >= -finalPatternCycleWidth && pointScreenX <= viewWidth + finalPatternCycleWidth) {
                    // Avoid duplicates if the pattern seamlessly connects or due to rounding
                    if (terrainSurfacePoints.isEmpty() || !currentScreenPoint.equals(terrainSurfacePoints.get(terrainSurfacePoints.size() - 1))) {
                        if (!currentScreenPoint.equals(lastOverallScreenPoint)) { // Stricter duplicate check
                            terrainSurfacePoints.add(currentScreenPoint);
                            lastOverallScreenPoint = currentScreenPoint;
                        }
                    }
                }
            }

            // Add landing pads from this repetition (with probability)
            for (LandingPad.PatternPadDefinition def : basePatternPadDefinitions) {
                if (Math.random() < PAD_SPAWN_PROBABILITY) { // Randomly decide if this pad spawns
                    float padWorldStartX = currentPatternCycleWorldXStart + def.patternStartX;
                    float padWorldEndX = currentPatternCycleWorldXStart + def.patternEndX;

                    // Transform pad X coords to current screen space
                    float padScreenStartX = padWorldStartX - worldXOffset;
                    float padScreenEndX = padWorldEndX - worldXOffset;

                    // Add pad if it's at least partially visible on screen
                    if (padScreenEndX >= 0 && padScreenStartX <= viewWidth) {
                        landingPads.add(new LandingPad(padScreenStartX, padScreenEndX, def.transformedScreenY, def.multiplier));
                    }
                }
            }
        }
        removeConsecutiveDuplicatePoints(); // Clean up any remaining identical adjacent points
        buildTerrainPolygon(); // Rebuild the collision polygon from the new surface points
    }


    /**
     * Removes consecutive duplicate points from the {@link #terrainSurfacePoints} list.
     * This is a cleanup step to ensure the polygon has no redundant vertices.
     */
    private void removeConsecutiveDuplicatePoints() {
        if (terrainSurfacePoints.size() < 2) return; // No duplicates possible with fewer than 2 points

        List<Point> uniquePoints = new ArrayList<>();
        uniquePoints.add(terrainSurfacePoints.get(0)); // Add the first point

        for (int i = 1; i < terrainSurfacePoints.size(); i++) {
            if (!terrainSurfacePoints.get(i).equals(terrainSurfacePoints.get(i - 1))) {
                uniquePoints.add(terrainSurfacePoints.get(i)); // Add if different from the previous one
            }
        }
        terrainSurfacePoints.clear();
        terrainSurfacePoints.addAll(uniquePoints);
    }


    /**
     * Builds the {@link #terrainPolygon} used for collision detection.
     * The polygon is formed by the {@link #terrainSurfacePoints} and extends downwards
     * to the bottom of the screen to create a solid filled area.
     */
    private void buildTerrainPolygon() {
        terrainPolygon.reset(); // Clear any existing points

        if (terrainSurfacePoints.isEmpty()) {
            // Fallback: create a flat ground at the bottom if no terrain points (should not happen in normal operation)
            terrainPolygon.addPoint(0, screenHeight);
            terrainPolygon.addPoint(screenWidth, screenHeight);
            terrainPolygon.addPoint(screenWidth, screenHeight - 1); // Make it a thin polygon
            terrainPolygon.addPoint(0, screenHeight - 1);
            return;
        }

        // Start polygon from bottom-left of the first terrain point
        terrainPolygon.addPoint(terrainSurfacePoints.get(0).x, screenHeight);

        // Add all terrain surface points
        for (Point p : terrainSurfacePoints) {
            terrainPolygon.addPoint(p.x, p.y);
        }

        // End polygon at bottom-right of the last terrain point
        terrainPolygon.addPoint(terrainSurfacePoints.get(terrainSurfacePoints.size() - 1).x, screenHeight);
        // The polygon is implicitly closed by Java's Polygon class.
    }

    /**
     * Renders the terrain, including the surface, fill, and landing pads.
     *
     * @param g The Graphics2D context to draw on.
     */
    public void render(Graphics2D g) {
        // Fill the terrain polygon (the solid part below the surface)
        g.setColor(new Color(40, 40, 50)); // Dark grey/blue for terrain body
        g.fillPolygon(terrainPolygon);

        // Draw the terrain surface line
        g.setColor(new Color(170, 170, 180)); // Light grey for surface line
        g.setStroke(new BasicStroke(2f)); // Thicker line for surface
        if (terrainSurfacePoints.size() > 1) {
            Point previousPoint = terrainSurfacePoints.get(0);
            for (int i = 1; i < terrainSurfacePoints.size(); i++) {
                Point currentPoint = terrainSurfacePoints.get(i);
                g.drawLine(previousPoint.x, previousPoint.y, currentPoint.x, currentPoint.y);
                previousPoint = currentPoint;
            }
        }

        // Draw landing pads
        for (LandingPad pad : landingPads) {
            g.setColor(pad.getColor());
            g.setStroke(new BasicStroke(3f)); // Even thicker for pads
            g.drawLine((int) pad.getStartX(), (int) pad.getY(), (int) pad.getEndX(), (int) pad.getY());

            // Draw multiplier text above the pad
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 13));
            String multiplierText = pad.getMultiplier() + "x";
            int textWidth = g.getFontMetrics().stringWidth(multiplierText);
            // Center text on the pad, slightly above it
            g.drawString(multiplierText, (int) (pad.getStartX() + (pad.getLength() - textWidth) / 2), (int) pad.getY() - 8);
        }
        g.setStroke(new BasicStroke(1f)); // Reset stroke to default
    }

    // --- Getters ---

    /**
     * @return An unmodifiable list of the current terrain surface points.
     */
    public List<Point> getTerrainSurfacePoints() {
        return Collections.unmodifiableList(terrainSurfacePoints);
    }

    /**
     * @return An unmodifiable list of the current active landing pads.
     */
    public List<LandingPad> getLandingPads() {
        return Collections.unmodifiableList(landingPads);
    }

    /**
     * @return The Polygon object representing the terrain for collision detection.
     */
    public Polygon getTerrainPolygon() {
        return terrainPolygon;
    }

    /**
     * @return The width of the screen.
     */
    public int getScreenWidth() {
        return this.screenWidth;
    }

    /**
     * @return The height of the screen.
     */
    public int getScreenHeight() {
        return this.screenHeight;
    }

    /**
     * Internal helper class to store raw X,Y data parsed from the terrain string.
     */
    private static class PointData {
        public final int y; // Raw Y coordinate
        public int x; // Raw X coordinate

        /**
         * Constructs a PointData object.
         *
         * @param x The X-coordinate.
         * @param y The Y-coordinate.
         */
        public PointData(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}