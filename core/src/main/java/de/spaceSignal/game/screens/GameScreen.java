package de.spaceSignal.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;
import de.spaceSignal.game.Main;
import de.spaceSignal.game.entities.Bullet;
import de.spaceSignal.game.entities.Enemy;
import de.spaceSignal.game.entities.Player;
import de.spaceSignal.game.entities.Upgrade;
import de.spaceSignal.game.levels.*;
import de.spaceSignal.game.managers.AssetManager;
import de.spaceSignal.game.util.Constants;
import de.spaceSignal.game.util.ScrollingBackground;

/**
 * Refactored GameScreen - Verwendet das GameMode-System für saubere Trennung der Modi
 */
public class GameScreen extends BaseScreen {
    private BitmapFont uiFont;
    private BitmapFont gameOverFont;

    // Entities
    private Player player;
    private Array<Bullet> bullets;
    private Array<Enemy> enemies;
    private Array<Upgrade> upgrades;
    private Pool<Bullet> bulletPool;

    // Aktueller Spielmodus
    private GameMode currentGameMode;
    private String modeName;

    // Game State
    private float gameOverTimer;
    private ScrollingBackground background;

    public GameScreen(Main game, String modeName) {
        super(game);
        this.modeName = modeName;

        initializeFonts();
        initializeEntities();
        initializeGameMode(modeName);

        bulletPool = new Pool<Bullet>() {
            @Override
            protected Bullet newObject() {
                return new Bullet(0, 0, 0, 0, 0,
                    AssetManager.getInstance().getBulletTexture(), modeName);
            }
        };
    }

    private void initializeFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/Orbitron-Regular.ttf")
            );
            FreeTypeFontGenerator.FreeTypeFontParameter param =
                new FreeTypeFontGenerator.FreeTypeFontParameter();

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
            Gdx.app.error("GameScreen", "Failed to load fonts", e);
            uiFont = new BitmapFont();
            uiFont.getData().setScale(1.2f);
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

        // Hintergrund
        String bgPath = "textures/background.png";
        if (Gdx.files.internal(bgPath).exists()) {
            Texture bgTexture = new Texture(Gdx.files.internal(bgPath));
            background = new ScrollingBackground(bgTexture);
        }
    }

    private void initializeGameMode(String modeName) {
        switch (modeName) {
            case "Boss Rush":
                currentGameMode = new BossRushMode(player, bullets, enemies, upgrades);
                break;
            case "Asteroid Dodger":
                currentGameMode = new AsteroidDodgerMode(player, bullets, enemies, upgrades);
                break;
            case "Flappy":
                currentGameMode = new FlappyMode(player, bullets, enemies, upgrades);
                break;
            case "Classic":
            default:
                currentGameMode = new ClassicMode(player, bullets, enemies, upgrades);
                break;
        }
    }

    @Override
    public void render(float delta) {
        if (background == null) {
            ScreenUtils.clear(0.02f, 0.02f, 0.1f, 1f);
        }

        if (currentGameMode.isGameOver()) {
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

        // Spieler-Schießen
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE) && player.canFire()) {
            fireBullets();
            player.resetFireTimer();
        }

        // Bullets updaten
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update(delta);
            if (!bullet.isAlive()) {
                bullets.removeIndex(i);
                bulletPool.free(bullet);
            }
        }

        // GameMode-spezifische Updates
        currentGameMode.update(delta);
        currentGameMode.checkGameOver();

        // ESC zum Hauptmenü
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    private void fireBullets() {
        float centerX = player.getPosition().x + Constants.PLAYER_WIDTH / 2;
        float topY = player.getPosition().y + Constants.PLAYER_HEIGHT;
        float damage = 10 * player.getDamageMultiplier();

        switch (player.getBulletLevel()) {
            case 1:
                Bullet bullet1 = bulletPool.obtain();
                bullet1.reset(centerX - Constants.BULLET_WIDTH / 2, topY, 0,
                    Constants.BULLET_SPEED, damage);
                bullets.add(bullet1);
                break;

            case 2:
                Bullet bullet2a = bulletPool.obtain();
                bullet2a.reset(centerX - Constants.BULLET_WIDTH / 2 - 10, topY, 0,
                    Constants.BULLET_SPEED, damage);
                Bullet bullet2b = bulletPool.obtain();
                bullet2b.reset(centerX - Constants.BULLET_WIDTH / 2 + 10, topY, 0,
                    Constants.BULLET_SPEED, damage);
                bullets.add(bullet2a);
                bullets.add(bullet2b);
                break;

            case 3:
                Bullet bullet3a = bulletPool.obtain();
                bullet3a.reset(centerX - Constants.BULLET_WIDTH / 2, topY, 0,
                    Constants.BULLET_SPEED, damage);
                Bullet bullet3b = bulletPool.obtain();
                bullet3b.reset(centerX - Constants.BULLET_WIDTH / 2 - 15, topY, -50,
                    Constants.BULLET_SPEED, damage);
                Bullet bullet3c = bulletPool.obtain();
                bullet3c.reset(centerX - Constants.BULLET_WIDTH / 2 + 15, topY, 50,
                    Constants.BULLET_SPEED, damage);
                bullets.add(bullet3a);
                bullets.add(bullet3b);
                bullets.add(bullet3c);
                break;
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

        // GameMode-spezifische Entities
        currentGameMode.renderEntities(game.batch);

        // UI
        currentGameMode.renderUI(game.batch, uiFont);

        game.batch.end();
    }

    private void handleGameOver(float delta) {
        // Verwende den neuen dedizierten GameOverScreen
        game.setScreen(new GameOverScreen(
            game,
            modeName,
            currentGameMode.getScore(),
            currentGameMode.getWave(),
            currentGameMode.getVictoryMessage() != null,
            currentGameMode.getVictoryMessage()
        ));
        dispose();
    }

    @Override
    public void dispose() {
        if (uiFont != null) uiFont.dispose();
        if (gameOverFont != null) gameOverFont.dispose();
        if (player != null) player.dispose();
        if (bullets != null) bullets.clear();
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
        if (currentGameMode != null) currentGameMode.dispose();
    }
}
