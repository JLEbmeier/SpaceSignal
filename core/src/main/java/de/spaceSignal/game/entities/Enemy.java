package de.spaceSignal.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.spaceSignal.game.util.Constants;

public class Enemy {
    private Sprite sprite;
    private Vector2 position;
    private Rectangle bounds;
    private float health;
    private boolean alive;
    private final String gameMode;
    private float speed;

    public Enemy(float x, float y, float health, Texture texture, String gameMode) {
        this.position = new Vector2(x, y);
        this.health = health;
        this.gameMode = gameMode;
        this.alive = true;
        this.speed = Constants.ENEMY_SPEED;
        this.sprite = new Sprite(texture);
        sprite.setPosition(x, y);
        sprite.setSize(Constants.ENEMY_WIDTH, Constants.ENEMY_HEIGHT);
        bounds = new Rectangle(x, y, Constants.ENEMY_WIDTH, Constants.ENEMY_HEIGHT);
    }

    public void update(float delta) {
        if (!alive) return;

        // Bewege den Gegner nach unten (standardmäßig, anpassen je nach Spielmodus)
        position.y -= speed * delta;
        sprite.setPosition(position.x, position.y);
        bounds.setPosition(position.x, position.y);

        // Entferne Gegner, wenn sie den Bildschirm verlassen (optional)
        if (position.y < -Constants.ENEMY_HEIGHT) {
            alive = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (alive) {
            sprite.draw(batch);
        }
    }

    public void takeDamage(float damage) {
        if (alive) {
            health -= damage;
            if (health <= 0) {
                alive = false;
            }
        }
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isAlive() {
        return alive;
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }
}
