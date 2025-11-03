package de.spaceSignal.game.managers;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;

public class AssetManager implements Disposable {
    private static AssetManager instance;
    private final com.badlogic.gdx.assets.AssetManager manager;

    private AssetManager() {
        manager = new com.badlogic.gdx.assets.AssetManager();
    }

    public static AssetManager getInstance() {
        if (instance == null) {
            instance = new AssetManager();
        }
        return instance;
    }

    public void loadAll() {
        // Lade Musik
        manager.load("audio/music/background.mp3", Music.class);
        
        // Lade Soundeffekte
        manager.load("audio/sounds/shoot.wav", Sound.class);
        manager.load("audio/sounds/explosion.wav", Sound.class);
        manager.load("audio/sounds/powerup.wav", Sound.class);

        // Warte bis alles geladen ist
        manager.finishLoading();

        // Initialisiere den AudioManager mit den geladenen Assets
        AudioManager.getInstance().setAssets(
            manager.get("audio/music/background.mp3", Music.class),
            manager.get("audio/sounds/shoot.wav", Sound.class),
            manager.get("audio/sounds/explosion.wav", Sound.class),
            manager.get("audio/sounds/powerup.wav", Sound.class)
        );
    }

    @Override
    public void dispose() {
        manager.dispose();
    }
}
