package com.vampirelikegame.model;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import com.vampirelikegame.manager.ResourceManager;
import java.util.ArrayList;
import java.util.List;

public class Player {
    private double x, y;
    private double speed;
    private int maxHealth;
    private int currentHealth;
    private int level;
    private double experience;
    private double experienceToNextLevel;
    private List<Weapon> weapons;
    private static final double SIZE = 32;
    private boolean invulnerable;
    private double invulnerabilityTimer;
    private static final double INVULNERABILITY_DURATION = 1.0;
    private boolean movingUp, movingDown, movingLeft, movingRight;

    private double animationTimer;
    private int currentFrame;
    private boolean facingRight;
    private String currentAnimation;
    private ResourceManager resourceManager;

    public Player(double x, double y) {
        this.x = x;
        this.y = y;
        this.speed = 200.0;
        this.maxHealth = 100;
        this.currentHealth = maxHealth;
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = 100;

        this.invulnerable = false;
        this.invulnerabilityTimer = 0;
        this.weapons = new ArrayList<>();

        this.animationTimer = 0;
        this.currentFrame = 0;
        this.facingRight = true;
        this.currentAnimation = "idle";
        this.resourceManager = ResourceManager.getInstance();
        weapons.add(new Weapon("Gun", 10, 1.0, 300));
    }

    public void update(double deltaTime, double screenWidth, double screenHeight) {
        double dx = 0, dy = 0;
        if (movingUp) dy -= 1;
        if (movingDown) dy += 1;
        if (movingLeft) dx -= 1;
        if (movingRight) dx += 1;

        if (dx > 0) facingRight = true;
        if (dx < 0) facingRight = false;

        if (dx != 0 && dy != 0) {
            dx *= 0.707;
            dy *= 0.707;
        }

        if (!isAlive()) {
            currentAnimation = "death";
        } else if (dx != 0 || dy != 0) {
            currentAnimation = "walk";
        } else {
            currentAnimation = "idle";
        }

        x += dx * speed * deltaTime;
        y += dy * speed * deltaTime;
        x = Math.max(SIZE / 2, Math.min(screenWidth - SIZE / 2, x));
        y = Math.max(SIZE / 2, Math.min(screenHeight - SIZE / 2, y));
        if (invulnerable) {
            invulnerabilityTimer -= deltaTime;
            if (invulnerabilityTimer <= 0) {
                invulnerable = false;
                invulnerabilityTimer = 0;
            }
        }
        animationTimer += deltaTime;
        if (animationTimer >= 0.1) { // 10 FPS анимация
            animationTimer = 0;

            int maxFrames = 1;
            switch (currentAnimation) {
                case "walk":
                    maxFrames = resourceManager.getPlayerWalkFrameCount();
                    break;
                case "idle":
                    maxFrames = resourceManager.getPlayerIdleFrameCount();
                    break;
                case "death":
                    maxFrames = 3;
                    break;
            }

            if (currentAnimation.equals("death")) {
                if (currentFrame < maxFrames - 1) {
                    currentFrame++;
                }
            } else {
                currentFrame = (currentFrame + 1) % maxFrames;
            }
        }
        for (Weapon weapon : weapons) {
            weapon.update(deltaTime);
        }
    }

    public void render(GraphicsContext gc) {
        if (invulnerable) {
            if (((int)(invulnerabilityTimer * 10)) % 2 == 0) {
                return;
            }
        }
        Image sprite = resourceManager.getPlayerFrame(currentAnimation, currentFrame);

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
            gc.setFill(Color.CYAN);
            gc.fillOval(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);
        }

        if (invulnerable) {
            gc.setStroke(Color.GOLD);
            gc.setLineWidth(2);
            gc.strokeOval(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);
        }
    }

    public void addExperience(double exp) {
        experience += exp;
    }

    public boolean canLevelUp() {
        return experience >= experienceToNextLevel;
    }

    public void levelUp() {
        if (canLevelUp()) {
            level++;
            experience -= experienceToNextLevel;
            experienceToNextLevel *= 1.5;
        }
    }

    public void takeDamage(int damage) {
        if (invulnerable) {
            return;
        }
        currentHealth -= damage;
        if (currentHealth < 0) currentHealth = 0;
        if (currentHealth > 0) {
            invulnerable = true;
            invulnerabilityTimer = INVULNERABILITY_DURATION;
        } else {
            currentFrame = 0;
        }
    }

    public void heal(int amount) {
        currentHealth += amount;
        if (currentHealth > maxHealth) currentHealth = maxHealth;
    }

    public void upgradeHealth(int amount) {
        maxHealth += amount;
        currentHealth = maxHealth;
    }

    public void upgradeSpeed(double amount) {
        speed += amount;
    }

    public void addWeapon(Weapon weapon) {
        weapons.add(weapon);
    }

    public void upgradeWeapon(int index) {
        if (index >= 0 && index < weapons.size()) {
            weapons.get(index).upgrade();
        }
    }

    public void reset(double x, double y) {
        this.x = x;
        this.y = y;
        this.currentHealth = maxHealth;
        this.level = 1;
        this.experience = 0;
        this.experienceToNextLevel = 100;
        this.invulnerable = false;
        this.invulnerabilityTimer = 0;
        this.movingUp = false;
        this.movingDown = false;
        this.movingLeft = false;
        this.movingRight = false;

        this.weapons.clear();
        this.weapons.add(new Weapon("Basic Gun", 10, 1.0, 300));
    }

    public void setMovingUp(boolean moving) { this.movingUp = moving; }
    public void setMovingDown(boolean moving) { this.movingDown = moving; }
    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getSize() { return SIZE; }
    public int getCurrentHealth() { return currentHealth; }
    public int getMaxHealth() { return maxHealth; }
    public int getLevel() { return level; }
    public double getExperience() { return experience; }
    public double getExperienceToNextLevel() { return experienceToNextLevel; }
    public List<Weapon> getWeapons() { return weapons; }
    public boolean isAlive() { return currentHealth > 0; }
    public double getSpeed() { return speed; }
    public boolean isInvulnerable() {
        return invulnerable;
    }
    @Override
    public String toString() {
        return String.format("Player[Level:%d, HP:%d/%d, Exp:%.0f/%.0f, Weapons:%d, Invuln:%b]",
                level, currentHealth, maxHealth, experience, experienceToNextLevel, weapons.size(), invulnerable);
    }
}
