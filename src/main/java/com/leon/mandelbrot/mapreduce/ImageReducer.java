package com.leon.mandelbrot.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Leon on 12.04.15.
 */
public class ImageReducer extends
        Reducer<IntWritable, KeyValueWritable<IntWritable, ArrayWritable>, IntWritable, BytesWritable> {

    private int height;
    private int width;
    private int frames;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration configuration = context.getConfiguration();
        this.width = configuration.getInt(MandelbrotProperties.WIDTH, MandelbrotProperties.STANDARD_WIDTH);
        this.height = configuration.getInt(MandelbrotProperties.HEIGHT, MandelbrotProperties.STANDARD_HEIGHT);
        this.frames = configuration.getInt(MandelbrotProperties.FRAMES, MandelbrotProperties.STANDARD_FRAMES);
    }

    @Override
    protected void reduce(IntWritable frame,
                          Iterable<KeyValueWritable<IntWritable, ArrayWritable>> rows,
                          Context context)
            throws IOException, InterruptedException {

        // write the rows sent from the map job into a picture
        BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
        for (KeyValueWritable<IntWritable, ArrayWritable> element : rows) {
            int row = element.getKey().get();
            Writable[] writableColors = element.getValue().get();
            int[] colors = Arrays.copyOf(writableColors, writableColors.length, int[].class);
            for (int i = 0; i < colors.length; i++) {
                img.setRGB(i, row, colors[i]);
            }
        }

        // create png file out of the image and save it as byte array.
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        byte[] bytes = baos.toByteArray();

        // write byte array together with frames into context
        context.write(frame, new BytesWritable(bytes));
    }
}
