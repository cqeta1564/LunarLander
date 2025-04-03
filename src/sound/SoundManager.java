package sound;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundManager {
    private Clip engineSound;
    private Clip landingSound;
    private Clip crashSound;
    private boolean enginePlaying;

    public SoundManager() {
        loadSounds();
    }

    private void loadSounds() {
        try {
            engineSound = loadClipFromResource("/sounds/engine.wav");
            landingSound = loadClipFromResource("/sounds/landing.wav");
            crashSound = loadClipFromResource("/sounds/lunarExplosion.wav");

            if (engineSound != null) {
                engineSound.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (Exception e) {
            System.err.println("Chyba při načítání zvuků: ");
            e.printStackTrace();
        }
    }

    private Clip loadClipFromResource(String resourcePath) {
        try {
            // Získání URL zdroje
            URL soundUrl = getClass().getResource(resourcePath);
            if (soundUrl == null) {
                throw new RuntimeException("Zvukový soubor nebyl nalezen: " + resourcePath);
            }

            // Načtení zvukového streamu
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundUrl);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            return clip;
        } catch (Exception e) {
            System.err.println("Chyba při načítání zvuku: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }

    private Clip getClip(String soundFile) throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        URL url = getClass().getResource(soundFile);
        if (url == null) {
            System.err.println("Zvukový soubor nebyl nalezen: " + soundFile);
            return null;
        }
        AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
        Clip clip = AudioSystem.getClip();
        clip.open(audioIn);
        return clip;
    }

    public void playEngine(boolean playing) {
        if (engineSound == null) return;

        if (playing && !enginePlaying) {
            engineSound.setFramePosition(0);
            engineSound.start();
            enginePlaying = true;
        } else if (!playing && enginePlaying) {
            engineSound.stop();
            enginePlaying = false;
        }
    }

    public void playLanding() {
        playSound(landingSound);
    }

    public void playCrash() {
        playSound(crashSound);
    }

    private void playSound(Clip clip) {
        if (clip == null) return;
        clip.setFramePosition(0);
        clip.start();
    }

    public void stopAll() {
        if (engineSound != null) engineSound.stop();
        if (landingSound != null) landingSound.stop();
        if (crashSound != null) crashSound.stop();
        enginePlaying = false;
    }

    public boolean isEnginePlaying() {
        return enginePlaying;
    }
}