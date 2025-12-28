package com.vampirelikegame.ui;

import com.vampirelikegame.model.Player;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * HUD (Heads-Up Display)
 * Отображает информацию об игроке: здоровье, опыт, время, уровень
 */
public class HUD extends VBox {
    private ProgressBar healthBar;
    private ProgressBar experienceBar;
    private Label timeLabel;
    private Label levelLabel;
    private Label enemyCountLabel;

    public HUD() {
        setAlignment(Pos.TOP_CENTER);
        setPadding(new Insets(10));
        setSpacing(5);
        setPickOnBounds(false);

        initComponents();
        styleComponents();
    }

    /**
     * Инициализация компонентов HUD
     */
    private void initComponents() {
        // Таймер и уровень
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER);

        timeLabel = new Label("00:00");
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        timeLabel.setTextFill(Color.WHITE);

        levelLabel = new Label("Уровень: 1");
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        levelLabel.setTextFill(Color.GOLD);

        enemyCountLabel = new Label("Убито: 0");
        enemyCountLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        enemyCountLabel.setTextFill(Color.LIGHTGRAY);

        topBar.getChildren().addAll(timeLabel, levelLabel, enemyCountLabel);

        VBox expBox = new VBox(3);
        expBox.setAlignment(Pos.CENTER);
        Label expLabel = new Label("Опыт");
        expLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        expLabel.setTextFill(Color.LIGHTBLUE);

        experienceBar = new ProgressBar(0);
        experienceBar.setPrefWidth(400);
        experienceBar.setPrefHeight(20);

        expBox.getChildren().addAll(expLabel, experienceBar);

        VBox healthBox = new VBox(3);
        healthBox.setAlignment(Pos.CENTER);
        Label healthLabel = new Label("Здоровье");
        healthLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        healthLabel.setTextFill(Color.LIGHTGREEN);

        healthBar = new ProgressBar(1.0);
        healthBar.setPrefWidth(400);
        healthBar.setPrefHeight(20);

        healthBox.getChildren().addAll(healthLabel, healthBar);

        Label controlsLabel = new Label("WASD - движение");
        controlsLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        controlsLabel.setTextFill(Color.GRAY);
        controlsLabel.setStyle("-fx-padding: 5 0 0 0;");

        getChildren().addAll(topBar, expBox, healthBox, controlsLabel);
    }

    /**
     * Стилизация компонентов
     */
    private void styleComponents() {
        healthBar.setStyle(
                "-fx-accent: #44ff44;" +
                        "-fx-control-inner-background: #333333;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;"
        );

        experienceBar.setStyle(
                "-fx-accent: #4444ff;" +
                        "-fx-control-inner-background: #333333;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-radius: 10;"
        );

        setStyle("-fx-background-color: rgba(0, 0, 0, 0.5); -fx-background-radius: 10;");
    }

    /**
     * Обновление HUD
     */
    public void update(Player player, double gameTime, int enemiesKilled) {
        double healthPercent = (double) player.getCurrentHealth() / player.getMaxHealth();
        healthBar.setProgress(healthPercent);

        if (healthPercent < 0.3) {
            healthBar.setStyle(
                    "-fx-accent: #ff3333;" +
                            "-fx-control-inner-background: #333333;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;"
            );
        } else if (healthPercent < 0.6) {
            healthBar.setStyle(
                    "-fx-accent:#ffaa00 ;" +
                            "-fx-control-inner-background: #333333;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;"
            );
        } else {
            healthBar.setStyle(
                    "-fx-accent:#44ff44;" +
                            "-fx-control-inner-background: #333333;" +
                            "-fx-background-radius: 10;" +
                            "-fx-border-radius: 10;"
            );
        }

        double expPercent = player.getExperience() / player.getExperienceToNextLevel();
        experienceBar.setProgress(expPercent);

        int minutes = (int) (gameTime / 60);
        int seconds = (int) (gameTime % 60);
        timeLabel.setText(String.format("%02d:%02d", minutes, seconds));

        levelLabel.setText("Уровень: " + player.getLevel());

        enemyCountLabel.setText("Убито: " + enemiesKilled);
    }
}