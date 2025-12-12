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
import de.spaceSignal.game.managers.AudioManager;
import de.spaceSignal.game.util.Constants;
import de.spaceSignal.game.util.ScrollingBackground;

public class SettingsScreen extends BaseScreen {
    private BitmapFont titleFont;
    private BitmapFont menuFont;
    private GlyphLayout layout;
    private ScrollingBackground background;
    private float animationTimer = 0;

    // Audio Manager Referenz
    private AudioManager audioManager;

    // Ausgewählte Option
    private int selectedOption = 0;
    private static final int OPTION_COUNT = 4;

    // Optionen
    private static final int MUSIC_TOGGLE = 0;
    private static final int MUSIC_VOLUME = 1;
    private static final int SOUND_TOGGLE = 2;
    private static final int SOUND_VOLUME = 3;

    // Debounce für Tasteneingaben
    private float inputCooldown = 0;
    private static final float INPUT_COOLDOWN_TIME = 0.15f;

    public SettingsScreen(Main game) {
        super(game);
        initializeFonts();
        layout = new GlyphLayout();
        initializeBackground();
        audioManager = AudioManager.getInstance();
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

        // Cooldown updaten
        if (inputCooldown > 0) {
            inputCooldown -= delta;
        }

        // Navigation
        if (inputCooldown <= 0) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
                selectedOption = (selectedOption - 1 + OPTION_COUNT) % OPTION_COUNT;
                audioManager.playShootSound();
                inputCooldown = INPUT_COOLDOWN_TIME;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
                selectedOption = (selectedOption + 1) % OPTION_COUNT;
                audioManager.playShootSound();
                inputCooldown = INPUT_COOLDOWN_TIME;
            }

            // Änderungen anwenden
            if (Gdx.input.isKeyJustPressed(Input.Keys.LEFT) || Gdx.input.isKeyJustPressed(Input.Keys.A)) {
                handleLeftInput();
                inputCooldown = INPUT_COOLDOWN_TIME;
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.RIGHT) || Gdx.input.isKeyJustPressed(Input.Keys.D)) {
                handleRightInput();
                inputCooldown = INPUT_COOLDOWN_TIME;
            }

            // Enter/Space für Toggle
            if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                handleEnterInput();
                inputCooldown = INPUT_COOLDOWN_TIME;
            }
        }

        // Zurück zum Hauptmenü
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
            dispose();
        }
    }

    private void handleLeftInput() {
        switch (selectedOption) {
            case MUSIC_VOLUME:
                audioManager.decreaseMusicVolume();
                audioManager.playShootSound();
                break;
            case SOUND_VOLUME:
                audioManager.decreaseSoundVolume();
                audioManager.playShootSound();
                break;
        }
    }

    private void handleRightInput() {
        switch (selectedOption) {
            case MUSIC_VOLUME:
                audioManager.increaseMusicVolume();
                audioManager.playShootSound();
                break;
            case SOUND_VOLUME:
                audioManager.increaseSoundVolume();
                audioManager.playShootSound();
                break;
        }
    }

    private void handleEnterInput() {
        switch (selectedOption) {
            case MUSIC_TOGGLE:
                audioManager.toggleMusic();
                audioManager.playShootSound();
                break;
            case SOUND_TOGGLE:
                audioManager.toggleSound();
                audioManager.playShootSound();
                break;
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
        float titleY = Constants.SCREEN_HEIGHT * 0.85f;
        titleFont.draw(game.batch, "Settings", titleX, titleY);

        // Einstellungen rendern
        float startY = Constants.SCREEN_HEIGHT * 0.65f;
        float lineHeight = 50f;

        // Music Toggle
        drawOption(MUSIC_TOGGLE, "Music: " + (audioManager.isMusicEnabled() ? "ON" : "OFF"),
            startY, selectedOption == MUSIC_TOGGLE);

        // Music Volume
        drawOption(MUSIC_VOLUME, "Music Volume: " + (int)(audioManager.getMusicVolume() * 100) + "%",
            startY - lineHeight, selectedOption == MUSIC_VOLUME);

        // Sound Toggle
        drawOption(SOUND_TOGGLE, "Sound: " + (audioManager.isSoundEnabled() ? "ON" : "OFF"),
            startY - lineHeight * 2, selectedOption == SOUND_TOGGLE);

        // Sound Volume
        drawOption(SOUND_VOLUME, "Sound Volume: " + (int)(audioManager.getSoundVolume() * 100) + "%",
            startY - lineHeight * 3, selectedOption == SOUND_VOLUME);

        // Volume Bars
        if (selectedOption == MUSIC_VOLUME || selectedOption == SOUND_VOLUME) {
            float barY = startY - lineHeight * selectedOption - 25;
            drawVolumeBar(barY, selectedOption == MUSIC_VOLUME ?
                audioManager.getMusicVolume() : audioManager.getSoundVolume());
        }

        // Steuerungshinweise
        menuFont.setColor(0.6f, 0.6f, 0.7f, 0.8f);
        float controlsY = Constants.SCREEN_HEIGHT * 0.2f;
        String controls = "↑↓: Navigate  ←→: Adjust  ENTER: Toggle  ESC: Back";
        layout.setText(menuFont, controls);
        menuFont.draw(game.batch, controls,
            (Constants.SCREEN_WIDTH - layout.width) / 2, controlsY);
        menuFont.setColor(1, 1, 1, 1);

        game.batch.end();
    }

    private void drawOption(int index, String text, float y, boolean selected) {
        if (selected) {
            // Highlight-Effekt
            float highlight = MathUtils.sin(animationTimer * 4f) * 0.2f + 0.8f;
            menuFont.setColor(1f * highlight, 1f * highlight, 0.3f, 1f);
            menuFont.getData().setScale(1.1f);

            // Pfeil
            menuFont.draw(game.batch, ">", Constants.SCREEN_WIDTH / 2 - 250, y);
        } else {
            menuFont.setColor(0.7f, 0.8f, 1f, 0.8f);
            menuFont.getData().setScale(1f);
        }

        layout.setText(menuFont, text);
        float textX = (Constants.SCREEN_WIDTH - layout.width) / 2;
        menuFont.draw(game.batch, text, textX, y);

        menuFont.getData().setScale(1f);
        menuFont.setColor(1, 1, 1, 1);
    }

    private void drawVolumeBar(float y, float volume) {
        float barWidth = 300f;
        float barHeight = 10f;
        float barX = (Constants.SCREEN_WIDTH - barWidth) / 2;

        // Hintergrund
        menuFont.setColor(0.3f, 0.3f, 0.4f, 0.8f);
        // Verwende ein kleines Rechteck als Balken (oder nutze ShapeRenderer wenn verfügbar)

        // Vordergrund (gefüllter Teil)
        float filledWidth = barWidth * volume;
        menuFont.setColor(0.3f, 1f, 0.3f, 1f);

        // Text als Balken-Ersatz (vereinfacht)
        String bar = "[";
        int segments = 20;
        int filled = (int)(segments * volume);
        for (int i = 0; i < segments; i++) {
            bar += i < filled ? "█" : "░";
        }
        bar += "]";

        menuFont.setColor(0.3f, 1f, 0.3f, 1f);
        layout.setText(menuFont, bar);
        menuFont.draw(game.batch, bar, (Constants.SCREEN_WIDTH - layout.width) / 2, y);

        menuFont.setColor(1, 1, 1, 1);
    }

    @Override
    public void dispose() {
        if (titleFont != null) titleFont.dispose();
        if (menuFont != null) menuFont.dispose();
        if (background != null) background.dispose();
    }
}
