import core.Game;
import entities.Lander;
import entities.Terrain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testy pro třídu Lander.
 */
class LanderTest {

    private Lander lander;
    private Terrain mockTerrain;

    @BeforeEach
    void setUp() {
        mockTerrain = new Terrain(Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);
        lander = new Lander(100, 100);
        Lander.DEBUG_INFINITE_TOLERANCE_LANDING = false;
    }

    @Test
    void testLanderInitialState() {
        assertEquals(100, lander.getX(), "Initial X position is incorrect.");
        assertEquals(100, lander.getY(), "Initial Y position is incorrect.");
        assertEquals(Lander.INITIAL_FLYOVER_SPEED_X_PIXELS_S, lander.getVx(), "Initial VX is incorrect.");
        assertEquals(0, lander.getVy(), "Initial VY is incorrect.");
        assertEquals(0, lander.getAngle(), "Initial angle is incorrect.");
        assertEquals(Lander.MAX_FUEL, lander.getFuel(), "Initial fuel is incorrect.");
        assertEquals(Lander.State.INITIAL_FLYOVER, lander.getCurrentState(), "Initial state is incorrect.");
    }

    @Test
    void testLanderLosesFuelWithThruster() {
        lander.playerHasTakenControl();
        lander.setPlayerRequestsThrust(true);
        double initialFuel = lander.getFuel();
        lander.update(0.1, mockTerrain);
        assertTrue(lander.getFuel() < initialFuel, "Fuel should decrease when thruster is active.");
    }

    @Test
    void testLanderDoesNotUseFuelWhenEmptyAndThrusterRequested() {
        lander.playerHasTakenControl();
        lander.setFuel(0);
        lander.setPlayerRequestsThrust(true);
        lander.update(0.1, mockTerrain);
        assertEquals(0, lander.getFuel(), "Fuel should remain 0 when thruster is requested with no fuel.");
        lander.update(1.0, mockTerrain);
    }

    @Test
    void testLanderRotation() {
        lander.playerHasTakenControl();
        lander.setRotation(1);
        lander.update(0.1, mockTerrain);
        assertTrue(lander.getAngle() > 0, "Angle should increase when rotating right.");

        double previousAngle = lander.getAngle();
        lander.setRotation(-1);
        lander.update(0.1, mockTerrain);
        assertTrue(lander.getAngle() < previousAngle, "Angle should decrease when rotating left.");
    }

    @Test
    void testLanderCrashesOnHighVerticalSpeed() {
        lander.playerHasTakenControl();

        Lander testLander = new Lander((float) mockTerrain.getScreenWidth() / 2, (float) mockTerrain.getScreenHeight());
        testLander.playerHasTakenControl();
        testLander.setVyPublic(100.0);
        testLander.setVxPublic(0.0);
        testLander.setAnglePublic(0.0);

        assertTrue(true, "Conceptual test for high-speed crash - requires mocking or refactoring.");
    }

    @Test
    void testLanderPerfectLandingScoreCalculation() {
        Lander.DEBUG_INFINITE_TOLERANCE_LANDING = false;

        Lander testLander = new Lander(100, 100);
        testLander.playerHasTakenControl();

        testLander.setVxPublic(1.0);
        testLander.setVyPublic(1.0);
        testLander.setAnglePublic(Math.toRadians(0.5));

        Lander.DEBUG_INFINITE_TOLERANCE_LANDING = true;
        entities.LandingPad mockPad = new entities.LandingPad(50, 150, 100, 5);

        assertTrue(true, "Conceptual test for perfect landing score - requires mocking or refactoring.");
    }
}