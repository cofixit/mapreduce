package com.leon.mandelbrot.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.monte.media.Format;
import org.monte.media.FormatKeys;
import org.monte.media.avi.AVIWriter;
import org.monte.media.math.Rational;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import static org.monte.media.FormatKeys.EncodingKey;
import static org.monte.media.FormatKeys.FrameRateKey;
import static org.monte.media.FormatKeys.MediaTypeKey;
import static org.monte.media.VideoFormatKeys.*;

/**
 * A map/reduce program that creates an animation of the mandelbrot set.
 * Given input parameters:
 *  - width: The width of the animated image in pixels
 *  - height: The height of the animated image in pixels
 *  - frames: The amount of the frames
 *  - firstScale: The multiplier for both x and y coordinate axis.
 *           A scale of 1 translates to the x interval being [-0.5,0.5].
 *  - firstTranslateX: Move the coordinate center to the right/left
 *  - firstTranslateY: Move the coordinate center to the top/bottom
 *  - lastScale, lastTranslateX, lastTranslateY
 *
 *
 * Mapper:
 *      The mapper's task is to calculate the colors of the given image frame.
 *      The mapper receives a list of key-value pairs to calculate
 *      in which the key is a frame number and the value is a row
 *      number of the specified key.
 *      The map then creates a list of key-value pairs with the key being
 *      the frame number and the value being the row number and an array of
 *      calculated colors.
 * Reducer:
 *      The reducer collects the created list of key-value pairs by key and
 *      then patches together the image for each frame, creating a two-dimensional
 *      array.
 *      The cleanup task then writes the frame into an image file.
 * Final Task:
 *      The final task is to take the iamges generated by the reducer and
 *      create a gif out of them.
 */
public class MandelbrotMapReduce extends Configured implements Tool{

    private static final String TMP_DIR_PREFIX = MandelbrotMapReduce.class.getSimpleName();
    private static final Log LOG = LogFactory.getLog(MandelbrotMapReduce.class);

    /**
     * Run a map/reduce job and create an animation out of the generated images with given parameters.
     *
     * It will have the given width, height and amount of frames.
     * A scale of x means that the length of the x-Axis is 1/x.
     * The scale of the y-Axis is so that the image is not deformed.
     * The standard translation is so that the coordinate point of origin is in the center.
     * firstScale, firstTranslationX and firstTranslationY define the scale and translation of the first frame.
     * lastScale, lastTranslationX and lastTranslationY define the scale and translation of the last frame.
     *
     * @param width The animation width
     * @param height The animation height
     * @param frames The amount of frames
     * @param firstScale The scale of the first frame
     * @param firstTranslateX The x translation of the first frame
     * @param firstTranslateY The y translation of the first frame
     * @param lastScale The scale of the last frame
     * @param lastTranslateX The x translation of the last frame
     * @param lastTranslateY The y translation of the last frame
     * @param tmpDir A HDFS temporary directory to work in
     * @param conf The Hadoop configuration.
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void createMandelbrotAnimation(
            int nMaps,
            int width,
            int height,
            int frames,
            int maxIterations,
            double firstScale,
            double firstTranslateX,
            double firstTranslateY,
            double lastScale,
            double lastTranslateX,
            double lastTranslateY,
            Path tmpDir,
            Configuration conf
    ) throws IOException, ClassNotFoundException, InterruptedException {
        // set the configuration parameters
        conf.setInt(MandelbrotProperties.WIDTH, width);
        conf.setInt(MandelbrotProperties.HEIGHT, height);
        conf.setInt(MandelbrotProperties.FRAMES, frames);
        conf.setInt(MandelbrotProperties.MAX_ITERATIONS, maxIterations);
        conf.setDouble(MandelbrotProperties.FIRST_SCALE, firstScale);
        conf.setDouble(MandelbrotProperties.FIRST_TRANSLATE_X, firstTranslateX);
        conf.setDouble(MandelbrotProperties.FIRST_TRANSLATE_Y, firstTranslateY);
        conf.setDouble(MandelbrotProperties.LAST_SCALE, lastScale);
        conf.setDouble(MandelbrotProperties.LAST_TRANSLATE_X, lastTranslateX);
        conf.setDouble(MandelbrotProperties.LAST_TRANSLATE_Y, lastTranslateY);
        // run the actual function (as the parameters are now saved in the configuration)
        createMandelbrotAnimation(nMaps, tmpDir, conf);
    }

    /**
     * Run a map/reduce job and create an animation out of the generated images with the standard parameters.
     *
     * @param tmpDir A temporary directory to work in
     * @param conf The Hadoop configuration
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InterruptedException
     */
    public static void createMandelbrotAnimation(
            int nMaps,
            Path tmpDir,
            Configuration conf
    ) throws IOException, ClassNotFoundException, InterruptedException {

        // fetch some needed configuration parameters, or their default
        int width = conf.getInt(MandelbrotProperties.WIDTH, MandelbrotProperties.STANDARD_WIDTH);
        int height = conf.getInt(MandelbrotProperties.HEIGHT, MandelbrotProperties.STANDARD_HEIGHT);
        int frames = conf.getInt(MandelbrotProperties.FRAMES, MandelbrotProperties.STANDARD_FRAMES);

        LOG.info("Starting Calculation of Mandelbrot Animation");
        LOG.info("Number of Maps                " + nMaps);
        LOG.info("Width:                        " + width);
        LOG.info("Height:                       " + height);
        LOG.info("Frames:                       " + frames);
        LOG.info("Maximum Iterations:           " + conf.getInt(MandelbrotProperties.MAX_ITERATIONS, MandelbrotProperties.STANDARD_MAX_ITERATIONS));
        LOG.info("Scale of First Frame:         " + conf.getDouble(MandelbrotProperties.FIRST_SCALE, MandelbrotProperties.STANDARD_FIRST_SCALE));
        LOG.info("x-Translation of First Frame: " + conf.getDouble(MandelbrotProperties.FIRST_TRANSLATE_X, MandelbrotProperties.STANDARD_FIRST_TRANSLATE_X));
        LOG.info("y-Translation of First Frame: " + conf.getDouble(MandelbrotProperties.FIRST_TRANSLATE_Y, MandelbrotProperties.STANDARD_FIRST_TRANSLATE_Y));
        LOG.info("Scale of Last Frame:          " + conf.getDouble(MandelbrotProperties.LAST_SCALE, MandelbrotProperties.STANDARD_LAST_SCALE));
        LOG.info("x-Translation of Last Frame:  " + conf.getDouble(MandelbrotProperties.LAST_TRANSLATE_X, MandelbrotProperties.STANDARD_LAST_TRANSLATE_X));
        LOG.info("y-Translation of Last Frame:  " + conf.getDouble(MandelbrotProperties.LAST_TRANSLATE_Y, MandelbrotProperties.STANDARD_LAST_TRANSLATE_Y));

        Job job = Job.getInstance(conf);

        // Set up Job configuration
        job.setJobName(MandelbrotMapReduce.class.getSimpleName());
        job.setJarByClass(MandelbrotMapReduce.class);

        job.setInputFormatClass(SequenceFileInputFormat.class);

        job.setOutputKeyClass(IntWritable.class);
        job.setOutputValueClass(BytesWritable.class);

        job.setMapOutputKeyClass(IntWritable.class);
        job.setMapOutputValueClass(KeyValueWritable.class);

        job.setOutputFormatClass(SequenceFileOutputFormat.class);

        job.setMapperClass(MandelbrotMapper.class);

        job.setReducerClass(ImageReducer.class);
        job.setNumReduceTasks(frames);

        final Path inDir = new Path(tmpDir, "in");
        final Path outDir = new Path(tmpDir, "out");
        final Path resultDir = new Path("mandelbrot_result");
        FileInputFormat.setInputPaths(job, inDir);
        FileOutputFormat.setOutputPath(job, outDir);
        
        final FileSystem fs = FileSystem.get(conf);
        if (fs.exists(tmpDir)) {
            throw new IOException("Tmp directory " + fs.makeQualified(tmpDir)
                + " already exists. Please remove it first.");   
        }
        if (!fs.mkdirs(inDir)) {
            throw new IOException("Cannot create input directory " + inDir);
        }

        try {
            long completeRows = (long)frames * (long)height;
            long rowsPerMapper = completeRows / (long)nMaps;
            long unassignedRows = completeRows % nMaps;
            // If the amount of unassigned rows is more than half of the rows per mapper,
            // add another mapper.
            boolean useExtraMapper = false;
            if (unassignedRows != 0) {
                useExtraMapper = (rowsPerMapper / unassignedRows) < 2;
            }
            if (useExtraMapper) {
                nMaps++;
            }

            for (int i = 0; i < nMaps; i++) {
                final Path file = new Path(inDir, "part" + i);
                final LongWritable offset = new LongWritable(i*rowsPerMapper);
                final LongWritable size = new LongWritable(rowsPerMapper);
                if (i == nMaps - 1) {
                    if (useExtraMapper) {
                        size.set(unassignedRows);
                    } else {
                        size.set(unassignedRows +rowsPerMapper);
                    }
                }
                try (SequenceFile.Writer writer = SequenceFile.createWriter(
                        conf,
                        SequenceFile.Writer.file(file),
                        SequenceFile.Writer.keyClass(LongWritable.class),
                        SequenceFile.Writer.valueClass(LongWritable.class),
                        SequenceFile.Writer.compression(SequenceFile.CompressionType.NONE)
                )) {
                    writer.append(offset, size);
                }
            }

            // start a map/reduce job
            LOG.info("Starting MapReduce Job");
            long startTime = System.currentTimeMillis();
            job.waitForCompletion(true);
            double duration = (System.currentTimeMillis() - startTime) / 1000.0;
            LOG.info("MapReduce Job Finished in " + duration + " seconds");

            LOG.info("Starting to create video");
            startTime = System.currentTimeMillis();
            // read outputs and write them into a video
            AVIWriter out = null;
            Format format = new Format(
                    EncodingKey, ENCODING_AVI_PNG,
                    DepthKey, 24,
                    MediaTypeKey, FormatKeys.MediaType.VIDEO,
                    FrameRateKey, new Rational(30, 1),
                    WidthKey, width,
                    HeightKey, height
            );
            try {
                // set up video
                Path video = new Path(resultDir, "mandelbrot.avi");
                int pathTries = 0;
                while (fs.exists(video) && pathTries < 100) {
                    video = new Path(resultDir, "mandelbrot" + pathTries + ".avi");
                    pathTries++;
                }
                if (fs.exists(video)) {
                    throw new IOException("Too many mandelbrot videos!");
                }
                LOG.info("Video is saved as " + video.getName());
                OutputStream os = video.getFileSystem(conf).create(video);
                ImageOutputStream ios = ImageIO.createImageOutputStream(os);
                out = new AVIWriter(ios);
                out.addTrack(format);
                out.setPalette(0, ColorModel.getRGBdefault());

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
                            BufferedImage img = ImageIO.read(in);
                            out.write(0, img, 1);
                            readerIteration++;
                            if (readerIteration > 1) {
                                System.err.println("Somehow more than one frame came up in one sequence file...");
                                System.err.println("File: " + inFile.getName());
                            }
                        }
                    }
                    if (i%1000 == 0) {
                        LOG.info(i/10 + "% done.");
                    }
                }
            } finally {
                if (out != null) {
                    out.close();
                }
                duration = (System.currentTimeMillis() - startTime) / 1000.0;
                LOG.info("Video creation finished in " + duration + " seconds");
            }
        } finally {
            fs.delete(tmpDir, true);
            LOG.info("Finished Calculation of Mandelbrot Animation");
        }
    }

    @Override
    public int run(String[] args) throws Exception {
        if (args.length != 1 && args.length != 11) {
            System.err.println("Usage: "
                    + getClass().getName()
                    + " <nMaps> [<width> <height> <frames> <maxIterations>" +
                    "<firstScale> <firstTranslateX> <firstTranslateY> " +
                    "<lastScale> <lsatTranslateX> <latTranslateY>]");
            ToolRunner.printGenericCommandUsage(System.err);
            return 2;
        }

        final int nMaps = Integer.parseInt(args[0]);
        final int width;
        final int height;
        final int frames;
        final int maxIterations;

        final double firstScale;
        final double firstTranslateX;
        final double firstTranslateY;

        final double lastScale;
        final double lastTranslateX;
        final double lastTranslateY;

        if (args.length == 11) {
            width = Integer.parseInt(args[1]);
            height = Integer.parseInt(args[2]);
            frames = Integer.parseInt(args[3]);
            maxIterations = Integer.parseInt(args[4]);

            firstScale = Double.parseDouble(args[5]);
            firstTranslateX = Double.parseDouble(args[6]);
            firstTranslateY = Double.parseDouble(args[7]);

            lastScale = Double.parseDouble(args[8]);
            lastTranslateX = Double.parseDouble(args[9]);
            lastTranslateY = Double.parseDouble(args[10]);
        } else {
            width = MandelbrotProperties.STANDARD_WIDTH;
            height = MandelbrotProperties.STANDARD_HEIGHT;
            frames = MandelbrotProperties.STANDARD_FRAMES;
            maxIterations = MandelbrotProperties.STANDARD_MAX_ITERATIONS;

            firstScale = MandelbrotProperties.STANDARD_FIRST_SCALE;
            firstTranslateX = MandelbrotProperties.STANDARD_FIRST_TRANSLATE_X;
            firstTranslateY = MandelbrotProperties.STANDARD_FIRST_TRANSLATE_Y;
            lastScale = MandelbrotProperties.STANDARD_LAST_SCALE;
            lastTranslateX = MandelbrotProperties.STANDARD_LAST_TRANSLATE_X;
            lastTranslateY = MandelbrotProperties.STANDARD_LAST_TRANSLATE_Y;
        }

        long now = System.currentTimeMillis();
        int rand = new Random().nextInt(Integer.MAX_VALUE);
        final Path tmpDir = new Path(TMP_DIR_PREFIX + "_" + now + "_" + rand);

        if (args.length == 1) {
            createMandelbrotAnimation(nMaps, tmpDir, getConf());
        } else {
            createMandelbrotAnimation(
                    nMaps, width, height, frames, maxIterations,
                    firstScale, firstTranslateX, firstTranslateY,
                    lastScale, lastTranslateX, lastTranslateY,
                    tmpDir, getConf());
        }
        return 0;
    }

    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(null, new MandelbrotMapReduce(), args));
    }

}
