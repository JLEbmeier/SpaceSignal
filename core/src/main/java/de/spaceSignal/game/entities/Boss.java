package de.spaceSignal.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import de.spaceSignal.game.managers.AudioManager;
import de.spaceSignal.game.util.Constants;

public class Boss {
    private Sprite sprite;
    private Vector2 position;
    private Rectangle bounds;
    private float health;
    private float maxHealth;
    private boolean alive;
    private int level;
    private float fireTimer;
    private Array<BossBullet> bullets;
    private Texture bulletTexture;

    // Boss Bewegung verbessert
    private float movementTimer = 0;
    private float movementDirection = 1;
    private float movementSpeed;

    // EXPLOSION Variablen
    private boolean exploding = false;
    private float explosionTimer = 0f;
    private float explosionDuration = 1.5f; // 1.5 Sekunden Explosion
    private Array<ExplosionParticle> explosionParticles;
    private Vector2 explosionOrigin;

    public Boss(int level, Texture texture, Texture bulletTexture) {
        this.level = level;
        this.bulletTexture = bulletTexture;
        this.bullets = new Array<>();
        this.movementSpeed = Constants.BOSS_SPEED * (1 + level * 0.1f);
        this.explosionParticles = new Array<>();

        // Boss Größe basierend auf Level
        float width = Constants.BOSS_WIDTH * (1 + level * 0.15f);
        float height = Constants.BOSS_HEIGHT * (1 + level * 0.15f);

        // Bessere Startposition
        this.position = new Vector2(
            Constants.SCREEN_WIDTH / 2 - width / 2,
            Constants.SCREEN_HEIGHT - height - 80
        );

        // Gesundheit basierend auf Level
        this.maxHealth = 100 + (level * 50);
        this.health = maxHealth;
        this.alive = true;

        this.sprite = new Sprite(texture);
        sprite.setPosition(position.x, position.y);
        sprite.setSize(width, height);
        bounds = new Rectangle(position.x, position.y, width, height);

        this.fireTimer = MathUtils.random(1f, 3f);
    }

    public void update(float delta) {
        if (!alive) return;

        if (exploding) {
            updateExplosion(delta);
            return;
        }

        // VERBESSERTE BEWEGUNG - Kontinuierliches Hin und Her
        movementTimer += delta;

        // Richtungswechsel basierend auf Zeit UND Position
        boolean shouldChangeDirection = false;

        if (movementTimer > 2f) { // Zeit-basierter Wechsel alle 2 Sekunden
            shouldChangeDirection = true;
            movementTimer = 0;
        }

        // Position-basierter Wechsel an den Rändern
        if (position.x <= 0 && movementDirection < 0) {
            shouldChangeDirection = true;
        } else if (position.x >= Constants.SCREEN_WIDTH - bounds.width && movementDirection > 0) {
            shouldChangeDirection = true;
        }

        if (shouldChangeDirection) {
            movementDirection *= -1;
        }

        // Bewegung anwenden
        position.x += movementSpeed * movementDirection * delta;

        // Sicherstellen, dass der Boss innerhalb des Bildschirms bleibt
        position.x = MathUtils.clamp(position.x, 0, Constants.SCREEN_WIDTH - bounds.width);

        sprite.setPosition(position.x, position.y);
        bounds.setPosition(position.x, position.y);

        // Schießen
        fireTimer -= delta;
        if (fireTimer <= 0) {
            fireBullets();
            fireTimer = getFireRate();
        }

        // Boss-Bullets updaten
        for (int i = bullets.size - 1; i >= 0; i--) {
            BossBullet bullet = bullets.get(i);
            bullet.update(delta);
            if (!bullet.isAlive()) {
                bullets.removeIndex(i);
            }
        }
    }

    private void updateExplosion(float delta) {
        explosionTimer += delta;

        // Explosions-Partikel aktualisieren
        for (int i = explosionParticles.size - 1; i >= 0; i--) {
            ExplosionParticle particle = explosionParticles.get(i);
            particle.update(delta);

            if (particle.isFinished()) {
                explosionParticles.removeIndex(i);
            }
        }

        // Während der ersten Hälfte der Explosion neue Partikel erzeugen
        if (explosionTimer < explosionDuration * 0.7f) {
            createExplosionParticles(delta);
        }

        // Explosion beendet
        if (explosionTimer >= explosionDuration) {
            alive = false;
        }
    }

    private void startExplosion() {
        if (!exploding) {
            exploding = true;
            explosionTimer = 0f;
            explosionOrigin = new Vector2(
                position.x + bounds.width / 2,
                position.y + bounds.height / 2
            );

            // Spiele Explosions-Sound
            AudioManager.getInstance().playExplosionSound();

            // Initiale Explosions-Partikel erzeugen
            createInitialExplosion();

            // Alle verbleibenden Bullets entfernen
            bullets.clear();
        }
    }

    private void createInitialExplosion() {
        int particleCount = 30 + (level * 5); // WENIGER Partikel für bessere Performance

        for (int i = 0; i < particleCount; i++) {
            // Performance-Optimierung: Weniger komplexe Berechnungen
            Vector2 velocity = new Vector2(
                MathUtils.random(-300f, 300f), // Reduzierte Geschwindigkeit
                MathUtils.random(-300f, 300f)
            );

            float lifetime = MathUtils.random(0.6f, 1.2f); // Kürzere Lebenszeit

            Vector2 particlePos = new Vector2(
                explosionOrigin.x + MathUtils.random(-bounds.width * 0.3f, bounds.width * 0.3f),
                explosionOrigin.y + MathUtils.random(-bounds.height * 0.3f, bounds.height * 0.3f)
            );

            explosionParticles.add(new ExplosionParticle(particlePos, velocity, lifetime));
        }
    }

    private void createExplosionParticles(float delta) {
        // WENIGER neue Partikel während der Explosion
        if (MathUtils.random() < 0.2f && explosionParticles.size < 50) { // Begrenzung auf 50 Partikel
            Vector2 randomPos = new Vector2(
                explosionOrigin.x + MathUtils.random(-bounds.width * 0.2f, bounds.width * 0.2f),
                explosionOrigin.y + MathUtils.random(-bounds.height * 0.2f, bounds.height * 0.2f)
            );

            Vector2 velocity = new Vector2(
                MathUtils.random(-150f, 150f),
                MathUtils.random(-150f, 150f)
            );

            float lifetime = MathUtils.random(0.3f, 0.8f);
            explosionParticles.add(new ExplosionParticle(randomPos, velocity, lifetime));
        }
    }

    // Alternative Bewegung: Sinus-Wellen Bewegung für interessantere Muster
    public void updateWithSinusMovement(float delta) {
        if (!alive || exploding) return;

        movementTimer += delta;

        // Sinus-Wellen Bewegung für natürlichere Bewegung
        float amplitude = (Constants.SCREEN_WIDTH - bounds.width) / 2;
        float frequency = 0.5f + (level * 0.1f); // Schnellere Bewegung bei höheren Levels

        position.x = amplitude + amplitude * MathUtils.sin(movementTimer * frequency);

        // Leichte vertikale Bewegung hinzufügen für dynamischeres Verhalten
        float verticalMovement = MathUtils.sin(movementTimer * frequency * 0.7f) * 10;
        position.y = (Constants.SCREEN_HEIGHT - bounds.height - 80) + verticalMovement;

        sprite.setPosition(position.x, position.y);
        bounds.setPosition(position.x, position.y);

        // Schießen
        fireTimer -= delta;
        if (fireTimer <= 0) {
            fireBullets();
            fireTimer = getFireRate();
        }

        // Boss-Bullets updaten
        for (int i = bullets.size - 1; i >= 0; i--) {
            BossBullet bullet = bullets.get(i);
            bullet.update(delta);
            if (!bullet.isAlive()) {
                bullets.removeIndex(i);
            }
        }
    }

    private void fireBullets() {
        int bulletCount = 1 + (level / 2);

        // Spiele den Schuss-Sound
        AudioManager.getInstance().playShootSound();

        switch (level) {
            case 1:
                // Einfacher gerader Schuss
                BossBullet bullet1 = new BossBullet(
                    position.x + bounds.width / 2 - Constants.BOSS_BULLET_WIDTH / 2,
                    position.y - Constants.BOSS_BULLET_HEIGHT,
                    0, -Constants.BOSS_BULLET_SPEED,
                    getDamage(), bulletTexture
                );
                bullets.add(bullet1);
                break;

            case 2:
                // Drei Bullets in Fächer-Formation
                for (int i = -1; i <= 1; i++) {
                    BossBullet bullet = new BossBullet(
                        position.x + bounds.width / 2 - Constants.BOSS_BULLET_WIDTH / 2,
                        position.y - Constants.BOSS_BULLET_HEIGHT,
                        i * 100, -Constants.BOSS_BULLET_SPEED,
                        getDamage(), bulletTexture
                    );
                    bullets.add(bullet);
                }
                break;

            case 3:
                // Fünf Bullets in komplexer Formation
                float[] angles = {-30, -15, 0, 15, 30};
                for (float angle : angles) {
                    float rad = angle * MathUtils.degreesToRadians;
                    BossBullet bullet = new BossBullet(
                        position.x + bounds.width / 2 - Constants.BOSS_BULLET_WIDTH / 2,
                        position.y - Constants.BOSS_BULLET_HEIGHT,
                        MathUtils.sin(rad) * 200,
                        MathUtils.cos(rad) * -Constants.BOSS_BULLET_SPEED,
                        getDamage(), bulletTexture
                    );
                    bullets.add(bullet);
                }
                break;

            default:
                // Spiralmuster für höhere Levels
                for (int i = 0; i < bulletCount; i++) {
                    float angle = (360f / bulletCount) * i + movementTimer * 100;
                    float rad = angle * MathUtils.degreesToRadians;
                    BossBullet bullet = new BossBullet(
                        position.x + bounds.width / 2 - Constants.BOSS_BULLET_WIDTH / 2,
                        position.y - Constants.BOSS_BULLET_HEIGHT,
                        MathUtils.sin(rad) * 150,
                        MathUtils.cos(rad) * -Constants.BOSS_BULLET_SPEED,
                        getDamage(), bulletTexture
                    );
                    bullets.add(bullet);
                }
                break;
        }
    }

    private float getFireRate() {
        return Math.max(0.5f, 2f - (level * 0.2f));
    }

    private float getDamage() {
        return 10 + (level * 5);
    }

    public void render(SpriteBatch batch) {
        if (exploding) {
            // Nur Explosions-Partikel rendern
            for (ExplosionParticle particle : explosionParticles) {
                particle.render(batch);
            }
        } else if (alive) {
            // Normalen Boss rendern
            sprite.draw(batch);
        }

        // Bullets rendern (auch während Explosion)
        for (BossBullet bullet : bullets) {
            bullet.render(batch);
        }
    }

    public void takeDamage(float damage) {
        if (alive && !exploding) {
            health -= damage;
            if (health <= 0) {
                startExplosion();
            }
        }
    }

    // NEUE METHODEN FÜR EXPLOSION
    public boolean isExploding() {
        return exploding;
    }

    public boolean isExplosionFinished() {
        return exploding && explosionTimer >= explosionDuration;
    }

    // Getter-Methoden
    public Vector2 getPosition() { return position; }
    public Rectangle getBounds() { return bounds; }
    public boolean isAlive() { return alive; }
    public float getHealth() { return health; }
    public float getMaxHealth() { return maxHealth; }
    public int getLevel() { return level; }
    public Array<BossBullet> getBullets() { return bullets; }

    public void dispose() {
        sprite.getTexture().dispose();
        bulletTexture.dispose();
        for (BossBullet bullet : bullets) {
            bullet.dispose();
        }
    }
}
