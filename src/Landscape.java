// Landscape.java
import java.util.ArrayList;
import java.util.List;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Path2D; // For drawing lines
import java.awt.geom.Rectangle2D; // For stars and view bounds

public class Landscape {
    public List<Vector2D> points;
    public List<LandscapeLine> lines;
    public List<Vector2D> stars; // Using Vector2D for star positions for simplicity
    private List<LandingZone> availableZones;
    private List<List<Integer>> zoneCombis; // Combinations of zone indices
    private int currentCombi;
    // private List<InfoBox> zoneInfos; // For displaying multipliers, needs InfoBox class

    public double tileWidth; // The width of one repeatable segment of the landscape
    private static int gameCounter = 0; // Shared counter, if needed static from Game class

    public Landscape() {
        this.points = new ArrayList<>();
        this.lines = new ArrayList<>();
        this.stars = new ArrayList<>();
        this.availableZones = new ArrayList<>();
        this.zoneCombis = new ArrayList<>();
        this.currentCombi = 0;
        // this.zoneInfos = new ArrayList<>();

        setupData();

        double landscale = 1.5;
        if (points.isEmpty()) {
            this.tileWidth = 0; // Avoid error if points is empty
        } else {
            this.tileWidth = points.get(points.size() - 1).x * landscale;
        }


        for (int i = 0; i < points.size(); i++) {
            Vector2D p = points.get(i);
            p.x *= landscale;
            p.y *= landscale;
            p.y -= 50;
        }

        for (int i = 1; i < points.size(); i++) {
            lines.add(new LandscapeLine(points.get(i - 1), points.get(i)));
        }

        // Create stars
        for (int i = 0; i < lines.size(); i++) {
            if (Math.random() < 0.1) {
                LandscapeLine line = lines.get(i);
                // Stars should be above the terrain
                double starY = Math.random() * 600; // Assuming max height for stars
                if (starY < line.p1.y && starY < line.p2.y) { // Ensure star is above this line segment
                    stars.add(new Vector2D(line.p1.x, starY));
                }
            }
        }
        setZones(); // Initialize zones
    }

    // Call this from your game loop or a timer to update animations
    public static void incrementCounter() {
        gameCounter++;
    }

    // view would be a Rectangle2D or a custom Viewport class in Java
    public void render(Graphics2D g, Viewport view) {
        // Rendering landscape lines
        double offset = 0;
        // Adjust offset to draw repeating landscape if view goes beyond tileWidth
        while (view.left - offset > tileWidth && tileWidth > 0) {
            offset += tileWidth;
        }
        while (view.left - offset < 0 && tileWidth > 0) { // Corrected condition for scrolling left
            offset -= tileWidth;
        }


        Path2D.Double path = new Path2D.Double();
        boolean firstMove = true;

        // Determine starting line index based on view.left and offset
        int lineStartIndex = 0;
        double currentOffsetX = offset;

        if (!lines.isEmpty() && tileWidth > 0) {
            while (lineStartIndex < lines.size() -1 && lines.get(lineStartIndex).p2.x + currentOffsetX < view.left) {
                lineStartIndex++;
                if (lineStartIndex >= lines.size()) { // Should not happen if logic is correct
                    lineStartIndex = 0;
                    currentOffsetX += tileWidth; // This logic might need refinement for multiple tiles
                }
            }
        }


        for (int i = lineStartIndex; i < lines.size(); ) {
            LandscapeLine line = lines.get(i);
            double p1x = line.p1.x + currentOffsetX;
            double p2x = line.p2.x + currentOffsetX;

            if (p1x > view.right) { // Line starts after the view
                // If we've wrapped around and still no lines in view, break
                if (currentOffsetX > offset + tileWidth && tileWidth > 0) break;
                // If tileWidth is 0 or negative, break to avoid infinite loop
                if (tileWidth <= 0 && i > lineStartIndex) break;


                // if we are past the initial offset and still nothing, try wrapping once more.
                if (tileWidth > 0 && currentOffsetX == offset) {
                    i = 0;
                    currentOffsetX += tileWidth;
                    firstMove = true;
                    continue;
                } else {
                    break; // No more lines to draw in current (or next) tile view
                }
            }


            if (firstMove) {
                path.moveTo(p1x, line.p1.y);
                firstMove = false;
            }
            path.lineTo(p2x, line.p2.y);

            // Display zone multipliers (simplified, actual InfoBox rendering needed)
            if ((gameCounter % 20 > 10) && (line.multiplier != 1)) {
                String multText = line.multiplier + "x";
                double midX = ((p1x + p2x) / 2.0 * view.scale) + view.xTranslate; // Screen coords
                double yPos = (line.p1.y + 2) * view.scale + view.yTranslate;    // Screen coords
                // In a real Java app, you'd add a JLabel or drawString here using g.
                // g.setColor(Color.YELLOW);
                // g.drawString(multText, (float)midX, (float)yPos);
            }

            i++;
            if (i >= lines.size() && tileWidth > 0) { // End of current tile, wrap to next if visible
                if (p2x < view.right) { // Only wrap if the end of the current tile is still in view
                    i = 0;
                    currentOffsetX += tileWidth;
                    firstMove = true; // Need moveTo for the new tile segment
                } else {
                    break; // Current tile extends beyond view, no need to wrap yet
                }
            }
        }

        // Flickering line effect
        double flickerAmount = Math.sin(gameCounter * 0.8) * 0.5 + 0.5;
        if (flickerAmount > 0.5) {
            // g.setStroke(new java.awt.BasicStroke((float)(2.0 / view.scale)));
            int channel = (int) Math.round((flickerAmount - 0.5) * 100);
            channel = (int) GameUtils.clamp(channel, 0, 255);
            g.setColor(new Color(channel, channel, channel));
            g.draw(path);
        }

        // g.setStroke(new java.awt.BasicStroke((float)((1.0 / view.scale) * (flickerAmount * 0.2 + 0.8))));
        g.setColor(Color.WHITE);
        // g.setLineJoin(java.awt.BasicStroke.JOIN_BEVEL); // Not directly available in Path2D drawing, set on Graphics2D stroke
        g.draw(path);


        // Rendering stars
        g.setColor(Color.WHITE); // Or some star color
        double starSize = 1.0 / view.scale; // Size of star on screen

        currentOffsetX = offset; // Reset offset for stars, similar logic to lines
        int starStartIndex = 0;
        if (!stars.isEmpty() && tileWidth > 0) {
            while(starStartIndex < stars.size() -1 && stars.get(starStartIndex).x + currentOffsetX < view.left) {
                starStartIndex++;
                if (starStartIndex >= stars.size()) {
                    starStartIndex = 0;
                    currentOffsetX += tileWidth;
                }
            }
        }


        for (int i = starStartIndex; i < stars.size(); ) {
            Vector2D star = stars.get(i);
            double starX = star.x + currentOffsetX;
            double starY = star.y; // Absolute Y

            if (starX > view.right) {
                if (tileWidth > 0 && currentOffsetX == offset) {
                    i = 0;
                    currentOffsetX += tileWidth;
                    continue;
                } else {
                    break;
                }
            }

            // Adjust starY if it's below the view (stars repeat vertically)
            while (starY > view.bottom && view.getHeight() > 0) starY -= view.getHeight(); // Assuming stars repeat within view height
            while (starY < view.top && view.getHeight() > 0) starY += view.getHeight();


            if (starX >= view.left) { // Only draw if star is within horizontal view bounds
                g.fill(new Rectangle2D.Double(starX, starY, starSize, starSize));
                // If stars repeat vertically and one repetition is still in view from top
                if (starY - view.getHeight() > view.top && view.getHeight() > 0) {
                    g.fill(new Rectangle2D.Double(starX, starY - view.getHeight(), starSize, starSize));
                }
            }

            i++;
            if (i >= stars.size() && tileWidth > 0) {
                if (starX < view.right) { // Simplified: if any part of current tile was visible
                    i = 0;
                    currentOffsetX += tileWidth;
                } else {
                    break;
                }
            }
        }
    }


    public void setZones() {
        for (LandscapeLine line : lines) {
            line.multiplier = 1; // Reset all multipliers
        }

        if (zoneCombis.isEmpty() || availableZones.isEmpty()) return;

        // Ensure currentCombi is within bounds
        if (currentCombi >= zoneCombis.size()) currentCombi = 0;

        List<Integer> combi = zoneCombis.get(currentCombi);

        for (int i = 0; i < combi.size(); i++) {
            int zoneNumber = combi.get(i);
            if (zoneNumber < availableZones.size()) {
                LandingZone zoneData = availableZones.get(zoneNumber);
                if (zoneData.lineNum < lines.size()) {
                    LandscapeLine line = lines.get(zoneData.lineNum);
                    line.multiplier = zoneData.multiplier;
                }
            }
        }

        currentCombi++;
        if (currentCombi >= zoneCombis.size()) currentCombi = 0;
    }

    // Data from the original JS file
    private void setupData() {
        points.add(new Vector2D(0.5, 355.55));
        points.add(new Vector2D(5.45, 355.55));
        points.add(new Vector2D(6.45, 359.4));
        points.add(new Vector2D(11.15, 359.4));
        points.add(new Vector2D(12.1, 363.65));
        points.add(new Vector2D(14.6, 363.65));
        points.add(new Vector2D(15.95, 375.75));
        points.add(new Vector2D(19.25, 388));
        points.add(new Vector2D(19.25, 391.9));
        points.add(new Vector2D(21.65, 400));
        points.add(new Vector2D(28.85, 404.25));
        points.add(new Vector2D(30.7, 412.4));
        points.add(new Vector2D(33.05, 416.7));
        points.add(new Vector2D(37.9, 420.5));
        points.add(new Vector2D(42.7, 420.5));
        points.add(new Vector2D(47.4, 416.65));
        points.add(new Vector2D(51.75, 409.5));
        points.add(new Vector2D(56.55, 404.25));
        points.add(new Vector2D(61.3, 400));
        points.add(new Vector2D(63.65, 396.15));
        points.add(new Vector2D(68, 391.9));
        points.add(new Vector2D(70.3, 388));
        points.add(new Vector2D(75.1, 386.1));
        points.add(new Vector2D(79.85, 379.95));
        points.add(new Vector2D(84.7, 378.95));
        points.add(new Vector2D(89.05, 375.65));
        points.add(new Vector2D(93.75, 375.65));
        points.add(new Vector2D(98.5, 376.55));
        points.add(new Vector2D(103.2, 379.95));
        points.add(new Vector2D(104.3, 383.8));
        points.add(new Vector2D(107.55, 388));
        points.add(new Vector2D(108.95, 391.9));
        points.add(new Vector2D(112.4, 396.15));
        points.add(new Vector2D(113.3, 400));
        points.add(new Vector2D(117.1, 404.25));
        points.add(new Vector2D(121.95, 404.25));
        points.add(new Vector2D(125.3, 396.3));
        points.add(new Vector2D(128.6, 394.2));
        points.add(new Vector2D(132.45, 396.15));
        points.add(new Vector2D(135.75, 399.9));
        points.add(new Vector2D(138.15, 408.15));
        points.add(new Vector2D(144.7, 412.4));
        points.add(new Vector2D(146.3, 424.8));
        points.add(new Vector2D(149.55, 436.65));
        points.add(new Vector2D(149.55, 441.05));
        points.add(new Vector2D(154.35, 444.85));
        points.add(new Vector2D(163.45, 444.85));
        points.add(new Vector2D(168.15, 441.05));
        points.add(new Vector2D(172.95, 436.75));
        points.add(new Vector2D(175.45, 432.9));
        points.add(new Vector2D(179.7, 428.6));
        points.add(new Vector2D(181.95, 424.8));
        points.add(new Vector2D(186.7, 422.5));
        points.add(new Vector2D(189.15, 412.4));
        points.add(new Vector2D(191.55, 404.35));
        points.add(new Vector2D(196.35, 402.4));
        points.add(new Vector2D(200.7, 398.1));
        points.add(new Vector2D(205.45, 391.9));
        points.add(new Vector2D(210.15, 383.8));
        points.add(new Vector2D(212.55, 375.75));
        points.add(new Vector2D(216.85, 371.8));
        points.add(new Vector2D(219.3, 367.55));
        points.add(new Vector2D(220.65, 363.65));
        points.add(new Vector2D(224, 359.4));
        points.add(new Vector2D(228.8, 359.4));
        points.add(new Vector2D(233.55, 355.55));
        points.add(new Vector2D(237.85, 348.45));
        points.add(new Vector2D(242.65, 343.2));
        points.add(new Vector2D(245, 335.15));
        points.add(new Vector2D(247.35, 322.8));
        points.add(new Vector2D(247.3, 314.5));
        points.add(new Vector2D(248.35, 306.55));
        points.add(new Vector2D(252.2, 296.5));
        points.add(new Vector2D(256.55, 294.55));
        points.add(new Vector2D(257.95, 290.4));
        points.add(new Vector2D(261.25, 285.95));
        points.add(new Vector2D(265.95, 285.95));
        points.add(new Vector2D(267, 290.25));
        points.add(new Vector2D(271.75, 290.25));
        points.add(new Vector2D(273.25, 294.55));
        points.add(new Vector2D(275.2, 294.55));
        points.add(new Vector2D(278.95, 296.5));
        points.add(new Vector2D(282.25, 300.3));
        points.add(new Vector2D(284.7, 308.45));
        points.add(new Vector2D(291.85, 312.65));
        points.add(new Vector2D(298.55, 330.8));
        points.add(new Vector2D(303.25, 331.8));
        points.add(new Vector2D(308, 335.05));
        points.add(new Vector2D(309, 338.9));
        points.add(new Vector2D(312.35, 343.2));
        points.add(new Vector2D(313.8, 347.05));
        points.add(new Vector2D(317.05, 351.4));
        points.add(new Vector2D(321.9, 351.4));
        points.add(new Vector2D(322.85, 363.8));
        points.add(new Vector2D(326.6, 375.75));
        points.add(new Vector2D(326.6, 379.95));
        points.add(new Vector2D(330.9, 379.95));
        points.add(new Vector2D(332.4, 383.8));
        points.add(new Vector2D(335.8, 388));
        points.add(new Vector2D(338.1, 396.15));
        points.add(new Vector2D(340.45, 400.1));
        points.add(new Vector2D(345.3, 404.25));
        points.add(new Vector2D(346.25, 416.65));
        points.add(new Vector2D(349.6, 428.7));
        points.add(new Vector2D(349.6, 432.85));
        points.add(new Vector2D(350.95, 436.75));
        points.add(new Vector2D(354.3, 441.05));
        points.add(new Vector2D(359, 441.05));
        points.add(new Vector2D(361.4, 449.1));
        points.add(new Vector2D(363.95, 453));
        points.add(new Vector2D(368.2, 457.2));
        points.add(new Vector2D(372.9, 461));
        points.add(new Vector2D(410.2, 461));
        points.add(new Vector2D(412.55, 449.1));
        points.add(new Vector2D(417.4, 441.05));
        points.add(new Vector2D(419.7, 432.9));
        points.add(new Vector2D(422.05, 432.9));
        points.add(new Vector2D(425.45, 424.8));
        points.add(new Vector2D(428.8, 422.35));
        points.add(new Vector2D(433.45, 416.65));
        points.add(new Vector2D(438.25, 415.15));
        points.add(new Vector2D(442.6, 412.4));
        points.add(new Vector2D(447.4, 412.4));
        points.add(new Vector2D(448.8, 416.65));
        points.add(new Vector2D(454.55, 430.55));
        points.add(new Vector2D(455.5, 434.8));
        points.add(new Vector2D(459.25, 438.6));
        points.add(new Vector2D(462.6, 440.9));
        points.add(new Vector2D(466, 444.85));
        points.add(new Vector2D(468.35, 452.9));
        points.add(new Vector2D(475.55, 457.3));
        points.add(new Vector2D(484.7, 457.3));
        points.add(new Vector2D(494.7, 458.2));
        points.add(new Vector2D(503.75, 461.1));
        points.add(new Vector2D(522.2, 461.1));
        points.add(new Vector2D(524.75, 453));
        points.add(new Vector2D(527.1, 441.05));
        points.add(new Vector2D(527.1, 432.9));
        points.add(new Vector2D(531.9, 432.9));
        points.add(new Vector2D(534.15, 424.8));
        points.add(new Vector2D(538.6, 420.5));
        points.add(new Vector2D(540.9, 416.65));
        points.add(new Vector2D(542.35, 412.5));
        points.add(new Vector2D(545.7, 408));
        points.add(new Vector2D(550.45, 408));
        points.add(new Vector2D(552.85, 398.1));
        points.add(new Vector2D(554.75, 389.95));
        points.add(new Vector2D(559.55, 388));
        points.add(new Vector2D(564.35, 391.9));
        points.add(new Vector2D(573.35, 391.9));
        points.add(new Vector2D(578.1, 388));
        points.add(new Vector2D(579.55, 379.95));
        points.add(new Vector2D(582.9, 369.4));
        points.add(new Vector2D(587.75, 367.55));
        points.add(new Vector2D(588.65, 363.8));
        points.add(new Vector2D(592.05, 359.5));
        points.add(new Vector2D(596.85, 355.55));

        availableZones.add(new LandingZone(0, 4));
        availableZones.add(new LandingZone(13, 3));
        availableZones.add(new LandingZone(25, 4));
        availableZones.add(new LandingZone(34, 4));
        availableZones.add(new LandingZone(63, 5));
        availableZones.add(new LandingZone(75, 4));
        availableZones.add(new LandingZone(106, 5));
        availableZones.add(new LandingZone(111, 2));
        availableZones.add(new LandingZone(121, 5));
        availableZones.add(new LandingZone(133, 2));
        availableZones.add(new LandingZone(148, 3));

        zoneCombis.add(new ArrayList<>(List.of(2, 3, 7, 9)));
        zoneCombis.add(new ArrayList<>(List.of(7, 8, 9, 10)));
        zoneCombis.add(new ArrayList<>(List.of(2, 3, 7, 9))); // Duplicates in original data
        zoneCombis.add(new ArrayList<>(List.of(1, 4, 7, 9)));
        zoneCombis.add(new ArrayList<>(List.of(0, 5, 7, 9)));
        zoneCombis.add(new ArrayList<>(List.of(6, 7, 8, 9)));
        zoneCombis.add(new ArrayList<>(List.of(1, 4, 7, 9)));
    }
}

// Represents the visible area of the game world
// In Java, this might map to a Rectangle2D representing world coordinates,
// and the GamePanel would handle transforming these to screen coordinates.
class Viewport {
    public double xTranslate; // Translation to apply to game world coordinates for drawing
    public double yTranslate;
    public double scale;
    public double left;   // World coordinate of left edge
    public double right;  // World coordinate of right edge
    public double top;    // World coordinate of top edge
    public double bottom; // World coordinate of bottom edge

    private double screenWidth;
    private double screenHeight;

    public Viewport(double screenWidth, double screenHeight) {
        this.scale = 1.0;
        this.xTranslate = 0;
        this.yTranslate = 0;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        updateBounds();
    }

    public void updateBounds() {
        // Given current xTranslate, yTranslate and scale, calculate world coordinates
        // view.x in JS is translation, so -view.x/view.scale is world_coord_left
        this.left = -xTranslate / scale;
        this.top = -yTranslate / scale;
        this.right = this.left + (screenWidth / scale);
        this.bottom = this.top + (screenHeight / scale);
    }

    public double getWidth() { return screenWidth; }
    public double getHeight() { return screenHeight; }
}