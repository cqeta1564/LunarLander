package physics;

import model.Lander;
import model.Terrain;

import java.awt.*;

public class PhysicsEngine {
    private static final double GRAVITY = 1.62; // Měsíční gravitace (m/s²)
    private static final double THRUST_POWER = 5.0; // Síla motoru
    private static final double MAX_SAFE_LANDING_SPEED = 3.0;
    private static final double MAX_SAFE_ANGLE = 15.0; // ±15° od svislice

    public void update(Lander lander, Terrain terrain, double deltaTime) {
        // Pokud lander už přistál nebo narazil, nezajišťujeme fyziku
        if (!lander.isLanded() && !lander.isCrashed()) {
            applyGravityAndThrust(lander, deltaTime);
            checkLanding(lander, terrain);
        }
    }

    private void applyGravityAndThrust(Lander lander, double deltaTime) {
        // Gravitační působení (vždy směrem dolů)
        lander.setVelocityY(lander.getVelocityY() + GRAVITY * deltaTime);

        // Pohon motoru (pokud je palivo a thrust je zapnutý)
        if (lander.isThrusting() && lander.getFuel() > 0) {
            double thrustX = THRUST_POWER * Math.sin(Math.toRadians(lander.getAngle()));
            double thrustY = -THRUST_POWER * Math.cos(Math.toRadians(lander.getAngle())); // Záporné Y = nahoru

            lander.setVelocityX(lander.getVelocityX() + thrustX * deltaTime);
            lander.setVelocityY(lander.getVelocityY() + thrustY * deltaTime);
            lander.consumeFuel(0.1 * deltaTime); // Spotřeba paliva
        }

        // Aktualizace pozice landeru
        lander.setX(lander.getX() + lander.getVelocityX() * deltaTime);
        lander.setY(lander.getY() + lander.getVelocityY() * deltaTime);
    }

    private void checkLanding(Lander lander, Terrain terrain) {
        // Kontrolujeme pouze pokud lander ještě není ve finálním stavu
        if (!lander.isLanded() && !lander.isCrashed()) {
            if (isLanderOnGround(lander, terrain)) {
                Rectangle landerBounds = getLanderBounds(lander);
                boolean isInLandingZone = terrain.getLandingZones().stream()
                        .anyMatch(zone -> zone.intersects(landerBounds));

                if (isSafeLanding(lander)) {
                    lander.setLanded(true);
                    if (isInLandingZone) lander.addScore(100);
                } else {
                    lander.setCrashed(true);
                }
            }
        }
    }

    private boolean isLanderOnGround(Lander lander, Terrain terrain) {
        int landerBottomY = (int) lander.getY() + 10; // Spodní část landeru
        boolean isTouchingGround = false;

        // Projdeme všechny body terénu
        for (Point groundPoint : terrain.getGroundPoints()) {
            // Kontrola, zda je lander nad tímto bodem terénu
            if (Math.abs(groundPoint.x - lander.getX()) < 15) { // Tolerance 15 pixelů
                if (landerBottomY >= groundPoint.y) {
                    isTouchingGround = true;
                    // Korekce pozice - "posadíme" lander na povrch
                    lander.setY(groundPoint.y - 10);
                    break;
                }
            }
        }

        return isTouchingGround;
    }

    private boolean isSafeLanding(Lander lander) {
        return Math.abs(lander.getVelocityY()) < MAX_SAFE_LANDING_SPEED &&
                Math.abs(lander.getAngle()) <= MAX_SAFE_ANGLE;
    }

    private Rectangle getLanderBounds(Lander lander) {
        return new Rectangle(
                (int) lander.getX() - 8,
                (int) lander.getY() - 10,
                16, 20
        );
    }
}
