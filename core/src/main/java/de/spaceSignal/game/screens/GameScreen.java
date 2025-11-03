package de.spaceSignal.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;

import de.spaceSignal.game.Main;
import de.spaceSignal.game.entities.BomberEnemy;
import de.spaceSignal.game.entities.Boss;
import de.spaceSignal.game.entities.BossBullet;
import de.spaceSignal.game.entities.Bullet;
import de.spaceSignal.game.entities.Enemy;
import de.spaceSignal.game.entities.Player;
import de.spaceSignal.game.entities.ScoutEnemy;
import de.spaceSignal.game.entities.Upgrade;
import de.spaceSignal.game.managers.AssetManager;
import de.spaceSignal.game.systems.SpawnSystem;
import de.spaceSignal.game.util.Constants;
import de.spaceSignal.game.util.ScrollingBackground;

public class GameScreen extends BaseScreen {
    private BitmapFont uiFont;
    private BitmapFont gameOverFont;
    private Player player;
    private Array<Bullet> bullets;
    private Array<Enemy> enemies;
    private Array<Upgrade> upgrades;
    private SpawnSystem spawnSystem;
    private int score;
    private int wave;
    private float timeLeft;
    private float gameOverTimer;
    private boolean isGameOver;
    private final String gameMode;
    private Pool<Bullet> bulletPool;
    private ScrollingBackground background;

    // Boss Rush Variablen
    private Boss boss;
    private boolean isBossRushMode;
    private int bossLevel;
    private Texture solidTexture;

    public GameScreen(Main game, String modeName) {
        super(game);
        this.gameMode = modeName;
        this.isBossRushMode = modeName.equals("Boss Rush");
        this.bossLevel = isBossRushMode ? Constants.BOSS_RUSH_START_LEVEL : 0;

        initializeFonts();
        initializeEntities();

        // Initialize spawn system
        AssetManager assetManager = AssetManager.getInstance();
        spawnSystem = new SpawnSystem(
            assetManager.getEnemyTexture(),
            assetManager.getBulletTexture(),
            assetManager.getBomberTexture(),
            assetManager.getScoutTexture()
        );

        score = 0;
        wave = 1;
        gameOverTimer = 0;
        isGameOver = false;
        if (gameMode.equals("Time Attack")) {
            timeLeft = 60f;
        }

        bulletPool = new Pool<Bullet>() {
            @Override
            protected Bullet newObject() {
                return new Bullet(0, 0, 0, 0, 0, AssetManager.getInstance().getBulletTexture(), gameMode);
            }
        };

        solidTexture = createSolidTexture();
    }

    private void initializeFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Orbitron-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

            param.size = 24;
            param.color = Color.WHITE;
            param.borderColor = Color.BLACK;
            param.borderWidth = 1;
            uiFont = generator.generateFont(param);

            param.size = 48;
            param.color = Color.RED;
            param.borderWidth = 2;
            gameOverFont = generator.generateFont(param);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load Orbitron-Regular.ttf, using fallback font", e);
            uiFont = new BitmapFont();
            uiFont.getData().setScale(1.2f);
            uiFont.setColor(Color.WHITE);

            gameOverFont = new BitmapFont();
            gameOverFont.getData().setScale(2f);
            gameOverFont.setColor(Color.RED);
        }
    }

    private void initializeEntities() {
        Texture playerTexture = new Texture(Gdx.files.internal("textures/player.png"));
        player = new Player(
            Constants.SCREEN_WIDTH / 2 - Constants.PLAYER_WIDTH / 2,
            50,
            playerTexture
        );
        bullets = new Array<>();
        enemies = new Array<>();
        upgrades = new Array<>();

        // Boss initialisieren falls Boss Rush Modus
        if (isBossRushMode) {
            try {
                Texture bossTexture = new Texture(Gdx.files.internal("textures/boss.png"));
                Texture bossBulletTexture = new Texture(Gdx.files.internal("textures/boss_bullet.png"));
                boss = new Boss(bossLevel, bossTexture, bossBulletTexture);
            } catch (Exception e) {
                Gdx.app.error("GameScreen", "Failed to load boss textures, using fallback", e);
                Texture bossTexture = new Texture(Gdx.files.internal("textures/enemy.png"));
                Texture bossBulletTexture = new Texture(Gdx.files.internal("textures/bullet.png"));
                boss = new Boss(bossLevel, bossTexture, bossBulletTexture);
            }
        }

        // Hintergrund initialisieren
        String bgPath = "textures/background.png";
        if (!Gdx.files.internal(bgPath).exists()) {
            Gdx.app.error("GameScreen", "Background texture not found: " + bgPath + ". Using solid color fallback.");
        } else {
            Texture bgTexture = new Texture(Gdx.files.internal(bgPath));
            background = new ScrollingBackground(bgTexture);
        }
    }

    @Override
    public void render(float delta) {
        if (background == null) {
            ScreenUtils.clear(0.02f, 0.02f, 0.1f, 1f);
        }

        if (isGameOver) {
            handleGameOver(delta);
            return;
        }

        update(delta);
        draw();
    }

    private void update(float delta) {
        if (background != null) {
            background.update(delta);
        }

        player.update(delta);

        if (isBossRushMode) {
            updateBossRush(delta);
        } else {
            updateNormalMode(delta);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    private void updateBossRush(float delta) {
        if (boss != null) {
            if (boss.isExploding()) {
                // Boss ist am Explodieren - nur Explosion updaten
                boss.update(delta);

                // Prüfe ob Explosion beendet ist
                if (boss.isExplosionFinished()) {
                    // Boss komplett zerstört - nächsten Boss spawnen
                    score += 100 * bossLevel;
                    bossLevel++;
                    if (bossLevel > Constants.BOSS_RUSH_MAX_LEVEL) {
                        isGameOver = true;
                    } else {
                        try {
                            Texture bossTexture = new Texture(Gdx.files.internal("textures/boss.png"));
                            Texture bossBulletTexture = new Texture(Gdx.files.internal("textures/boss_bullet.png"));
                            boss = new Boss(bossLevel, bossTexture, bossBulletTexture);
                        } catch (Exception e) {
                            Texture bossTexture = new Texture(Gdx.files.internal("textures/enemy.png"));
                            Texture bossBulletTexture = new Texture(Gdx.files.internal("textures/bullet.png"));
                            boss = new Boss(bossLevel, bossTexture, bossBulletTexture);
                        }
                    }
                }
            } else if (boss.isAlive()) {
                // Boss ist lebendig - normale Updates
                // Wähle eine der Bewegungsmethoden:
                //boss.update(delta);
                boss.updateWithSinusMovement(delta);

                // Boss-Bullets Kollision mit Spieler
                for (BossBullet bossBullet : boss.getBullets()) {
                    if (bossBullet.isAlive() && player.getBounds().overlaps(bossBullet.getBounds())) {
                        player.takeDamage(bossBullet.getDamage());
                        bossBullet.destroy();
                        if (!player.isAlive()) {
                            isGameOver = true;
                        }
                    }
                }

                // Spieler-Bullets Kollision mit Boss
                for (int i = bullets.size - 1; i >= 0; i--) {
                    Bullet bullet = bullets.get(i);
                    if (bullet.isAlive() && boss.getBounds().overlaps(bullet.getBounds())) {
                        boss.takeDamage(bullet.getDamage());
                        bullet.destroy();
                    }
                }
            }
        }

        // Spieler kann schießen (immer)
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && player.canFire()) {
            fireBullets();
            player.resetFireTimer();
        }

        // Bullets updaten (immer)
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta);
            if (!bullet.isAlive()) {
                bullets.removeIndex(i);
                bulletPool.free(bullet);
            }
        }
    }

    private void updateNormalMode(float delta) {
        if (gameMode.equals("Time Attack")) {
            timeLeft -= delta;
            if (timeLeft <= 0) {
                isGameOver = true;
                return;
            }
        }

        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && player.canFire()) {
            fireBullets();
            player.resetFireTimer();
        }

        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta);
            if (!bullet.isAlive()) {
                bullets.removeIndex(i);
                bulletPool.free(bullet);
            }
        }

        // Update spawn system
        spawnSystem.update(delta);
        
        // Get new enemies from spawn system
        Array<Enemy> newEnemies = spawnSystem.getEnemies();
        enemies.addAll(newEnemies);
        newEnemies.clear();

        // Update existing enemies
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta);
            if (!enemy.isAlive()) {
                enemies.removeIndex(i);
                score += getScorePerEnemy();
                if (MathUtils.random() < Constants.UPGRADE_SPAWN_CHANCE) {
                    spawnUpgrade(enemy.getPosition().x, enemy.getPosition().y);
                }
                if (score % 100 == 0) {
                    wave++;
                    // Increase difficulty in spawn system
                    spawnSystem.increaseDifficulty();
                }
            }
        }

        for (int i = upgrades.size - 1; i >= 0; i--) {
            Upgrade upgrade = upgrades.get(i);
            upgrade.update(delta);
            if (!upgrade.isAlive()) {
                upgrades.removeIndex(i);
            } else if (player.getBounds().overlaps(upgrade.getBounds())) {
                player.applyUpgrade(upgrade.getType());
                upgrade.collect();
            }
        }

        checkCollisions();
    }

    private float getSpawnInterval() {
        switch (gameMode) {
            case "Classic Mode":
                return Math.max(0.5f, Constants.ENEMY_SPAWN_INTERVAL - wave * 0.1f);
            case "Survival Mode":
                return Math.max(0.3f, Constants.ENEMY_SPAWN_INTERVAL - wave * 0.15f);
            case "Time Attack":
                return Math.max(0.4f, Constants.ENEMY_SPAWN_INTERVAL - wave * 0.12f);
            default:
                return Constants.ENEMY_SPAWN_INTERVAL;
        }
    }

    private int getScorePerEnemy() {
        switch (gameMode) {
            case "Classic Mode":
                return 10;
            case "Survival Mode":
                return 15;
            case "Time Attack":
                return 20;
            default:
                return 10;
        }
    }

    private void fireBullets() {
        float centerX = player.getPosition().x + Constants.PLAYER_WIDTH / 2;
        float topY = player.getPosition().y + Constants.PLAYER_HEIGHT;
        float damage = 10 * player.getDamageMultiplier();

        switch (player.getBulletLevel()) {
            case 1:
                Bullet bullet1 = bulletPool.obtain();
                bullet1.reset(centerX - Constants.BULLET_WIDTH / 2, topY, 0, Constants.BULLET_SPEED, damage);
                bullets.add(bullet1);
                break;
            case 2:
                Bullet bullet2a = bulletPool.obtain();
                bullet2a.reset(centerX - Constants.BULLET_WIDTH / 2 - 10, topY, 0, Constants.BULLET_SPEED, damage);
                Bullet bullet2b = bulletPool.obtain();
                bullet2b.reset(centerX - Constants.BULLET_WIDTH / 2 + 10, topY, 0, Constants.BULLET_SPEED, damage);
                bullets.add(bullet2a);
                bullets.add(bullet2b);
                break;
            case 3:
                Bullet bullet3a = bulletPool.obtain();
                bullet3a.reset(centerX - Constants.BULLET_WIDTH / 2, topY, 0, Constants.BULLET_SPEED, damage);
                Bullet bullet3b = bulletPool.obtain();
                bullet3b.reset(centerX - Constants.BULLET_WIDTH / 2 - 15, topY, -50, Constants.BULLET_SPEED, damage);
                Bullet bullet3c = bulletPool.obtain();
                bullet3c.reset(centerX - Constants.BULLET_WIDTH / 2 + 15, topY, 50, Constants.BULLET_SPEED, damage);
                bullets.add(bullet3a);
                bullets.add(bullet3b);
                bullets.add(bullet3c);
                break;
        }
    }

    private void spawnEnemy() {
        float x = MathUtils.random(0, Constants.SCREEN_WIDTH - Constants.ENEMY_WIDTH);
        float y = Constants.SCREEN_HEIGHT;
        float health = getEnemyHealth();
        AssetManager assetManager = AssetManager.getInstance();

        // Wähle zufällig einen Gegnertyp basierend auf dem Fortschritt
        float random = MathUtils.random(1f);
        Enemy enemy;

        if (wave < 3) {
            // Frühe Wellen: Hauptsächlich normale Gegner
            if (random < 0.8f) {
                enemy = new Enemy(x, y, health, assetManager.getEnemyTexture(), gameMode);
            } else {
                enemy = new ScoutEnemy(x, y, assetManager.getScoutTexture());
            }
        } else {
            // Spätere Wellen: Alle Gegnertypen
            if (random < 0.4f) {
                enemy = new Enemy(x, y, health, assetManager.getEnemyTexture(), gameMode);
            } else if (random < 0.7f) {
                enemy = new ScoutEnemy(x, y, assetManager.getScoutTexture());
            } else {
                enemy = new BomberEnemy(x, y, assetManager.getBomberTexture());
            }
        }

        enemies.add(enemy);
    }

    private void spawnUpgrade(float x, float y) {
        String[] types = {"BulletLevel", "Health", "Damage"};
        String type = types[MathUtils.random(0, types.length - 1)];
        Texture upgradeTexture;
        try {
            upgradeTexture = new Texture(Gdx.files.internal("textures/upgrade_" + type.toLowerCase() + ".png"));
        } catch (Exception e) {
            Gdx.app.error("GameScreen", "Failed to load upgrade texture, using fallback", e);
            upgradeTexture = new Texture(Gdx.files.internal("textures/upgrade.png"));
        }
        upgrades.add(new Upgrade(x, y, type, upgradeTexture));
    }

    private float getEnemyHealth() {
        switch (gameMode) {
            case "Classic Mode":
                return 10 + wave * 5;
            case "Survival Mode":
                return 15 + wave * 7;
            case "Time Attack":
                return 8 + wave * 4;
            default:
                return 10;
        }
    }

    private void checkCollisions() {
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (bullet.getBounds().overlaps(enemy.getBounds()) && enemy.isAlive()) {
                    enemy.takeDamage(bullet.getDamage());
                    bullet.destroy();
                    if (!enemy.isAlive()) {
                        enemies.removeIndex(j);
                        score += getScorePerEnemy();
                        if (MathUtils.random() < Constants.UPGRADE_SPAWN_CHANCE) {
                            spawnUpgrade(enemy.getPosition().x, enemy.getPosition().y);
                        }
                        if (score % 100 == 0) {
                            wave++;
                        }
                    }
                    break;
                }
            }
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (player.getBounds().overlaps(enemy.getBounds()) && enemy.isAlive()) {
                player.takeDamage(1);
                enemy.takeDamage(999);
                if (!player.isAlive()) {
                    isGameOver = true;
                }
                if (!enemy.isAlive()) {
                    enemies.removeIndex(i);
                }
            }
        }
    }

    private void draw() {
        game.batch.begin();

        // Hintergrund
        if (background != null) {
            background.render(game.batch);
        }

        // Spieler und Bullets (immer rendern)
        player.render(game.batch);
        for (Bullet bullet : bullets) {
            bullet.render(game.batch);
        }

        // Modus-spezifische Entities
        if (isBossRushMode) {
            if (boss != null) {
                boss.render(game.batch);
            }
        } else {
            for (Enemy enemy : enemies) {
                enemy.render(game.batch);
            }
            for (Upgrade upgrade : upgrades) {
                upgrade.render(game.batch);
            }
        }

        // UI je nach Modus
        drawUI();

        game.batch.end();
    }

    private void drawUI() {
        if (isBossRushMode) {
            drawBossRushUI();
        } else {
            drawNormalUI();
        }
    }

    private void drawBossRushUI() {
        // Linke Seite - Spieler-Info (kompakt)
        uiFont.draw(game.batch, "Score: " + score, 15, Constants.SCREEN_HEIGHT - 25);
        uiFont.draw(game.batch, "Lvl: " + bossLevel, 15, Constants.SCREEN_HEIGHT - 50);
        uiFont.draw(game.batch, "HP: " + (int) player.getHealth(), 15, Constants.SCREEN_HEIGHT - 75);
        uiFont.draw(game.batch, "Bullets: " + player.getBulletLevel(), 15, Constants.SCREEN_HEIGHT - 100);

        // Boss Health Bar nur anzeigen wenn Boss lebendig und nicht explodiert
        if (boss != null && boss.isAlive() && !boss.isExploding()) {
            drawBossHealthBar();
        }

        // Explosions-Info anzeigen
        if (boss != null && boss.isExploding()) {
            uiFont.setColor(1f, 0.5f, 0f, 1f); // Orange für Explosions-Text
            uiFont.draw(game.batch, "BOSS EXPLODING!", Constants.SCREEN_WIDTH / 2 - 80, Constants.SCREEN_HEIGHT - 30);
            uiFont.setColor(1, 1, 1, 1); // Zurück zu weiß
        }
    }

    private void drawNormalUI() {
        uiFont.draw(game.batch, "Score: " + score, 10, Constants.SCREEN_HEIGHT - 10);
        uiFont.draw(game.batch, "Wave: " + wave, 10, Constants.SCREEN_HEIGHT - 35);
        uiFont.draw(game.batch, "Health: " + (int) player.getHealth(), 10, Constants.SCREEN_HEIGHT - 60);
        uiFont.draw(game.batch, "Bullets: Lvl " + player.getBulletLevel(), 10, Constants.SCREEN_HEIGHT - 85);

        if (gameMode.equals("Time Attack")) {
            uiFont.draw(game.batch, "Time: " + (int) timeLeft + "s", 10, Constants.SCREEN_HEIGHT - 110);
        }
    }

    private void drawBossHealthBar() {
        float healthPercent = boss.getHealth() / boss.getMaxHealth();
        float barWidth = 250;
        float barHeight = 25;
        float barX = Constants.SCREEN_WIDTH - barWidth - 250;
        float barY = Constants.SCREEN_HEIGHT - 50;

        // Hintergrund (dunkelrot)
        game.batch.setColor(0.3f, 0, 0, 0.9f);
        game.batch.draw(solidTexture, barX, barY, barWidth, barHeight);

        // Lebensbalken (hellrot/grün basierend auf Gesundheit)
        Color healthColor = healthPercent > 0.3f ?
            new Color(1, 0, 0, 0.9f) : // Rot bei hoher Gesundheit
            new Color(1, 0.5f, 0, 0.9f); // Orange bei niedriger Gesundheit

        game.batch.setColor(healthColor);
        game.batch.draw(solidTexture, barX, barY, barWidth * healthPercent, barHeight);
        game.batch.setColor(1, 1, 1, 1);

        // Rahmen
        game.batch.setColor(1, 1, 1, 0.8f);
        game.batch.draw(solidTexture, barX, barY, barWidth, 2); // Oben
        game.batch.draw(solidTexture, barX, barY, 2, barHeight); // Links
        game.batch.draw(solidTexture, barX + barWidth, barY, 2, barHeight); // Rechts
        game.batch.draw(solidTexture, barX, barY + barHeight, barWidth, 2); // Unten
        game.batch.setColor(1, 1, 1, 1);

        // Health Text (zentriert über der Bar)
        String healthText = "BOSS " + (int)boss.getHealth() + "/" + (int)boss.getMaxHealth();
        float textX = barX + (barWidth - (healthText.length() * 8)) / 2; // Einfache Zentrierung
        uiFont.draw(game.batch, healthText, textX, barY + barHeight + 20);
    }

    private void handleGameOver(float delta) {
        gameOverTimer += delta;
        float alpha = MathUtils.sin(gameOverTimer * 2) * 0.3f + 0.7f;
        game.batch.begin();

        if (isBossRushMode && bossLevel > Constants.BOSS_RUSH_MAX_LEVEL) {
            gameOverFont.setColor(0, 1, 0, alpha);
            gameOverFont.draw(game.batch, "VICTORY!", Constants.SCREEN_WIDTH / 2 - 100, Constants.SCREEN_HEIGHT / 2);
            uiFont.setColor(1, 1, 1, alpha);
            uiFont.draw(game.batch, "You defeated all bosses!", Constants.SCREEN_WIDTH / 2 - 120, Constants.SCREEN_HEIGHT / 2 - 50);
        } else {
            gameOverFont.setColor(1, 0, 0, alpha);
            gameOverFont.draw(game.batch, "GAME OVER", Constants.SCREEN_WIDTH / 2 - 120, Constants.SCREEN_HEIGHT / 2);
            uiFont.setColor(1, 1, 1, alpha);
        }

        uiFont.draw(game.batch, "Final Score: " + score, Constants.SCREEN_WIDTH / 2 - 80, Constants.SCREEN_HEIGHT / 2 - 50);
        uiFont.draw(game.batch, "Press SPACE to restart", Constants.SCREEN_WIDTH / 2 - 120, Constants.SCREEN_HEIGHT / 2 - 100);
        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game, gameMode));
            dispose();
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    private Texture createSolidTexture() {
        Texture texture = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        if (uiFont != null) uiFont.dispose();
        if (gameOverFont != null) gameOverFont.dispose();
        if (player != null) player.dispose();

        if (bullets != null) {
            for (Bullet bullet : bullets) {
                if (bullet != null) bullet.dispose();
            }
            bullets.clear();
        }

        if (enemies != null) {
            for (Enemy enemy : enemies) {
                if (enemy != null) enemy.dispose();
            }
            enemies.clear();
        }

        if (upgrades != null) {
            for (Upgrade upgrade : upgrades) {
                if (upgrade != null) upgrade.dispose();
            }
            upgrades.clear();
        }

        if (bulletPool != null) bulletPool.clear();
        if (background != null) background.dispose();
        if (boss != null) boss.dispose();
        if (solidTexture != null) solidTexture.dispose();
    }
}
