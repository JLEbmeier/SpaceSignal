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

public class FlappyMode extends GameMode {
    private float velocity;
    private float gravity;
    private float flapStrength;

    private Array<Obstacle> obstacles;
    private float obstacleTimer;
    private float obstacleSpawnInterval;

    private Array<PowerUp> powerUps;
    private float powerUpTimer;
    private int passedObstacles;

    private Texture obstacleTexture;
    private Texture solidTexture;

    private Vector2 playerPos;

    // Fix für Sprite Stretching
    private float playerWidth = Constants.PLAYER_WIDTH;
    private float playerHeight = Constants.PLAYER_HEIGHT;

    // Fix für Dauer-Schießen
    private float shootCooldown = 0f;
    private static final float SHOOT_COOLDOWN_TIME = 0.3f;

    private static class Obstacle {
        Rectangle top;
        Rectangle bottom;
        float x;
        float gapY;
        float gapSize;
        boolean passed;
        boolean destroyed;

        Obstacle(float x, float gapY, float gapSize) {
            this.x = x;
            this.gapY = gapY;
            this.gapSize = gapSize;
            this.passed = false;
            this.destroyed = false;
            updateBounds();
        }

        void updateBounds() {
            top = new Rectangle(x, gapY + gapSize / 2, 60, Constants.SCREEN_HEIGHT - (gapY + gapSize / 2));
            bottom = new Rectangle(x, 0, 60, gapY - gapSize / 2);
        }

        void update(float delta, float speed) {
            if (!destroyed) {
                x -= speed * delta;
                updateBounds();
            }
        }

        boolean isOffScreen() {
            return x < -70;
        }

        boolean collidesWith(Rectangle bounds) {
            return !destroyed && (top.overlaps(bounds) || bottom.overlaps(bounds));
        }
    }

    private static class PowerUp {
        Vector2 position;
        Rectangle bounds;
        String type;
        boolean collected;
        float bobTimer;

        PowerUp(float x, float y, String type) {
            this.position = new Vector2(x, y);
            this.bounds = new Rectangle(x, y, 30, 30);
            this.type = type;
            this.collected = false;
            this.bobTimer = 0;
        }

        void update(float delta, float speed) {
            if (!collected) {
                position.x -= speed * delta;
                bobTimer += delta * 3;
                float bobOffset = MathUtils.sin(bobTimer) * 5;
                bounds.setPosition(position.x, position.y + bobOffset);
            }
        }

        boolean isOffScreen() {
            return position.x < -40;
        }
    }

    public FlappyMode(Player player, Array<Bullet> bullets, Array<Enemy> enemies, Array<Upgrade> upgrades) {
        super(player, bullets, enemies, upgrades);

        // Schießen deaktivieren für Flappy Mode
        player.setCanShoot(false);

        velocity = 0;
        gravity = -800f;
        flapStrength = 350f;

        obstacles = new Array<>();
        obstacleTimer = 0;
        obstacleSpawnInterval = 2.0f;
        passedObstacles = 0;

        powerUps = new Array<>();
        powerUpTimer = 0;

        playerPos = new Vector2(100, Constants.SCREEN_HEIGHT / 2);

        try {
            obstacleTexture = new Texture(Gdx.files.internal("textures/obstacle.png"));
        } catch (Exception e) {
            obstacleTexture = AssetManager.getInstance().getEnemyTexture();
        }
        solidTexture = createSolidTexture();
    }

    @Override
    public void update(float delta) {
        // Shoot Cooldown updaten
        if (shootCooldown > 0) {
            shootCooldown -= delta;
        }

        // Flappy-Steuerung: SPACE zum Fliegen
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            velocity = flapStrength;
            AudioManager.getInstance().playShootSound();
        }

        // Schwerkraft anwenden
        velocity += gravity * delta;
        playerPos.y += velocity * delta;

        // Boden- und Decken-Kollision
        if (playerPos.y <= 0) {
            playerPos.y = 0;
            velocity = 0;
            setGameOver(true);
        }
        if (playerPos.y >= Constants.SCREEN_HEIGHT - playerHeight) {
            playerPos.y = Constants.SCREEN_HEIGHT - playerHeight;
            velocity = 0;
        }

        // Spieler-Position synchronisieren OHNE Sprite zu stretchen
        player.getPosition().set(playerPos.x, playerPos.y);

        // KEIN Schießen in FlappyMode mehr möglich

        // Hindernisse spawnen
        obstacleTimer += delta;
        if (obstacleTimer >= obstacleSpawnInterval) {
            spawnObstacle();
            obstacleTimer = 0;
            obstacleSpawnInterval = MathUtils.random(1.8f, 2.8f);
        }

        // Hindernisse updaten
        float obstacleSpeed = 200f + (passedObstacles * 3f);
        for (int i = obstacles.size - 1; i >= 0; i--) {
            Obstacle obstacle = obstacles.get(i);
            obstacle.update(delta, obstacleSpeed);

            // Kollision prüfen
            Rectangle playerBounds = new Rectangle(playerPos.x, playerPos.y, playerWidth, playerHeight);
            if (obstacle.collidesWith(playerBounds)) {
                setGameOver(true);
                AudioManager.getInstance().playExplosionSound();
            }

            // Punkte zählen wenn passiert
            if (!obstacle.passed && obstacle.x + 60 < playerPos.x) {
                obstacle.passed = true;
                passedObstacles++;
                incrementScore(10);
                AudioManager.getInstance().playPowerupSound();

                if (passedObstacles % 5 == 0) {
                    incrementWave();
                }
            }

            // Hindernisse können NICHT mehr zerstört werden (kein Schießen)

            if (obstacle.isOffScreen()) {
                obstacles.removeIndex(i);
            }
        }

        // Power-Ups spawnen
        powerUpTimer += delta;
        if (powerUpTimer >= 6f && MathUtils.random() < 0.4f) {
            spawnPowerUp();
            powerUpTimer = 0;
        }

        // Power-Ups updaten
        for (int i = powerUps.size - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            powerUp.update(delta, obstacleSpeed);

            Rectangle playerBounds = new Rectangle(playerPos.x, playerPos.y, playerWidth, playerHeight);
            if (!powerUp.collected && powerUp.bounds.overlaps(playerBounds)) {
                powerUp.collected = true;
                player.applyUpgrade(powerUp.type);
                incrementScore(20);
                powerUps.removeIndex(i);
                AudioManager.getInstance().playPowerupSound();
            } else if (powerUp.isOffScreen()) {
                powerUps.removeIndex(i);
            }
        }
    }

    private void spawnObstacle() {
        float gapSize = MathUtils.random(140f, 200f);
        float minY = 150f;
        float maxY = Constants.SCREEN_HEIGHT - 150f;
        float gapY = MathUtils.random(minY, maxY);

        obstacles.add(new Obstacle(Constants.SCREEN_WIDTH, gapY, gapSize));
    }

    private void spawnPowerUp() {
        float x = Constants.SCREEN_WIDTH;
        float y = MathUtils.random(100f, Constants.SCREEN_HEIGHT - 100f);
        String type = MathUtils.random() < 0.5f ? "Health" : "Damage";
        powerUps.add(new PowerUp(x, y, type));
    }

    @Override
    public void renderEntities(SpriteBatch batch) {
        // Spieler rendern (OHNE Stretching)
        batch.draw(player.getSprite().getTexture(),
            playerPos.x, playerPos.y,
            playerWidth, playerHeight);

        // Hindernisse rendern
        for (Obstacle obstacle : obstacles) {
            if (!obstacle.destroyed) {
                batch.setColor(0.8f, 0.2f, 0.2f, 1f);
                batch.draw(solidTexture, obstacle.top.x, obstacle.top.y,
                    obstacle.top.width, obstacle.top.height);
                batch.draw(solidTexture, obstacle.bottom.x, obstacle.bottom.y,
                    obstacle.bottom.width, obstacle.bottom.height);
                batch.setColor(1, 1, 1, 1);

                // Textur-Overlay
                for (float y = obstacle.top.y; y < Constants.SCREEN_HEIGHT; y += 32) {
                    batch.draw(obstacleTexture, obstacle.top.x, y, 60, 32);
                }
                for (float y = 0; y < obstacle.bottom.height; y += 32) {
                    batch.draw(obstacleTexture, obstacle.bottom.x, y, 60, 32);
                }
            } else {
                batch.setColor(0.5f, 0.5f, 0.5f, 0.3f);
                batch.draw(solidTexture, obstacle.top.x, obstacle.top.y,
                    obstacle.top.width, obstacle.top.height);
                batch.draw(solidTexture, obstacle.bottom.x, obstacle.bottom.y,
                    obstacle.bottom.width, obstacle.bottom.height);
                batch.setColor(1, 1, 1, 1);
            }
        }

        // Power-Ups rendern
        for (PowerUp powerUp : powerUps) {
            if (!powerUp.collected) {
                Color color = powerUp.type.equals("Health")
                    ? new Color(0.2f, 1f, 0.2f, 1f)
                    : new Color(1f, 0.8f, 0.2f, 1f);
                batch.setColor(color);
                batch.draw(solidTexture, powerUp.bounds.x, powerUp.bounds.y,
                    powerUp.bounds.width, powerUp.bounds.height);
                batch.setColor(1, 1, 1, 1);
            }
        }
    }

    @Override
    public void renderUI(SpriteBatch batch, BitmapFont uiFont) {
        uiFont.draw(batch, "Score: " + score, 10, Constants.SCREEN_HEIGHT - 10);
        uiFont.draw(batch, "Passed: " + passedObstacles, 10, Constants.SCREEN_HEIGHT - 35);
        uiFont.draw(batch, "Health: " + (int) player.getHealth(), 10, Constants.SCREEN_HEIGHT - 60);

        uiFont.setColor(0.8f, 0.8f, 0.8f, 0.6f);
        uiFont.draw(batch, "SPACE: Fly", 10, Constants.SCREEN_HEIGHT - 85);
        uiFont.setColor(1, 1, 1, 1);
    }

    @Override
    public boolean checkGameOver() {
        return isGameOver;
    }

    @Override
    public String getVictoryMessage() {
        if (passedObstacles >= 50) {
            return "AMAZING! You passed " + passedObstacles + " obstacles!";
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
