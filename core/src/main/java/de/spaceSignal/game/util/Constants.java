package de.spaceSignal.game.util;

public class Constants {
    // Bildschirmauflösung
    public static final int SCREEN_WIDTH = 640;
    public static final int SCREEN_HEIGHT = 480;

    // Spieler
    public static final float PLAYER_SPEED = 250f;
    public static final float PLAYER_WIDTH = 32f;
    public static final float PLAYER_HEIGHT = 32f;
    public static final int PLAYER_MAX_HEALTH = 3;
    public static final float PLAYER_FIRE_RATE = 0.2f; // Schüsse pro Sekunde

    // Gegner
    public static final float ENEMY_SPEED = 100f;
    public static final float ENEMY_WIDTH = 32f;
    public static final float ENEMY_HEIGHT = 32f;
    public static final float ENEMY_SPAWN_INTERVAL = 2f;

    // Projektile
    public static final float BULLET_SPEED = 400f;
    public static final float BULLET_WIDTH = 8f;
    public static final float BULLET_HEIGHT = 16f;

    // Upgrades
    public static final float UPGRADE_FALL_SPEED = 80f;
    public static final float UPGRADE_SPAWN_CHANCE = 0.3f;

    private Constants() {}
}
