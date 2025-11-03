package de.spaceSignal.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;
import de.spaceSignal.game.Main;
import de.spaceSignal.game.entities.Bullet;
import de.spaceSignal.game.entities.Enemy;
import de.spaceSignal.game.entities.Player;
import de.spaceSignal.game.entities.Upgrade;
import de.spaceSignal.game.util.Constants;
import de.spaceSignal.game.util.ScrollingBackground;

public class GameScreen extends BaseScreen {
    private BitmapFont uiFont;
    private BitmapFont gameOverFont;
    private Player player;
    private Array<Bullet> bullets;
    private Array<Enemy> enemies;
    private Array<Upgrade> upgrades;
    private float enemySpawnTimer;
    private int score;
    private int wave;
    private float timeLeft;
    private float gameOverTimer;
    private boolean isGameOver;
    private final String gameMode;
    private Pool<Bullet> bulletPool;
    private ScrollingBackground background; // Neuer Hintergrund

    public GameScreen(Main game, String modeName) {
        super(game);
        this.gameMode = modeName;
        initializeFonts();
        initializeEntities();

        enemySpawnTimer = 0;
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
                return new Bullet(0, 0, 0, 0, 0, new Texture(Gdx.files.internal("textures/bullet.png")), gameMode);
            }
        };
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
        // Kein ScreenUtils.clear mehr, wenn Hintergrund vorhanden
        if (background == null) {
            ScreenUtils.clear(0.02f, 0.02f, 0.1f, 1f); // Fallback
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
            background.update(delta); // Hintergrund scrollen
        }

        player.update(delta);

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

        enemySpawnTimer += delta;
        float spawnInterval = getSpawnInterval();
        if (enemySpawnTimer >= spawnInterval) {
            spawnEnemy();
            enemySpawnTimer = 0;
        }

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

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
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
        Texture enemyTexture = new Texture(Gdx.files.internal("textures/enemy.png"));
        enemies.add(new Enemy(x, y, health, enemyTexture, gameMode));
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
                        enemies.removeIndex(j); // Entferne nur, wenn der Gegner tot ist
                        score += getScorePerEnemy();
                        if (MathUtils.random() < Constants.UPGRADE_SPAWN_CHANCE) {
                            spawnUpgrade(enemy.getPosition().x, enemy.getPosition().y);
                        }
                        if (score % 100 == 0) {
                            wave++;
                        }
                    }
                    break; // Verlasse die innere Schleife nach Kollision
                }
            }
        }

        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (player.getBounds().overlaps(enemy.getBounds()) && enemy.isAlive()) {
                player.takeDamage(1);
                enemy.takeDamage(999); // Sofortiger Tod bei Kollision mit Spieler
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
        if (background != null) {
            background.render(game.batch); // Hintergrund zeichnen
        }
        player.render(game.batch);
        for (Bullet bullet : bullets) {
            bullet.render(game.batch);
        }
        for (Enemy enemy : enemies) {
            enemy.render(game.batch);
        }
        for (Upgrade upgrade : upgrades) {
            upgrade.render(game.batch);
        }

        uiFont.draw(game.batch, "Score: " + score, 10, Constants.SCREEN_HEIGHT - 10);
        uiFont.draw(game.batch, "Wave: " + wave, 10, Constants.SCREEN_HEIGHT - 35);
        uiFont.draw(game.batch, "Health: " + (int) player.getHealth(), 10, Constants.SCREEN_HEIGHT - 60);
        uiFont.draw(game.batch, "Bullets: Lvl " + player.getBulletLevel(), 10, Constants.SCREEN_HEIGHT - 85);
        if (gameMode.equals("Time Attack")) {
            uiFont.draw(game.batch, "Time: " + (int) timeLeft + "s", 10, Constants.SCREEN_HEIGHT - 110);
        }
        game.batch.end();
    }

    private void handleGameOver(float delta) {
        gameOverTimer += delta;
        float alpha = MathUtils.sin(gameOverTimer * 2) * 0.3f + 0.7f;
        game.batch.begin();
        gameOverFont.setColor(1, 0, 0, alpha);
        gameOverFont.draw(game.batch, "GAME OVER", Constants.SCREEN_WIDTH / 2 - 120, Constants.SCREEN_HEIGHT / 2);
        uiFont.setColor(1, 1, 1, alpha);
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

    @Override
    public void dispose() {
        uiFont.dispose();
        gameOverFont.dispose();
        player.dispose();
        for (Bullet bullet : bullets) {
            bullet.dispose();
        }
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
        for (Upgrade upgrade : upgrades) {
            upgrade.dispose();
        }
        bulletPool.clear();
        if (background != null) {
            background.dispose();
        }
    }
}
