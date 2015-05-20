package com.leon.mandelbrot.standard;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Mandelbrot {

    protected static final int WIDTH = 1280;
    protected static final int HEIGHT = 720;
    protected static final int MAX_ITERATIONS = 250;

    protected static final double SCALE = 3.5;
    protected static final double TRANSLATE_X = -0.75;
    protected static final double TRANSLATE_Y = 0.0;

    protected final Color[][] img;
    protected final int width;
    protected final int height;
    protected final int maxIterations;

    protected final double scale;
    protected final double yScale;
    protected final double translateX;
    protected final double translateY;

    public Mandelbrot(int width, int height) {
        this(
                width,
                height,
                Mandelbrot.MAX_ITERATIONS,
                Mandelbrot.SCALE,
                Mandelbrot.TRANSLATE_X,
                Mandelbrot.TRANSLATE_Y
        );
    }

    public Mandelbrot(int width,
                      int height,
                      double scale,
                      double translateX,
                      double translateY) {
        this(width, height, Mandelbrot.MAX_ITERATIONS, scale, translateX, translateY);
    }

    public Mandelbrot(int width,
                      int height,
                      int maxIterations,
                      double scale,
                      double translateX,
                      double translateY) {
        this.width = width;
        this.height = height;
        this.img = new Color[height][width];
        this.maxIterations = maxIterations;
        this.scale = scale;
        this.yScale = this.scale * this.height / this.width;
        this.translateX = translateX;
        this.translateY = translateY;
    }

    public void create() {
        for (int i = 0; i < this.img.length; i++) {
            for (int j = 0; j < this.img[i].length; j++) {
                int iterations = this.getIterations(j, i);
                this.img[i][j] = this.getHSBColor(iterations);
            }
        }
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

        while (x*x + y*y < 4.0 && i < this.maxIterations) {
            xtmp = x*x - y*y + x0;
            ytmp = 2.0*x*y + y0;
            if (x == xtmp && y == ytmp) {
                i = this.maxIterations;
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
        if (iterations == this.maxIterations) {
            return new Color(0, 0, 0);
        }
        float h = iterations / (float) this.maxIterations;
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
        int shortArgCount = 1;
        int longArgCount = 7;

        if (args.length != shortArgCount && args.length != longArgCount) {
            System.err.println("Usage: "
                    + Mandelbrot.class.getName()
                    + " [<width> <height> <maxIterations>" +
                    "<scale> <translateX> <translateY>] <fileName>");
            System.exit(2);
        }

        int width = Mandelbrot.WIDTH;
        int height = Mandelbrot.HEIGHT;
        int maxIterations = Mandelbrot.MAX_ITERATIONS;

        double scale = Mandelbrot.SCALE;
        double translateX = Mandelbrot.TRANSLATE_X;
        double translateY = Mandelbrot.TRANSLATE_Y;
        
        String fileName = args[0];

        if (args.length == longArgCount) {
            width = Integer.parseInt(args[0]);
            height = Integer.parseInt(args[1]);
            maxIterations = Integer.parseInt(args[2]);
            scale = Double.parseDouble(args[3]);
            translateX = Double.parseDouble(args[4]);
            translateY = Double.parseDouble(args[5]);
            fileName = args[6];
        }

        Mandelbrot m = new Mandelbrot(
                width,
                height,
                maxIterations,
                scale,
                translateX,
                translateY
        );
        m.create();
        BufferedImage img = m.getImage();

        try {
            File f = new File(fileName);
            ImageIO.write(img, "png", f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
