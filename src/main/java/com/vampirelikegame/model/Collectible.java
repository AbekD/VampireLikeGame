package com.vampirelikegame.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import com.vampirelikegame.manager.ResourceManager;

/**
 * Класс собираемых предметов со спрайтами
 */
public class Collectible {
    private double x, y;
    private CollectibleType type;
    private double value;
    private static final double SIZE = 16;
    private boolean collected;
    private double animationTimer;
    private double bobOffset;
    private ResourceManager resourceManager;

    public enum CollectibleType {
        EXPERIENCE,
        HEALTH
    }

    public Collectible(double x, double y, CollectibleType type, double value) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.value = value;
        this.collected = false;
        this.animationTimer = 0;
        this.bobOffset = 0;
        this.resourceManager = ResourceManager.getInstance();
    }

    public void moveTowardsPlayer(Player player, double deltaTime, double magnetRange) {
        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < magnetRange && distance > 0) {
            dx /= distance;
            dy /= distance;

            double magnetSpeed = 300;
            x += dx * magnetSpeed * deltaTime;
            y += dy * magnetSpeed * deltaTime;
        }

        animationTimer += deltaTime * 5;
        bobOffset = Math.sin(animationTimer) * 3;
    }

    public void render(GraphicsContext gc) {
        Image sprite = null;

        switch (type) {
            case EXPERIENCE:
                sprite = resourceManager.getExperienceSprite();
                break;
            case HEALTH:
                sprite = resourceManager.getExperienceSprite();
                break;
        }

        if (sprite != null) {
            gc.save();

            if (type == CollectibleType.HEALTH) {
                gc.setGlobalAlpha(0.8);
            }

            double renderY = y + bobOffset;
            gc.drawImage(sprite, x - SIZE / 2, renderY - SIZE / 2, SIZE, SIZE);

            gc.setGlobalAlpha(0.3);
            Color glowColor = type == CollectibleType.EXPERIENCE ?
                    Color.GOLD : Color.LIGHTGREEN;
            gc.setFill(glowColor);
            gc.fillOval(x - SIZE, renderY - SIZE, SIZE * 2, SIZE * 2);

            gc.restore();
        } else {
            gc.save();
            double renderY = y + bobOffset;

            switch (type) {
                case EXPERIENCE:
                    gc.setFill(Color.GOLD);
                    break;
                case HEALTH:
                    gc.setFill(Color.LIGHTGREEN);
                    break;
            }
            gc.fillOval(x - SIZE / 2, renderY - SIZE / 2, SIZE, SIZE);
            gc.restore();
        }
    }

    public boolean collidesWith(Player player) {
        double dx = x - player.getX();
        double dy = y - player.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (SIZE / 2 + player.getSize() / 2);
    }

    public void applyEffect(Player player) {
        if (!collected) {
            switch (type) {
                case EXPERIENCE:
                    player.addExperience(value);
                    break;
                case HEALTH:
                    player.heal((int) value);
                    break;
            }
            collected = true;
        }
    }

    public boolean isCollected() { return collected; }
    public CollectibleType getType() { return type; }
}