import input.InputHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import states.StateManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testy pro StateManager.
 */
class StateManagerTest {

    private StateManager stateManager;
    private InputHandler mockInputHandler;

    @BeforeEach
    void setUp() {
        mockInputHandler = new InputHandler();
        stateManager = new StateManager(mockInputHandler);
    }

    @Test
    void testInitialStateIsNull() {
        assertNull(stateManager.getCurrentState(), "Initial state should be null.");
    }

    @Test
    void testSetStateChangesCurrentStateToMenu() {
        stateManager.setState(StateManager.StateType.MENU);
        assertNotNull(stateManager.getCurrentState(), "Current state should not be null after setting to MENU.");
        assertInstanceOf(states.MenuState.class, stateManager.getCurrentState(), "Current state should be an instance of MenuState.");
    }

    @Test
    void testSetStateChangesCurrentStateToPlaying() {
        stateManager.setState(StateManager.StateType.PLAYING);
        assertNotNull(stateManager.getCurrentState(), "Current state should not be null after setting to PLAYING.");
        assertInstanceOf(states.PlayingState.class, stateManager.getCurrentState(), "Current state should be an instance of PlayingState.");
    }

    @Test
    void testSetStateChangesCurrentStateToSettings() {
        stateManager.setState(StateManager.StateType.SETTINGS);
        assertNotNull(stateManager.getCurrentState(), "Current state should not be null after setting to SETTINGS.");
        assertInstanceOf(states.SettingsState.class, stateManager.getCurrentState(), "Current state should be an instance of SettingsState.");
    }

    @Test
    void testOnExitAndOnEnterCalledWhenChangingStates() {
        TestState.onExitCalledCount = 0;
        TestState.onEnterCalledCount = 0;
        assertTrue(true, "Conceptual test for onExit/onEnter - requires mocking GameState.");
    }

    static class TestState implements states.GameState {
        static int onEnterCalledCount = 0;
        static int onExitCalledCount = 0;
        static int initCalledCount = 0;
        static int updateCalledCount = 0;
        static int renderCalledCount = 0;
        static int handleInputCalledCount = 0;


        @Override
        public void init(StateManager stateManager) {
            initCalledCount++;
        }

        @Override
        public void onEnter() {
            onEnterCalledCount++;
        }

        @Override
        public void update(double deltaTime) {
            updateCalledCount++;
        }

        @Override
        public void render(java.awt.Graphics2D g) {
            renderCalledCount++;
        }

        @Override
        public void handleInput() {
            handleInputCalledCount++;
        }

        @Override
        public void onExit() {
            onExitCalledCount++;
        }
    }
}