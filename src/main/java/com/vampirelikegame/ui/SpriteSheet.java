package com.vampirelikegame.ui;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import java.util.ArrayList;
import java.util.List;

public class SpriteSheet {

    public static List<Image> sliceRow(Image sheet, int frameWidth, int frameHeight) {
        List<Image> frames = new ArrayList<>();
        PixelReader reader = sheet.getPixelReader();

        int count = (int)(sheet.getWidth() / frameWidth);

        for (int i = 0; i < count; i++) {
            frames.add(new WritableImage(
                    reader,
                    i * frameWidth,
                    0,
                    frameWidth,
                    frameHeight
            ));
        }
        return frames;
    }
}

