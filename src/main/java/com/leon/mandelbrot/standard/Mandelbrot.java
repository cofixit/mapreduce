package com.leon.mandelbrot.standard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Mandelbrot {

    protected static final int MAX_ITERATIONS = 250;


    protected Color[][] img;
    protected int width;
    protected int height;

    protected double scale;
    protected double yScale;
    protected double translateX;
    protected double translateY;


    public Mandelbrot(int width, int height) {
        this.width = width;
        this.height = height;
        this.img = new Color[height][width];
        this.scale = 3.5;
        this.yScale = this.scale * this.height / this.width;
        this.translateX = -0.75;
        this.translateY = 0.0;
    }

    public Mandelbrot(int width, int height, double scale, double translateX, double translateY) {
        this.width = width;
        this.height = height;
        this.img = new Color[height][width];
        this.scale = scale;
        this.yScale = this.scale * this.height / this.width;
        this.translateX = translateX;
        this.translateY = translateY;
    }

    public void create() {
//        System.out.println("Staring calculation of the Mandelbrot set...");
        long start = System.currentTimeMillis();
        for (int i = 0; i < this.img.length; i++) {
            if (i%100 == 0) {
//                System.out.println((100*i/this.img.length) + "% done.");
            }
            for (int j = 0; j < this.img[i].length; j++) {
                int iterations = this.getIterations(j, i);
                this.img[i][j] = this.getHSBColor(iterations);
            }
        }
        long end = System.currentTimeMillis();
//        System.out.println("100% Done. Calculated a " + width + "x" + height + " image.");
        long time = end-start;
//        System.out.println("Calculation time: " + (time/1000.0) + "s");
    }

    private int getIterations(
            int px,
            int py
    ) {
        double x0 = this.xPixelToCoordinate(px);
        double y0 = this.yPixelToCoordinate(py);

        int i = 0;
        double x = 0.0;
        double y = 0.0;
        double xtmp;
        double ytmp;

        while (x*x + y*y < 4.0 && i < Mandelbrot.MAX_ITERATIONS) {
            xtmp = x*x - y*y + x0;
            ytmp = 2.0*x*y + y0;
            if (x == xtmp && y == ytmp) {
                i = Mandelbrot.MAX_ITERATIONS;
            } else {
                x = xtmp;
                y = ytmp;
                i++;
            }
        }

        return i;
    }

    protected double xPixelToCoordinate(int px) {
        return (px * this.scale)/this.width - this.scale/2 + this.translateX;
    }

    protected double yPixelToCoordinate(int py) {
        return this.yScale/2 - (py*this.yScale)/this.height + this.translateY;
    }

    protected Color getHSBColor(int iterations) {
        if (iterations == Mandelbrot.MAX_ITERATIONS) {
            return new Color(0, 0, 0);
        }
        float h = iterations / (float) MAX_ITERATIONS;
        return Color.getHSBColor(h, 1.0f, 1.0f);
    }

    public BufferedImage getImage() {
        int height = this.img.length;
        int width = this.img[0].length;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        for (int i = 0; i < this.img.length; i++) {
            Color[] row = this.img[i];
            if (row.length != width) {
                throw new ArrayIndexOutOfBoundsException();
            }
            for (int j = 0; j < row.length; j++) {
                Color c = row[j];
                image.setRGB(j, i, c.getRGB());
            }
        }

        return image;
    }

    public static void main(String[] args) {
        Mandelbrot m = new Mandelbrot(
                1200,
                800,
                0.0000005,
                -0.1637007,
                -1.0259398);
        m.create();
        BufferedImage img = m.getImage();

        try {
            File f = new File("mandelbrot_result/standard/maneldbrot.png");
            f.mkdirs();
            ImageIO.write(img, "png", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
