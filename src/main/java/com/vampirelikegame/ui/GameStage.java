package com.vampirelikegame.ui;

import com.vampirelikegame.manager.GameManager;
import com.vampirelikegame.model.Player;
import com.vampirelikegame.model.Upgrade;
import com.vampirelikegame.db.db;
import com.vampirelikegame.manager.ResourceManager;
import javafx.animation.AnimationTimer;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.util.List;

/**
 * Главное окно игры с фоном из тайлов
 */
public class GameStage {
    private Stage stage;
    private Scene scene;
    private StackPane root;
    private Canvas canvas;
    private GraphicsContext gc;
    private GameManager gameManager;
    private AnimationTimer gameLoop;
    private long lastUpdate;
    private HUD hud;
    private UpgradePanel upgradePanel;
    private db databaseManager;
    private VBox gameOverPanel;
    private ResourceManager resourceManager;

    private static final double SCREEN_WIDTH = 1200;
    private static final double SCREEN_HEIGHT = 800;
    private static final int TILE_SIZE = 16;

    private Image[][] backgroundTiles;

    public GameStage(Stage stage) {
        this.stage = stage;
        this.databaseManager = new db();
        this.resourceManager = ResourceManager.getInstance();

        resourceManager.loadResources();

        initUI();
        generateBackground();
        initGame();
        setupControls();
        startGameLoop();
    }

    private void generateBackground() {
        int tilesX = (int) Math.ceil(SCREEN_WIDTH / TILE_SIZE) + 1;
        int tilesY = (int) Math.ceil(SCREEN_HEIGHT / TILE_SIZE) + 1;

        backgroundTiles = new Image[tilesX][tilesY];

        for (int x = 0; x < tilesX; x++) {
            for (int y = 0; y < tilesY; y++) {
                backgroundTiles[x][y] = resourceManager.getRandomGroundTile();
            }
        }
    }

    private void initUI() {
        root = new StackPane();
        root.setStyle("-fx-background-color: #1a1a2e;");

        canvas = new Canvas(SCREEN_WIDTH, SCREEN_HEIGHT);
        gc = canvas.getGraphicsContext2D();

        hud = new HUD();
        upgradePanel = new UpgradePanel();
        upgradePanel.setVisible(false);

        createGameOverPanel();

        root.getChildren().addAll(canvas, hud, upgradePanel, gameOverPanel);

        scene = new Scene(root, SCREEN_WIDTH, SCREEN_HEIGHT);
        stage.setScene(scene);
        stage.setTitle("Vampire Survivor - JavaFX");
        stage.setResizable(false);
    }

    private void createGameOverPanel() {
        gameOverPanel = new VBox(20);
        gameOverPanel.setAlignment(Pos.CENTER);
        gameOverPanel.setStyle("-fx-background-color: rgba(0, 0, 0, 0.9); -fx-padding: 50;");
        gameOverPanel.setVisible(false);
        gameOverPanel.setPickOnBounds(true);

        Label titleLabel = new Label("GAME OVER");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        titleLabel.setTextFill(Color.RED);

        Label statsLabel = new Label();
        statsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 24));
        statsLabel.setTextFill(Color.WHITE);
        statsLabel.setAlignment(Pos.CENTER);

        Button restartButton = new Button("ПЕРЕЗАПУСТИТЬ");
        restartButton.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        restartButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4a9eff, #357abd);" +
                        "-fx-text-fill: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15 40;" +
                        "-fx-cursor: hand;"
        );

        restartButton.setOnMouseEntered(e ->
                restartButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #5aafff, #458acd);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 15 40;" +
                                "-fx-cursor: hand;"
                )
        );

        restartButton.setOnMouseExited(e ->
                restartButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #4a9eff, #357abd);" +
                                "-fx-text-fill: white;" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 15 40;" +
                                "-fx-cursor: hand;"
                )
        );

        restartButton.setOnAction(e -> restartGame());

        gameOverPanel.getChildren().addAll(titleLabel, statsLabel, restartButton);
    }

    private void initGame() {
        gameManager = new GameManager(SCREEN_WIDTH, SCREEN_HEIGHT);
        lastUpdate = System.nanoTime();
    }

    private void setupControls() {
        scene.setOnKeyPressed(event -> {
            Player player = gameManager.getPlayer();
            KeyCode code = event.getCode();

            if (code == KeyCode.W) player.setMovingUp(true);
            if (code == KeyCode.S) player.setMovingDown(true);
            if (code == KeyCode.A) player.setMovingLeft(true);
            if (code == KeyCode.D) player.setMovingRight(true);

            if (code == KeyCode.R && gameManager.isGameOver()) {
                restartGame();
            }
        });

        scene.setOnKeyReleased(event -> {
            Player player = gameManager.getPlayer();
            KeyCode code = event.getCode();

            if (code == KeyCode.W) player.setMovingUp(false);
            if (code == KeyCode.S) player.setMovingDown(false);
            if (code == KeyCode.A) player.setMovingLeft(false);
            if (code == KeyCode.D) player.setMovingRight(false);
        });

        upgradePanel.setOnUpgradeSelected(upgrade -> {
            upgrade.apply(gameManager.getPlayer());
            gameManager.getPlayer().levelUp();
            upgradePanel.setVisible(false);
            gameManager.setPaused(false);
        });
    }

    private void startGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;

                update(deltaTime);
                render();
            }
        };
        gameLoop.start();
    }

    private void update(double deltaTime) {
        if (deltaTime > 0.1) deltaTime = 0.1;

        gameManager.update(deltaTime);

        Player player = gameManager.getPlayer();

        if (player.canLevelUp() && !gameManager.isPaused() && !gameManager.isGameOver()) {
            showUpgradePanel();
        }

        hud.update(player, gameManager.getGameTime(), gameManager.getEnemiesKilled());

        if (gameManager.isGameOver() && !gameOverPanel.isVisible()) {
            showGameOver();
        }
    }

    private void render() {
        // ✨ НОВОЕ: Отрисовка фона из тайлов
        drawBackground();

        // Отрисовка игровых объектов
        gameManager.render(gc);
    }

    private void drawBackground() {
        for (int x = 0; x < backgroundTiles.length; x++) {
            for (int y = 0; y < backgroundTiles[0].length; y++) {
                Image tile = backgroundTiles[x][y];
                if (tile != null) {
                    gc.drawImage(tile, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                } else {
                    // Fallback: темно-синий фон
                    gc.setFill(Color.rgb(26, 26, 46));
                    gc.fillRect(x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
        }
    }

    private void showUpgradePanel() {
        gameManager.setPaused(true);
        List<Upgrade> upgrades = gameManager.generateUpgradeOptions();
        upgradePanel.showUpgrades(upgrades);
        upgradePanel.setVisible(true);
    }

    private void showGameOver() {
        gameManager.setPaused(true);

        Player player = gameManager.getPlayer();
        databaseManager.saveGameResult(
                player.getLevel(),
                (int) gameManager.getGameTime(),
                gameManager.getEnemiesKilled()
        );

        int minutes = (int) (gameManager.getGameTime() / 60);
        int seconds = (int) (gameManager.getGameTime() % 60);

        Label statsLabel = (Label) gameOverPanel.getChildren().get(1);
        statsLabel.setText(String.format(
                "Уровень: %d\nВремя: %02d:%02d\nВрагов убито: %d\n\nНажмите R или кнопку для перезапуска",
                player.getLevel(),
                minutes,
                seconds,
                gameManager.getEnemiesKilled()
        ));

        gameOverPanel.setVisible(true);
    }

    private void restartGame() {
        gameOverPanel.setVisible(false);
        upgradePanel.setVisible(false);
        generateBackground(); // ✨ НОВОЕ: Генерируем новый фон
        gameManager.restart();
        lastUpdate = System.nanoTime();
    }

    public void show() {
        stage.show();
    }
}