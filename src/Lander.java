// Lander.java
import java.util.ArrayList;
import java.util.List;
// Assuming Graphics2D for rendering, replace with your GUI choice
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.geom.Path2D; // For drawing shapes

public class Lander {
    public Vector2D vel;
    public Vector2D pos;
    public Vector2D bottomLeft;
    public Vector2D bottomRight;
    private Vector2D thrustVec;
    private double gravity;
    private double thrustAcceleration;
    private double thrustBuild; // Current thrust level being applied
    private double topSpeed;
    private double drag;
    private double bouncing; // Animation state for bounce
    private boolean exploding;
    private double targetRotation; // Degrees
    private long lastRotationTime;
    private int counter; // General purpose counter, e.g., for animation frames
    private int abortCounter;
    private long lastAbort;


    public double rotation; // Current rotation in degrees
    public double thrusting; // Requested thrust power (0 to 1)
    public double altitude;
    public boolean active;
    public double fuel;
    public double scale; // Visual scale of the lander
    public double left, right, bottom, top; // Bounding box
    public Color colour;

    // For rendering shapes after explosion
    private List<List<Object>> shapes; // Using List<Object> for mixed type arrays (cmd, x, y)
    private List<Vector2D> shapePos;
    private List<Vector2D> shapeVels;
    public double thrustLevel; // Actual thrust being applied after ramp-up

    public Lander() {
        this.vel = new Vector2D(0, 0);
        this.pos = new Vector2D(0, 0);
        this.bottomLeft = new Vector2D(0, 0);
        this.bottomRight = new Vector2D(0, 0);
        this.thrustVec = new Vector2D(0, 0);
        this.gravity = 0.0005;
        this.thrustAcceleration = 0.0015;
        this.thrustBuild = 0;
        this.topSpeed = 0.35;
        this.drag = 0.9997;
        this.bouncing = 0;
        this.exploding = false;
        this.targetRotation = 0;
        this.lastRotationTime = 0;
        this.counter = 0;
        this.abortCounter = -1;
        this.lastAbort = 0; // Initialize properly

        this.rotation = 0;
        this.thrusting = 0;
        this.altitude = 0;
        this.active = true;
        this.fuel = 0;
        this.scale = 0.8;
        this.colour = Color.WHITE;

        this.shapes = new ArrayList<>();
        this.shapePos = new ArrayList<>();
        this.shapeVels = new ArrayList<>();
        this.thrustLevel = 0;

        defineShape();
        reset();
    }

    public void reset() {
        abortCounter = -1;
        lastAbort = System.currentTimeMillis(); // Use System.currentTimeMillis for time
        vel.reset(0.415, 0);
        pos.reset(110, 150);
        this.rotation = targetRotation = -90;
        // scale = 1; // Original JS, but this.scale is used elsewhere. Clarify if this is a different 'scale'
        thrustBuild = 0;
        bouncing = 0;
        this.active = true;
        exploding = false;
        for (Vector2D sp : shapePos) {
            sp.reset(0, 0);
        }
        this.thrusting = 0;
        this.colour = Color.WHITE; // Reset color if needed
    }

    public void rotate(int direction) {
        long now = System.currentTimeMillis();
        if (now - lastRotationTime > 80) {
            targetRotation += direction * 15;
            targetRotation = GameUtils.clamp(targetRotation, -90, 90); // Assuming a GameUtils.clamp
            lastRotationTime = now;
        }
    }

    public void setRotation(double angle) {
        targetRotation = Math.round(GameUtils.clamp(angle, -90, 90) / 10.0) * 10.0;
    }

    public void thrust(double power) {
        this.thrusting = GameUtils.clamp(power, 0, 1); // Ensure power is within [0,1]
    }

    public void abort() {
        long now = System.currentTimeMillis();
        if (now - lastAbort > 10000) {
            abortCounter = 100;
            lastAbort = now;
        }
    }

    public void update() {
        counter++;

        this.rotation += (targetRotation - this.rotation) * 0.3;
        if (Math.abs(this.rotation - targetRotation) < 0.1) {
            this.rotation = targetRotation;
        }

        if (exploding) {
            updateShapesAnimation();
        }

        if (this.active) {
            if (abortCounter > -1) {
                targetRotation = 0;
                if (this.fuel > 0) thrustBuild = 3; // Max thrust for abort
                abortCounter--;
                this.fuel -= 1;
            }

            if (this.fuel <= 0) this.thrusting = 0;

            // Smoothly adjust thrustBuild towards the requested thrusting power
            thrustBuild += (this.thrusting - thrustBuild) * 0.2;
            thrustBuild = GameUtils.clamp(thrustBuild, 0, 3); // Clamp thrustBuild (max 3 from abort)


            if (thrustBuild > 0) {
                thrustVec.reset(0, -thrustAcceleration * thrustBuild);
                thrustVec.rotate(this.rotation, false); // false for degrees
                vel.plusEq(thrustVec);
                this.fuel -= (0.2 * thrustBuild);
            }

            pos.plusEq(vel);
            vel.x *= drag;
            vel.y += gravity;

            // Clamp velocity to topSpeed
            if (vel.y > topSpeed) vel.y = topSpeed;
            else if (vel.y < -topSpeed) vel.y = -topSpeed;
            // Similar clamping for vel.x might be needed depending on game design

            // Update bounding box
            this.left = pos.x - (10 * this.scale);
            this.right = pos.x + (10 * this.scale);
            this.bottom = pos.y + (14 * this.scale);
            this.top = pos.y - (5 * this.scale);
            bottomLeft.reset(this.left, this.bottom);
            bottomRight.reset(this.right, this.bottom);

        } else if (bouncing > 0) {
            pos.y += Math.sin(bouncing) * 0.07;
            bouncing -= Math.PI / 20;
            if (bouncing < 0) bouncing = 0;
        }

        if (this.fuel < 0) this.fuel = 0;

        // SoundManager.setThrustVolume(Math.min(1, thrustBuild)); // Placeholder for sound
        this.thrustLevel = thrustBuild;
    }


    public void render(Graphics2D g, double viewScale) {
        g.setColor(this.colour);
        g.translate(pos.x, pos.y);
        g.scale(this.scale, this.scale);
        // Convert to radians for AffineTransform
        g.rotate(this.rotation * Vector2D.TO_RADIANS);

        // Set stroke considering the view and lander scale
        // BasicStroke stroke = new BasicStroke((float)(1.0 / (this.scale * viewScale)));
        // g.setStroke(stroke);

        Path2D.Double path = new Path2D.Double();
        renderShapesToPath(path, g); // Pass Graphics2D if shapes need individual colors/strokes

        if ((thrustBuild > 0) && (this.active)) {
            // Approximate thrust flame: line from (0,11) to a point based on thrustBuild
            double flameLength = 11 + (Math.min(thrustBuild, 1) * 20 * ((((counter >> 1) % 3) * 0.2) + 1));
            path.moveTo(0, 11); // Assuming (0,0) is center of lander body, positive y is down
            path.lineTo(0, flameLength);
            // path.closePath(); // ClosePath was in JS for the main body, might not be needed here if flame is separate
        }
        g.draw(path);
    }

    private void renderShapesToPath(Path2D.Double generalPath, Graphics2D gContext) {
        // In Java, Path2D is good for complex shapes.
        // Each "shape" from JS can be a separate Path2D object or appended to a general path.
        for (int i = 0; i < shapes.size(); i++) {
            List<Object> sCommands = shapes.get(i);
            Vector2D sPos = shapePos.get(i); // Position offset for this part if exploding

            Path2D.Double currentShapePath = new Path2D.Double();
            // Apply individual shape offset if exploding
            // AffineTransform partTransform = AffineTransform.getTranslateInstance(sPos.x, sPos.y);
            // currentShapePath.transform(partTransform);


            // This loop needs to be before g.translate(sPos.x, sPos.y) if sPos is relative to lander's main pos
            // If sPos are absolute coords for exploded parts, then new transform for each.
            // For now, assuming sPos is relative to the lander's already transformed origin.
            double currentX = sPos.x; // Start with the offset
            double currentY = sPos.y;

            for (int j = 0; j < sCommands.size(); ) {
                String cmd = (String) sCommands.get(j++);
                switch (cmd) {
                    case "m": // moveTo
                        currentX = sPos.x + (Double) sCommands.get(j++);
                        currentY = sPos.y + (Double) sCommands.get(j++);
                        generalPath.moveTo(currentX, currentY);
                        break;
                    case "l": // lineTo
                        currentX = sPos.x + (Double) sCommands.get(j++);
                        currentY = sPos.y + (Double) sCommands.get(j++);
                        generalPath.lineTo(currentX, currentY);
                        break;
                    case "cp": // closePath
                        generalPath.closePath();
                        break;
                    case "r": // rect
                        double rx = sPos.x + (Double) sCommands.get(j++);
                        double ry = sPos.y + (Double) sCommands.get(j++);
                        double rw = (Double) sCommands.get(j++);
                        double rh = (Double) sCommands.get(j++);
                        generalPath.moveTo(rx, ry);
                        generalPath.lineTo(rx + rw, ry);
                        generalPath.lineTo(rx + rw, ry + rh);
                        generalPath.lineTo(rx, ry + rh);
                        generalPath.closePath();
                        break;
                    default:
                        System.out.println("Unknown shape command: " + cmd);
                }
            }
        }
    }


    public void crash() {
        this.rotation = 0;
        this.targetRotation = 0;
        this.active = false;
        this.exploding = true;
        this.thrustBuild = 0;
        // this.colour = Color.RED; // Example: change color on crash
    }

    public void land() {
        this.active = false;
        this.thrustBuild = 0;
        // this.colour = Color.GREEN; // Example: change color on land
    }

    public void makeBounce() {
        bouncing = Math.PI * 2;
    }

    private void defineShape() {
        // Structure: List of commands and coordinates. 'm' for moveTo, 'l' for lineTo, 'cp' for closePath, 'r' for rect.
        // Each command string is followed by its required double parameters.
        shapes.clear();
        shapePos.clear();
        shapeVels.clear();

        double min = 2.6, max = 5;
        List<Object> mainBody = new ArrayList<>();
        mainBody.add("m"); mainBody.add(min); mainBody.add(-max);
        mainBody.add("l"); mainBody.add(max); mainBody.add(-min);
        mainBody.add("l"); mainBody.add(max); mainBody.add(min);
        mainBody.add("l"); mainBody.add(min); mainBody.add(max);
        mainBody.add("l"); mainBody.add(-min); mainBody.add(max);
        mainBody.add("l"); mainBody.add(-max); mainBody.add(min);
        mainBody.add("l"); mainBody.add(-max); mainBody.add(-min);
        mainBody.add("l"); mainBody.add(-min); mainBody.add(-max);
        mainBody.add("cp");
        shapes.add(mainBody);
        shapeVels.add(new Vector2D(1, -2.5));
        shapePos.add(new Vector2D(0,0)); // Initial position offset for this part

        List<Object> window = new ArrayList<>(); // Example name
        window.add("r"); window.add(-6.0); window.add(5.0); window.add(12.0); window.add(2.0); // rect: x, y, width, height
        shapes.add(window);
        shapeVels.add(new Vector2D(2, -1.5));
        shapePos.add(new Vector2D(0,0));

        List<Object> leftLeg = new ArrayList<>();
        leftLeg.add("m"); leftLeg.add(-5.0); leftLeg.add(7.5);
        leftLeg.add("l"); leftLeg.add(-9.0); leftLeg.add(13.0);
        leftLeg.add("m"); leftLeg.add(-11.0); leftLeg.add(13.0); // Strut foot
        leftLeg.add("l"); leftLeg.add(-7.0); leftLeg.add(13.0);
        shapes.add(leftLeg);
        shapeVels.add(new Vector2D(0, -3));
        shapePos.add(new Vector2D(0,0));

        List<Object> rightLeg = new ArrayList<>();
        rightLeg.add("m"); rightLeg.add(5.0); rightLeg.add(7.5);
        rightLeg.add("l"); rightLeg.add(9.0); rightLeg.add(13.0);
        rightLeg.add("m"); rightLeg.add(11.0); rightLeg.add(13.0); // Strut foot
        rightLeg.add("l"); rightLeg.add(7.0); rightLeg.add(13.0);
        shapes.add(rightLeg);
        shapeVels.add(new Vector2D(3, -1));
        shapePos.add(new Vector2D(0,0));

        List<Object> thrusterLeft = new ArrayList<>();
        thrusterLeft.add("m"); thrusterLeft.add(-3.0); thrusterLeft.add(7.5);
        thrusterLeft.add("l"); thrusterLeft.add(-5.0); thrusterLeft.add(12.0);
        thrusterLeft.add("l"); thrusterLeft.add(-4.5); thrusterLeft.add(13.0);
        shapes.add(thrusterLeft);
        shapeVels.add(new Vector2D(1,-1));
        shapePos.add(new Vector2D(0,0));

        List<Object> thrusterRight = new ArrayList<>();
        thrusterRight.add("m"); thrusterRight.add(3.0); thrusterRight.add(7.5);
        thrusterRight.add("l"); thrusterRight.add(5.0); thrusterRight.add(12.0);
        thrusterRight.add("l"); thrusterRight.add(4.5); thrusterRight.add(13.0);
        shapes.add(thrusterRight);
        shapeVels.add(new Vector2D(2.5,-1));
        shapePos.add(new Vector2D(0,0));

        List<Object> thrusterBottom = new ArrayList<>();
        thrusterBottom.add("m"); thrusterBottom.add(4.0); thrusterBottom.add(11.0);
        thrusterBottom.add("l"); thrusterBottom.add(-4.0); thrusterBottom.add(11.0);
        shapes.add(thrusterBottom);
        shapeVels.add(new Vector2D(2,-0.5));
        shapePos.add(new Vector2D(0,0));

        // Ensure shapePos has an entry for every shape, initialized to (0,0) if not exploding
        // Done by adding new Vector2D(0,0) after each shape.
    }


    private void updateShapesAnimation() {
        if (!exploding) return;
        for (int i = 0; i < shapePos.size(); i++) {
            if (i < shapeVels.size()) { // Ensure shapeVels has a corresponding entry
                shapePos.get(i).plusEq(shapeVels.get(i));
            }
        }
    }
}

// Helper / Utility class (can be separate or static methods in Game)
class GameUtils {
    public static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    public static double randomRange(double min, double max) {
        return min + (Math.random() * (max - min));
    }

    public static double map(double value, double min1, double max1, double min2, double max2, boolean clamp) {
        if (clamp) {
            if (min1 > max1) {
                double tmp1 = min1; min1 = max1; max1 = tmp1;
                double tmp2 = min2; min2 = max2; max2 = tmp2;
            }
            if (value <= min1) return min2;
            if (value >= max1) return max2;
        }
        return (((value - min1) / (max1 - min1)) * (max2 - min2)) + min2;
    }
}