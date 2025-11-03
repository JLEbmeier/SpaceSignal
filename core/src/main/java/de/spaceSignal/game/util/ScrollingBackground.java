package de.spaceSignal.game.util;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import de.spaceSignal.game.util.Constants;

public class ScrollingBackground {
    private Sprite farLayer; // Langsame Schicht (Sterne)
    private Sprite nearLayer; // Schnelle Schicht (Debris)
    private Vector2 farOffset;
    private Vector2 nearOffset;
    private float farSpeed = 20f; // Langsam
    private float nearSpeed = 60f; // Schnell

    public ScrollingBackground(Texture texture) {
        farLayer = new Sprite(texture);
        nearLayer = new Sprite(texture); // Für Einfachheit: Gleiche Textur, später anpassbar
        farOffset = new Vector2(0, 0);
        nearOffset = new Vector2(0, 0);
    }

    public void update(float delta) {
        farOffset.y -= farSpeed * delta;
        nearOffset.y -= nearSpeed * delta;

        // Tiling: Reset, wenn Schicht den Bildschirm verlässt
        if (farOffset.y <= -Constants.SCREEN_HEIGHT) {
            farOffset.y += Constants.SCREEN_HEIGHT;
        }
        if (nearOffset.y <= -Constants.SCREEN_HEIGHT) {
            nearOffset.y += Constants.SCREEN_HEIGHT;
        }
    }

    public void render(SpriteBatch batch) {
        // Far layer (zweimal zeichnen für Tiling)
        farLayer.setPosition(farOffset.x, farOffset.y);
        farLayer.draw(batch);
        farLayer.setPosition(farOffset.x, farOffset.y + Constants.SCREEN_HEIGHT);
        farLayer.draw(batch);

        // Near layer (zweimal zeichnen)
        nearLayer.setPosition(nearOffset.x, nearOffset.y);
        nearLayer.draw(batch);
        nearLayer.setPosition(nearOffset.x, nearOffset.y + Constants.SCREEN_HEIGHT);
        nearLayer.draw(batch);
    }

    public void dispose() {
        farLayer.getTexture().dispose();
        nearLayer.getTexture().dispose();
    }
}
