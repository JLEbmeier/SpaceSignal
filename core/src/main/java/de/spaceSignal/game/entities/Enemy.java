package de.spaceSignal.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import de.spaceSignal.game.managers.AudioManager;
import de.spaceSignal.game.util.Constants;

public class Enemy {
    protected final Sprite sprite;
    protected final Vector2 position;
    protected final Rectangle bounds;
    protected float health;
    protected boolean alive;
    protected float speed;
    protected final Array<Bullet> bullets;
    protected float fireTimer;

    public Enemy(float x, float y, float health, Texture texture, String gameMode) {
        this.position = new Vector2(x, y);
        this.health = health;
        this.alive = true;
        this.speed = Constants.ENEMY_SPEED;
        this.sprite = new Sprite(texture);
        sprite.setPosition(x, y);
        sprite.setSize(Constants.ENEMY_WIDTH, Constants.ENEMY_HEIGHT);
        bounds = new Rectangle(x, y, Constants.ENEMY_WIDTH, Constants.ENEMY_HEIGHT);
        this.bullets = new Array<>();
        this.fireTimer = 0;
    }

    // Getter Methoden
    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getHealth() {
        return health;
    }

    public boolean isAlive() {
        return alive;
    }

    public float getSpeed() {
        return speed;
    }

    public Array<Bullet> getBullets() {
        return bullets;
    }

    // Setter Methoden
    public void setHealth(float health) {
        this.health = health;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void update(float delta) {
        if (!alive) return;

        // Bewege den Gegner nach unten (standardmäßig, anpassen je nach Spielmodus)
        position.y -= speed * delta;
        updateBounds();

        // Entferne Gegner, wenn sie den Bildschirm verlassen (optional)
        if (position.y < -Constants.ENEMY_HEIGHT) {
            alive = false;
        }

        // Update bullets if enemy can shoot
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta);
            if (!bullet.isAlive()) {
                bullets.removeIndex(i);
            }
        }
    }

    protected void updateBounds() {
        sprite.setPosition(position.x, position.y);
        bounds.setPosition(position.x, position.y);
    }

    public void render(SpriteBatch batch) {
        if (alive) {
            sprite.draw(batch);
            // Render bullets if enemy can shoot
            for (Bullet bullet : bullets) {
                bullet.render(batch);
            }
        }
    }

    public void takeDamage(float damage) {
        if (alive) {
            health -= damage;
            if (health <= 0) {
                alive = false;
                // Spiele Explosions-Sound wenn der Gegner zerstört wird
                AudioManager.getInstance().playExplosionSound();
            }
        }
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }
}
