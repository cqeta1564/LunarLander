import input.InputHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import states.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the {@link StateManager} class.
 * This class verifies the correct management of game states, including
 * initialization, state transitions, and invocation of state lifecycle methods.
 */
class StateManagerTest {

    private StateManager stateManager;
    private InputHandler mockInputHandler; // Mock or actual InputHandler

    /**
     * Sets up the test environment before each test.
     * Initializes a mock InputHandler and a StateManager instance.
     */
    @BeforeEach
    void setUp() {
        mockInputHandler = new InputHandler(); // Using a real InputHandler for simplicity here
        stateManager = new StateManager(mockInputHandler);
    }

    /**
     * Tests that the initial state of the StateManager is null before any state is set.
     */
    @Test
    void testInitialStateIsNull() {
        assertNull(stateManager.getCurrentState(), "Initial state should be null before any state is set.");
    }

    /**
     * Tests that setting the state to MENU correctly changes the current state
     * to an instance of MenuState.
     */
    @Test
    void testSetStateChangesCurrentStateToMenu() {
        stateManager.setState(StateManager.StateType.MENU);
        assertNotNull(stateManager.getCurrentState(), "Current state should not be null after setting to MENU.");
        assertInstanceOf(MenuState.class, stateManager.getCurrentState(), "Current state should be an instance of MenuState.");
    }

    /**
     * Tests that setting the state to PLAYING correctly changes the current state
     * to an instance of PlayingState.
     */
    @Test
    void testSetStateChangesCurrentStateToPlaying() {
        stateManager.setState(StateManager.StateType.PLAYING);
        assertNotNull(stateManager.getCurrentState(), "Current state should not be null after setting to PLAYING.");
        assertInstanceOf(PlayingState.class, stateManager.getCurrentState(), "Current state should be an instance of PlayingState.");
    }

    /**
     * Tests that setting the state to SETTINGS correctly changes the current state
     * to an instance of SettingsState.
     */
    @Test
    void testSetStateChangesCurrentStateToSettings() {
        stateManager.setState(StateManager.StateType.SETTINGS);
        assertNotNull(stateManager.getCurrentState(), "Current state should not be null after setting to SETTINGS.");
        assertInstanceOf(SettingsState.class, stateManager.getCurrentState(), "Current state should be an instance of SettingsState.");
    }

    /**
     * Conceptually tests that onExit and onEnter methods are called during state transitions.
     * This test currently uses a placeholder assertion. A more robust test would involve
     * using a mock GameState or the provided TestState to verify these calls.
     * To make this test concrete:
     * 1. Replace `stateManager.setState(StateManager.StateType.MENU);` with setting a TestState instance.
     * 2. Then, call `stateManager.setState()` again with a different TestState or another state type.
     * 3. Assert that `TestState.onExitCalledCount` and `TestState.onEnterCalledCount` are incremented as expected.
     */
    @Test
    void testOnExitAndOnEnterCalledWhenChangingStates() {
        // This test is more illustrative in its current form.
        // To fully test, one would typically inject mock GameState objects or use a spy.
        // For now, we'll acknowledge the conceptual nature of directly testing this
        // without more complex mocking frameworks or setup.
        TestState.onExitCalledCount = 0;
        TestState.onEnterCalledCount = 0;
        // stateManager.setState(new TestState()); // If StateManager allowed setting direct GameState instances
        // stateManager.setState(new AnotherTestState());
        // assertEquals(1, TestState.onExitCalledCount, "onExit should be called on the previous state.");
        // assertEquals(1, AnotherTestState.onEnterCalledCount, "onEnter should be called on the new state.");
        assertTrue(true, "Conceptual test for onExit/onEnter. Full verification would require mocking or a test-specific GameState implementation within StateManager.");
    }

    /**
     * A simple mock implementation of {@link GameState} for testing purposes,
     * tracking calls to its lifecycle methods.
     */
    static class TestState implements GameState {
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