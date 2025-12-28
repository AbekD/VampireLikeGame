package com.vampirelikegame.manager;

import com.vampirelikegame.ui.SpriteSheet;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

import java.util.*;

/**
 * Менеджер ресурсов
 */
public class ResourceManager {

    private static ResourceManager instance;
    private static final int TILE_SIZE = 16;


    public enum EnemyType {
        ENEMY_0(112, 18),
        ENEMY_3(149, 18);

        public final int width;
        public final int height;

        EnemyType(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    private final Random random = new Random();
    private final List<Image> groundTiles = new ArrayList<>();
    private final Map<EnemyType, Image> enemySprites = new HashMap<>();

    // Игрок
    private List<Image> playerWalkFrames;
    private List<Image> playerIdleFrames;
    private List<Image> playerDeathFrames;

    // Враги
    private List<Image> enemy0WalkFrames;
    private List<Image> enemy3WalkFrames;
    private Image enemy0Hurt;
    private Image enemy3Hurt;
    private Image enemy0Death;
    private Image enemy3Death;

    // Оружие
    private Image gunSprite;
    private Image shotgunSprite;
    private Image shovelSprite;
    private Image bulletSprite;
    private Image shotgunBulletSprite;
    private Image experienceSprite;

    private ResourceManager() {}

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }


    public void loadResources() {
        loadGroundTiles();
        loadWeapons();
        loadProjectiles();
        loadPlayer();
        loadEnemies();
        System.out.println("Resources loaded successfully!");
    }

    private void loadGroundTiles() {
        Image sheet = new Image(getClass().getResourceAsStream("/images/Tiles.png"));
        int perRow = (int) (sheet.getWidth() / TILE_SIZE);

        for (int i = 0; i < 6; i++) {
            int x = (i % perRow) * TILE_SIZE;
            int y = (i / perRow) * TILE_SIZE;
            groundTiles.add(crop(sheet, x, y, TILE_SIZE, TILE_SIZE));
        }
        System.out.println("Loaded " + groundTiles.size() + " ground tiles");
    }

    private void loadWeapons() {
        Image sheet = new Image(getClass().getResourceAsStream("/images/Props.png"));
        gunSprite     = crop(sheet, 0, 0, 18, 12);
        shotgunSprite = crop(sheet, 18, 0, 18, 12);
        shovelSprite  = crop(sheet, 36, 0, 18, 12);
        System.out.println("Loaded weapons");
    }

    private void loadProjectiles() {
        Image sheet = new Image(getClass().getResourceAsStream("/images/Props.png"));
        bulletSprite        = crop(sheet, 54, 0, 8, 12);
        shotgunBulletSprite = crop(sheet, 62, 0, 8, 12);
        experienceSprite    = crop(sheet, 70, 0, 8, 12);
        System.out.println("Loaded projectiles");
    }


    private void loadPlayer() {
        Image sheet = new Image(getClass().getResourceAsStream("/images/Farmer 2.png"));
        List<Image> allFrames = SpriteSheet.sliceRow(sheet, 18, 18);

        int totalFrames = allFrames.size();
        System.out.println("Total player frames: " + totalFrames);

        if (totalFrames >= 12) {
            playerWalkFrames = new ArrayList<>(allFrames.subList(0, 6));

            playerIdleFrames = new ArrayList<>(allFrames.subList(6, 9));

            playerDeathFrames = new ArrayList<>(allFrames.subList(9, 12));
        } else {
            System.err.println("WARNING: Not enough player frames! Using fallback");
            playerWalkFrames = new ArrayList<>(allFrames);
            playerIdleFrames = List.of(allFrames.get(0));
            playerDeathFrames = List.of(allFrames.get(allFrames.size() - 1));
        }

        System.out.println("Player animations: walk=" + playerWalkFrames.size() +
                ", idle=" + playerIdleFrames.size() +
                ", death=" + playerDeathFrames.size());
    }

    private void loadEnemies() {
        Image e0 = new Image(getClass().getResourceAsStream("/images/Enemy 0.png"));
        Image e3 = new Image(getClass().getResourceAsStream("/images/Enemy 3.png"));

        List<Image> allEnemy0Frames = SpriteSheet.sliceRow(e0, 18, 18);
        List<Image> allEnemy3Frames = SpriteSheet.sliceRow(e3, 18, 18);

        System.out.println("Enemy0 total frames: " + allEnemy0Frames.size());
        System.out.println("Enemy3 total frames: " + allEnemy3Frames.size());

        if (allEnemy0Frames.size() >= 3) {
            int walkCount = allEnemy0Frames.size() - 2;
            enemy0WalkFrames = new ArrayList<>(allEnemy0Frames.subList(0, walkCount));

            enemy0Hurt = allEnemy0Frames.get(allEnemy0Frames.size() - 2);

            enemy0Death = allEnemy0Frames.get(allEnemy0Frames.size() - 1);
        } else {
            enemy0WalkFrames = new ArrayList<>(allEnemy0Frames);
            enemy0Hurt = allEnemy0Frames.get(0);
            enemy0Death = allEnemy0Frames.get(allEnemy0Frames.size() - 1);
        }

        if (allEnemy3Frames.size() >= 3) {
            int walkCount = allEnemy3Frames.size() - 2;
            enemy3WalkFrames = new ArrayList<>(allEnemy3Frames.subList(0, walkCount));
            enemy3Hurt = allEnemy3Frames.get(allEnemy3Frames.size() - 2);
            enemy3Death = allEnemy3Frames.get(allEnemy3Frames.size() - 1);
        } else {
            enemy3WalkFrames = new ArrayList<>(allEnemy3Frames);
            enemy3Hurt = allEnemy3Frames.get(0);
            enemy3Death = allEnemy3Frames.get(allEnemy3Frames.size() - 1);
        }

        System.out.println("Enemy0: walk=" + enemy0WalkFrames.size() + " frames, hurt + death");
        System.out.println("Enemy3: walk=" + enemy3WalkFrames.size() + " frames, hurt + death");
    }


    private Image crop(Image src, int x, int y, int w, int h) {
        PixelReader pr = src.getPixelReader();
        return new WritableImage(pr, x, y, w, h);
    }


    public Image getRandomGroundTile() {
        return groundTiles.isEmpty()
                ? null
                : groundTiles.get(random.nextInt(groundTiles.size()));
    }

    public Image getEnemyFrame(int type, String anim, int frame) {
        if (type == 3) {
            return switch (anim) {
                case "walk"  -> enemy3WalkFrames.isEmpty() ? null :
                        enemy3WalkFrames.get(frame % enemy3WalkFrames.size());
                case "hurt"  -> enemy3Hurt;
                case "death" -> enemy3Death;
                default -> null;
            };
        } else {
            return switch (anim) {
                case "walk"  -> enemy0WalkFrames.isEmpty() ? null :
                        enemy0WalkFrames.get(frame % enemy0WalkFrames.size());
                case "hurt"  -> enemy0Hurt;
                case "death" -> enemy0Death;
                default -> null;
            };
        }
    }

    public int getEnemyWalkFrameCount(int type) {
        return (type == 3)
                ? Math.max(1, enemy3WalkFrames.size())
                : Math.max(1, enemy0WalkFrames.size());
    }

    public Image getPlayerFrame(String anim, int frame) {
        return switch (anim) {
            case "walk" -> playerWalkFrames.isEmpty() ? null :
                    playerWalkFrames.get(frame % playerWalkFrames.size());
            case "idle" -> playerIdleFrames.isEmpty() ? null :
                    playerIdleFrames.get(frame % playerIdleFrames.size());
            case "death" -> playerDeathFrames.isEmpty() ? null :
                    playerDeathFrames.get(Math.min(frame, playerDeathFrames.size() - 1));
            default -> null;
        };
    }

    public int getPlayerWalkFrameCount() {
        return Math.max(1, playerWalkFrames.size());
    }

    public int getPlayerIdleFrameCount() {
        return Math.max(1, playerIdleFrames.size());
    }

    public Image getWeaponSprite(String name) {
        if (name.contains("Shotgun")) return shotgunSprite;
        if (name.contains("Shovel")) return shovelSprite;
        return gunSprite;
    }

    public Image getBulletSprite(String name) {
        return name.contains("Shotgun") ? shotgunBulletSprite : bulletSprite;
    }

    public Image getExperienceSprite() {
        return experienceSprite;
    }
}