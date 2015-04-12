package com.leon.mandelbrot;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Mandelbrot {

    protected static final int MAX_ITERATIONS = 150;


    protected Color[][] img;
    protected int width;
    protected int height;
    protected double xLeft;
    protected double xRight;
    protected double yTop;
    protected double yBottom;

    public Mandelbrot(int width, int height) {
        this.width = width;
        this.height = height;
        this.img = new Color[height][width];
        this.xLeft = -2.5;
        this.xRight = 1.0;
        this.yTop = -1.0;
        this.yBottom = 1.0;
    }

    public Mandelbrot(int width, int height, double xLeft, double xRight, double yTop, double yBottom) {
        this.width = width;
        this.height = height;
        this.img = new Color[height][width];
        this.xLeft = xLeft;
        this.xRight = xRight;
        this.yTop = yTop;
        this.yBottom = yBottom;
    }

    public void create() {
        System.out.println("Staring calculation of the Mandelbrot set...");
        long start = System.currentTimeMillis();
        for (int i = 0; i < this.img.length; i++) {
            if (i%100 == 0) {
                System.out.println((100*i/this.img.length) + "% done.");
            }
            for (int j = 0; j < this.img[i].length; j++) {
                int iterations = this.getIterations(j, i);
                this.img[i][j] = this.getHSBColor(iterations);
            }
        }
        long end = System.currentTimeMillis();
        System.out.println("100% Done. Calculated a " + width + "x" + height + " image.");
        long time = end-start;
        System.out.println("Calculation time: " + (time/1000.0) + "s");
        System.out.println(img[0][0]);
        System.out.println(img[0][this.width-1]);
        System.out.println(img[this.height-1][0]);
        System.out.println(img[this.height-1][this.width-1]);
    }

    private int getIterations(
            int px,
            int py
    ) {
        double x0 = Mandelbrot.pixelToCoordinate(px, this.xLeft, this.xRight, this.width);
        double y0 = Mandelbrot.pixelToCoordinate(py, this.yBottom, this.yTop, this.height);

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

    public static double pixelToCoordinate(int pixel, double leftBorder, double rightBorder, int pixels) {
        return ((rightBorder - leftBorder) * pixel / pixels) + leftBorder;
    }

    protected Color getHSBColor(int iterations) {
        if (iterations == Mandelbrot.MAX_ITERATIONS) {
            return new Color(0, 0, 0);
        }
        float h = iterations / (float) MAX_ITERATIONS;
        return Color.getHSBColor(h, 1.0f, 1.0f);
    }

    public BufferedImage getImage() {
        return ImageCreator.create(this.img);
    }

}
