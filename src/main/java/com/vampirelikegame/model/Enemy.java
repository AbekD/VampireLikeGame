package com.vampirelikegame.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import com.vampirelikegame.manager.ResourceManager;

/**
 * Класс врага с анимацией и разными типами
 */
public class Enemy {
    private double x, y;
    private double speed;
    private int maxHealth;
    private int currentHealth;
    private int damage;
    private double experienceValue;
    private static final double SIZE = 32;
    private boolean isDead;

    private double lookDirectionX;
    private double lookDirectionY;

    private int enemyType; // 0 или 3
    private double animationTimer;
    private int currentFrame;
    private boolean facingRight;
    private double hurtTimer;
    private ResourceManager resourceManager;

    public Enemy(double x, double y, double difficultyMultiplier) {
        this(x, y, difficultyMultiplier, Math.random() < 0.5 ? 0 : 3);
    }

    public Enemy(double x, double y, double difficultyMultiplier, int type) {
        this.x = x;
        this.y = y;
        this.enemyType = type;
        this.speed = 80 * difficultyMultiplier;
        this.maxHealth = (int)(30 * difficultyMultiplier);
        this.currentHealth = maxHealth;
        this.damage = (int)(10 * difficultyMultiplier);
        this.experienceValue = 15 * difficultyMultiplier;
        this.isDead = false;
        this.lookDirectionX = 1;
        this.lookDirectionY = 0;

        this.animationTimer = 0;
        this.currentFrame = 0;
        this.facingRight = true;
        this.hurtTimer = 0;
        this.resourceManager = ResourceManager.getInstance();
    }

    public void update(double deltaTime, Player player) {
        if (isDead) return;

        double dx = player.getX() - x;
        double dy = player.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance > 0) {
            dx /= distance;
            dy /= distance;

            lookDirectionX = dx;
            lookDirectionY = dy;

            facingRight = dx > 0;

            x += dx * speed * deltaTime;
            y += dy * speed * deltaTime;
        }

        animationTimer += deltaTime;
        if (animationTimer >= 0.15) { // ~6.7 FPS для врагов
            animationTimer = 0;
            int maxFrames = resourceManager.getEnemyWalkFrameCount(enemyType);
            if (maxFrames > 0) {
                currentFrame = (currentFrame + 1) % maxFrames;
            }
        }

        if (hurtTimer > 0) {
            hurtTimer -= deltaTime;
        }
    }

    public void render(GraphicsContext gc) {
        if (isDead) {
            Image sprite = resourceManager.getEnemyFrame(enemyType, "death", 0);
            if (sprite != null) {
                gc.save();
                gc.translate(x, y);
                if (!facingRight) {
                    gc.scale(-1, 1);
                    gc.translate(-SIZE, 0);
                }
                double scale = 2.0;
                double w = sprite.getWidth() * scale;
                double h = sprite.getHeight() * scale;

                gc.drawImage(sprite, -w / 2, -h / 2, w, h);

                gc.restore();
            }
            return;
        }

        Image sprite;
        if (hurtTimer > 0) {
            sprite = resourceManager.getEnemyFrame(enemyType, "hurt", 0);
        } else {
            sprite = resourceManager.getEnemyFrame(enemyType, "walk", currentFrame);
        }

        if (sprite != null) {
            gc.save();
            gc.translate(x, y);

            if (!facingRight) {
                gc.scale(-1, 1);
            }

            double scale = 2.0;
            double w = sprite.getWidth() * scale;
            double h = sprite.getHeight() * scale;

            gc.drawImage(sprite, -w / 2, -h / 2, w, h);

            gc.restore();
        } else {
            gc.save();
            double angle = Math.atan2(lookDirectionY, lookDirectionX);
            gc.translate(x, y);
            gc.rotate(Math.toDegrees(angle));

            gc.setFill(Color.RED);
            gc.fillRect(-SIZE / 2, -SIZE / 2, SIZE, SIZE);

            gc.setFill(Color.DARKRED);
            double[] xPoints = {SIZE / 2, SIZE / 2 - 5, SIZE / 2 - 5};
            double[] yPoints = {0, -3, 3};
            gc.fillPolygon(xPoints, yPoints, 3);

            gc.restore();
        }

        double healthBarWidth = SIZE;
        double healthBarHeight = 3;
        double healthPercentage = (double) currentHealth / maxHealth;

        gc.setFill(Color.BLACK);
        gc.fillRect(x - healthBarWidth / 2, y - SIZE / 2 - 5, healthBarWidth, healthBarHeight);

        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(x - healthBarWidth / 2, y - SIZE / 2 - 5,
                healthBarWidth * healthPercentage, healthBarHeight);
    }

    public void takeDamage(int damage) {
        currentHealth -= damage;
        hurtTimer = 0.1;

        if (currentHealth <= 0) {
            currentHealth = 0;
            isDead = true;
        }
    }

    public boolean collidesWith(Player player) {
        double dx = x - player.getX();
        double dy = y - player.getY();
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (SIZE / 2 + player.getSize() / 2);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return SIZE; }
    public int getDamage() { return damage; }
    public double getExperienceValue() { return experienceValue; }
    public boolean isAlive() { return !isDead && currentHealth > 0; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public double getLookDirectionX() { return lookDirectionX; }
    public double getLookDirectionY() { return lookDirectionY; }
    public int getEnemyType() { return enemyType; }
}