package com.vampirelikegame;

import javafx.application.Application;
import javafx.stage.Stage;
import com.vampirelikegame.ui.GameStage;

/**
 * Главный класс приложения Vampire Survivor
 * Точка входа в игру
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        GameStage gameStage = new GameStage(primaryStage);
        gameStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}