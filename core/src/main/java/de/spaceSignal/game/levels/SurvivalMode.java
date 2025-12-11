package de.spaceSignal.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import de.spaceSignal.game.entities.*;
import de.spaceSignal.game.managers.AssetManager;
import de.spaceSignal.game.systems.SpawnSystem;
import de.spaceSignal.game.systems.UpgradeSystem;
import de.spaceSignal.game.util.Constants;

public class SurvivalMode extends GameMode {
    private SpawnSystem spawnSystem;
    private UpgradeSystem upgradeSystem;

    public SurvivalMode(Player player, Array<Bullet> bullets, Array<Enemy> enemies, Array<Upgrade> upgrades) {
        super(player, bullets, enemies, upgrades);

        AssetManager assetManager = AssetManager.getInstance();
        this.spawnSystem = new SpawnSystem(
            assetManager.getEnemyTexture(),
            assetManager.getBulletTexture(),
            assetManager.getBomberTexture(),
            assetManager.getScoutTexture()
        );
        this.upgradeSystem = new UpgradeSystem();

        // Survival Mode startet mit höherer Schwierigkeit
        spawnSystem.increaseDifficulty();
    }

    @Override
    public void update(float delta) {
        // Spawn-System mit aggressiverer Schwierigkeit
        spawnSystem.update(delta);
        Array<Enemy> newEnemies = spawnSystem.getEnemies();
        enemies.addAll(newEnemies);
        newEnemies.clear();

        // Gegner updaten
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta);

            if (!enemy.isAlive()) {
                enemies.removeIndex(i);
                incrementScore(15); // Mehr Punkte im Survival Mode

                String possibleUpgrade = upgradeSystem.getValidUpgradeType(player);
                if (possibleUpgrade != null && MathUtils.random() < Constants.UPGRADE_SPAWN_CHANCE) {
                    spawnUpgrade(enemy.getPosition().x, enemy.getPosition().y);
                }

                if (score % 150 == 0) { // Schnellere Wave-Erhöhung
                    incrementWave();
                    spawnSystem.increaseDifficulty();
                }
            }
        }

        // Upgrades updaten
        for (int i = upgrades.size - 1; i >= 0; i--) {
            Upgrade upgrade = upgrades.get(i);
            upgrade.update(delta);

            if (!upgrade.isAlive()) {
                upgrades.removeIndex(i);
            } else if (player.getBounds().overlaps(upgrade.getBounds())) {
                String type = upgrade.getType();
                if (upgradeSystem.isUpgradeValid(type, player)) {
                    player.applyUpgrade(type);
                }
                upgrade.collect();
            }
        }

        checkCollisions();
    }

    @Override
    public void renderEntities(SpriteBatch batch) {
        for (Enemy enemy : enemies) {
            enemy.render(batch);
        }

        for (Upgrade upgrade : upgrades) {
            upgrade.render(batch);
        }
    }

    @Override
    public void renderUI(SpriteBatch batch, BitmapFont uiFont) {
        uiFont.draw(batch, "Score: " + score, 10, Constants.SCREEN_HEIGHT - 10);
        uiFont.draw(batch, "Wave: " + wave, 10, Constants.SCREEN_HEIGHT - 35);
        uiFont.draw(batch, "Health: " + (int) player.getHealth(), 10, Constants.SCREEN_HEIGHT - 60);
        uiFont.draw(batch, "Bullets: Lvl " + player.getBulletLevel(), 10, Constants.SCREEN_HEIGHT - 85);

        // Survival-spezifische Info
        uiFont.draw(batch, "Mode: SURVIVAL", 10, Constants.SCREEN_HEIGHT - 110);
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
        return null; // Kein Victory im Survival Mode
    }

    private void checkCollisions() {
        // Bullets vs Enemies
        for (int i = bullets.size - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            for (int j = enemies.size - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);
                if (bullet.getBounds().overlaps(enemy.getBounds()) && enemy.isAlive()) {
                    enemy.takeDamage(bullet.getDamage());
                    bullet.destroy();

                    if (!enemy.isAlive()) {
                        enemies.removeIndex(j);
                        incrementScore(15);

                        String possibleUpgrade = upgradeSystem.getValidUpgradeType(player);
                        if (possibleUpgrade != null && MathUtils.random() < Constants.UPGRADE_SPAWN_CHANCE) {
                            spawnUpgrade(enemy.getPosition().x, enemy.getPosition().y);
                        }

                        if (score % 150 == 0) {
                            incrementWave();
                        }
                    }
                    break;
                }
            }
        }

        // Player vs Enemies
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            if (player.getBounds().overlaps(enemy.getBounds()) && enemy.isAlive()) {
                player.takeDamage(1);
                enemy.takeDamage(999);

                if (!player.isAlive()) {
                    setGameOver(true);
                }

                if (!enemy.isAlive()) {
                    enemies.removeIndex(i);
                }
            }
        }
    }

    private void spawnUpgrade(float x, float y) {
        String type = upgradeSystem.getValidUpgradeType(player);
        if (type != null) {
            try {
                Texture upgradeTexture = new Texture(
                    Gdx.files.internal("textures/upgrade_" + type.toLowerCase() + ".png")
                );
                upgrades.add(new Upgrade(x, y, type, upgradeTexture));
            } catch (Exception e) {
                Gdx.app.error("SurvivalMode", "Failed to load upgrade texture", e);
            }
        }
    }

    @Override
    public void dispose() {
        // Cleanup wenn nötig
    }
}
