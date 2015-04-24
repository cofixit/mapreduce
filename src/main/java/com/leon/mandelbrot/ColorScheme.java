package com.leon.mandelbrot;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public class ColorScheme {

    public static void main(String[] args) throws Exception {

        BufferedImage img = new BufferedImage(120, 30, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < 120; i++) {
            Color c = new Color(0);
            if (i < 100) {
                float h = i / 100f;
                c = Color.getHSBColor(h, 1.0f, 1.0f);
            }
            for (int j = 0; j < 30; j++) {
                img.setRGB(i, j, c.getRGB());
            }
        }

        ImageIO.write(img, "png", new File("colorscheme.png"));
    }

}
