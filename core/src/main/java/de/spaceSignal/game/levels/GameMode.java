package de.spaceSignal.game.levels;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import de.spaceSignal.game.entities.Bullet;
import de.spaceSignal.game.entities.Enemy;
import de.spaceSignal.game.entities.Player;
import de.spaceSignal.game.entities.Upgrade;

/**
 * Abstrakte Basisklasse für alle Spielmodi.
 * Definiert die gemeinsame Schnittstelle und Logik.
 */
public abstract class GameMode {
    protected Player player;
    protected Array<Bullet> bullets;
    protected Array<Enemy> enemies;
    protected Array<Upgrade> upgrades;
    protected int score;
    protected int wave;
    protected boolean isGameOver;

    public GameMode(Player player, Array<Bullet> bullets, Array<Enemy> enemies, Array<Upgrade> upgrades) {
        this.player = player;
        this.bullets = bullets;
        this.enemies = enemies;
        this.upgrades = upgrades;
        this.score = 0;
        this.wave = 1;
        this.isGameOver = false;
    }

    /**
     * Update-Logik des Spielmodus
     */
    public abstract void update(float delta);

    /**
     * Render-Logik für spielmodus-spezifische Elemente
     */
    public abstract void renderEntities(SpriteBatch batch);

    /**
     * UI-Rendering des Spielmodus
     */
    public abstract void renderUI(SpriteBatch batch, BitmapFont uiFont);

    /**
     * Prüft ob das Spiel vorbei ist
     */
    public abstract boolean checkGameOver();

    /**
     * Gibt die Victory-Nachricht zurück (null wenn keine Victory-Condition)
     */
    public abstract String getVictoryMessage();

    /**
     * Cleanup beim Beenden
     */
    public abstract void dispose();

    // Getter
    public int getScore() { return score; }
    public int getWave() { return wave; }
    public boolean isGameOver() { return isGameOver; }

    // Hilfsmethoden für alle Modi
    protected void incrementScore(int amount) {
        score += amount;
    }

    protected void incrementWave() {
        wave++;
    }

    protected void setGameOver(boolean gameOver) {
        this.isGameOver = gameOver;
    }
}
