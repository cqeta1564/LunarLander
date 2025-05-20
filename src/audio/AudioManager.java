package audio;

public class AudioManager {
    private static AudioManager instance;
    private int globalVolume; // Hlasitost 0-100

    private AudioManager() {
        this.globalVolume = 70; // Výchozí hlasitost
    }

    public static synchronized AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public int getGlobalVolume() {
        return globalVolume;
    }

    public void setGlobalVolume(int volume) {
        if (volume < 0) {
            this.globalVolume = 0;
        } else if (volume > 100) {
            this.globalVolume = 100;
        } else {
            this.globalVolume = volume;
        }
        System.out.println("Global volume set to: " + this.globalVolume + "%"); // Pro ladění, lze ponechat/odstranit
    }

    public void increaseVolume(int amount) {
        setGlobalVolume(this.globalVolume + amount);
    }

    public void decreaseVolume(int amount) {
        setGlobalVolume(this.globalVolume - amount);
    }

    // public void playSound(Sound sound) { ... }
    // public void playMusic(Music music) { ... }
    // public void stopAllSounds() { ... }
}