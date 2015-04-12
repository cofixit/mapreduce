package com.leon.mandelbrot.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.awt.*;
import java.io.IOException;

public class MandelbrotMapper
        extends Mapper<IntWritable, IntWritable, IntWritable, KeyValueWritable<IntWritable, ArrayWritable>> {

    private int height;
    private int width;
    private int frames;
    private double firstLeftXBorder;
    private double firstRightXBorder;
    private double lastLeftXBorder;
    private double lastRightXBorder;
    private double firstTopYBorder;
    private double firstBottomYBorder;
    private double lastTopYBorder;
    private double lastBottomYBorder;
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
        this.firstLeftXBorder = configuration.getDouble(MandelbrotProperties.FIRST_LEFT_X_BORDER, MandelbrotProperties.STANDARD_FIRST_LEFT_X_BORDER);
        this.firstRightXBorder = configuration.getDouble(MandelbrotProperties.FIRST_RIGHT_X_BORDER, MandelbrotProperties.STANDARD_FIRST_RIGHT_X_BORDER);
        this.lastLeftXBorder = configuration.getDouble(MandelbrotProperties.LAST_LEFT_X_BORDER, MandelbrotProperties.STANDARD_LAST_LEFT_X_BORDER);
        this.lastRightXBorder = configuration.getDouble(MandelbrotProperties.LAST_RIGHT_X_BORDER, MandelbrotProperties.STANDARD_LAST_RIGHT_X_BORDER);
        this.firstTopYBorder = configuration.getDouble(MandelbrotProperties.FIRST_TOP_Y_BORDER, MandelbrotProperties.STANDARD_FIRST_TOP_Y_BORDER);
        this.firstBottomYBorder = configuration.getDouble(MandelbrotProperties.FIRST_BOTTOM_Y_BORDER, MandelbrotProperties.STANDARD_FIRST_BOTTOM_Y_BORDER);
        this.lastTopYBorder = configuration.getDouble(MandelbrotProperties.LAST_TOP_Y_BORDER, MandelbrotProperties.STANDARD_LAST_TOP_Y_BORDER);
        this.lastBottomYBorder = configuration.getDouble(MandelbrotProperties.LAST_BOTTOM_Y_BORDER, MandelbrotProperties.STANDARD_LAST_BOTTOM_Y_BORDER);
    }

    @Override
    protected void map(IntWritable frame,
                       IntWritable row,
                       Context context)
            throws IOException, InterruptedException {
        // calculate the coordinates for the pixels that shall be rendered
        double relativeFrame = (double) frame.get() / (double) this.frames;
        double xLeft = this.firstLeftXBorder + (this.lastLeftXBorder - this.firstLeftXBorder) * relativeFrame;
        double xRight = this.firstRightXBorder + (this.lastRightXBorder - this.firstRightXBorder) * relativeFrame;
        double yTop = this.firstTopYBorder + (this.lastTopYBorder - this.firstTopYBorder) * relativeFrame;
        double yBottom = this.firstBottomYBorder + (this.lastBottomYBorder - this.firstBottomYBorder) * relativeFrame;

        // calculate the y coordinate for the corresponding row
        double y0 = ((yBottom - yTop) * row.get() / this.height) + yTop;

        // iterate through the pixels of the row
        IntWritable[] imgRow = new IntWritable[this.width];
        for (int i = 0; i < imgRow.length; i++) {
            // calculate the x coordinate of this pixel
            double x0 = ((xRight - xLeft) * i / this.width) + xLeft;

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
