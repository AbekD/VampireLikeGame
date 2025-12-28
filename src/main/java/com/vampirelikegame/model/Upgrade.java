package com.vampirelikegame.model;

/**
 * Класс улучшений с новыми типами оружия
 */
public class Upgrade {
    private UpgradeType type;
    private String name;
    private String description;
    private int weaponIndex;
    private String newWeaponType;

    public enum UpgradeType {
        WEAPON_DAMAGE,
        WEAPON_FIRERATE,
        MAX_HEALTH,
        SPEED,
        HEAL,
        NEW_WEAPON
    }

    public Upgrade(UpgradeType type, String name, String description) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.weaponIndex = -1;
        this.newWeaponType = "Shotgun"; // По умолчанию
    }

    public Upgrade(UpgradeType type, String name, String description, int weaponIndex) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.weaponIndex = weaponIndex;
    }

    public Upgrade(UpgradeType type, String name, String description, String weaponType) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.weaponIndex = -1;
        this.newWeaponType = weaponType;
    }

    public void apply(Player player) {
        switch (type) {
            case WEAPON_DAMAGE:
                if (weaponIndex >= 0 && weaponIndex < player.getWeapons().size()) {
                    player.upgradeWeapon(weaponIndex);
                }
                break;
            case MAX_HEALTH:
                player.upgradeHealth(20);
                break;
            case SPEED:
                player.upgradeSpeed(30);
                break;
            case HEAL:
                player.heal(50);
                break;
            case NEW_WEAPON:
                if (player.getWeapons().size() < 6) {
                    if (newWeaponType.equals("Shotgun")) {
                        player.addWeapon(new Weapon("Shotgun", 15, 0.8, 250));
                    } else if (newWeaponType.equals("Shovel")) {
                        player.addWeapon(new Weapon("Shovel", 20, 0.5, 150));
                    }
                }
                break;
        }
    }

    public UpgradeType getType() { return type; }
    public String getName() { return name; }
    public String getDescription() { return description; }
}