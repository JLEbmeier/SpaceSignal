package de.spaceSignal.game.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import de.spaceSignal.game.util.Constants;

public class Upgrade {
    private Sprite sprite;
    private Vector2 position;
    private Rectangle bounds;
    private boolean alive;
    private final String type; // "BulletLevel", "Health", "Damage"

    public Upgrade(float x, float y, String type, Texture texture) {
        this.position = new Vector2(x, y);
        this.type = type;
        this.alive = true;
        this.sprite = new Sprite(texture);
        sprite.setPosition(x, y);
        sprite.setSize(Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT); // Gleiche Größe wie Spieler
        bounds = new Rectangle(x, y, Constants.PLAYER_WIDTH, Constants.PLAYER_HEIGHT);
    }

    public void update(float delta) {
        if (!alive) return;
        position.y -= Constants.UPGRADE_FALL_SPEED * delta;
        sprite.setPosition(position.x, position.y);
        bounds.setPosition(position.x, position.y);

        if (position.y < -Constants.PLAYER_HEIGHT) {
            alive = false;
        }
    }

    public void render(SpriteBatch batch) {
        sprite.draw(batch);
    }

    public void collect() {
        alive = false;
    }

    public String getType() {
        return type;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public boolean isAlive() {
        return alive;
    }

    public void dispose() {
        sprite.getTexture().dispose();
    }
}
