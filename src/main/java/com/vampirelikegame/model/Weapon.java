package com.vampirelikegame.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.List;

/**
 * Класс оружия с типами: Gun, Shotgun, Shovel
 */
public class Weapon {
    private String name;
    private int damage;
    private double fireRate;
    private double range;
    private int level;
    private double cooldown;
    private int projectileCount;
    private Color weaponColor;
    private Enemy currentTarget;

    public Weapon(String name, int damage, double fireRate, double range) {
        this.name = name;
        this.damage = damage;
        this.fireRate = fireRate;
        this.range = range;
        this.level = 1;
        this.cooldown = 0;
        this.projectileCount = 1;
        this.currentTarget = null;

        if (name.contains("Gun")) {
            this.weaponColor = Color.YELLOW;
        } else if (name.contains("Shotgun")) {
            this.weaponColor = Color.ORANGE;
            this.projectileCount = 3;
        } else if (name.contains("Shovel")) {
            this.weaponColor = Color.GRAY;
        } else {
            this.weaponColor = Color.WHITE;
        }
    }

    public void update(double deltaTime) {
        if (cooldown > 0) {
            cooldown -= deltaTime;
            if (cooldown < 0) cooldown = 0;
        }
    }

    public boolean canFire() {
        return cooldown <= 0;
    }

    public Enemy findTarget(double playerX, double playerY, List<Enemy> enemies) {
        Enemy nearestEnemy = null;
        double minDistance = Double.MAX_VALUE;

        for (Enemy enemy : enemies) {
            if (!enemy.isAlive()) continue;

            double dx = enemy.getX() - playerX;
            double dy = enemy.getY() - playerY;
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (distance <= range && distance < minDistance) {
                minDistance = distance;
                nearestEnemy = enemy;
            }
        }

        currentTarget = nearestEnemy;
        return nearestEnemy;
    }

    public List<Projectile> fire(double playerX, double playerY, Enemy target) {
        List<Projectile> projectiles = new ArrayList<>();

        if (!canFire() || target == null) {
            return projectiles;
        }

        double dx = target.getX() - playerX;
        double dy = target.getY() - playerY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > range) {
            return projectiles;
        }

        dx /= distance;
        dy /= distance;

        for (int i = 0; i < projectileCount; i++) {
            double angle = 0;

            if (projectileCount > 1) {
                angle = (i - projectileCount / 2.0) * 0.2;
            }

            double rotatedDx = dx * Math.cos(angle) - dy * Math.sin(angle);
            double rotatedDy = dx * Math.sin(angle) + dy * Math.cos(angle);

            Projectile projectile = new Projectile(
                    playerX,
                    playerY,
                    rotatedDx,
                    rotatedDy,
                    damage,
                    range,
                    weaponColor,
                    name
            );
            projectiles.add(projectile);
        }

        cooldown = 1.0 / fireRate;
        return projectiles;
    }

    public void renderIndicator(GraphicsContext gc, double playerX, double playerY, int weaponIndex) {
        double angle = (weaponIndex * Math.PI * 2 / 6);
        double indicatorDistance = 45;
        double indicatorX = playerX + Math.cos(angle) * indicatorDistance;
        double indicatorY = playerY + Math.sin(angle) * indicatorDistance;

        double indicatorSize = 10;
        gc.setFill(weaponColor);
        gc.fillOval(indicatorX - indicatorSize / 2, indicatorY - indicatorSize / 2,
                indicatorSize, indicatorSize);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1);
        gc.strokeOval(indicatorX - indicatorSize / 2, indicatorY - indicatorSize / 2,
                indicatorSize, indicatorSize);

        if (cooldown > 0) {
            double cooldownPercent = cooldown / (1.0 / fireRate);
            gc.setFill(Color.rgb(0, 0, 0, 0.5));
            gc.fillArc(indicatorX - indicatorSize / 2, indicatorY - indicatorSize / 2,
                    indicatorSize, indicatorSize, 90, -360 * cooldownPercent,
                    javafx.scene.shape.ArcType.ROUND);
        }

        if (currentTarget != null && currentTarget.isAlive()) {
            gc.setStroke(weaponColor);
            gc.setLineWidth(1);
            gc.setGlobalAlpha(0.3);
            gc.strokeLine(playerX, playerY, currentTarget.getX(), currentTarget.getY());
            gc.setGlobalAlpha(1.0);
        }
    }

    public void upgrade() {
        level++;
        damage += 5;
        if (level % 2 == 0) fireRate += 0.2;
        if (level % 3 == 0) projectileCount++;
        if (level % 4 == 0) range += 50;
    }

    public String getDescription() {
        return String.format(
                "%s (Ур.%d): Урон %d, %.1f выстр/сек, дальность %.0f, снарядов %d",
                name, level, damage, fireRate, range, projectileCount
        );
    }

    public String getName() { return name; }
    public int getDamage() { return damage; }
    public double getFireRate() { return fireRate; }
    public double getRange() { return range; }
    public int getLevel() { return level; }
    public int getProjectileCount() { return projectileCount; }
    public double getCooldown() { return cooldown; }
    public Color getWeaponColor() { return weaponColor; }
    public Enemy getCurrentTarget() { return currentTarget; }
}