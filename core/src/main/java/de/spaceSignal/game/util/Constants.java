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

    // Gegner Basis
    public static final float ENEMY_SPEED = 100f;
    public static final float ENEMY_WIDTH = 32f;
    public static final float ENEMY_HEIGHT = 32f;
    public static final float ENEMY_SPAWN_INTERVAL = 2f;
    public static final float BASE_ENEMY_HEALTH = 1f;

    // Scout
    public static final float SCOUT_SPEED = ENEMY_SPEED * 1.8f;
    public static final float SCOUT_HEALTH = BASE_ENEMY_HEALTH * 0.5f;
    
    // Tank
    public static final float TANK_SPEED = ENEMY_SPEED * 0.5f;
    public static final float TANK_HEALTH = BASE_ENEMY_HEALTH * 4f;
    
    // Bomber
    public static final float BOMBER_SPEED = ENEMY_SPEED * 0.7f;
    public static final float BOMBER_HEALTH = BASE_ENEMY_HEALTH * 2f;
    public static final float BOMBER_EXPLOSION_RADIUS = 100f;
    public static final float BOMBER_EXPLOSION_DAMAGE = 2f;
    
    // Shooter
    public static final float SHOOTER_SPEED = ENEMY_SPEED * 0.8f;
    public static final float SHOOTER_HEALTH = BASE_ENEMY_HEALTH * 1.5f;
    public static final float SHOOTER_FIRE_RATE = 1.5f;
    
    // Elite
    public static final float ELITE_SPEED = ENEMY_SPEED * 1.2f;
    public static final float ELITE_HEALTH = BASE_ENEMY_HEALTH * 3f;
    public static final float ELITE_FIRE_RATE = 1.0f;

    // Projektile
    public static final float BULLET_SPEED = 400f;
    public static final float BULLET_WIDTH = 8f;
    public static final float BULLET_HEIGHT = 16f;

    // Upgrades
    public static final float UPGRADE_FALL_SPEED = 80f;
    public static final float UPGRADE_SPAWN_CHANCE = 0.3f;

    // Boss Konstanten
    public static final float BOSS_WIDTH = 100;
    public static final float BOSS_HEIGHT = 70;
    public static final float BOSS_SPEED = 150;  // Erhöhte Geschwindigkeit
    public static final float BOSS_BULLET_WIDTH = 12;
    public static final float BOSS_BULLET_HEIGHT = 20;
    public static final float BOSS_BULLET_SPEED = 180;

    // Boss Rush spezifisch
    public static final int BOSS_RUSH_START_LEVEL = 1;
    public static final int BOSS_RUSH_MAX_LEVEL = 10;

    private Constants() {}
}
