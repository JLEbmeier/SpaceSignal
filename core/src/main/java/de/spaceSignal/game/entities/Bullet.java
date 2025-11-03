package de.spaceSignal.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.spaceSignal.game.util.Constants;

public class Bullet {
    private Sprite sprite;
    private Vector2 position;
    private Vector2 velocity;
    private Rectangle bounds;
    private boolean alive;
    private float damage;
    private float scale;
    private final String gameMode;

    public Bullet(float x, float y, float velocityX, float velocityY, float damage, Texture texture, String gameMode) {
        this.gameMode = gameMode;
        this.sprite = new Sprite(texture);
        reset(x, y, velocityX, velocityY, damage);
    }

    public void reset(float x, float y, float velocityX, float velocityY, float damage) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(velocityX, velocityY);
        this.bounds = new Rectangle(x, y, Constants.BULLET_WIDTH, Constants.BULLET_HEIGHT);
        this.damage = damage;
        this.alive = true;
        this.scale = 1f;
        sprite.setPosition(x, y);
        sprite.setSize(Constants.BULLET_WIDTH, Constants.BULLET_HEIGHT);
        sprite.setScale(1f);
        adjustForGameMode();
    }

    private void adjustForGameMode() {
        switch (gameMode) {
            case "Survival Mode":
                velocity.scl(1.2f);
                damage *= 1.5f;
                break;
            case "Time Attack":
                velocity.scl(0.8f);
                break;
            default: // Classic Mode
                break;
        }
    }

    public void update(float delta) {
        if (!alive) return;

        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position);
        sprite.setPosition(position.x, position.y);

        if (scale < 1f) {
            scale += delta * 2f;
            sprite.setScale(scale);
        }

        if (position.y > Constants.SCREEN_HEIGHT || position.y < 0 ||
            position.x < 0 || position.x > Constants.SCREEN_WIDTH) {
            alive = false;
        }
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void destroy() {
        alive = false;
        scale = 0.5f;
    }

    public void dispose() {
        // Bullet textures are managed centrally (AssetManager); do not dispose here.
    }

    public Rectangle getBounds() { return bounds; }
    public boolean isAlive() { return alive; }
    public float getDamage() { return damage; }
}
