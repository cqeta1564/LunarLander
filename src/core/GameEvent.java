package core;

public class GameEvent {
    public enum EventType {
        LANDER_CRASHED,
        LANDER_LANDED,
        FUEL_EMPTY
    }

    private EventType type;

    public GameEvent(EventType type) {
        this.type = type;
    }

    public EventType getType() {
        return type;
    }
}
