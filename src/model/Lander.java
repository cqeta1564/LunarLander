package model;

//Reprezentuje lunární modul
public class Lander {
    private double x, y;           // Pozice
    private double velocityX, velocityY; // Rychlost
    private double angle;          // Náklon (stupně)
    private double fuel;           // Palivo
    private boolean thrusting;     // Zda je zapnutý pohon
    private boolean landed; // Úspěšné přistání
    private boolean crashed; // Exploze
    private int score;

    // Konstruktor, gettery, settery...
    public Lander(double x, double y) {
        this.x = x;
        this.y = y;
        this.velocityX = 0;
        this.velocityY = 0;
        this.angle = 0;
        this.fuel = 100.0; // Plná nádrž
        this.thrusting = false;
    }

    // Přidání paliva / spotřeba
    public void consumeFuel(double amount) {
        fuel = Math.max(0, fuel - amount);
        if (fuel <= 0) {
            setThrusting(false); // Automatické vypnutí motoru
        }
    }

    // Pohyb (voláno z PhysicsEngine)
    public void move(double deltaX, double deltaY) {
        x += deltaX;
        y += deltaY;
    }

    public void setAngle(double angle) {
        // Omezení úhlu na rozumné hodnoty (-90° až +90°)
        this.angle = Math.max(-90, Math.min(90, angle));
    }

    // Přidáme gettery a settery

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

    public void setVelocityX(double velocityX) {
        this.velocityX = velocityX;
    }

    public void setVelocityY(double velocityY) {
        this.velocityY = velocityY;
    }

    public double getAngle() {
        return angle;
    }

    public double getFuel() {
        return fuel;
    }

    public boolean isThrusting() {
        return thrusting;
    }

    // Přidáme nový stav pro zvuk motoru
    public void setThrusting(boolean thrusting) {
        if (this.thrusting != thrusting) {
            this.thrusting = thrusting;
            // Zde bychom mohli volat SoundManager, ale raději to uděláme přes GameEngine
        }
    }

    public boolean isLanded() {
        return landed;
    }

    public void setLanded(boolean landed) {
        this.landed = landed;
    }

    public boolean isCrashed() {
        return crashed;
    }

    public void setCrashed(boolean crashed) {
        this.crashed = crashed;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int points) {
        score += points;
    }

    public void reset() {
        this.x = 100;  // Počáteční pozice X
        this.y = 100;  // Počáteční pozice Y
        this.velocityX = 0;
        this.velocityY = 0;
        this.angle = 0;
        this.fuel = 100;
        this.thrusting = false;
        this.landed = false;
        this.crashed = false;
        // Score se nemusí resetovat, pokud chcete celkové skóre
        // this.score = 0;  // Odkomentovat pro reset skóre
    }
}
