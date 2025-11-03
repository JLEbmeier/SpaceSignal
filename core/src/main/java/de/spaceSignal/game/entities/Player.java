package de.spaceSignal.game.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import de.spaceSignal.game.managers.AudioManager;
import de.spaceSignal.game.util.Constants;

public class Player {
    private Sprite sprite;
    private Vector2 position;
    private Rectangle bounds;
    private float health;
    private float fireTimer;
    private int bulletLevel;
    private float damageMultiplier;
    private boolean alive;

    public Player(float x, float y, Texture texture) {
        position = new Vector2(x, y);
        sprite = new Sprite(texture);
        sprite.setPosition(x, y);
        sprite.setSize(Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        bounds = new Rectangle(x, y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
        health = Constants.PLAYER_MAX_HEALTH;
        fireTimer = 0;
        bulletLevel = 1;
        damageMultiplier = 1f;
        alive = true;
    }

    public void update(float delta) {
        if (!alive) return;

        // Bewegung
        float speed = Constants.PLAYER_SPEED;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            position.x -= speed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            position.x += speed * delta;
        }
        position.x = MathUtils.clamp(position.x, 0, Constants.SCREEN_WIDTH - Constants.PLAYER_WIDTH);
        sprite.setPosition(position.x, position.y);
        bounds.setPosition(position.x, position.y);

        // Schuss-Timer
        fireTimer += delta;
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void takeDamage(float damage) {
        health -= damage;
        if (health <= 0) {
            alive = false;
            // Play explosion sound when player dies
            AudioManager.getInstance().playExplosionSound();
        }
    }

    public boolean canFire() {
        return fireTimer >= Constants.PLAYER_FIRE_RATE && alive;
    }

    public void resetFireTimer() {
        fireTimer = 0;
        // Play shoot sound when firing
        AudioManager.getInstance().playShootSound();
    }

    public void applyUpgrade(String type) {
        switch (type) {
            case "BulletLevel":
                bulletLevel = Math.min(bulletLevel + 1, 3); // Max Level 3
                break;
            case "Health":
                health = Math.min(health + 1, Constants.PLAYER_MAX_HEALTH);
                break;
            case "Damage":
                damageMultiplier += 0.5f;
                break;
        }
        // Play powerup sound when collecting any upgrade
        AudioManager.getInstance().playPowerupSound();
    }

    public Vector2 getPosition() {
        return position;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public float getHealth() {
        return health;
    }

    public int getBulletLevel() {
        return bulletLevel;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public boolean isAlive() {
        return alive;
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }
}
