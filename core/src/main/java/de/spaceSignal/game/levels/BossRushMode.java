package de.spaceSignal.game.levels;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import de.spaceSignal.game.entities.*;
import de.spaceSignal.game.util.Constants;

public class BossRushMode extends GameMode {
    private Boss boss;
    private int bossLevel;
    private Texture solidTexture;
    private boolean victory;

    public BossRushMode(Player player, Array<Bullet> bullets, Array<Enemy> enemies, Array<Upgrade> upgrades) {
        super(player, bullets, enemies, upgrades);
        this.bossLevel = Constants.BOSS_RUSH_START_LEVEL;
        this.victory = false;

        solidTexture = createSolidTexture();
        spawnBoss();
    }

    private void spawnBoss() {
        try {
            Texture bossTexture = new Texture(Gdx.files.internal("textures/boss.png"));
            Texture bossBulletTexture = new Texture(Gdx.files.internal("textures/boss_bullet.png"));
            boss = new Boss(bossLevel, bossTexture, bossBulletTexture);
        } catch (Exception e) {
            Gdx.app.error("BossRushMode", "Failed to load boss textures, using fallback", e);
            Texture bossTexture = new Texture(Gdx.files.internal("textures/enemy.png"));
            Texture bossBulletTexture = new Texture(Gdx.files.internal("textures/bullet.png"));
            boss = new Boss(bossLevel, bossTexture, bossBulletTexture);
        }
    }

    @Override
    public void update(float delta) {
        if (boss == null) return;

        if (boss.isExploding()) {
            boss.update(delta);

            if (boss.isExplosionFinished()) {
                incrementScore(100 * bossLevel);
                bossLevel++;

                if (bossLevel > Constants.BOSS_RUSH_MAX_LEVEL) {
                    victory = true;
                    setGameOver(true);
                } else {
                    spawnBoss();
                }
            }
        } else if (boss.isAlive()) {
            boss.updateWithSinusMovement(delta);

            // Boss-Bullets vs Player
            for (BossBullet bossBullet : boss.getBullets()) {
                if (bossBullet.isAlive() && player.getBounds().overlaps(bossBullet.getBounds())) {
                    player.takeDamage(bossBullet.getDamage());
                    bossBullet.destroy();

                    if (!player.isAlive()) {
                        setGameOver(true);
                    }
                }
            }

            // Player-Bullets vs Boss
            for (int i = bullets.size - 1; i >= 0; i--) {
                Bullet bullet = bullets.get(i);
                if (bullet.isAlive() && boss.getBounds().overlaps(bullet.getBounds())) {
                    boss.takeDamage(bullet.getDamage());
                    bullet.destroy();
                }
            }
        }
    }

    @Override
    public void renderEntities(SpriteBatch batch) {
        if (boss != null) {
            boss.render(batch);
        }
    }

    @Override
    public void renderUI(SpriteBatch batch, BitmapFont uiFont) {
        // Spieler-Info
        uiFont.draw(batch, "Score: " + score, 15, Constants.SCREEN_HEIGHT - 25);
        uiFont.draw(batch, "Lvl: " + bossLevel, 15, Constants.SCREEN_HEIGHT - 50);
        uiFont.draw(batch, "HP: " + (int) player.getHealth(), 15, Constants.SCREEN_HEIGHT - 75);
        uiFont.draw(batch, "Bullets: " + player.getBulletLevel(), 15, Constants.SCREEN_HEIGHT - 100);

        // Boss Health Bar
        if (boss != null && boss.isAlive() && !boss.isExploding()) {
            drawBossHealthBar(batch, uiFont);
        }

        // Explosion Info
        if (boss != null && boss.isExploding()) {
            uiFont.setColor(1f, 0.5f, 0f, 1f);
            uiFont.draw(batch, "BOSS EXPLODING!", Constants.SCREEN_WIDTH / 2 - 80, Constants.SCREEN_HEIGHT - 30);
            uiFont.setColor(1, 1, 1, 1);
        }
    }

    private void drawBossHealthBar(SpriteBatch batch, BitmapFont font) {
        float healthPercent = boss.getHealth() / boss.getMaxHealth();
        float barWidth = 250;
        float barHeight = 25;
        float barX = Constants.SCREEN_WIDTH - barWidth - 250;
        float barY = Constants.SCREEN_HEIGHT - 50;

        // Hintergrund
        batch.setColor(0.3f, 0, 0, 0.9f);
        batch.draw(solidTexture, barX, barY, barWidth, barHeight);

        // Lebensbalken
        Color healthColor = healthPercent > 0.3f
            ? new Color(1, 0, 0, 0.9f)
            : new Color(1, 0.5f, 0, 0.9f);
        batch.setColor(healthColor);
        batch.draw(solidTexture, barX, barY, barWidth * healthPercent, barHeight);
        batch.setColor(1, 1, 1, 1);

        // Rahmen
        batch.setColor(1, 1, 1, 0.8f);
        batch.draw(solidTexture, barX, barY, barWidth, 2);
        batch.draw(solidTexture, barX, barY, 2, barHeight);
        batch.draw(solidTexture, barX + barWidth, barY, 2, barHeight);
        batch.draw(solidTexture, barX, barY + barHeight, barWidth, 2);
        batch.setColor(1, 1, 1, 1);

        // Health Text
        String healthText = "BOSS " + (int)boss.getHealth() + "/" + (int)boss.getMaxHealth();
        float textX = barX + (barWidth - (healthText.length() * 8)) / 2;
        font.draw(batch, healthText, textX, barY + barHeight + 20);
    }

    @Override
    public boolean checkGameOver() {
        return isGameOver;
    }

    @Override
    public String getVictoryMessage() {
        return victory ? "VICTORY! You defeated all bosses!" : null;
    }

    private Texture createSolidTexture() {
        Texture texture = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        if (boss != null) boss.dispose();
        if (solidTexture != null) solidTexture.dispose();
    }
}
