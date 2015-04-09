package com.leon.mandelbrot;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Launcher {

    public static void main(String[] args) {
        Mandelbrot m = new Mandelbrot(8750*2, 5000*2);
        m.create();
        BufferedImage img = m.getImage();

        try {
            ImageIO.write(img, "png", new File("mandelbrot.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}