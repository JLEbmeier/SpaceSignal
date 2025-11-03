package de.spaceSignal.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import de.spaceSignal.game.Main;
import de.spaceSignal.game.util.Constants;
import de.spaceSignal.game.util.ScrollingBackground;

public class SettingsScreen extends BaseScreen {
    private BitmapFont titleFont;
    private BitmapFont menuFont;
    private GlyphLayout layout;
    private ScrollingBackground background;
    private float animationTimer = 0;

    public SettingsScreen(Main game) {
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

            param.size = (int) (40 * scaleFactor);
            param.color = new Color(0.3f, 0.85f, 1f, 1f);
            param.borderColor = new Color(0.1f, 0.4f, 0.6f, 1f);
            param.borderWidth = 1.5f * scaleFactor;
            titleFont = generator.generateFont(param);

            param.size = (int) (24 * scaleFactor);
            param.color = new Color(0.8f, 0.9f, 1f, 1f);
            param.borderColor = new Color(0.3f, 0.3f, 0.4f, 1f);
            param.borderWidth = 1f * scaleFactor;
            menuFont = generator.generateFont(param);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("SettingsScreen", "Font error", e);
            titleFont = new BitmapFont();
            titleFont.getData().setScale(1.5f);
            menuFont = new BitmapFont();
            menuFont.getData().setScale(1.2f);
        }
    }

    private void initializeBackground() {
        try {
            String bgPath = "textures/background.png";
            if (Gdx.files.internal(bgPath).exists()) {
                background = new ScrollingBackground(new Texture(Gdx.files.internal(bgPath)));
            }
        } catch (Exception e) {
            Gdx.app.error("SettingsScreen", "Failed to initialize background", e);
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

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
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

        layout.setText(titleFont, "Settings");
        float titleX = (Constants.SCREEN_WIDTH - layout.width) / 2;
        float titleY = Constants.SCREEN_HEIGHT * 0.75f;
        titleFont.draw(game.batch, "Settings", titleX, titleY);

        // Haupt-Text
        layout.setText(menuFont, "Press ESC to return");
        float textX = (Constants.SCREEN_WIDTH - layout.width) / 2;
        float textY = Constants.SCREEN_HEIGHT * 2.5f;
        menuFont.draw(game.batch, "Press ESC to return", textX, textY);

        // Zusätzliche Platzhalter-Einstellungen (kannst du später erweitern)
        String[] settings = {
            "Sound: ON",
            "Music: ON",
            "Graphics: HIGH",
            "Controls: KEYBOARD"
        };

        float settingsY = Constants.SCREEN_HEIGHT * 0.6f;
        for (int i = 0; i < settings.length; i++) {
            layout.setText(menuFont, settings[i]);
            float settingX = (Constants.SCREEN_WIDTH - layout.width) / 2;
            menuFont.setColor(0.7f, 0.8f, 1f, 0.8f);
            menuFont.draw(game.batch, settings[i], settingX, settingsY - i * 30);
        }

        game.batch.end();
    }

    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (menuFont != null) menuFont.dispose();
        if (background != null) background.dispose();
    }
}
