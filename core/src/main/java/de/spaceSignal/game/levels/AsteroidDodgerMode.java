package de.spaceSignal.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import de.spaceSignal.game.entities.Bullet;
import de.spaceSignal.game.entities.Enemy;
import de.spaceSignal.game.entities.Player;
import de.spaceSignal.game.entities.Upgrade;
import de.spaceSignal.game.managers.AssetManager;
import de.spaceSignal.game.managers.AudioManager;
import de.spaceSignal.game.util.Constants;

/**
 * Asteroid Dodger Mode:
 * - Spieler kann sich frei in alle 4 Richtungen bewegen (WASD/Pfeiltasten)
 * - Asteroiden kommen aus allen Richtungen
 * - Verschiedene Asteroiden-Typen mit unterschiedlichen Bewegungsmustern
 * - Power-Ups: Schild, Slow Motion, Score Multiplier
 * - Combo-System: Je mehr Asteroiden du ausweichst, desto höher der Multiplier
 * - Geschwindigkeit erhöht sich mit der Zeit – EXPONENTIELL. UNBARMHERZIG.
 */
public class AsteroidDodgerMode extends GameMode {
    private Array<Asteroid> asteroids;
    private float spawnTimer;
    private float difficultyTimer;
    private float baseSpawnInterval;
    private float currentSpeed;

    // Combo-System
    private int comboCount;
    private float comboTimer;
    private float scoreMultiplier;
    private static final float COMBO_TIMEOUT = 3f;

    // Shield Power-Up
    private boolean shieldActive;
    private float shieldTimer;
    private static final float SHIELD_DURATION = 5f;

    // Slow Motion
    private boolean slowMotionActive;
    private float slowMotionTimer;
    private static final float SLOW_MOTION_DURATION = 3f;

    // Power-Ups
    private Array<PowerUp> powerUps;
    private float powerUpSpawnTimer;

    private Texture asteroidTexture;
    private Texture solidTexture;

    // Spieler-Bewegung (vollständige 360° Kontrolle)
    private Vector2 playerVelocity;
    private float playerAcceleration = 800f;
    private float playerMaxSpeed = 400f;
    private float playerFriction = 0.92f;

    // === EXPONENTIELLE SCHWIERIGKEIT – NUR FÜR ASTEROID DODGER ===
    private float difficultyMultiplier = 1f;
    private static final float DIFFICULTY_GROWTH_RATE = 0.08f; // 8% Wachstum pro Sekunde
    private static final float MIN_SPAWN_INTERVAL = 0.08f;
    private static final float MAX_SPAWN_INTERVAL = 0.3f;

    private enum AsteroidType {
        NORMAL,     // Geradeaus
        SPINNING,   // Dreht sich
        HOMING,     // Verfolgt Spieler leicht
        BOUNCING    // Prallt von Wänden ab
    }

    private enum PowerUpType {
        SHIELD,
        SLOW_MOTION,
        SCORE_MULTIPLIER
    }

    private static class Asteroid {
        Vector2 position;
        Vector2 velocity;
        Rectangle bounds;
        float rotation;
        float rotationSpeed;
        AsteroidType type;
        float size;
        boolean alive;

        Asteroid(float x, float y, float vx, float vy, AsteroidType type) {
            this.position = new Vector2(x, y);
            this.velocity = new Vector2(vx, vy);
            this.type = type;
            this.size = MathUtils.random(30f, 60f);
            this.bounds = new Rectangle(x, y, size, size);
            this.rotation = MathUtils.random(0f, 360f);
            this.rotationSpeed = MathUtils.random(-180f, 180f);
            this.alive = true;
        }

        void update(float delta, Vector2 playerPos, float timeScale) {
            if (!alive) return;

            float adjustedDelta = delta * timeScale;

            switch (type) {
                case NORMAL:
                    position.add(velocity.x * adjustedDelta, velocity.y * adjustedDelta);
                    break;

                case SPINNING:
                    position.add(velocity.x * adjustedDelta, velocity.y * adjustedDelta);
                    rotation += rotationSpeed * adjustedDelta;
                    break;

                case HOMING:
                    Vector2 direction = new Vector2(playerPos).sub(position).nor();
                    velocity.add(direction.scl(50f * adjustedDelta));
                    velocity.clamp(0, 250f);
                    position.add(velocity.x * adjustedDelta, velocity.y * adjustedDelta);
                    break;

                case BOUNCING:
                    position.add(velocity.x * adjustedDelta, velocity.y * adjustedDelta);
                    if (position.x <= 0 || position.x >= Constants.SCREEN_WIDTH - size) {
                        velocity.x *= -1;
                    }
                    if (position.y <= 0 || position.y >= Constants.SCREEN_HEIGHT - size) {
                        velocity.y *= -1;
                    }
                    break;
            }

            bounds.setPosition(position.x, position.y);

            if (type != AsteroidType.BOUNCING) {
                if (position.x < -100 || position.x > Constants.SCREEN_WIDTH + 100 ||
                    position.y < -100 || position.y > Constants.SCREEN_HEIGHT + 100) {
                    alive = false;
                }
            }
        }

        boolean collidesWith(Rectangle other) {
            return alive && bounds.overlaps(other);
        }
    }

    private static class PowerUp {
        Vector2 position;
        Rectangle bounds;
        PowerUpType type;
        boolean collected;
        float bobTimer;
        float lifetime;

        PowerUp(float x, float y, PowerUpType type) {
            this.position = new Vector2(x, y);
            this.bounds = new Rectangle(x, y, 35, 35);
            this.type = type;
            this.collected = false;
            this.bobTimer = 0;
            this.lifetime = 10f;
        }

        void update(float delta) {
            if (collected) return;

            bobTimer += delta * 3;
            lifetime -= delta;
            float bobOffset = MathUtils.sin(bobTimer) * 8;
            bounds.setPosition(position.x, position.y + bobOffset);
        }

        boolean isExpired() {
            return lifetime <= 0;
        }
    }

    public AsteroidDodgerMode(Player player, Array<Bullet> bullets, Array<Enemy> enemies, Array<Upgrade> upgrades) {
        super(player, bullets, enemies, upgrades);

        asteroids = new Array<>();
        powerUps = new Array<>();
        playerVelocity = new Vector2(0, 0);

        spawnTimer = 0;
        difficultyTimer = 0;
        baseSpawnInterval = 1.5f;
        currentSpeed = 150f;

        comboCount = 0;
        comboTimer = 0;
        scoreMultiplier = 1f;

        shieldActive = false;
        shieldTimer = 0;
        slowMotionActive = false;
        slowMotionTimer = 0;
        powerUpSpawnTimer = 0;

        try {
            asteroidTexture = new Texture(Gdx.files.internal("textures/enemy.png"));
        } catch (Exception e) {
            asteroidTexture = AssetManager.getInstance().getEnemyTexture();
        }
        solidTexture = createSolidTexture();

        player.getPosition().set(Constants.SCREEN_WIDTH / 2, Constants.SCREEN_HEIGHT / 2);
    }

    @Override
    public void update(float delta) {
        float timeScale = slowMotionActive ? 0.5f : 1f;

        // Power-Up Timer
        if (shieldActive) {
            shieldTimer -= delta;
            if (shieldTimer <= 0) shieldActive = false;
        }
        if (slowMotionActive) {
            slowMotionTimer -= delta;
            if (slowMotionTimer <= 0) slowMotionActive = false;
        }

        // Combo Timer
        if (comboCount > 0) {
            comboTimer -= delta;
            if (comboTimer <= 0) {
                comboCount = 0;
                scoreMultiplier = 1f;
            }
        }

        // Spieler-Bewegung
        Vector2 acceleration = new Vector2(0, 0);
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) acceleration.x -= playerAcceleration;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) acceleration.x += playerAcceleration;
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) acceleration.y += playerAcceleration;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) acceleration.y -= playerAcceleration;

        playerVelocity.add(acceleration.scl(delta));
        playerVelocity.clamp(0, playerMaxSpeed);
        playerVelocity.scl(playerFriction);

        Vector2 playerPos = player.getPosition();
        playerPos.add(playerVelocity.x * delta, playerVelocity.y * delta);
        playerPos.x = MathUtils.clamp(playerPos.x, 0, Constants.SCREEN_WIDTH - Constants.PLAYER_WIDTH);
        playerPos.y = MathUtils.clamp(playerPos.y, 0, Constants.SCREEN_HEIGHT - Constants.PLAYER_HEIGHT);

        // === APOKALYPTISCHE SCHWIERIGKEIT ===
        difficultyTimer += delta;
        difficultyMultiplier = (float) Math.pow(1f + DIFFICULTY_GROWTH_RATE, difficultyTimer);

        float targetSpeed = 150f + (difficultyTimer * 8f);
        currentSpeed = MathUtils.lerp(currentSpeed, targetSpeed, 0.05f);

        float targetInterval = 1.5f / difficultyMultiplier;
        baseSpawnInterval = MathUtils.clamp(targetInterval, MIN_SPAWN_INTERVAL, MAX_SPAWN_INTERVAL);

        if (difficultyTimer >= 15f) {
            difficultyTimer = 0;
            incrementWave();
        }

        // Asteroiden spawnen
        spawnTimer += delta;
        if (spawnTimer >= baseSpawnInterval) {
            spawnAsteroid();
            spawnTimer = 0;
        }

        // Asteroiden updaten
        for (int i = asteroids.size - 1; i >= 0; i--) {
            Asteroid asteroid = asteroids.get(i);
            asteroid.update(delta, playerPos, timeScale);
            if (!asteroid.alive) {
                asteroids.removeIndex(i);
                continue;
            }
            if (asteroid.collidesWith(player.getBounds())) {
                if (shieldActive) {
                    asteroid.alive = false;
                    asteroids.removeIndex(i);
                    AudioManager.getInstance().playExplosionSound();
                    incrementScore((int)(50 * scoreMultiplier));
                } else {
                    player.takeDamage(1);
                    asteroid.alive = false;
                    asteroids.removeIndex(i);
                    AudioManager.getInstance().playExplosionSound();
                    comboCount = 0;
                    scoreMultiplier = 1f;
                    if (!player.isAlive()) setGameOver(true);
                }
            }
        }

        // Combo für knappes Ausweichen
        for (Asteroid asteroid : asteroids) {
            float distance = asteroid.position.dst(playerPos);
            if (distance < 80f && distance > 50f) {
                comboCount++;
                comboTimer = COMBO_TIMEOUT;
                scoreMultiplier = 1f + (comboCount * 0.1f);
                incrementScore((int)(5 * scoreMultiplier));
            }
        }

        // Power-Ups spawnen
        powerUpSpawnTimer += delta;
        if (powerUpSpawnTimer >= 8f && MathUtils.random() < 0.5f) {
            spawnPowerUp();
            powerUpSpawnTimer = 0;
        }

        // Power-Ups updaten
        for (int i = powerUps.size - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update(delta);
            if (powerUp.isExpired()) {
                powerUps.removeIndex(i);
                continue;
            }
            if (!powerUp.collected && powerUp.bounds.overlaps(player.getBounds())) {
                powerUp.collected = true;
                applyPowerUp(powerUp.type);
                powerUps.removeIndex(i);
                AudioManager.getInstance().playPowerupSound();
            }
        }
    }

    private void spawnAsteroid() {
        int side = MathUtils.random(3);
        float x, y, vx, vy;

        switch (side) {
            case 0: x = MathUtils.random(0f, Constants.SCREEN_WIDTH); y = Constants.SCREEN_HEIGHT + 50; vx = MathUtils.random(-50f, 50f); vy = -currentSpeed; break;
            case 1: x = Constants.SCREEN_WIDTH + 50; y = MathUtils.random(0f, Constants.SCREEN_HEIGHT); vx = -currentSpeed; vy = MathUtils.random(-50f, 50f); break;
            case 2: x = MathUtils.random(0f, Constants.SCREEN_WIDTH); y = -50; vx = MathUtils.random(-50f, 50f); vy = currentSpeed; break;
            default: x = -50; y = MathUtils.random(0f, Constants.SCREEN_HEIGHT); vx = currentSpeed; vy = MathUtils.random(-50f, 50f); break;
        }

        AsteroidType type = getWeightedAsteroidType();
        float minSize = 30f + wave * 3f;
        float maxSize = 60f + wave * 5f;
        float size = MathUtils.random(minSize, maxSize);

        Asteroid asteroid = new Asteroid(x, y, vx, vy, type);
        asteroid.size = size;
        asteroid.bounds.setSize(size, size);

        if (type == AsteroidType.BOUNCING) {
            asteroid.velocity.scl(1.3f);
        }

        asteroids.add(asteroid);
    }

    private AsteroidType getWeightedAsteroidType() {
        float r = MathUtils.random();
        float waveFactor = MathUtils.clamp(wave * 0.1f, 0f, 0.6f);
        if (r < 0.3f + waveFactor) return AsteroidType.HOMING;
        if (r < 0.55f + waveFactor) return AsteroidType.BOUNCING;
        if (r < 0.75f + waveFactor) return AsteroidType.SPINNING;
        return AsteroidType.NORMAL;
    }

    private void spawnPowerUp() {
        float x = MathUtils.random(50f, Constants.SCREEN_WIDTH - 50f);
        float y = MathUtils.random(50f, Constants.SCREEN_HEIGHT - 50f);
        PowerUpType type = PowerUpType.values()[MathUtils.random(PowerUpType.values().length - 1)];
        powerUps.add(new PowerUp(x, y, type));
    }

    private void applyPowerUp(PowerUpType type) {
        switch (type) {
            case SHIELD: shieldActive = true; shieldTimer = SHIELD_DURATION; break;
            case SLOW_MOTION: slowMotionActive = true; slowMotionTimer = SLOW_MOTION_DURATION; break;
            case SCORE_MULTIPLIER: scoreMultiplier += 1f; comboTimer = COMBO_TIMEOUT; break;
        }
    }

    @Override
    public void renderEntities(SpriteBatch batch) {
        // === ASTEROIDEN RENDERN ===
        for (Asteroid asteroid : asteroids) {
            if (!asteroid.alive) continue;

            Color color;
            switch (asteroid.type) {
                case NORMAL:
                    color = new Color(0.6f, 0.6f, 0.6f, 1f);
                    break;
                case SPINNING:
                    color = new Color(0.8f, 0.4f, 0.8f, 1f);
                    break;
                case HOMING:
                    color = new Color(1f, 0.3f, 0.3f, 1f);
                    break;
                case BOUNCING:
                    color = new Color(0.3f, 0.8f, 1f, 1f);
                    break;
                default:
                    color = Color.GRAY;
            }

            batch.setColor(color);
            batch.draw(asteroidTexture,
                asteroid.position.x, asteroid.position.y,
                asteroid.size / 2, asteroid.size / 2,
                asteroid.size, asteroid.size,
                1f, 1f,
                asteroid.rotation,
                0, 0,
                asteroidTexture.getWidth(), asteroidTexture.getHeight(),
                false, false);
            batch.setColor(1, 1, 1, 1);
        }

        // === POWER-UPS RENDERN ===
        for (PowerUp powerUp : powerUps) {
            if (powerUp.collected) continue;

            Color color;
            switch (powerUp.type) {
                case SHIELD:
                    color = new Color(0.3f, 0.3f, 1f, 1f);
                    break;
                case SLOW_MOTION:
                    color = new Color(0.8f, 0.3f, 1f, 1f);
                    break;
                case SCORE_MULTIPLIER:
                    color = new Color(1f, 0.8f, 0.2f, 1f);
                    break;
                default:
                    color = Color.WHITE;
            }

            batch.setColor(color);
            batch.draw(solidTexture,
                powerUp.bounds.x, powerUp.bounds.y,
                powerUp.bounds.width, powerUp.bounds.height);
            batch.setColor(1, 1, 1, 1);
        }

        // === SCHILD-EFFEKT ===
        if (shieldActive) {
            batch.setColor(0.3f, 0.3f, 1f, 0.3f);
            batch.draw(solidTexture,
                player.getPosition().x - 10,
                player.getPosition().y - 10,
                Constants.PLAYER_WIDTH + 20,
                Constants.PLAYER_HEIGHT + 20);
            batch.setColor(1, 1, 1, 1);
        }
    }

    @Override
    public void renderUI(SpriteBatch batch, BitmapFont uiFont) {
        uiFont.draw(batch, "Score: " + score, 10, Constants.SCREEN_HEIGHT - 10);
        uiFont.draw(batch, "Wave: " + wave, 10, Constants.SCREEN_HEIGHT - 35);
        uiFont.draw(batch, "Health: " + (int) player.getHealth(), 10, Constants.SCREEN_HEIGHT - 60);

        if (comboCount > 0) {
            uiFont.setColor(1f, 1f, 0.3f, 1f);
            uiFont.draw(batch, "COMBO x" + comboCount, 10, Constants.SCREEN_HEIGHT - 85);
            uiFont.draw(batch, "Multiplier: x" + String.format("%.1f", scoreMultiplier), 10, Constants.SCREEN_HEIGHT - 110);
            uiFont.setColor(1, 1, 1, 1);
        }

        float powerUpY = Constants.SCREEN_HEIGHT - 135;
        if (shieldActive) {
            uiFont.setColor(0.3f, 0.3f, 1f, 1f);
            uiFont.draw(batch, "SHIELD: " + (int)shieldTimer + "s", 10, powerUpY);
            powerUpY -= 25;
            uiFont.setColor(1, 1, 1, 1);
        }
        if (slowMotionActive) {
            uiFont.setColor(0.8f, 0.3f, 1f, 1f);
            uiFont.draw(batch, "SLOW-MO: " + (int)slowMotionTimer + "s", 10, powerUpY);
            uiFont.setColor(1, 1, 1, 1);
        }

        // === HÖLLEN-ANZEIGE ===
        uiFont.setColor(1f, 0.3f, 0.3f, 1f);
        uiFont.draw(batch, "HELL: x" + String.format("%.1f", difficultyMultiplier),
            10, Constants.SCREEN_HEIGHT - 160);
        uiFont.setColor(1, 1, 1, 1);

        uiFont.setColor(0.6f, 0.6f, 0.7f, 0.6f);
        uiFont.draw(batch, "WASD/Arrows: Move", 10, 40);
        uiFont.setColor(1, 1, 1, 1);
    }

    @Override
    public boolean checkGameOver() {
        if (!player.isAlive()) {
            setGameOver(true);
            return true;
        }
        return false;
    }

    @Override
    public String getVictoryMessage() {
        if (wave >= 10) {
            return "INCREDIBLE! You survived " + wave + " waves!";
        }
        return null;
    }

    private Texture createSolidTexture() {
        Texture texture = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        if (solidTexture != null) solidTexture.dispose();
    }
}
