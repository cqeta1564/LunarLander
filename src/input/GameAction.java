package input;

public enum GameAction {
    ROTATE_LEFT("Rotace vlevo"),
    ROTATE_RIGHT("Rotace vpravo"),
    THRUST("Tah motoru");

    private final String displayName;

    GameAction(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static GameAction[] getAllActions() {
        return values();
    }
}