package de.spaceSignal.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

import de.spaceSignal.game.util.Constants;

public class ScoutEnemy extends Enemy {
    private float zigzagTimer = 0;
    private float zigzagAmplitude = 100f;
    private float zigzagFrequency = 2f;
    private float baseX;

    public ScoutEnemy(float x, float y, Texture texture) {
        super(x, y, Constants.SCOUT_HEALTH, texture, "normal");
        this.baseX = x;
        setSpeed(Constants.ENEMY_SPEED * 1.8f); // Scouts sind schneller
    }

    @Override
    public void update(float delta) {
        if (!isAlive()) return;

        // Zickzack-Bewegung
        zigzagTimer += delta;
        float newX = baseX + MathUtils.sin(zigzagTimer * zigzagFrequency) * zigzagAmplitude;
        
        // Vertikale Bewegung
        float newY = getPosition().y - getSpeed() * delta;
        
        // Position aktualisieren
        getPosition().set(newX, newY);
        updateBounds();

        // Prüfen ob außerhalb des Bildschirms
        if (getPosition().y < -Constants.ENEMY_HEIGHT) {
            setAlive(false);
        }
    }
}