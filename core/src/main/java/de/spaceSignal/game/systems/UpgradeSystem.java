package de.spaceSignal.game.systems;

import de.spaceSignal.game.entities.Player;
import de.spaceSignal.game.util.Constants;

public class UpgradeSystem {
    public String getValidUpgradeType(Player player) {
        String[] possibleUpgrades = {"BulletLevel", "Health", "Damage"};
        
        // Entferne Upgrades die nicht mehr nützlich sind
        java.util.List<String> validUpgrades = new java.util.ArrayList<>();
        
        for (String type : possibleUpgrades) {
            if (isUpgradeValid(type, player)) {
                validUpgrades.add(type);
            }
        }
        
        // Wenn keine gültigen Upgrades verfügbar sind, return null
        if (validUpgrades.isEmpty()) {
            return null;
        }
        
        // Wähle zufällig ein gültiges Upgrade
        int index = (int) (Math.random() * validUpgrades.size());
        return validUpgrades.get(index);
    }
    
    public boolean isUpgradeValid(String type, Player player) {
        switch (type) {
            case "BulletLevel":
                return player.getBulletLevel() < 3;  // Max Level ist 3
                
            case "Health":
                return player.getHealth() < Constants.PLAYER_MAX_HEALTH;
                
            case "Damage":
                // Limitiere auch Damage-Upgrades
                return player.getDamageMultiplier() < 3.0f;  // Maximal 3x Damage
                
            default:
                return false;
        }
    }
}
