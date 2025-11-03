package de.spaceSignal.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import de.spaceSignal.game.screens.MainMenuScreen;
import de.spaceSignal.game.managers.AssetManager;
import de.spaceSignal.game.managers.AudioManager;

public class Main extends Game {
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();
        
        // Initialize assets and load audio
        AssetManager.getInstance().loadAll();
        
        // Start background music
        AudioManager.getInstance().playBackgroundMusic();
        
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
        if (getScreen() != null) {
            getScreen().dispose();
        }
        // Dispose assets when the game closes
        AssetManager.getInstance().dispose();
    }
}
