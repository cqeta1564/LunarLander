package states;

import core.Game;
import entities.Lander;
import entities.Terrain;
import input.GameAction;
import input.InputHandler;
import input.KeyBindings;

import java.awt.*;

public class PlayingState implements GameState {

    private final StateManager stateManager;
    private final InputHandler inputHandler;
    private Terrain terrain;
    private Lander lander;
    private float cameraX = 0;
    private boolean playerControlTakenSinceEnter = false;

    public PlayingState(StateManager stateManager, InputHandler inputHandler) {
        this.stateManager = stateManager;
        this.inputHandler = inputHandler;
    }

    @Override
    public void init(StateManager manager) {
    }

    @Override
    public void onEnter() {
        System.out.println("Vstup do PlayingState.");
        if (this.terrain == null) {
            this.terrain = new Terrain(Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);
        }
        this.cameraX = 0;
        this.terrain.populateVisibleTerrain(cameraX, Game.DEFAULT_WIDTH);

        if (this.lander == null) {
            this.lander = new Lander((float) -Lander.INITIAL_FLYOVER_SPEED_X_PIXELS_S, Game.DEFAULT_HEIGHT * Lander.INITIAL_FLYOVER_START_Y_RATIO);
        } else {
            lander.reset((float) -Lander.INITIAL_FLYOVER_SPEED_X_PIXELS_S, Game.DEFAULT_HEIGHT * Lander.INITIAL_FLYOVER_START_Y_RATIO);
        }
        playerControlTakenSinceEnter = false;
    }

    @Override
    public void update(double deltaTime) {
        handleInput();

        if (lander != null) {
            lander.update(deltaTime, terrain);
        }
    }

    @Override
    public void handleInput() {
        if (inputHandler.isEscJustPressed()) {
            stateManager.setState(StateManager.StateType.MENU);
            return;
        }

        if (lander == null) return;

        boolean actionKeyPressed = false;
        int rotation = 0;
        if (inputHandler.isActionActive(GameAction.ROTATE_LEFT)) {
            rotation = -1;
            actionKeyPressed = true;
        } else if (inputHandler.isActionActive(GameAction.ROTATE_RIGHT)) {
            rotation = 1;
            actionKeyPressed = true;
        }
        lander.setRotation(rotation);

        boolean thrusting = inputHandler.isActionActive(GameAction.THRUST);
        if (thrusting) {
            actionKeyPressed = true;
        }
        lander.setThrusterActive(thrusting);

        if (actionKeyPressed && !playerControlTakenSinceEnter) {
            lander.playerHasTakenControl();
            playerControlTakenSinceEnter = true;
        }
    }

    @Override
    public void render(Graphics2D g) {
        g.setColor(new Color(10, 10, 20));
        g.fillRect(0, 0, Game.DEFAULT_WIDTH, Game.DEFAULT_HEIGHT);

        if (terrain != null) {
            terrain.render(g);
        }

        if (lander != null) {
            lander.render(g);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        if (lander != null) {
            g.drawString(String.format("X: %.0f Y: %.0f", lander.getX(), lander.getY()), 10, 20);
        }
        g.drawString("SKÓRE: 0", Game.DEFAULT_WIDTH - 150, 20);

        KeyBindings kb = inputHandler.getKeyBindings();
        if (kb != null) {
            g.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g.setColor(new Color(200, 200, 200, 180));
            int yPos = Game.DEFAULT_HEIGHT - 45;
            g.drawString("Ovládání:", 10, yPos);
            yPos += 12;
            g.drawString(" Tah: " + kb.getKeyTextForAction(GameAction.THRUST), 10, yPos);
            yPos += 12;
            g.drawString(" Vlevo: " + kb.getKeyTextForAction(GameAction.ROTATE_LEFT), 10, yPos);
            yPos += 12;
            g.drawString(" Vpravo: " + kb.getKeyTextForAction(GameAction.ROTATE_RIGHT), 10, yPos);
        }
    }

    @Override
    public void onExit() {
        System.out.println("Opuštění PlayingState.");
        if (lander != null) {
            lander.setThrusterActive(false);
            lander.setRotation(0);
        }
    }
}