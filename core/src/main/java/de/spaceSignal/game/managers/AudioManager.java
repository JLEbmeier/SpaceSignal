package de.spaceSignal.game.managers;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

public class AudioManager {
    private static AudioManager instance;

    private Music backgroundMusic;
    private Sound shootSound;
    private Sound explosionSound;
    private Sound powerupSound;

    // Lautstärke-Einstellungen (0.0 bis 1.0)
    private float musicVolume = 0.5f;
    private float soundVolume = 0.7f;

    // Ein/Aus-Einstellungen
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;

    private AudioManager() {
        // Private Konstruktor für Singleton
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void setAssets(Music backgroundMusic, Sound shootSound, Sound explosionSound, Sound powerupSound) {
        this.backgroundMusic = backgroundMusic;
        this.shootSound = shootSound;
        this.explosionSound = explosionSound;
        this.powerupSound = powerupSound;

        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(musicVolume);
            if (musicEnabled) {
                backgroundMusic.play();
            }
        }
    }

    // Musik-Kontrolle
    public void playBackgroundMusic() {
        if (backgroundMusic != null && musicEnabled && !backgroundMusic.isPlaying()) {
            backgroundMusic.play();
        }
    }

    public void stopBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.stop();
        }
    }

    public void pauseBackgroundMusic() {
        if (backgroundMusic != null && backgroundMusic.isPlaying()) {
            backgroundMusic.pause();
        }
    }

    // Sound-Effekte
    public void playShootSound() {
        if (shootSound != null && soundEnabled) {
            shootSound.play(soundVolume);
        }
    }

    public void playExplosionSound() {
        if (explosionSound != null && soundEnabled) {
            explosionSound.play(soundVolume);
        }
    }

    public void playPowerupSound() {
        if (powerupSound != null && soundEnabled) {
            powerupSound.play(soundVolume);
        }
    }

    // Lautstärke-Einstellungen
    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, Math.min(1f, volume));
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(this.musicVolume);
        }
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0f, Math.min(1f, volume));
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    // Ein/Aus-Kontrolle
    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (backgroundMusic != null) {
            if (enabled && !backgroundMusic.isPlaying()) {
                backgroundMusic.play();
            } else if (!enabled && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }
        }
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    // Lautstärke um 10% erhöhen
    public void increaseMusicVolume() {
        setMusicVolume(musicVolume + 0.1f);
    }

    public void decreaseMusicVolume() {
        setMusicVolume(musicVolume - 0.1f);
    }

    public void increaseSoundVolume() {
        setSoundVolume(soundVolume + 0.1f);
    }

    public void decreaseSoundVolume() {
        setSoundVolume(soundVolume - 0.1f);
    }

    // Toggle-Funktionen
    public void toggleMusic() {
        setMusicEnabled(!musicEnabled);
    }

    public void toggleSound() {
        setSoundEnabled(!soundEnabled);
    }

    public void dispose() {
        if (backgroundMusic != null) {
            backgroundMusic.dispose();
        }
        if (shootSound != null) {
            shootSound.dispose();
        }
        if (explosionSound != null) {
            explosionSound.dispose();
        }
        if (powerupSound != null) {
            powerupSound.dispose();
        }
    }
}
