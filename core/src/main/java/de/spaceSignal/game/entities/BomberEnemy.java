package de.spaceSignal.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import de.spaceSignal.game.managers.AudioManager;
import de.spaceSignal.game.util.Constants;

public class BomberEnemy extends Enemy {
    private Vector2 playerPosition;
    private boolean isExploding;
    private float explosionTimer;
    private static final float EXPLOSION_DURATION = 0.5f;

    public BomberEnemy(float x, float y, Texture texture) {
        super(x, y, Constants.BOMBER_HEALTH, texture, "normal");
        this.speed = Constants.BOMBER_SPEED;
        this.isExploding = false;
        this.explosionTimer = 0;
    }

    @Override
    public void update(float delta) {
        if (!isAlive()) return;

        if (isExploding) {
            updateExplosion(delta);
            return;
        }

        // Normale Bewegung nach unten
        position.y -= speed * delta;
        updateBounds();

        // Prüfe Abstand zum Spieler
        if (playerPosition != null) {
            float distance = Vector2.dst(position.x, position.y, 
                                      playerPosition.x, playerPosition.y);
            if (distance < Constants.BOMBER_EXPLOSION_RADIUS) {
                startExplosion();
            }
        }

        // Entferne wenn außerhalb des Bildschirms
        if (position.y < -Constants.ENEMY_HEIGHT) {
            setAlive(false);
        }
    }

    private void updateExplosion(float delta) {
        explosionTimer += delta;
        if (explosionTimer >= EXPLOSION_DURATION) {
            setAlive(false);
        }
    }

    private void startExplosion() {
        isExploding = true;
        explosionTimer = 0;
        AudioManager.getInstance().playExplosionSound();
    }

    public void setPlayerPosition(Vector2 playerPos) {
        this.playerPosition = playerPos;
    }

    public boolean isExploding() {
        return isExploding;
    }

    public float getExplosionRadius() {
        return Constants.BOMBER_EXPLOSION_RADIUS;
    }

    public float getExplosionDamage() {
        return Constants.BOMBER_EXPLOSION_DAMAGE;
    }
}