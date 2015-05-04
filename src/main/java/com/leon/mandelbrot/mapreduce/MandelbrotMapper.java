package com.leon.mandelbrot.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

/**
 * This is the Mapper class. Its task is to calculate the colors of the pixels of a mandelbrot picture and save them
 * in a list of Key/Value pairs. The Structure is &lt;frame: &lt;row: pixels&gt;&gt;.
 * For further information take a look at the following method: <pre>MandelbrotMapper.map()</pre>
 */
public class MandelbrotMapper
        extends Mapper<LongWritable, LongWritable, IntWritable, KeyValueWritable> {

    private static final Log LOG = LogFactory.getLog(MapReduce.class);

    private int height;
    private int width;
    private int frames;

    private double firstScale;
    private double firstTranslateX;
    private double firstTranslateY;
    private double lastScale;
    private double lastTranslateX;
    private double lastTranslateY;

    private int maxIterations;

    private HashMap<Integer, Double> scales;
    private HashMap<Integer, Double> xTranslations;
    private HashMap<Integer, Double> yTranslations;

    /**
     * Get the configuration parameters for calculating the video frames. Also set up the three hash maps.
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration configuration = context.getConfiguration();
        this.height = configuration.getInt(MandelbrotProperties.HEIGHT, MandelbrotProperties.STANDARD_HEIGHT);
        this.width = configuration.getInt(MandelbrotProperties.WIDTH, MandelbrotProperties.STANDARD_WIDTH);
        this.frames = configuration.getInt(MandelbrotProperties.FRAMES, MandelbrotProperties.STANDARD_FRAMES);
        this.maxIterations = configuration.getInt(MandelbrotProperties.MAX_ITERATIONS, MandelbrotProperties.STANDARD_MAX_ITERATIONS);

        this.firstScale = configuration.getDouble(MandelbrotProperties.FIRST_SCALE, MandelbrotProperties.STANDARD_FIRST_SCALE);
        this.firstTranslateX = configuration.getDouble(MandelbrotProperties.FIRST_TRANSLATE_X, MandelbrotProperties.STANDARD_FIRST_TRANSLATE_X);
        this.firstTranslateY = configuration.getDouble(MandelbrotProperties.FIRST_TRANSLATE_Y, MandelbrotProperties.STANDARD_FIRST_TRANSLATE_Y);
        this.lastScale = configuration.getDouble(MandelbrotProperties.LAST_SCALE, MandelbrotProperties.STANDARD_LAST_SCALE);
        this.lastTranslateX = configuration.getDouble(MandelbrotProperties.LAST_TRANSLATE_X, MandelbrotProperties.STANDARD_LAST_TRANSLATE_X);
        this.lastTranslateY = configuration.getDouble(MandelbrotProperties.LAST_TRANSLATE_Y, MandelbrotProperties.STANDARD_LAST_TRANSLATE_Y);

        this.scales = new HashMap<>();
        this.xTranslations = new HashMap<>();
        this.yTranslations = new HashMap<>();
    }

    /**
     * Calculates the given rows that are to be calculated.
     * @param offset The offset in frames*rows.
     * @param rows The amount of rows to calculate in frames*rows.
     * @param context The context.
     * @throws IOException
     * @throws InterruptedException
     */
    @Override
    protected void map(LongWritable offset,
                       LongWritable rows,
                       Context context)
            throws IOException, InterruptedException {
        LOG.info("Calculating combined rows " + offset.get() + " to " + (offset.get()+rows.get()));
        long statusSize = rows.get() / 100;
        int percentageIterator = 0;
        for (long i = 0; i < rows.get(); i++) {
            long combinedRow = offset.get() + i;
            long longFrame = combinedRow / this.height;
            long longRow = combinedRow % this.height;
            IntWritable frame = new IntWritable((int)longFrame);
            IntWritable row = new IntWritable((int)longRow);
            calculateRow(frame, row, context);
            if (i > 0L && i%statusSize == 0) {
                context.setStatus(++percentageIterator + "% done. ");
                context.progress();
            }
        }
    }

    protected void calculateRow(IntWritable frame,
                                IntWritable row,
                                Context context)
            throws IOException, InterruptedException {
        // calculate the coordinates for the pixels that shall be rendered
        int theFrame = frame.get();
        double relativeFrame = (double) theFrame / (double) this.frames;

        // save the scale / tx / ty in a hash map
        Double scaleX = this.scales.get(theFrame);
        if (scaleX == null) {
            scaleX = this.firstScale * Math.pow(
                    Math.pow(
                        10,
                        Math.log10(this.lastScale / this.firstScale) / (this.frames - 1)
                    ),
                    theFrame
            );
            this.scales.put(theFrame, scaleX);
        }

        Double translateX = this.xTranslations.get(theFrame);
        if (translateX == null) {
            translateX = this.firstTranslateX + (this.lastTranslateX - this.firstTranslateX) * relativeFrame;
            this.xTranslations.put(theFrame, translateX);
        }

        Double translateY = this.yTranslations.get(theFrame);
        if (translateY == null) {
            translateY = this.firstTranslateY + (this.lastTranslateY - this.firstTranslateY) * relativeFrame;
            this.yTranslations.put(theFrame, translateY);
        }

        double scaleY = scaleX * this.height / this.width;

        // calculate the y coordinate for the corresponding row
        double y0 = scaleY/2 - (row.get()*scaleY)/this.height + translateY;

        // iterate through the pixels of the row
        IntWritable[] imgRow = new IntWritable[this.width];
        for (int i = 0; i < imgRow.length; i++) {
            // calculate the x coordinate of this pixel
            double x0 = (i * scaleX)/this.width - scaleX/2 + translateX;

            // run the escape algorithm.
            // check for how much iterations x+iy is in the mandelbrot set
            int iterations = 0;
            double x = 0.0;
            double y = 0.0;
            double xTemp;
            double yTemp;

            while (x*x + y*y < 4.0 && iterations < this.maxIterations) {
                xTemp = x*x - y*y + x0;
                yTemp = 2.0*x*y + y0;
                if (x == xTemp && y == yTemp) {
                    iterations = this.maxIterations;
                } else {
                    x = xTemp;
                    y = yTemp;
                    iterations++;
                }
            }

            // calculate the color of the pixel according to its iterations.
            if (iterations >= this.maxIterations) {
                imgRow[i] = new IntWritable(new Color(0).getRGB());
            } else {
                float hue = iterations / (float) this.maxIterations;
                imgRow[i] = new IntWritable(Color.getHSBColor(hue, 1.0f, 1.0f).getRGB());
            }
        }

        // Done. Write calculated pixels into the key-value pair for the reduce job.
        // Data structure: <frame; Map<row; pixels>>
        ArrayWritable a = new ArrayWritable(IntWritable.class, imgRow);
        context.write(frame, new KeyValueWritable(row, a));
    }
}
