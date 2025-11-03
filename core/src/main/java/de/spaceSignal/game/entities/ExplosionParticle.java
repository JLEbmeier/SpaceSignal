package de.spaceSignal.game.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

class ExplosionParticle {
    private Vector2 position;
    private Vector2 velocity;
    private float lifetime;
    private float maxLifetime;
    private Color color;
    private float size;
    private float initialSize;
    private static Texture particleTexture; // STATIC für alle Partikel

    public ExplosionParticle(Vector2 position, Vector2 velocity, float lifetime) {
        this.position = new Vector2(position);
        this.velocity = new Vector2(velocity);
        this.maxLifetime = lifetime;
        this.lifetime = lifetime;
        this.initialSize = MathUtils.random(2f, 8f);
        this.size = initialSize;

        // Zufällige Feuerfarbe (Rot, Orange, Gelb)
        float colorChoice = MathUtils.random();
        if (colorChoice < 0.6f) {
            this.color = new Color(1f, 0.3f, 0.1f, 1f); // Rot
        } else if (colorChoice < 0.9f) {
            this.color = new Color(1f, 0.6f, 0.1f, 1f); // Orange
        } else {
            this.color = new Color(1f, 0.9f, 0.1f, 1f); // Gelb
        }

        // Texture nur einmal erstellen
        if (particleTexture == null) {
            createParticleTexture();
        }
    }

    private void createParticleTexture() {
        Pixmap pixmap = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fillCircle(8, 8, 8);
        particleTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void update(float delta) {
        // Bewegung
        position.x += velocity.x * delta;
        position.y += velocity.y * delta;

        // Schwerkraft für Rauch-Partikel
        if (color.r < 0.7f) { // Rauch-Partikel (grau)
            velocity.y += 50f * delta; // Rauch steigt langsamer auf
        } else {
            velocity.y -= 100f * delta; // Feuer-Partikel fallen
        }

        // Luftwiderstand
        velocity.x *= 0.98f;
        velocity.y *= 0.98f;

        // Lebenszeit verringern
        lifetime -= delta;

        // Farbe und Größe über Zeit verändern
        color.a = lifetime / maxLifetime;

        // Partikel schrumpfen oder wachsen lassen
        if (color.r < 0.7f) { // Rauch wächst
            size = initialSize * (1 + (1 - lifetime/maxLifetime) * 2f);
        } else { // Feuer schrumpft
            size = initialSize * (lifetime / maxLifetime);
        }
    }

    public void render(SpriteBatch batch) {
        // OPTIMIERT: Verwende Textur statt ShapeRenderer
        batch.setColor(color);
        batch.draw(particleTexture,
            position.x - size / 2,
            position.y - size / 2,
            size,
            size
        );
        batch.setColor(Color.WHITE); // Farbe zurücksetzen
    }

    public boolean isFinished() {
        return lifetime <= 0f;
    }

    public static void disposeTexture() {
        if (particleTexture != null) {
            particleTexture.dispose();
            particleTexture = null;
        }
    }
}
