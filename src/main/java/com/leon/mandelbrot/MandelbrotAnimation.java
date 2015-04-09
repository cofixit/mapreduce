package com.leon.mandelbrot;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.IOException;

/**
 * A map/reduce program that creates an animation of the mandelbrot set.
 * Given input parameters:
 *  - width: The width of the animated image in pixels
 *  - height: The height of the animated image in pixels
 *  - frames: The amount of the frames
 *  - left X border at beginning
 *  - right X border at beginning
 *  - left X border at end
 *  - right X border at end
 *  - left Y border at beginning
 *  - right Y border at beginning
 *  - left Y border at end
 *  - right Y border at end
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
public class MandelbrotAnimation extends Configured implements Tool {
    private static final String TMP_DIR_PREFIX = MandelbrotAnimation.class.getSimpleName();
    public static final String IMAGE_HEIGHT = "IMAGE_HEIGHT";
    public static final String IMAGE_WIDTH = "IMAGE_WIDTH";
    public static final String IMAGE_FRAMES = "IMAGE_FRAMES";
    public static final String IMAGE_FIRST_LEFT_X_BORDER = "IMAGE_FIRST_LEFT_X_BORDER";
    public static final String IMAGE_FIRST_RIGHT_X_BORDER = "IMAGE_FIRST_RIGHT_X_BORDER";
    public static final String IMAGE_LAST_LEFT_X_BORDER = "IMAGE_LAST_LEFT_X_BORDER";
    public static final String IMAGE_LAST_RIGHT_X_BORDER = "IMAGE_LAST_RIGHT_X_BORDER";
    public static final String IMAGE_FIRST_TOP_Y_BORDER = "IMAGE_FIRST_TOP_Y_BORDER";
    public static final String IMAGE_FIRST_BOTTOM_Y_BORDER = "IMAGE_FIRST_BOTTOM_Y_BORDER";
    public static final String IMAGE_LAST_TOP_Y_BORDER = "IMAGE_LAST_TOP_Y_BORDER";
    public static final String IMAGE_LAST_BOTTOM_Y_BORDER = "IMAGE_LAST_BOTTOM_Y_BORDER";

    public static class MandelbrotMapper extends
            Mapper<IntWritable, IntWritable, IntWritable, MapWritable> {

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

        /**
         * Get the configuration parameters for calculating the images:
         * @param context
         * @throws IOException
         * @throws InterruptedException
         */
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Configuration configuration = context.getConfiguration();
            this.height = configuration.getInt(MandelbrotAnimation.IMAGE_HEIGHT, 0);
            this.width = configuration.getInt(MandelbrotAnimation.IMAGE_WIDTH, 0);
            this.frames = configuration.getInt(MandelbrotAnimation.IMAGE_FRAMES, 0);
            this.firstLeftXBorder = configuration.getDouble(MandelbrotAnimation.IMAGE_FIRST_LEFT_X_BORDER, 0);
            this.firstRightXBorder = configuration.getDouble(MandelbrotAnimation.IMAGE_FIRST_RIGHT_X_BORDER, 0);
            this.lastLeftXBorder = configuration.getDouble(MandelbrotAnimation.IMAGE_LAST_LEFT_X_BORDER, 0);
            this.lastRightXBorder = configuration.getDouble(MandelbrotAnimation.IMAGE_LAST_RIGHT_X_BORDER, 0);
            this.firstTopYBorder = configuration.getDouble(MandelbrotAnimation.IMAGE_FIRST_TOP_Y_BORDER, 0);
            this.firstBottomYBorder = configuration.getDouble(MandelbrotAnimation.IMAGE_FIRST_BOTTOM_Y_BORDER, 0);
            this.lastTopYBorder = configuration.getDouble(MandelbrotAnimation.IMAGE_LAST_TOP_Y_BORDER, 0);
            this.lastBottomYBorder = configuration.getDouble(MandelbrotAnimation.IMAGE_LAST_BOTTOM_Y_BORDER, 0);

        }

        @Override
        protected void map(IntWritable frame,
                           IntWritable row,
                           Context context)
                throws IOException, InterruptedException {
            // get the main configuration of the job

        }
    }

    public static class ImageReducer extends
            Reducer<IntWritable, MapWritable, IntWritable, BytesWritable> {
        @Override
        protected void reduce(IntWritable key,
                              Iterable<MapWritable> values,
                              Context context)
                throws IOException, InterruptedException {

        }
    }

    /**
     * Parses arguments and then runs a map/reduce job.
     * Prints output into standard out.
     *
     * @return 0 if successful, non-zero value if there is an error.
     */
    @Override
    public int run(String[] args) throws Exception {
        return 0;
    }

    /**
     * Main method for running it as a stand alone command.
     */
    public static void main(String[] args) throws Exception {
        System.exit(ToolRunner.run(null, new MandelbrotAnimation(), args));
    }
}