package com.vampirelikegame.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import com.vampirelikegame.manager.ResourceManager;

/**
 * Класс снаряда со спрайтами
 */
public class Projectile {
    private double x, y;
    private double directionX, directionY;
    private double speed;
    private int damage;
    private double maxDistance;
    private double traveledDistance;
    private static final double SIZE = 16;
    private boolean isActive;
    private Color projectileColor;

    private String weaponType;
    private ResourceManager resourceManager;
    private double rotation;

    public Projectile(double x, double y, double directionX, double directionY,
                      int damage, double maxDistance, Color color, String weaponType) {
        this.x = x;
        this.y = y;
        this.directionX = directionX;
        this.directionY = directionY;
        this.speed = 400;
        this.damage = damage;
        this.maxDistance = maxDistance;
        this.traveledDistance = 0;
        this.isActive = true;
        this.projectileColor = color;
        this.weaponType = weaponType;
        this.resourceManager = ResourceManager.getInstance();

        this.rotation = Math.toDegrees(Math.atan2(directionY, directionX));
    }


    public void update(double deltaTime) {
        if (!isActive) return;

        double moveDistance = speed * deltaTime;
        x += directionX * moveDistance;
        y += directionY * moveDistance;
        traveledDistance += moveDistance;

        if (traveledDistance >= maxDistance) {
            isActive = false;
        }
    }

    public void render(GraphicsContext gc) {
        if (!isActive) return;

        Image sprite = resourceManager.getBulletSprite(weaponType);

        if (sprite != null) {
            gc.save();
            gc.translate(x, y);
            gc.rotate(rotation);
            gc.drawImage(sprite, -SIZE / 2, -SIZE / 2, SIZE, SIZE);
            gc.restore();
        } else {
            gc.setFill(projectileColor);
            gc.fillOval(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);

            Color glowColor = new Color(
                    projectileColor.getRed(),
                    projectileColor.getGreen(),
                    projectileColor.getBlue(),
                    0.3
            );
            gc.setFill(glowColor);
            gc.fillOval(x - SIZE, y - SIZE, SIZE * 2, SIZE * 2);
        }
    }

    public boolean collidesWith(Enemy enemy) {
        if (!isActive || !enemy.isAlive()) return false;

        double dx = x - enemy.getX();
        double dy = y - enemy.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        return distance < (SIZE / 2 + enemy.getSize() / 2);
    }

    public boolean isExpired() {
        return !isActive || traveledDistance >= maxDistance;
    }

    public void deactivate() {
        isActive = false;
    }

    public boolean isActive() { return isActive; }
    public double getX() { return x; }
    public double getY() { return y; }
    public int getDamage() { return damage; }
    public double getSize() { return SIZE; }
    public double getTraveledDistance() { return traveledDistance; }
    public double getMaxDistance() { return maxDistance; }
    public Color getProjectileColor() { return projectileColor; }

    public double getDistancePercent() {
        return Math.min(traveledDistance / maxDistance, 1.0);
    }
}