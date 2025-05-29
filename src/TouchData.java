// TouchData.java (Simplified for mouse, or conceptual for touch)
class TouchData {
    public int id = -1; // For multi-touch, mouse uses one "touch"
    public boolean touching = false;
    public Vector2D touchStartPos = new Vector2D();
    public Vector2D touchCurrentPos = new Vector2D(); // Renamed from touchPos for clarity
    public Vector2D touchVector = new Vector2D(); // Offset from start

    public void startTouch(double x, double y, int id) {
        this.id = id;
        this.touching = true;
        this.touchStartPos.reset(x, y);
        this.touchCurrentPos.reset(x,y);
        this.touchVector.reset(0, 0);
    }

    public void updateTouch(double x, double y) {
        if (!touching) return;
        this.touchCurrentPos.reset(x,y);
        // Corrected: Perform operations in two steps
        this.touchVector.copyFrom(this.touchCurrentPos);
        this.touchVector.minusEq(this.touchStartPos);
    }

    public void endTouch() {
        this.touching = false;
        this.id = -1; // Reset ID
    }

    public double getX() { return touchCurrentPos.x; }
    public double getY() { return touchCurrentPos.y; }
    public double getXOffset() { return touchVector.x; }
    public double getYOffset() { return touchVector.y; }
}