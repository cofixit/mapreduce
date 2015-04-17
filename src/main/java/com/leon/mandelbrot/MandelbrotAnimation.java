package com.leon.mandelbrot;

import org.monte.media.Format;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;
import static org.monte.media.VideoFormatKeys.*;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

public class MandelbrotAnimation {

    public static void main(String[] args) {

        int width = 875;
        int height = 500;
        int frames = 100;

        double firstScale = 3.5;
        double firstTranslateX = -0.75;
        double firstTranslateY = 0.0;
        double lastScale = 0.35;
        double lastTranslateX = -0.75;
        double lastTranslateY = 0.0;

        AVIWriter out = null;
        Format format = new Format(
                EncodingKey, ENCODING_AVI_PNG,
                DepthKey, 24,
                MediaTypeKey, MediaType.VIDEO,
                FrameRateKey, new Rational(30, 1),
                WidthKey, width,
                HeightKey, height);

        try {
            long start = System.currentTimeMillis();
            try {
                out = new AVIWriter(new File("mandelbrot.avi"));
                out.addTrack(format);
                out.setPalette(0, ColorModel.getRGBdefault());

                for (int i = 0; i < frames; i++) {
                    double relativeFrame = (double) i / (double) frames;
                    double scale = firstScale + (lastScale - firstScale) * relativeFrame;
                    double translateX = firstTranslateX + (lastTranslateX - firstTranslateX) * relativeFrame;
                    double translateY = firstTranslateY + (lastTranslateY - firstTranslateY) * relativeFrame;
                    Mandelbrot m = new Mandelbrot(width, height, scale, translateX, translateY);
                    m.create();
                    BufferedImage img = m.getImage();
                    out.write(0, img, 1);
                }
            } finally {
                if (out != null) {
                    out.close();
                }
                long end = System.currentTimeMillis();
                long time = end-start;
                System.out.println("Video creation done. Calculation time: " + (time / 1000.0) + "s");
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}