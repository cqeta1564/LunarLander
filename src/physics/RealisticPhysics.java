package physics;

import model.Lander;

public class RealisticPhysics implements PhysicsStrategy {
    private static final double GRAVITY = 10.62; // Měsíční gravitace (m/s²)
    private static final double THRUST_POWER = 5.0;

    @Override
    public void applyPhysics(Lander lander, double deltaTime) {
        // Gravitační působení
        lander.move(lander.getVelocityX(), lander.getVelocityY() + GRAVITY * deltaTime);

        // Pohon (pokud je aktivní)
        if (lander.isThrusting() && lander.getFuel() > 0) {
            double thrustX = THRUST_POWER * Math.sin(Math.toRadians(lander.getAngle()));
            double thrustY = -THRUST_POWER * Math.cos(Math.toRadians(lander.getAngle()));
            lander.move(thrustX * deltaTime, thrustY * deltaTime);
            lander.consumeFuel(0.1 * deltaTime);
        }
    }
}
