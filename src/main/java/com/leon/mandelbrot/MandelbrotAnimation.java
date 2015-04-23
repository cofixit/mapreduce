package com.leon.mandelbrot;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.scale.AWTUtil;
import org.jcodec.scale.RgbToYuv420;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


public class MandelbrotAnimation {

    public static void main(String[] args) {

        int width = 1280;
        int height = 720;
        int frames = 100;

        double firstScale = 4;
        double firstTranslateX = -0.1637007;
        double firstTranslateY = -1.0259398;
        double lastScale = 0.0000005;
        double lastTranslateX = -0.1637007;
        double lastTranslateY = -1.0259398;

        FileChannel sink = null;
        long start = System.currentTimeMillis();

        try {
            sink = new FileOutputStream(new File("mandelbrot.mp4")).getChannel();
            H264Encoder encoder = new H264Encoder();
            RgbToYuv420 transform = new RgbToYuv420(0, 0);

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
                BufferedImage rgb = m.getImage();
                Picture yuv = Picture.create(rgb.getWidth(), rgb.getHeight(), ColorSpace.YUV420);
                transform.transform(AWTUtil.fromBufferedImage(rgb), yuv);
                ByteBuffer buf = ByteBuffer.allocate(rgb.getWidth() * rgb.getHeight() * 3);

                ByteBuffer ff = encoder.encodeFrame(buf, yuv);
                sink.write(ff);

                s *= sFactor;
                tx += txInc;
                ty += tyInc;
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (sink != null) {
                try {
                    sink.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        long end = System.currentTimeMillis();
        long time = end-start;
        System.out.println("Video creation done. Calculation time: " + (time / 1000.0) + "s");
    }

    public static double getFactor(double first, double last, int frames) {
        return Math.pow(10, (Math.log10(last / first))/(frames - 1));
    }

    public static double getIncrement(double first, double last, int frames) {
        return (last - first) / (frames - 1);
    }

}
