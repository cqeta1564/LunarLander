import audio.AudioManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Testy pro AudioManager.
 */
class AudioManagerTest {

    private AudioManager audioManager;

    @BeforeEach
    void setUp() {
        audioManager = AudioManager.getInstance();
    }

    @Test
    void testGetInstanceReturnsSameInstance() {
        AudioManager instance1 = AudioManager.getInstance();
        AudioManager instance2 = AudioManager.getInstance();
        assertSame(instance1, instance2, "AudioManager.getInstance() should return the same instance.");
    }

    @Test
    void testGlobalVolumeClamping() {
        audioManager.setGlobalVolume(50);
        assertEquals(50, audioManager.getGlobalVolume(), "Volume should be set to 50.");

        audioManager.setGlobalVolume(150);
        assertEquals(100, audioManager.getGlobalVolume(), "Volume should be clamped to 100.");

        audioManager.setGlobalVolume(-50);
        assertEquals(0, audioManager.getGlobalVolume(), "Volume should be clamped to 0.");
    }

    @Test
    void testIncreaseAndDecreaseVolume() {
        audioManager.setGlobalVolume(50);
        audioManager.increaseVolume(10);
        assertEquals(60, audioManager.getGlobalVolume(), "Volume should be 60 after increase.");

        audioManager.decreaseVolume(20);
        assertEquals(40, audioManager.getGlobalVolume(), "Volume should be 40 after decrease.");

        audioManager.increaseVolume(100);
        assertEquals(100, audioManager.getGlobalVolume(), "Volume should be capped at 100 on large increase.");

        audioManager.decreaseVolume(200);
        assertEquals(0, audioManager.getGlobalVolume(), "Volume should be floored at 0 on large decrease.");
    }
}