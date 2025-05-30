import audio.AudioManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Tests for the {@link AudioManager} class.
 * This class verifies the correct functioning of the audio management system,
 * including singleton behavior and volume controls.
 */
class AudioManagerTest {

    private AudioManager audioManager;

    /**
     * Sets up the test environment before each test.
     * Initializes the AudioManager instance.
     */
    @BeforeEach
    void setUp() {
        audioManager = AudioManager.getInstance(); // Get the singleton instance
    }

    /**
     * Tests that {@link AudioManager#getInstance()} always returns the same instance,
     * verifying the singleton pattern implementation.
     */
    @Test
    void testGetInstanceReturnsSameInstance() {
        AudioManager instance1 = AudioManager.getInstance();
        AudioManager instance2 = AudioManager.getInstance();
        assertSame(instance1, instance2, "AudioManager.getInstance() should return the same instance.");
    }

    /**
     * Tests the global volume setting, ensuring that values are clamped
     * to the valid range of 0-100.
     */
    @Test
    void testGlobalVolumeClamping() {
        audioManager.setGlobalVolume(50);
        assertEquals(50, audioManager.getGlobalVolume(), "Volume should be set to 50.");

        audioManager.setGlobalVolume(150); // Test upper clamp
        assertEquals(100, audioManager.getGlobalVolume(), "Volume should be clamped to 100.");

        audioManager.setGlobalVolume(-50); // Test lower clamp
        assertEquals(0, audioManager.getGlobalVolume(), "Volume should be clamped to 0.");
    }

    /**
     * Tests the {@link AudioManager#increaseVolume(int)} and {@link AudioManager#decreaseVolume(int)} methods,
     * including their clamping behavior at the 0 and 100 boundaries.
     */
    @Test
    void testIncreaseAndDecreaseVolume() {
        audioManager.setGlobalVolume(50); // Start at a mid-range volume

        audioManager.increaseVolume(10);
        assertEquals(60, audioManager.getGlobalVolume(), "Volume should be 60 after increase.");

        audioManager.decreaseVolume(20);
        assertEquals(40, audioManager.getGlobalVolume(), "Volume should be 40 after decrease.");

        audioManager.increaseVolume(100); // Increase beyond max
        assertEquals(100, audioManager.getGlobalVolume(), "Volume should be capped at 100 on large increase.");

        audioManager.setGlobalVolume(50); // Reset for decrease test
        audioManager.decreaseVolume(200); // Decrease below min
        assertEquals(0, audioManager.getGlobalVolume(), "Volume should be floored at 0 on large decrease.");
    }
}