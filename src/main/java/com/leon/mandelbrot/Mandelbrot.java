package com.leon.mandelbrot;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Mandelbrot {

    protected static final int MAX_ITERATIONS = 100;

    protected Color[][] img;
    protected int width;
    protected int height;

    public Mandelbrot(int width, int height) {
        this.width = width;
        this.height = height;
        this.img = new Color[width][height];
    }

    public void create() {
        for (int i = 0; i < this.img.length; i++) {
            System.out.println("Calculating Line " + (i+1));
            for (int j = 0; j < this.img[i].length; j++) {
                int iterations = this.getIterations(i, j, -2.5, 1.0, -1.0, 1.0);
                this.img[i][j] = this.getHSBColor(iterations);
            }
        }
    }

    private int getIterations(
            int px,
            int py,
            double negativeXBorder,
            double positiveXBorder,
            double negativeYBorder,
            double positiveYBorder
    ) {
        double x0 = Mandelbrot.pixelToCoordinate(px, negativeXBorder, positiveXBorder, this.width);
        double y0 = Mandelbrot.pixelToCoordinate(py, positiveYBorder, negativeYBorder, this.height);

        int i = 0;
        double x = 0.0;
        double y = 0.0;
        double xtmp;

        while (x*x + y*y < 4.0 && i < Mandelbrot.MAX_ITERATIONS) {
            xtmp = x*x - y*y + x0;
            y = 2.0*x*y + y0;
            x = xtmp;
            i++;
        }

        return i;
    }

    private static double pixelToCoordinate(int pixel, double leftBorder, double rightBorder, int pixels) {
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
