package com.leon.mandelbrot;

import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.VideoFormatKeys.*;

public class MandelbrotAnimation {

    public static void main(String[] args) {

        int width = 600;
        int height = 400;
        int frames = 500;

        double firstScale = 4;
        double firstTranslateX = -0.1637007;
        double firstTranslateY = -1.0259398;
        double lastScale = 0.0000005;
        double lastTranslateX = -0.1637007;
        double lastTranslateY = -1.0259398;

        AVIWriter out = null;
        Format format = new Format(
                EncodingKey, ENCODING_AVI_PNG,
                DepthKey, 24,
                MediaTypeKey, FormatKeys.MediaType.VIDEO,
                FrameRateKey, new Rational(30, 1),
                WidthKey, width,
                HeightKey, height);

        try {
            long start = System.currentTimeMillis();
            try {
                out = new AVIWriter(new File("mandelbrot.avi"));
                out.addTrack(format);
                out.setPalette(0, ColorModel.getRGBdefault());
                
                double s = firstScale;
                double tx = firstTranslateX;
                double ty = firstTranslateY;
                
                double sFactor = MandelbrotAnimation.getFactor(firstScale, lastScale, frames);
                double txInc = MandelbrotAnimation.getIncrement(firstTranslateX, lastTranslateX, frames);
                double tyInc = MandelbrotAnimation.getIncrement(firstTranslateY, lastTranslateY, frames);


                for (int i = 0; i < frames; i++) {
                    System.out.println("i = " + i + ", s=" + s + ", tx=" + tx + ", ty=" + ty);
                    Mandelbrot m = new Mandelbrot(width, height, s, tx, ty);
                    m.create();
                    BufferedImage img = m.getImage();
                    out.write(0, img, 1);
                    s *= sFactor;
                    tx += txInc;
                    ty += tyInc;
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

    public static double getFactor(double first, double last, int frames) {
        return Math.pow(10, (Math.log10(last / first))/(frames - 1));
    }

    public static double getIncrement(double first, double last, int frames) {
        return (last - first) / (frames - 1);
    }

}
