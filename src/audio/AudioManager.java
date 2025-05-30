package audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

/**
 * Manages loading and playback of sound effects for the game.
 * Implemented as a Singleton to ensure a single instance manages all audio.
 */
public class AudioManager {
    // Singleton instance of the AudioManager.
    private static AudioManager instance;
    // Map to store loaded sound clips, associated with their SoundEffect enum.
    private final Map<SoundEffect, Clip> soundMap;
    // Current global volume level in percentage (0-100).
    private int currentGlobalVolumePercent;

    /**
     * Private constructor to initialize the AudioManager.
     * Sets the default volume and loads all sound effects.
     */
    private AudioManager() {
        this.currentGlobalVolumePercent = 70; // Default volume set to 70%.
        this.soundMap = new EnumMap<>(SoundEffect.class); // Initialize the map for sound effects.
        loadAllSounds(); // Load all predefined sound effects.
    }

    /**
     * Returns the singleton instance of the AudioManager.
     * Creates the instance if it doesn't exist yet (lazy initialization).
     *
     * @return The singleton AudioManager instance.
     */
    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    /**
     * Loads all predefined sound effects into the soundMap.
     */
    private void loadAllSounds() {
        loadSound(SoundEffect.BUTTON_CLICK, "/sounds/buttonClick.wav");
        loadSound(SoundEffect.ENGINE, "/sounds/engine.wav");
        loadSound(SoundEffect.LUNAR_EXPLOSION, "/sounds/lunarExplosion.wav");
        loadSound(SoundEffect.PLAYER_DEAD, "/sounds/playerDead.wav");
    }

    /**
     * Loads a single sound effect from the given file path and associates it with a SoundEffect enum.
     * Handles potential errors during loading, such as file not found or unsupported audio format.
     *
     * @param effect   The SoundEffect enum to associate with the loaded sound.
     * @param filePath The path to the sound file (within the resources).
     */
    private void loadSound(SoundEffect effect, String filePath) {
        try {
            // Get the URL of the sound file from resources.
            URL soundUrl = AudioManager.class.getResource(filePath);
            if (soundUrl == null) {
                System.err.println("Error: Sound file not found - " + filePath); // Chyba: Zvukový soubor nenalezen
                return;
            }
            // Get an audio input stream from the sound file URL.
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
            // Get a clip resource.
            Clip clip = AudioSystem.getClip();
            // Open the audio input stream with the clip.
            clip.open(audioInputStream);
            // Store the loaded clip in the map.
            soundMap.put(effect, clip);
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Error: Unsupported audio file format - " + filePath + ": " + e.getMessage()); // Chyba: Nepodporovaný formát zvukového souboru
        } catch (IOException e) {
            System.err.println("Error: I/O during sound loading - " + filePath + ": " + e.getMessage()); // Chyba: I/O při načítání zvuku
        } catch (LineUnavailableException e) {
            System.err.println("Error: Audio line unavailable - " + filePath + ": " + e.getMessage()); // Chyba: Linka pro zvuk není dostupná
        } catch (Exception e) {
            // Catch-all for any other unexpected errors during sound loading.
            System.err.println("General error loading sound - " + filePath + ": " + e.getMessage()); // Obecná chyba při načítání zvuku
        }
    }

    /**
     * Sets the volume for a specific audio Clip based on the currentGlobalVolumePercent.
     *
     * @param clip The Clip whose volume needs to be set.
     */
    private void setClipVolume(Clip clip) {
        // Check if the clip is valid and supports master gain control.
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        // Get the gain control for the clip.
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (currentGlobalVolumePercent == 0) {
            // If volume is 0%, set gain to its minimum (mute).
            gainControl.setValue(gainControl.getMinimum());
        } else {
            // Convert percentage volume to a float (0.0 to 1.0).
            float volumeLevel = currentGlobalVolumePercent / 100.0f;
            // Define a practical minimum decibel level if the actual minimum is too low or positive.
            float minDb = gainControl.getMinimum();
            float maxDb = 0.0f; // Target max decibels (0dB is typically full volume without amplification).

            // Ensure minDb is a reasonable negative value for logarithmic scaling.
            if (minDb >= 0.0f || minDb < -100.0f) { // If minDb is positive or extremely low, use -80dB as a practical minimum.
                minDb = -80.0f;
            }
            // Ensure maxDb is greater than minDb.
            if (maxDb < minDb) {
                maxDb = minDb + 1.0f; // Make sure maxDb is at least slightly above minDb.
            }
            // Ensure maxDb does not exceed the control's maximum.
            if (maxDb > gainControl.getMaximum()) {
                maxDb = gainControl.getMaximum();
            }

            // Calculate gain in decibels using a logarithmic scale.
            // This provides a more natural perception of volume change.
            float gain = minDb + (volumeLevel * (maxDb - minDb));
            // Clamp the gain to the valid range of the gain control.
            gain = Math.max(minDb, Math.min(gain, maxDb));

            try {
                // Set the calculated gain value.
                gainControl.setValue(gain);
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid value for volume setting: " + gain + "dB. Range: " + gainControl.getMinimum() + " to " + gainControl.getMaximum() + "dB. " + e.getMessage()); // Chybná hodnota pro nastavení hlasitosti
            }
        }
    }

    /**
     * Plays a loaded sound effect once from the beginning.
     * If the sound is already playing, it's stopped and restarted.
     *
     * @param effect The SoundEffect to play.
     */
    public void playSound(SoundEffect effect) {
        Clip clip = soundMap.get(effect);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop(); // Stop if already playing.
            }
            clip.setFramePosition(0); // Rewind to the beginning.
            setClipVolume(clip);      // Apply current global volume.
            clip.start();             // Play the sound.
        } else {
            System.err.println("Attempt to play unloaded sound: " + effect); // Pokus o přehrání nenahraného zvuku
        }
    }

    /**
     * Loops a loaded sound effect continuously from the beginning.
     * If the sound is already looping, this method does nothing.
     *
     * @param effect The SoundEffect to loop.
     */
    public void loopSound(SoundEffect effect) {
        Clip clip = soundMap.get(effect);
        if (clip != null) {
            if (!clip.isRunning()) { // Only start looping if not already running.
                clip.setFramePosition(0); // Rewind to the beginning.
                setClipVolume(clip);      // Apply current global volume.
                clip.loop(Clip.LOOP_CONTINUOUSLY); // Loop indefinitely.
            }
        } else {
            System.err.println("Attempt to loop unloaded sound: " + effect); // Pokus o spuštění smyčky nenahraného zvuku
        }
    }

    /**
     * Stops a currently playing or looping sound effect and rewinds it.
     *
     * @param effect The SoundEffect to stop.
     */
    public void stopSound(SoundEffect effect) {
        Clip clip = soundMap.get(effect);
        if (clip != null && clip.isRunning()) {
            clip.stop(); // Stop the sound.
            clip.setFramePosition(0); // Rewind to the beginning.
        }
    }

    /**
     * Gets the current global volume level.
     *
     * @return The global volume in percentage (0-100).
     */
    public int getGlobalVolume() {
        return currentGlobalVolumePercent;
    }

    /**
     * Sets the global volume level.
     * This affects all currently playing and future sounds.
     * The volume is clamped between 0 and 100.
     *
     * @param volumePercent The new global volume in percentage (0-100).
     */
    public void setGlobalVolume(int volumePercent) {
        // Clamp volume to the 0-100 range.
        if (volumePercent < 0) {
            this.currentGlobalVolumePercent = 0;
        } else if (volumePercent > 100) {
            this.currentGlobalVolumePercent = 100;
        } else {
            this.currentGlobalVolumePercent = volumePercent;
        }
        System.out.println("Global volume set to: " + this.currentGlobalVolumePercent + "%"); // Globální hlasitost nastavena na:

        // Apply the new volume to all currently loaded (and potentially playing) clips.
        if (soundMap != null) {
            for (Clip clip : soundMap.values()) {
                if (clip != null) {
                    setClipVolume(clip);
                }
            }
        }
    }

    /**
     * Increases the global volume by a specified amount.
     *
     * @param amount The amount (in percentage points) to increase the volume by.
     */
    public void increaseVolume(int amount) {
        setGlobalVolume(this.currentGlobalVolumePercent + amount);
    }

    /**
     * Decreases the global volume by a specified amount.
     *
     * @param amount The amount (in percentage points) to decrease the volume by.
     */
    public void decreaseVolume(int amount) {
        setGlobalVolume(this.currentGlobalVolumePercent - amount);
    }

    /**
     * Shuts down the AudioManager, stopping all sounds and releasing audio resources.
     * This should be called when the game is closing.
     */
    public void shutdown() {
        if (soundMap != null) {
            for (Clip clip : soundMap.values()) {
                if (clip != null) {
                    if (clip.isRunning()) {
                        clip.stop(); // Stop the clip if it's running.
                    }
                    clip.close(); // Release system resources used by the clip.
                }
            }
            soundMap.clear(); // Clear the map of sound effects.
        }
        System.out.println("AudioManager has been shut down and resources released."); // AudioManager byl ukončen a zdroje uvolněny.
    }

    /**
     * Enum representing the different sound effects used in the game.
     */
    public enum SoundEffect {
        /**
         * Sound for button clicks in the UI.
         */
        BUTTON_CLICK,
        /**
         * Sound for the lander's engine thrust.
         */
        ENGINE,
        /**
         * Sound for the lander exploding.
         */
        LUNAR_EXPLOSION,
        /**
         * Sound for when the player runs out of fuel after a successful landing or crashes.
         */
        PLAYER_DEAD
    }
}