package de.spaceSignal.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.spaceSignal.game.util.Constants;

public class BossBullet {
    private Sprite sprite;
    private Vector2 position;
    private Vector2 velocity;
    private Rectangle bounds;
    private boolean alive;
    private float damage;

    public BossBullet(float x, float y, float velocityX, float velocityY, float damage, Texture texture) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(velocityX, velocityY);
        this.damage = damage;
        this.alive = true;

        this.sprite = new Sprite(texture);
        sprite.setPosition(x, y);
        sprite.setSize(Constants.BOSS_BULLET_WIDTH, Constants.BOSS_BULLET_HEIGHT);
        sprite.setColor(1, 0, 0, 1); // Rote Bullets für Boss

        bounds = new Rectangle(x, y, Constants.BOSS_BULLET_WIDTH, Constants.BOSS_BULLET_HEIGHT);
    }

    public void update(float delta) {
        if (!alive) return;

        position.add(velocity.x * delta, velocity.y * delta);
        bounds.setPosition(position);
        sprite.setPosition(position.x, position.y);

        // Entferne Bullet wenn es den Bildschirm verlässt
        if (position.y < -Constants.BOSS_BULLET_HEIGHT ||
            position.y > Constants.SCREEN_HEIGHT ||
            position.x < -Constants.BOSS_BULLET_WIDTH ||
            position.x > Constants.SCREEN_WIDTH) {
            alive = false;
        }
    }

    public void render(SpriteBatch batch) {
        if (alive) {
            sprite.draw(batch);
        }
    }

    public void destroy() {
        alive = false;
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }

    public Rectangle getBounds() { return bounds; }
    public boolean isAlive() { return alive; }
    public float getDamage() { return damage; }
}
