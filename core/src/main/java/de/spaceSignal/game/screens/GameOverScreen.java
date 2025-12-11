package de.spaceSignal.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import de.spaceSignal.game.Main;
import de.spaceSignal.game.util.Constants;
import de.spaceSignal.game.util.ScrollingBackground;

/**
 * Dedizierter Game Over Screen mit Statistiken und Optionen
 */
public class GameOverScreen extends BaseScreen {
    private BitmapFont titleFont;
    private BitmapFont statsFont;
    private BitmapFont menuFont;
    private GlyphLayout layout;
    private ScrollingBackground background;

    private final String gameMode;
    private final int finalScore;
    private final int wave;
    private final boolean isVictory;
    private final String victoryMessage;

    private float animationTimer;
    private float fadeInTimer;
    private int selectedOption;
    private final String[] menuOptions = {"Retry", "Main Menu", "Exit"};

    // Partikel-Effekt für Victory
    private static class Particle {
        float x, y, vx, vy;
        float life, maxLife;
        Color color;

        Particle(float x, float y) {
            this.x = x;
            this.y = y;
            float angle = MathUtils.random(0f, MathUtils.PI2);
            float speed = MathUtils.random(50f, 150f);
            this.vx = MathUtils.cos(angle) * speed;
            this.vy = MathUtils.sin(angle) * speed;
            this.maxLife = MathUtils.random(1f, 2f);
            this.life = maxLife;
            this.color = new Color(
                MathUtils.random(0.5f, 1f),
                MathUtils.random(0.5f, 1f),
                MathUtils.random(0.5f, 1f),
                1f
            );
        }

        void update(float delta) {
            x += vx * delta;
            y += vy * delta;
            vy -= 200 * delta; // Schwerkraft
            life -= delta;
            color.a = life / maxLife;
        }

        boolean isDead() {
            return life <= 0;
        }
    }

    private java.util.ArrayList<Particle> particles;

    public GameOverScreen(Main game, String gameMode, int finalScore, int wave,
                          boolean isVictory, String victoryMessage) {
        super(game);
        this.gameMode = gameMode;
        this.finalScore = finalScore;
        this.wave = wave;
        this.isVictory = isVictory;
        this.victoryMessage = victoryMessage;

        this.animationTimer = 0;
        this.fadeInTimer = 0;
        this.selectedOption = 0;
        this.particles = new java.util.ArrayList<>();

        initializeFonts();
        initializeBackground();
        layout = new GlyphLayout();

        // Victory-Partikel spawnen
        if (isVictory) {
            spawnVictoryParticles();
        }
    }

    private void initializeFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/Orbitron-Regular.ttf")
            );
            FreeTypeFontGenerator.FreeTypeFontParameter param =
                new FreeTypeFontGenerator.FreeTypeFontParameter();
            float scaleFactor = Constants.SCREEN_WIDTH / 1280f;

            // Titel-Font (Game Over / Victory)
            param.size = (int) (64 * scaleFactor);
            param.color = isVictory ? new Color(0.2f, 1f, 0.2f, 1f) : new Color(1f, 0.2f, 0.2f, 1f);
            param.borderColor = Color.BLACK;
            param.borderWidth = 3 * scaleFactor;
            titleFont = generator.generateFont(param);

            // Stats-Font
            param.size = (int) (28 * scaleFactor);
            param.color = new Color(0.9f, 0.9f, 1f, 1f);
            param.borderColor = new Color(0.2f, 0.2f, 0.3f, 1f);
            param.borderWidth = 1.5f * scaleFactor;
            statsFont = generator.generateFont(param);

            // Menu-Font
            param.size = (int) (32 * scaleFactor);
            param.color = Color.WHITE;
            param.borderColor = new Color(0.3f, 0.3f, 0.4f, 1f);
            param.borderWidth = 2f * scaleFactor;
            menuFont = generator.generateFont(param);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Font error", e);
            titleFont = new BitmapFont();
            titleFont.getData().setScale(3f);
            statsFont = new BitmapFont();
            statsFont.getData().setScale(1.5f);
            menuFont = new BitmapFont();
            menuFont.getData().setScale(1.8f);
        }
    }

    private void initializeBackground() {
        try {
            String bgPath = "textures/background.png";
            if (Gdx.files.internal(bgPath).exists()) {
                background = new ScrollingBackground(new Texture(Gdx.files.internal(bgPath)));
            }
        } catch (Exception e) {
            Gdx.app.error("GameOverScreen", "Background error", e);
        }
    }

    private void spawnVictoryParticles() {
        for (int i = 0; i < 50; i++) {
            particles.add(new Particle(
                Constants.SCREEN_WIDTH / 2,
                Constants.SCREEN_HEIGHT * 0.7f
            ));
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.01f, 0.01f, 0.05f, 1f);
        Gdx.gl.glClear(com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT);

        update(delta);
        draw();
    }

    private void update(float delta) {
        if (background != null) background.update(delta * 0.3f); // Langsamer

        animationTimer += delta;
        fadeInTimer = Math.min(fadeInTimer + delta, 1f);

        // Menu-Navigation
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedOption = Math.max(0, selectedOption - 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedOption = Math.min(menuOptions.length - 1, selectedOption + 1);
        }

        // Auswahl
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) ||
            Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            handleSelection();
        }

        // Partikel updaten
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update(delta);
            if (p.isDead()) {
                particles.remove(i);
            }
        }

        // Neue Partikel spawnen bei Victory
        if (isVictory && MathUtils.random() < 0.3f) {
            particles.add(new Particle(
                MathUtils.random(Constants.SCREEN_WIDTH * 0.3f, Constants.SCREEN_WIDTH * 0.7f),
                Constants.SCREEN_HEIGHT * 0.7f
            ));
        }
    }

    private void handleSelection() {
        switch (menuOptions[selectedOption]) {
            case "Retry":
                game.setScreen(new GameScreen(game, gameMode));
                dispose();
                break;
            case "Main Menu":
                game.setScreen(new MainMenuScreen(game));
                dispose();
                break;
            case "Exit":
                Gdx.app.exit();
                break;
        }
    }

    private void draw() {
        game.batch.begin();

        // Hintergrund
        if (background != null) {
            game.batch.setColor(1, 1, 1, 0.5f);
            background.render(game.batch);
            game.batch.setColor(1, 1, 1, 1);
        }

        // Dunkles Overlay
        game.batch.setColor(0, 0, 0, 0.7f * fadeInTimer);
        game.batch.draw(createSolidTexture(), 0, 0, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT);
        game.batch.setColor(1, 1, 1, 1);

        float alpha = fadeInTimer;

        // Titel (Game Over / Victory)
        String titleText = isVictory ? "VICTORY!" : "GAME OVER";
        float pulse = MathUtils.sin(animationTimer * 3f) * 0.15f + 0.85f;

        if (isVictory) {
            titleFont.setColor(0.2f + pulse * 0.3f, 1f, 0.2f + pulse * 0.3f, alpha);
        } else {
            titleFont.setColor(1f, 0.2f + pulse * 0.2f, 0.2f, alpha);
        }

        layout.setText(titleFont, titleText);
        float titleX = (Constants.SCREEN_WIDTH - layout.width) / 2;
        float titleY = Constants.SCREEN_HEIGHT * 0.75f;
        titleFont.draw(game.batch, titleText, titleX, titleY);

        // Victory-Message
        if (isVictory && victoryMessage != null) {
            statsFont.setColor(1f, 1f, 0.8f, alpha * 0.9f);
            layout.setText(statsFont, victoryMessage);
            float msgX = (Constants.SCREEN_WIDTH - layout.width) / 2;
            statsFont.draw(game.batch, victoryMessage, msgX, titleY - 50);
        }

        // Statistiken
        statsFont.setColor(0.9f, 0.9f, 1f, alpha * 0.9f);
        String[] stats = {
            "Mode: " + gameMode,
            "Final Score: " + finalScore,
            "Wave: " + wave
        };

        float statsY = Constants.SCREEN_HEIGHT * 0.55f;
        for (int i = 0; i < stats.length; i++) {
            layout.setText(statsFont, stats[i]);
            float statsX = (Constants.SCREEN_WIDTH - layout.width) / 2;
            statsFont.draw(game.batch, stats[i], statsX, statsY - i * 40);
        }

        // Menu-Optionen
        float menuY = Constants.SCREEN_HEIGHT * 0.35f;
        for (int i = 0; i < menuOptions.length; i++) {
            boolean selected = (i == selectedOption);
            float scale = selected ?
                Interpolation.smooth.apply(1f, 1.2f, MathUtils.sin(animationTimer * 4f) * 0.5f + 0.5f) : 1f;

            if (selected) {
                menuFont.setColor(0.3f + pulse * 0.4f, 0.85f + pulse * 0.15f, 1f, alpha);
            } else {
                menuFont.setColor(1f, 1f, 1f, alpha * 0.6f);
            }

            menuFont.getData().setScale(scale);
            layout.setText(menuFont, menuOptions[i]);
            float menuX = (Constants.SCREEN_WIDTH - layout.width) / 2;
            menuFont.draw(game.batch, menuOptions[i], menuX, menuY - i * 60);
            menuFont.getData().setScale(1f);
        }

        // Partikel rendern
        Texture particleTexture = createSolidTexture();
        for (Particle p : particles) {
            game.batch.setColor(p.color);
            game.batch.draw(particleTexture, p.x - 3, p.y - 3, 6, 6);
        }
        game.batch.setColor(1, 1, 1, 1);

        game.batch.end();
    }

    private Texture createSolidTexture() {
        Texture texture = new Texture(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        return texture;
    }

    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (statsFont != null) statsFont.dispose();
        if (menuFont != null) menuFont.dispose();
        if (background != null) background.dispose();
    }
}
