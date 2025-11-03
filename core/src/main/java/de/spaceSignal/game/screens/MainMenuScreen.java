package de.spaceSignal.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import de.spaceSignal.game.Main;
import de.spaceSignal.game.util.Constants;

public class MainMenuScreen extends BaseScreen {
    private BitmapFont titleFont;
    private BitmapFont menuFont;
    private BitmapFont descFont;
    private GlyphLayout layout;

    private String title = "SPACE SIGNAL";
    private final MenuItem[] menuItems = {
        new MenuItem("Classic Mode", "Standard mission with balanced difficulty"),
        new MenuItem("Survival Mode", "Endless waves, test your endurance"),
        new MenuItem("Time Attack", "Race against time for high scores"),
        new MenuItem("Exit", "Leave the game")
    };
    private int selectedIndex = 0;
    private float animationTimer = 0;
    private boolean transitionOut = false;

    private static class MenuItem {
        String name;
        String description;
        float scale = 1f;
        float alpha = 1f;

        MenuItem(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    public MainMenuScreen(Main game) {
        super(game);
        initializeFonts();
        layout = new GlyphLayout();
    }

    private void initializeFonts() {
        try {
            FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Orbitron-Regular.ttf"));
            FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();

            // Title Font
            param.size = 48;
            param.color = Color.CYAN;
            param.borderColor = Color.BLACK;
            param.borderWidth = 2;
            titleFont = generator.generateFont(param);

            // Menu Font
            param.size = 24;
            param.color = Color.WHITE;
            param.borderWidth = 1;
            menuFont = generator.generateFont(param);

            // Description Font
            param.size = 16;
            param.color = Color.LIGHT_GRAY;
            param.borderWidth = 0;
            descFont = generator.generateFont(param);

            generator.dispose();
        } catch (Exception e) {
            Gdx.app.error("MainMenuScreen", "Failed to load Orbitron-Regular.ttf, using fallback font", e);
            // Fallback: Standard LibGDX BitmapFont
            titleFont = new BitmapFont();
            titleFont.getData().setScale(2f);
            titleFont.setColor(Color.CYAN);

            menuFont = new BitmapFont();
            menuFont.getData().setScale(1.2f);
            menuFont.setColor(Color.WHITE);

            descFont = new BitmapFont();
            descFont.getData().setScale(0.8f);
            descFont.setColor(Color.LIGHT_GRAY);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.02f, 0.02f, 0.1f, 1f);
        update(delta);
        draw();
    }

    private void update(float delta) {
        animationTimer += delta;

        if (Gdx.input.isKeyJustPressed(Input.Keys.UP)) {
            selectedIndex = Math.max(0, selectedIndex - 1);
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN)) {
            selectedIndex = Math.min(menuItems.length - 1, selectedIndex + 1);
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            handleSelection();
        }

        for (int i = 0; i < menuItems.length; i++) {
            MenuItem item = menuItems[i];
            float targetScale = (i == selectedIndex) ? 1.2f : 1f;
            float targetAlpha = (i == selectedIndex) ? 1f : 0.7f;
            item.scale = Interpolation.smooth.apply(item.scale, targetScale, 0.2f);
            item.alpha = Interpolation.smooth.apply(item.alpha, targetAlpha, 0.2f);
        }

        if (transitionOut) {
            float alpha = Interpolation.fade.apply(1f, 0f, animationTimer / 0.5f);
            titleFont.setColor(1, 1, 1, alpha);
            menuFont.setColor(1, 1, 1, alpha);
            descFont.setColor(1, 1, 1, alpha);
            if (animationTimer >= 0.5f) {
                finalizeSelection();
            }
        }
    }

    private void handleSelection() {
        if (menuItems[selectedIndex].name.equals("Exit")) {
            transitionOut = true;
            animationTimer = 0;
        } else {
            transitionOut = true;
            animationTimer = 0;
        }
    }

    private void finalizeSelection() {
        if (menuItems[selectedIndex].name.equals("Exit")) {
            Gdx.app.exit();
        } else {
            game.setScreen(new GameScreen(game, menuItems[selectedIndex].name));
            dispose();
        }
    }

    private void draw() {
        game.batch.begin();

        // Titel
        layout.setText(titleFont, title);
        float titleX = (Constants.SCREEN_WIDTH - layout.width) / 2;
        float titleY = Constants.SCREEN_HEIGHT * 0.75f;
        titleFont.draw(game.batch, title, titleX, titleY);

        // Menüpunkte
        float menuY = Constants.SCREEN_HEIGHT * 0.5f;
        Matrix4 originalMatrix = game.batch.getTransformMatrix().cpy();
        for (int i = 0; i < menuItems.length; i++) {
            MenuItem item = menuItems[i];
            menuFont.setColor(1, 1, 1, item.alpha);
            layout.setText(menuFont, item.name);
            float menuX = (Constants.SCREEN_WIDTH - layout.width) / 2;

            Matrix4 scaledMatrix = originalMatrix.cpy();
            scaledMatrix.translate(menuX, menuY - i * 60, 0);
            scaledMatrix.scale(item.scale, item.scale, 1);
            scaledMatrix.translate(-menuX, -(menuY - i * 60), 0);
            game.batch.setTransformMatrix(scaledMatrix);

            menuFont.draw(game.batch, item.name, menuX, menuY - i * 60);

            if (i == selectedIndex) {
                layout.setText(descFont, item.description);
                float descX = (Constants.SCREEN_WIDTH - layout.width) / 2;
                game.batch.setTransformMatrix(originalMatrix);
                descFont.draw(game.batch, item.description, descX, menuY - i * 60 - 30);
            }
        }
        game.batch.setTransformMatrix(originalMatrix);
        game.batch.end();
    }

    @Override
    public void dispose() {
        titleFont.dispose();
        menuFont.dispose();
        descFont.dispose();
    }
}
