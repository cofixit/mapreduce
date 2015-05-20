package com.leon.mandelbrot.standard;

import org.jcodec.codecs.h264.H264Encoder;
import org.jcodec.codecs.h264.H264Utils;
import org.jcodec.common.NIOUtils;
import org.jcodec.common.SeekableByteChannel;
import org.jcodec.common.model.ColorSpace;
import org.jcodec.common.model.Picture;
import org.jcodec.containers.mp4.Brand;
import org.jcodec.containers.mp4.MP4Packet;
import org.jcodec.containers.mp4.TrackType;
import org.jcodec.containers.mp4.muxer.FramesMP4MuxerTrack;
import org.jcodec.containers.mp4.muxer.MP4Muxer;
import org.jcodec.scale.AWTUtil;
import org.jcodec.scale.RgbToYuv420;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;


public class MandelbrotAnimation {

    public static void main(String[] args) {

        int longArgCount = 11;
        int shortArgCount = 1;

        if (args.length != longArgCount && args.length != shortArgCount) {
            System.err.println("Usage: "
                    + MandelbrotAnimation.class.getName()
                    + " [<width> <height> <frames> <maxIterations>" +
                    "<firstScale> <firstTranslateX> <firstTranslateY>" +
                    "<lastScale> <lastTranslateX> <lastTranslateY>] <fileName>");
            System.exit(2);
        }

        int width = MandelbrotProperties.STANDARD_WIDTH;
        int height = MandelbrotProperties.STANDARD_HEIGHT;
        int frames = MandelbrotProperties.STANDARD_FRAMES;
        int maxIterations = MandelbrotProperties.STANDARD_MAX_ITERATIONS;

        double firstScale = MandelbrotProperties.STANDARD_FIRST_SCALE;
        double firstTranslateX = MandelbrotProperties.STANDARD_FIRST_TRANSLATE_X;
        double firstTranslateY = MandelbrotProperties.STANDARD_FIRST_TRANSLATE_Y;
        double lastScale = MandelbrotProperties.STANDARD_LAST_SCALE;
        double lastTranslateX = MandelbrotProperties.STANDARD_LAST_TRANSLATE_X;
        double lastTranslateY = MandelbrotProperties.STANDARD_LAST_TRANSLATE_Y;
        
        String fileName = args[0];

        if (args.length == longArgCount) {
            width = Integer.parseInt(args[0]);
            height = Integer.parseInt(args[1]);
            frames = Integer.parseInt(args[2]);
            maxIterations = Integer.parseInt(args[3]);
            
            firstScale = Integer.parseInt(args[4]);
            firstTranslateX = Integer.parseInt(args[5]);
            firstTranslateY = Integer.parseInt(args[6]);
            
            lastScale = Integer.parseInt(args[7]);
            lastTranslateX = Integer.parseInt(args[8]);
            lastTranslateY = Integer.parseInt(args[9]);

            fileName = args[10];
        }

        SeekableByteChannel ch = null;
        long start = System.currentTimeMillis();

        try {
            File f = new File(fileName);
            ch = NIOUtils.writableFileChannel(f);

            MP4Muxer muxer = new MP4Muxer(ch, Brand.MP4);
            FramesMP4MuxerTrack outTrack = muxer.addTrackForCompressed(TrackType.VIDEO, 25);
            ByteBuffer out = ByteBuffer.allocate(width * height * 6);

            H264Encoder encoder = new H264Encoder();

            ArrayList<ByteBuffer> spsList = new ArrayList<>();
            ArrayList<ByteBuffer> ppsList = new ArrayList<>();

            Picture toEncode = Picture.create(width, height, ColorSpace.YUV420);
            RgbToYuv420 transform = new RgbToYuv420(0, 0);

            double s = firstScale;
            double tx = firstTranslateX;
            double ty = firstTranslateY;

            double sFactor = MandelbrotAnimation.getFactor(firstScale, lastScale, frames);
            double txInc = MandelbrotAnimation.getIncrement(firstTranslateX, lastTranslateX, frames);
            double tyInc = MandelbrotAnimation.getIncrement(firstTranslateY, lastTranslateY, frames);

            for (int i = 0; i < frames; i++) {
                System.out.println("i = " + i + ", s=" + s + ", tx=" + tx + ", ty=" + ty);
                Mandelbrot m = new Mandelbrot(width, height, maxIterations, s, tx, ty);
                m.create();
                BufferedImage rgb = m.getImage();

                Picture pic = AWTUtil.fromBufferedImage(rgb);

                transform.transform(pic, toEncode);

                out.clear();
                ByteBuffer result = encoder.encodeFrame(out, toEncode);

                spsList.clear();
                ppsList.clear();
                H264Utils.encodeMOVPacket(result, spsList, ppsList);

                outTrack.addFrame(
                        new MP4Packet(
                                result,
                                i,
                                25,
                                1,
                                i,
                                true,
                                null,
                                i,
                                0));

                s *= sFactor;
                tx += txInc;
                ty += tyInc;
            }

            outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList));
            muxer.writeHeader();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (ch != null) {
                NIOUtils.closeQuietly(ch);
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
