package physics;

import model.Lander;

public interface PhysicsStrategy {
    void applyPhysics(Lander lander, double deltaTime);
}
