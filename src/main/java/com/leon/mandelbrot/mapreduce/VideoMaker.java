package com.leon.mandelbrot.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class VideoMaker {

    private final File videoFile;

    public VideoMaker(String fileName) throws IOException  {
        this.videoFile = new File(fileName);
    }

    private static final Log LOG = LogFactory.getLog(MapReduce.class);

    /**
     * Creates a new video file without overriding the old one.
     * @param name The name of the video file.
     * @param suffix The suffix of the video file <b><i>without suffix</i></b> (e.g. mp4)
     * @return A File object to write the new video into.
     * @throws IOException If there are too many mandelbrot videos.
     */
    public File getVideoFile(String name, String suffix) throws IOException {
        File dir = new File("build/resources/main/mandelbrot_result/mapreduce");
        if (dir.mkdirs()) {
            LOG.info("Created directory mandelbrot_result/mapreduce");
        }
        File video = new File(dir, name + "." + suffix);
        int fileTries = 0;
        while (video.exists() && fileTries < 100) {
            video = new File(dir, "mandelbrot" + fileTries + ".mp4");
            fileTries++;
        }
        if (video.exists()) {
            throw new IOException("Too many mandelbrot videos!");
        }
        return video;
    }

    public void createVideo(int width, int height, int frames, Path outDir, Configuration conf) throws IOException {
        LOG.info("Starting to create video");
        double startTime = System.currentTimeMillis();

        File video = this.videoFile;
        LOG.info("Video saved in " + video.getName());

        SeekableByteChannel ch = NIOUtils.writableFileChannel(video);

        MP4Muxer muxer = new MP4Muxer(ch, Brand.MP4);
        FramesMP4MuxerTrack outTrack = muxer.addTrackForCompressed(TrackType.VIDEO, 25);
        ByteBuffer out = ByteBuffer.allocate(width * height * 6);

        H264Encoder encoder = new H264Encoder();

        ArrayList<ByteBuffer> spsList = new ArrayList<>();
        ArrayList<ByteBuffer> ppsList = new ArrayList<>();

        Picture toEncode = Picture.create(width, height, ColorSpace.YUV420);
        RgbToYuv420 transform = new RgbToYuv420(0, 0);

        LOG.info("Preparation done. Iterating through frames...");

        for (int i = 0; i < frames; i++) {
            Path inFile = new Path(outDir, String.format("part-r-%05d", i));
            IntWritable frame = new IntWritable();
            BytesWritable image = new BytesWritable();

            try (SequenceFile.Reader reader = new SequenceFile.Reader(
                    conf,
                    SequenceFile.Reader.file(inFile))) {
                int readerIteration = 0;
                while (reader.next(frame, image)) {
                    InputStream in = new ByteArrayInputStream(image.copyBytes());
                    BufferedImage rgb = ImageIO.read(in);
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
                                    0
                            )
                    );

                    readerIteration++;
                    if (readerIteration > 1) {
                        System.err.println("Somehow more than one frame came up in one sequence file...");
                        System.err.println("File: " + inFile.getName());
                    }
                }
            }
            if (i % 20 == 0) {
                LOG.info("Frame #" + i + " written into video.");
            }
        }

        outTrack.addSampleEntry(H264Utils.createMOVSampleEntry(spsList, ppsList));
        muxer.writeHeader();

        NIOUtils.closeQuietly(ch);

        double duration = (System.currentTimeMillis() - startTime) / 1000.0;
        LOG.info("Video creation finished in " + duration + " seconds");
    }
}
