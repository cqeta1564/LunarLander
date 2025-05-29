package audio;

import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

public class AudioManager {
    private static AudioManager instance;
    private final Map<SoundEffect, Clip> soundMap;
    private int currentGlobalVolumePercent;

    private AudioManager() {
        this.currentGlobalVolumePercent = 70;
        this.soundMap = new EnumMap<>(SoundEffect.class);
        loadAllSounds();
    }

    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    private void loadAllSounds() {
        loadSound(SoundEffect.BUTTON_CLICK, "/sounds/buttonClick.wav");
        loadSound(SoundEffect.ENGINE, "/sounds/engine.wav");
        loadSound(SoundEffect.LUNAR_EXPLOSION, "/sounds/lunarExplosion.wav");
        loadSound(SoundEffect.PLAYER_DEAD, "/sounds/playerDead.wav");
    }

    private void loadSound(SoundEffect effect, String filePath) {
        try {
            URL soundUrl = AudioManager.class.getResource(filePath);
            if (soundUrl == null) {
                System.err.println("Chyba: Zvukový soubor nenalezen - " + filePath);
                return;
            }
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            soundMap.put(effect, clip);
        } catch (UnsupportedAudioFileException e) {
            System.err.println("Chyba: Nepodporovaný formát zvukového souboru - " + filePath + ": " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Chyba: I/O při načítání zvuku - " + filePath + ": " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.err.println("Chyba: Linka pro zvuk není dostupná - " + filePath + ": " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Obecná chyba při načítání zvuku - " + filePath + ": " + e.getMessage());
        }
    }

    private void setClipVolume(Clip clip) {
        if (clip == null || !clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (currentGlobalVolumePercent == 0) {
            gainControl.setValue(gainControl.getMinimum());
        } else {
            float volumeLevel = currentGlobalVolumePercent / 100.0f;
            float minDb = gainControl.getMinimum();
            float maxDb = 0.0f;

            if (minDb >= 0.0f || minDb < -100.0f) {
                minDb = -80.0f;
            }
            if (maxDb < minDb) {
                maxDb = minDb + 1.0f;
            }
            if (maxDb > gainControl.getMaximum()) {
                maxDb = gainControl.getMaximum();
            }


            float gain = minDb + (volumeLevel * (maxDb - minDb));
            gain = Math.max(minDb, Math.min(gain, maxDb));

            try {
                gainControl.setValue(gain);
            } catch (IllegalArgumentException e) {
                System.err.println("Chybná hodnota pro nastavení hlasitosti: " + gain + "dB. Rozsah: " + gainControl.getMinimum() + " až " + gainControl.getMaximum() + "dB. " + e.getMessage());
            }
        }
    }

    public void playSound(SoundEffect effect) {
        Clip clip = soundMap.get(effect);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            setClipVolume(clip);
            clip.start();
        } else {
            System.err.println("Pokus o přehrání nenahraného zvuku: " + effect);
        }
    }

    public void loopSound(SoundEffect effect) {
        Clip clip = soundMap.get(effect);
        if (clip != null) {
            if (!clip.isRunning()) {
                clip.setFramePosition(0);
                setClipVolume(clip);
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } else {
            System.err.println("Pokus o spuštění smyčky nenahraného zvuku: " + effect);
        }
    }

    public void stopSound(SoundEffect effect) {
        Clip clip = soundMap.get(effect);
        if (clip != null && clip.isRunning()) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    public int getGlobalVolume() {
        return currentGlobalVolumePercent;
    }

    public void setGlobalVolume(int volumePercent) {
        if (volumePercent < 0) {
            this.currentGlobalVolumePercent = 0;
        } else if (volumePercent > 100) {
            this.currentGlobalVolumePercent = 100;
        } else {
            this.currentGlobalVolumePercent = volumePercent;
        }
        System.out.println("Globální hlasitost nastavena na: " + this.currentGlobalVolumePercent + "%");

        if (soundMap != null) {
            for (Clip clip : soundMap.values()) {
                if (clip != null) {
                    setClipVolume(clip);
                }
            }
        }
    }

    public void increaseVolume(int amount) {
        setGlobalVolume(this.currentGlobalVolumePercent + amount);
    }

    public void decreaseVolume(int amount) {
        setGlobalVolume(this.currentGlobalVolumePercent - amount);
    }

    public void shutdown() {
        if (soundMap != null) {
            for (Clip clip : soundMap.values()) {
                if (clip != null) {
                    if (clip.isRunning()) {
                        clip.stop();
                    }
                    clip.close();
                }
            }
            soundMap.clear();
        }
        System.out.println("AudioManager byl ukončen a zdroje uvolněny.");
    }

    public enum SoundEffect {
        BUTTON_CLICK, ENGINE, LUNAR_EXPLOSION, PLAYER_DEAD
    }
}