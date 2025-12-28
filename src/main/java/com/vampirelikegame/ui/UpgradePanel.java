package com.vampirelikegame.ui;

import com.vampirelikegame.model.Upgrade;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import java.util.List;
import java.util.function.Consumer;

/**
 * Панель выбора улучшений
 * Отображается при повышении уровня, предлагает 3 случайных улучшения
 */
public class UpgradePanel extends VBox {
    private Label titleLabel;
    private HBox upgradeButtonsBox;
    private Consumer<Upgrade> onUpgradeSelected;

    public UpgradePanel() {
        setAlignment(Pos.CENTER);
        setSpacing(30);
        setPadding(new Insets(50));
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.95); -fx-background-radius: 20;");
        setMaxWidth(900);
        setMaxHeight(500);

        initComponents();
    }

    /**
     * Инициализация компонентов панели
     */
    private void initComponents() {
        titleLabel = new Label("ВЫБЕРИТЕ УЛУЧШЕНИЕ");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 36));
        titleLabel.setTextFill(Color.GOLD);
        titleLabel.setTextAlignment(TextAlignment.CENTER);

        upgradeButtonsBox = new HBox(20);
        upgradeButtonsBox.setAlignment(Pos.CENTER);

        getChildren().addAll(titleLabel, upgradeButtonsBox);
    }

    /**
     * Показ улучшений
     */
    public void showUpgrades(List<Upgrade> upgrades) {
        upgradeButtonsBox.getChildren().clear();

        for (Upgrade upgrade : upgrades) {
            VBox upgradeBox = createUpgradeBox(upgrade);
            upgradeButtonsBox.getChildren().add(upgradeBox);
        }
    }

    /**
     * Создание карточки улучшения
     */
    private VBox createUpgradeBox(Upgrade upgrade) {
        VBox box = new VBox(15);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));
        box.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #2a2a4a, #1a1a3a);" +
                        "-fx-background-radius: 15;" +
                        "-fx-border-color: #4a4a6a;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 15;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);"
        );
        box.setPrefWidth(250);
        box.setPrefHeight(300);

        Label iconLabel = new Label(getUpgradeIcon(upgrade.getType()));
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48));
        iconLabel.setTextFill(getUpgradeColor(upgrade.getType()));

        Label nameLabel = new Label(upgrade.getName());
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        nameLabel.setMaxWidth(220);

        Label descLabel = new Label(upgrade.getDescription());
        descLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        descLabel.setTextFill(Color.LIGHTGRAY);
        descLabel.setWrapText(true);
        descLabel.setTextAlignment(TextAlignment.CENTER);
        descLabel.setMaxWidth(220);

        Button selectButton = new Button("ВЫБРАТЬ");
        selectButton.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        selectButton.setTextFill(Color.WHITE);
        selectButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #4a9eff, #357abd);" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 10 30;"
        );

        selectButton.setOnMouseEntered(e ->
                selectButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #5aafff, #458acd);" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 10 30;" +
                                "-fx-cursor: hand;"
                )
        );

        selectButton.setOnMouseExited(e ->
                selectButton.setStyle(
                        "-fx-background-color: linear-gradient(to bottom, #4a9eff, #357abd);" +
                                "-fx-background-radius: 10;" +
                                "-fx-padding: 10 30;"
                )
        );

        selectButton.setOnAction(e -> {
            if (onUpgradeSelected != null) {
                onUpgradeSelected.accept(upgrade);
            }
        });

        box.getChildren().addAll(iconLabel, nameLabel, descLabel, selectButton);

        box.setOpacity(0);
        javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(
                javafx.util.Duration.millis(300), box
        );
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        return box;
    }

    /**
     * Получение иконки для типа улучшения
     */
    private String getUpgradeIcon(Upgrade.UpgradeType type) {
        switch (type) {
            case WEAPON_DAMAGE:
            case WEAPON_FIRERATE:
                return "⚔";
            case MAX_HEALTH:
                return "❤";
            case SPEED:
                return "⚡";
            case HEAL:
                return "✚";
            case NEW_WEAPON:
                return "🗡";
            default:
                return "★";
        }
    }

    /**
     * Получение цвета для типа улучшения
     */
    private Color getUpgradeColor(Upgrade.UpgradeType type) {
        switch (type) {
            case WEAPON_DAMAGE:
            case WEAPON_FIRERATE:
            case NEW_WEAPON:
                return Color.ORANGE;
            case MAX_HEALTH:
            case HEAL:
                return Color.LIGHTGREEN;
            case SPEED:
                return Color.CYAN;
            default:
                return Color.GOLD;
        }
    }

    /**
     * Установка обработчика выбора улучшения
     */
    public void setOnUpgradeSelected(Consumer<Upgrade> handler) {
        this.onUpgradeSelected = handler;
    }
}