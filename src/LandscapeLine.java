// LandscapeLine.java
import java.awt.geom.Point2D; // Or use Vector2D

public class LandscapeLine {
    public Vector2D p1;
    public Vector2D p2;
    public boolean landable;
    public int multiplier; // Score multiplier for landing here
    public boolean checked; // For collision detection optimization/debug

    public LandscapeLine(Vector2D p1, Vector2D p2) {
        this.p1 = p1;
        this.p2 = p2;
        // Consider a small tolerance for y-coordinate equality
        this.landable = Math.abs(p1.y - p2.y) < 0.01; // Landable if horizontal
        this.multiplier = 1;
        this.checked = false;
    }
}