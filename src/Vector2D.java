// Vector2D.java
public class Vector2D {
    public double x;
    public double y;

    public static final double TO_DEGREES = 180 / Math.PI;
    public static final double TO_RADIANS = Math.PI / 180;
    private static final Vector2D temp = new Vector2D(); // For internal calculations

    public Vector2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vector2D() {
        this(0, 0);
    }

    public Vector2D reset(double x, double y) {
        this.x = x;
        this.y = y;
        return this;
    }

    @Override
    public String toString() {
        return "[" + x + ", " + y + "]";
    }

    public String toString(int decPlaces) {
        double scalar = Math.pow(10, decPlaces);
        return "[" + Math.round(this.x * scalar) / scalar + ", " + Math.round(this.y * scalar) / scalar + "]";
    }

    public Vector2D clone() {
        return new Vector2D(this.x, this.y);
    }

    public void copyTo(Vector2D v) {
        v.x = this.x;
        v.y = this.y;
    }

    public void copyFrom(Vector2D v) {
        this.x = v.x;
        this.y = v.y;
    }

    public double magnitude() {
        return Math.sqrt((this.x * this.x) + (this.y * this.y));
    }

    public double magnitudeSquared() {
        return (this.x * this.x) + (this.y * this.y);
    }

    public Vector2D normalise() {
        double m = this.magnitude();
        if (m != 0) { // Avoid division by zero
            this.x = this.x / m;
            this.y = this.y / m;
        }
        return this;
    }

    public Vector2D reverse() {
        this.x = -this.x;
        this.y = -this.y;
        return this;
    }

    public Vector2D plusEq(Vector2D v) {
        this.x += v.x;
        this.y += v.y;
        return this;
    }

    public Vector2D plusNew(Vector2D v) {
        return new Vector2D(this.x + v.x, this.y + v.y);
    }

    public Vector2D minusEq(Vector2D v) {
        this.x -= v.x;
        this.y -= v.y;
        return this;
    }

    public Vector2D minusNew(Vector2D v) {
        return new Vector2D(this.x - v.x, this.y - v.y);
    }

    public Vector2D multiplyEq(double scalar) {
        this.x *= scalar;
        this.y *= scalar;
        return this;
    }

    public Vector2D multiplyNew(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    public Vector2D divideEq(double scalar) {
        if (scalar != 0) { // Avoid division by zero
            this.x /= scalar;
            this.y /= scalar;
        }
        return this;
    }

    public Vector2D divideNew(double scalar) {
        if (scalar != 0) {
            return new Vector2D(this.x / scalar, this.y / scalar);
        }
        return this.clone(); // Or throw an exception
    }

    public double dot(Vector2D v) {
        return (this.x * v.x) + (this.y * v.y);
    }

    public double angle(boolean useRadians) {
        return Math.atan2(this.y, this.x) * (useRadians ? 1 : TO_DEGREES);
    }

    public Vector2D rotate(double angle, boolean useRadians) {
        double cosRY = Math.cos(angle * (useRadians ? 1 : TO_RADIANS));
        double sinRY = Math.sin(angle * (useRadians ? 1 : TO_RADIANS));

        synchronized (temp) { // Synchronize access to static temp
            temp.copyFrom(this);
            this.x = (temp.x * cosRY) - (temp.y * sinRY);
            this.y = (temp.x * sinRY) + (temp.y * cosRY);
        }
        return this;
    }

    public boolean equals(Vector2D v) {
        return (this.x == v.x) && (this.y == v.y); // Consider using a tolerance for floating point comparison
    }

    public boolean isCloseTo(Vector2D v, double tolerance) {
        if (this.equals(v)) return true;
        synchronized (temp) {
            temp.copyFrom(this);
            temp.minusEq(v);
            return (temp.magnitudeSquared() < tolerance * tolerance);
        }
    }

    public Vector2D rotateAroundPoint(Vector2D point, double angle, boolean useRadians) {
        synchronized (temp) {
            temp.copyFrom(this);
            temp.minusEq(point);
            temp.rotate(angle, useRadians);
            temp.plusEq(point);
            this.copyFrom(temp);
        }
        return this;
    }

    public boolean isMagLessThan(double distance) {
        return (this.magnitudeSquared() < distance * distance);
    }

    public boolean isMagGreaterThan(double distance) {
        return (this.magnitudeSquared() > distance * distance);
    }
}