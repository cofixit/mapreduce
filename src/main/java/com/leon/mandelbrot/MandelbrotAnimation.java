package com.leon.mandelbrot;

import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.VideoFormatKeys;
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
        double firstLeftXBorder = -2.5;
        double firstRightXBorder = 1.0;
        double lastLeftXBorder = -0.25;
        double lastRightXBorder = 0.1;
        double firstTopYBorder = -1.0;
        double firstBottomYBorder = 1.0;
        double lastTopYBorder = -0.1;
        double lastBottomYBorder = 0.1;

        AVIWriter out = null;
        Format format = new Format(
                EncodingKey, ENCODING_AVI_PNG,
                DepthKey, 24).prepend(
                MediaTypeKey, MediaType.VIDEO,
                FrameRateKey, new Rational(30, 1),
                WidthKey, width,
                HeightKey, height);

        try {
            try {
                out = new AVIWriter(new File("mandelbrot.avi"));
                out.addTrack(format);
                out.setPalette(0, ColorModel.getRGBdefault());

                for (int i = 0; i < frames; i++) {
                    double relativeFrame = (double) i / (double) frames;
                    double xLeft = firstLeftXBorder + (lastLeftXBorder - firstLeftXBorder) * relativeFrame;
                    double xRight = firstRightXBorder + (lastRightXBorder - firstRightXBorder) * relativeFrame;
                    double yTop = firstTopYBorder + (lastTopYBorder - firstTopYBorder) * relativeFrame;
                    double yBottom = firstBottomYBorder + (lastBottomYBorder - firstBottomYBorder) * relativeFrame;
                    Mandelbrot m = new Mandelbrot(width, height, xLeft, xRight, yTop, yBottom);
                    m.create();
                    BufferedImage img = m.getImage();
                    out.write(0, img, 1);
                }
            } finally {
                out.close();
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

}