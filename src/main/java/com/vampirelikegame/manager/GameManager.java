package com.vampirelikegame.manager;

import com.vampirelikegame.model.*;
import javafx.scene.canvas.GraphicsContext;
import java.util.*;

/**
 * Менеджер игры с улучшенной генерацией улучшений
 */
public class GameManager {
    private Player player;
    private List<Enemy> enemies;
    private List<Projectile> projectiles;
    private List<Collectible> collectibles;
    private double gameTime;
    private double enemySpawnTimer;
    private double enemySpawnInterval;
    private double difficultyMultiplier;
    private double screenWidth, screenHeight;
    private boolean paused;
    private boolean gameOver;
    private Random random;
    private int enemiesKilled;

    public GameManager(double screenWidth, double screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.random = new Random();
        initializeGame();
    }

    private void initializeGame() {
        this.player = new Player(screenWidth / 2, screenHeight / 2);
        this.enemies = new ArrayList<>();
        this.projectiles = new ArrayList<>();
        this.collectibles = new ArrayList<>();
        this.gameTime = 0;
        this.enemySpawnTimer = 0;
        this.enemySpawnInterval = 2.0;
        this.difficultyMultiplier = 1.0;
        this.paused = false;
        this.gameOver = false;
        this.enemiesKilled = 0;
    }

    public void restart() {
        initializeGame();
    }

    public void update(double deltaTime) {
        if (paused || gameOver) return;

        gameTime += deltaTime;

        int currentMinute = (int) (gameTime / 60);
        difficultyMultiplier = 1.0 + (currentMinute * 0.15);

        player.update(deltaTime, screenWidth, screenHeight);

        enemySpawnTimer += deltaTime;
        if (enemySpawnTimer >= enemySpawnInterval) {
            spawnEnemy();
            enemySpawnTimer = 0;
            if (enemySpawnInterval > 0.5) {
                enemySpawnInterval -= 0.05;
            }
        }

        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            enemy.update(deltaTime, player);

            if (enemy.collidesWith(player) && !player.isInvulnerable()) {
                player.takeDamage(enemy.getDamage());
            }

            if (!enemy.isAlive()) {
                createCollectibles(enemy.getX(), enemy.getY(), enemy.getExperienceValue());
                enemiesKilled++;
                enemyIterator.remove();
            }
        }

        for (Weapon weapon : player.getWeapons()) {
            Enemy target = weapon.findTarget(player.getX(), player.getY(), enemies);
            if (target != null) {
                List<Projectile> newProjectiles = weapon.fire(player.getX(), player.getY(), target);
                projectiles.addAll(newProjectiles);
            }
        }

        Iterator<Projectile> projectileIterator = projectiles.iterator();
        while (projectileIterator.hasNext()) {
            Projectile projectile = projectileIterator.next();
            projectile.update(deltaTime);

            boolean hit = false;
            for (Enemy enemy : enemies) {
                if (projectile.collidesWith(enemy)) {
                    enemy.takeDamage(projectile.getDamage());
                    hit = true;
                    break;
                }
            }

            if (hit || projectile.isExpired()) {
                projectileIterator.remove();
            }
        }

        Iterator<Collectible> collectibleIterator = collectibles.iterator();
        while (collectibleIterator.hasNext()) {
            Collectible collectible = collectibleIterator.next();
            collectible.moveTowardsPlayer(player, deltaTime, 150);

            if (collectible.collidesWith(player)) {
                collectible.applyEffect(player);
            }

            if (collectible.isCollected()) {
                collectibleIterator.remove();
            }
        }

        if (!player.isAlive()) {
            gameOver = true;
        }
    }

    public void render(GraphicsContext gc) {
        for (Collectible collectible : collectibles) {
            collectible.render(gc);
        }

        for (Enemy enemy : enemies) {
            enemy.render(gc);
        }

        for (Projectile projectile : projectiles) {
            projectile.render(gc);
        }

        player.render(gc);

        List<Weapon> weapons = player.getWeapons();
        for (int i = 0; i < weapons.size(); i++) {
            weapons.get(i).renderIndicator(gc, player.getX(), player.getY(), i);
        }
    }

    private void spawnEnemy() {
        double x, y;
        int corner = random.nextInt(4);

        switch (corner) {
            case 0:
                x = random.nextDouble() * 100;
                y = random.nextDouble() * 100;
                break;
            case 1:
                x = screenWidth - random.nextDouble() * 100;
                y = random.nextDouble() * 100;
                break;
            case 2:
                x = random.nextDouble() * 100;
                y = screenHeight - random.nextDouble() * 100;
                break;
            default:
                x = screenWidth - random.nextDouble() * 100;
                y = screenHeight - random.nextDouble() * 100;
                break;
        }

        enemies.add(new Enemy(x, y, difficultyMultiplier));
    }

    private void createCollectibles(double x, double y, double experienceValue) {
        collectibles.add(new Collectible(x, y, Collectible.CollectibleType.EXPERIENCE, experienceValue));

        if (random.nextDouble() < 0.2) {
            collectibles.add(new Collectible(x + 10, y + 10, Collectible.CollectibleType.HEALTH, 10));
        }
    }

    /**
     * Генерация улучшений
     */
    public List<Upgrade> generateUpgradeOptions() {
        List<Upgrade> options = new ArrayList<>();
        Set<Upgrade.UpgradeType> usedTypes = new HashSet<>();

        while (options.size() < 3) {
            Upgrade.UpgradeType type = Upgrade.UpgradeType.values()[random.nextInt(Upgrade.UpgradeType.values().length)];

            if (usedTypes.contains(type)) continue;
            usedTypes.add(type);

            switch (type) {
                case WEAPON_DAMAGE:
                    if (!player.getWeapons().isEmpty()) {
                        int weaponIndex = random.nextInt(player.getWeapons().size());
                        Weapon weapon = player.getWeapons().get(weaponIndex);
                        options.add(new Upgrade(
                                type,
                                "Улучшить " + weapon.getName(),
                                "Урон: +" + (5 + weapon.getLevel() * 2),
                                weaponIndex
                        ));
                    }
                    break;
                case MAX_HEALTH:
                    options.add(new Upgrade(type, "Увеличить HP", "+20 к максимальному здоровью"));
                    break;
                case SPEED:
                    options.add(new Upgrade(type, "Увеличить скорость", "+30 к скорости движения"));
                    break;
                case HEAL:
                    options.add(new Upgrade(type, "Восстановить HP", "Восстанавливает 50 HP"));
                    break;
                case NEW_WEAPON:
                    if (player.getWeapons().size() < 6) {
                        // Случайный выбор нового оружия
                        String[] weaponTypes = {"Shotgun", "Shovel"};
                        String weaponType = weaponTypes[random.nextInt(weaponTypes.length)];

                        String desc = weaponType.equals("Shotgun") ?
                                "Дробовик: высокий урон, средняя дальность" :
                                "Лопата: очень высокий урон, ближний бой";

                        options.add(new Upgrade(type, "Новое оружие: " + weaponType, desc, weaponType));
                    }
                    break;
            }
        }

        return options;
    }

    public Player getPlayer() { return player; }
    public double getGameTime() { return gameTime; }
    public void setPaused(boolean paused) { this.paused = paused; }
    public boolean isPaused() { return paused; }
    public boolean isGameOver() { return gameOver; }
    public int getEnemyCount() { return enemies.size(); }
    public int getEnemiesKilled() { return enemiesKilled; }
}