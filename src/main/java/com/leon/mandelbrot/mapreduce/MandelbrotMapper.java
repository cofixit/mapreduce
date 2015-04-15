package com.leon.mandelbrot.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.awt.*;
import java.io.IOException;

public class MandelbrotMapper
        extends Mapper<IntWritable, IntWritable, IntWritable, KeyValueWritable<IntWritable, ArrayWritable>> {

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

    /**
     * Get the configuration parameters for calculating the images:
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
    }

    @Override
    protected void map(IntWritable frame,
                       IntWritable row,
                       Context context)
            throws IOException, InterruptedException {
        // calculate the coordinates for the pixels that shall be rendered
        double relativeFrame = (double) frame.get() / (double) this.frames;
        double scaleX = this.firstScale + (this.lastScale - this.firstScale) * relativeFrame;
        double scaleY = scaleX * this.height / this.width;
        double translateX = this.firstTranslateX + (this.lastTranslateX - this.firstTranslateX) * relativeFrame;
        double translateY = this.firstTranslateY + (this.lastTranslateY - this.firstTranslateY) * relativeFrame;

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
                    i = this.maxIterations;
                } else {
                    x = xTemp;
                    y = yTemp;
                    i++;
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
        context.write(frame, new KeyValueWritable<>(row, a));
    }
}
