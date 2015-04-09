package com.leon.mandelbrot;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageCreator {

    public static BufferedImage create(Color[][] pixels) throws ArrayIndexOutOfBoundsException {
        int height = pixels.length;
        int width = pixels[0].length;
        BufferedImage img = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < pixels.length; i++) {
            Color[] row = pixels[i];
            if (row.length != width) {
                throw new ArrayIndexOutOfBoundsException();
            }
            for (int j = 0; j < row.length; j++) {
                Color c = row[j];
                img.setRGB(i, j, c.getRGB());
            }
        }

        return img;
    }

}
