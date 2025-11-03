package de.spaceSignal.game.managers;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

public class AudioManager implements Disposable {
    private static AudioManager instance;
    private Music backgroundMusic;
    private Sound shootSound;
    private Sound explosionSound;
    private Sound powerupSound;
    private float soundVolume = 1.0f;
    private float musicVolume = 0.5f;
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;

    private AudioManager() {}

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void setAssets(Music bgMusic, Sound shoot, Sound explosion, Sound powerup) {
        this.backgroundMusic = bgMusic;
        this.shootSound = shoot;
        this.explosionSound = explosion;
        this.powerupSound = powerup;
        
        if (backgroundMusic != null) {
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(musicVolume);
        }
    }

    public void playBackgroundMusic() {
        if (musicEnabled && backgroundMusic != null) {
            backgroundMusic.play();
        }
    }

    public void pauseBackgroundMusic() {
        if (backgroundMusic != null) {
            backgroundMusic.pause();
        }
    }

    public void playShootSound() {
        if (soundEnabled && shootSound != null) {
            shootSound.play(soundVolume);
        }
    }

    public void playExplosionSound() {
        if (soundEnabled && explosionSound != null) {
            explosionSound.play(soundVolume);
        }
    }

    public void playPowerupSound() {
        if (soundEnabled && powerupSound != null) {
            powerupSound.play(soundVolume);
        }
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0, Math.min(1, volume));
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume));
        if (backgroundMusic != null) {
            backgroundMusic.setVolume(musicVolume);
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled && backgroundMusic != null) {
            backgroundMusic.pause();
        } else if (enabled && backgroundMusic != null) {
            backgroundMusic.play();
        }
    }

    @Override
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