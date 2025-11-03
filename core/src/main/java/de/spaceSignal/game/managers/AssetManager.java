package de.spaceSignal.game.managers;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

public class AssetManager implements Disposable {
    private static AssetManager instance;
    private final com.badlogic.gdx.assets.AssetManager manager;
    
    // Texturen für Gegner
    private Texture enemyTexture;
    private Texture scoutTexture;
    private Texture bomberTexture;
    private Texture bulletTexture;

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

        // Lade Gegner-Texturen
        manager.load("textures/enemies/enemy.png", Texture.class);
        manager.load("textures/enemies/scout.png", Texture.class);
        manager.load("textures/enemies/bomber.png", Texture.class);
        manager.load("textures/enemies/bullet.png", Texture.class);

        // Warte bis alles geladen ist
        manager.finishLoading();

        // Speichere die Texturen
        enemyTexture = manager.get("textures/enemies/enemy.png", Texture.class);
        scoutTexture = manager.get("textures/enemies/scout.png", Texture.class);
        bomberTexture = manager.get("textures/enemies/bomber.png", Texture.class);
        bulletTexture = manager.get("textures/enemies/bullet.png", Texture.class);

        // Initialisiere den AudioManager mit den geladenen Assets
        AudioManager.getInstance().setAssets(
            manager.get("audio/music/background.mp3", Music.class),
            manager.get("audio/sounds/shoot.wav", Sound.class),
            manager.get("audio/sounds/explosion.wav", Sound.class),
            manager.get("audio/sounds/powerup.wav", Sound.class)
        );
    }

    // Getter für die Texturen
    public Texture getEnemyTexture() { return enemyTexture; }
    public Texture getScoutTexture() { return scoutTexture; }
    public Texture getBomberTexture() { return bomberTexture; }
    public Texture getBulletTexture() { return bulletTexture; }

    @Override
    public void dispose() {
        manager.dispose();
    }
}
