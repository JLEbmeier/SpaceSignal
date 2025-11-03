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

public class GameSelectionScreen extends BaseScreen {
    private BitmapFont titleFont;
    private BitmapFont menuFont;
    private BitmapFont descFont;
    private GlyphLayout layout;
    private ScrollingBackground background;

    private static class GameMode {
        String name;
        String description;
        float scale = 1f;
        float alpha = 0.7f;
        float glowIntensity = 0f;

        GameMode(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    private final GameMode[] modes = {
        new GameMode("Classic", "Traditional gameplay experience"),
        new GameMode("Survival", "Endless waves of enemies"),
        new GameMode("Time Attack", "Race against the clock"),
        new GameMode("Boss Rush", "Fight powerful bosses") // Kürzere Beschreibung
    };

    private int selectedIndex = 0;
    private float animationTimer = 0;
    private boolean transitionOut = false;

    public GameSelectionScreen(Main game) {
        super(game);
        initializeFonts();
        layout = new GlyphLayout();
        initializeBackground();
    }

    private void initializeFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Orbitron-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
            float scaleFactor = Constants.SCREEN_WIDTH / 1280f;

            // Kleinere Titel-Font für mehr Platz
            param.size = (int) (36 * scaleFactor);
            param.color = new Color(0.3f, 0.85f, 1f, 1f);
            param.borderColor = new Color(0.1f, 0.4f, 0.6f, 1f);
            param.borderWidth = 1.5f * scaleFactor;
            titleFont = generator.generateFont(param);

            // Kleinere Menu-Font
            param.size = (int) (22 * scaleFactor);
            param.color = Color.WHITE;
            param.borderColor = new Color(0.3f, 0.3f, 0.4f, 1f);
            param.borderWidth = 1.2f * scaleFactor;
            menuFont = generator.generateFont(param);

            // Kleinere Beschreibungs-Font
            param.size = (int) (14 * scaleFactor);
            param.color = new Color(0.7f, 0.8f, 0.9f, 1f);
            param.borderWidth = 0;
            descFont = generator.generateFont(param);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("GameSelectionScreen", "Font error", e);
            titleFont = new BitmapFont();
            titleFont.getData().setScale(1.3f);
            menuFont = new BitmapFont();
            menuFont.getData().setScale(1.0f);
            descFont = new BitmapFont();
            descFont.getData().setScale(0.7f);
        }
    }

    private void initializeBackground() {
        try {
            String bgPath = "textures/background.png";
            if (Gdx.files.internal(bgPath).exists()) {
                background = new ScrollingBackground(new Texture(Gdx.files.internal(bgPath)));
            }
        } catch (Exception e) {
            Gdx.app.error("GameSelectionScreen", "Failed to initialize background", e);
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
        if (background != null) background.update(delta);
        animationTimer += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = Math.min(modes.length - 1, selectedIndex + 1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            transitionOut = true;
            animationTimer = 0;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }

        // Smooth animations
        for (int i = 0; i < modes.length; i++) {
            GameMode mode = modes[i];
            float targetScale = (i == selectedIndex) ? 1.10f : 1f; // Weniger Skalierung
            float targetAlpha = (i == selectedIndex) ? 1f : 0.6f;
            float targetGlow = (i == selectedIndex) ? 1f : 0f;

            mode.scale = Interpolation.smooth.apply(mode.scale, targetScale, 0.15f);
            mode.alpha = Interpolation.smooth.apply(mode.alpha, targetAlpha, 0.15f);
            mode.glowIntensity = Interpolation.smooth.apply(mode.glowIntensity, targetGlow, 0.1f);
        }

        if (transitionOut && animationTimer >= 0.5f) {
            game.setScreen(new GameScreen(game, modes[selectedIndex].name));
            dispose();
        }
    }

    private void draw() {
        game.batch.begin();

        // Hintergrund rendern
        if (background != null) {
            background.render(game.batch);
        }

        // Titel mit Puls-Effekt
        float pulse = MathUtils.sin(animationTimer * 2f) * 0.1f + 0.9f;
        titleFont.setColor(0.3f + pulse * 0.2f, 0.85f, 1f, 1f);

        layout.setText(titleFont, "Select Game Mode");
        float titleX = (Constants.SCREEN_WIDTH - layout.width) / 2;
        float titleY = Constants.SCREEN_HEIGHT * 0.78f; // Höher positioniert
        titleFont.draw(game.batch, "Select Game Mode", titleX, titleY);

        // Menüpunkte mit mehr Platz
        float menuY = Constants.SCREEN_HEIGHT * 0.55f; // Mehr Platz nach oben
        float lineSpacing = 60 * (Constants.SCREEN_HEIGHT / 480f); // Weniger Abstand

        for (int i = 0; i < modes.length; i++) {
            GameMode mode = modes[i];

            // Farbeffekt für ausgewähltes Item
            if (i == selectedIndex) {
                menuFont.setColor(0.8f + mode.glowIntensity * 0.2f,
                    0.9f + mode.glowIntensity * 0.1f,
                    1f, mode.alpha);
            } else {
                menuFont.setColor(1f, 1f, 1f, mode.alpha);
            }

            // Skalierung anwenden
            menuFont.getData().setScale(mode.scale);
            layout.setText(menuFont, mode.name);
            float menuX = (Constants.SCREEN_WIDTH - layout.width) / 2;
            float yPos = menuY - i * lineSpacing;

            menuFont.draw(game.batch, mode.name, menuX, yPos);
            menuFont.getData().setScale(1f); // Zurücksetzen

            // Beschreibung für ausgewähltes Item
            if (i == selectedIndex) {
                descFont.setColor(0.7f, 0.85f, 1f, mode.alpha * 0.9f);
                layout.setText(descFont, mode.description);
                float descX = (Constants.SCREEN_WIDTH - layout.width) / 2;
                descFont.draw(game.batch, mode.description, descX,
                    yPos - 25 * (Constants.SCREEN_HEIGHT / 480f)); // Weniger Abstand
            }
        }

        game.batch.end();
    }

    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (menuFont != null) menuFont.dispose();
        if (descFont != null) descFont.dispose();
        if (background != null) background.dispose();
    }
}
