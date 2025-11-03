package de.spaceSignal.game.systems;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import de.spaceSignal.game.entities.BomberEnemy;
import de.spaceSignal.game.entities.Enemy;
import de.spaceSignal.game.entities.ScoutEnemy;
import de.spaceSignal.game.util.Constants;

public class SpawnSystem {
    private float spawnTimer;
    private float timeSinceStart;
    private float difficulty;
    private Array<Enemy> enemies;
    private final Texture enemyTexture;
    private final Texture bulletTexture;
    private final Texture bomberTexture;
    private final Texture scoutTexture;

    public SpawnSystem(Texture enemyTexture, Texture bulletTexture,
                      Texture bomberTexture, Texture scoutTexture) {
        this.enemyTexture = enemyTexture;
        this.bulletTexture = bulletTexture;
        this.bomberTexture = bomberTexture;
        this.scoutTexture = scoutTexture;
        this.enemies = new Array<>();
        this.spawnTimer = 0;
        this.timeSinceStart = 0;
        this.difficulty = 1.0f;
    }

    public void update(float delta) {
        timeSinceStart += delta;
        spawnTimer += delta;
        
        // Erhöhe die Schwierigkeit mit der Zeit
        difficulty = 1.0f + (timeSinceStart / 30.0f); // Erhöht sich alle 30 Sekunden
        
        // Berechne wie viele Gegner gleichzeitig spawnen sollen
        int simultaneousSpawns = calculateSimultaneousSpawns();
        
        // Spawn-Interval wird mit steigender Schwierigkeit kürzer
        float spawnInterval = calculateSpawnInterval();
        
        if (spawnTimer >= spawnInterval) {
            // Spawne mehrere Gegner in verschiedenen Formationen
            spawnEnemyWave(simultaneousSpawns);
            spawnTimer = 0;
        }

        // Update alle Gegner
        for (int i = enemies.size - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            enemy.update(delta);
            if (!enemy.isAlive()) {
                enemies.removeIndex(i);
            }
        }
    }

    private int calculateSimultaneousSpawns() {
        if (difficulty < 2.0f) {
            return 2; // Start mit 2 Gegnern
        } else if (difficulty < 3.0f) {
            return 3; // Frühe Phase: 3 Gegner
        } else if (difficulty < 4.0f) {
            return 4; // Mittlere Phase: 4 Gegner
        } else if (difficulty < 5.0f) {
            return 6; // Fortgeschrittene Phase: 6 Gegner
        } else {
            // Bullet Hell: Basis von 8 Gegnern plus zusätzliche basierend auf Schwierigkeit
            return 8 + (int)((difficulty - 5.0f) * 2);
        }
    }

    private float calculateSpawnInterval() {
        // Startinterval ist 1.5 Sekunden, wird schnell reduziert
        float baseInterval = 1.5f;
        float minInterval = 0.2f; // Schnellere minimale Spawn-Rate
        float intervalReduction = 0.25f * difficulty; // Schnellere Reduktion
        return Math.max(minInterval, baseInterval - intervalReduction);
    }

    public void increaseDifficulty() {
        difficulty += 0.5f; // Größere Schwierigkeitssteigerung
    }

    private void spawnEnemyWave(int count) {
        // Wähle zufällig eine Formation
        float random = MathUtils.random(1f);
        
        if (count <= 2) {
            spawnSimpleFormation(count);
        } else if (random < 0.2f) {
            spawnVFormation(count);
        } else if (random < 0.4f) {
            spawnCircleFormation(count);
        } else if (random < 0.6f) {
            spawnZigZagFormation(count);
        } else if (random < 0.8f) {
            spawnDiagonalFormation(count);
        } else {
            spawnRandomFormation(count);
        }
    }

    private void spawnSimpleFormation(int count) {
        if (count == 1) {
            spawnEnemyAt(MathUtils.random(Constants.SCREEN_WIDTH));
        } else {
            float spacing = Constants.SCREEN_WIDTH / 3;
            spawnEnemyAt(Constants.SCREEN_WIDTH / 2 - spacing);
            spawnEnemyAt(Constants.SCREEN_WIDTH / 2 + spacing);
        }
    }

    private void spawnVFormation(int count) {
        float centerX = Constants.SCREEN_WIDTH / 2;
        float spacing = 50f;
        float yOffset = 30f;
        
        // Mittlerer Gegner
        spawnEnemyAt(centerX, Constants.SCREEN_HEIGHT);
        
        // Flügel der V-Formation
        for (int i = 1; i < count / 2 + 1; i++) {
            spawnEnemyAt(centerX - spacing * i, Constants.SCREEN_HEIGHT + yOffset * i);
            spawnEnemyAt(centerX + spacing * i, Constants.SCREEN_HEIGHT + yOffset * i);
        }
    }

    private void spawnCircleFormation(int count) {
        float centerX = Constants.SCREEN_WIDTH / 2;
        float radius = Math.min(100f, Constants.SCREEN_WIDTH / 4);
        
        for (int i = 0; i < count; i++) {
            float angle = (float)(i * 2 * Math.PI / count);
            float x = centerX + radius * MathUtils.cos(angle);
            float y = Constants.SCREEN_HEIGHT + radius * MathUtils.sin(angle);
            spawnEnemyAt(x, y);
        }
    }

    private void spawnZigZagFormation(int count) {
        float spacing = Constants.SCREEN_WIDTH / (count + 1);
        float yOffset = 40f;
        
        for (int i = 0; i < count; i++) {
            float x = (i + 1) * spacing;
            float y = Constants.SCREEN_HEIGHT + (i % 2 == 0 ? 0 : yOffset);
            spawnEnemyAt(x, y);
        }
    }

    private void spawnDiagonalFormation(int count) {
        float startX = MathUtils.randomBoolean() ? 0 : Constants.SCREEN_WIDTH;
        float spacing = Constants.SCREEN_WIDTH / (count + 1);
        float ySpacing = 40f;
        
        for (int i = 0; i < count; i++) {
            float x = startX + (startX == 0 ? 1 : -1) * i * spacing;
            float y = Constants.SCREEN_HEIGHT + i * ySpacing;
            spawnEnemyAt(x, y);
        }
    }

    private void spawnRandomFormation(int count) {
        // Zufällige Cluster von Gegnern
        for (int i = 0; i < count; i++) {
            float x = MathUtils.random(Constants.SCREEN_WIDTH);
            float y = Constants.SCREEN_HEIGHT + MathUtils.random(100f);
            spawnEnemyAt(x, y);
        }
    }

    private void spawnEnemyAt(float x, float y) {
        Enemy enemy = createRandomEnemy(x, y);
        enemies.add(enemy);
    }

    private void spawnEnemyAt(float x) {
        spawnEnemyAt(x, Constants.SCREEN_HEIGHT);
    }

    private Enemy createRandomEnemy(float x, float y) {
        Enemy enemy;

        // Wähle Gegnertyp basierend auf Schwierigkeit und Zufall
        float random = MathUtils.random(1f);

        if (difficulty < 2.0f) {
            // Frühe Phase: Hauptsächlich normale Gegner
            if (random < 0.8f) {
                enemy = new Enemy(x, y, Constants.BASE_ENEMY_HEALTH, enemyTexture, "normal");
            } else {
                enemy = new ScoutEnemy(x, y, scoutTexture);
            }
        } else {
            // Spätere Phase: Alle Gegnertypen
            if (random < 0.4f) {
                enemy = new Enemy(x, y, Constants.BASE_ENEMY_HEALTH * difficulty, enemyTexture, "normal");
            } else if (random < 0.7f) {
                enemy = new ScoutEnemy(x, y, scoutTexture);
            } else {
                enemy = new BomberEnemy(x, y, bomberTexture);
            }
        }
        return enemy;
    }



    public Array<Enemy> getEnemies() {
        return enemies;
    }

    public void dispose() {
        for (Enemy enemy : enemies) {
            enemy.dispose();
        }
    }
}
