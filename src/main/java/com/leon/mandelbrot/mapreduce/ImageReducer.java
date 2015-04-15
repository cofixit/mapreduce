package com.leon.mandelbrot.mapreduce;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ImageReducer extends
        Reducer<IntWritable, KeyValueWritable, IntWritable, BytesWritable> {

    private int height;
    private int width;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        Configuration configuration = context.getConfiguration();
        this.width = configuration.getInt(MandelbrotProperties.WIDTH, MandelbrotProperties.STANDARD_WIDTH);
        this.height = configuration.getInt(MandelbrotProperties.HEIGHT, MandelbrotProperties.STANDARD_HEIGHT);
    }

    @Override
    protected void reduce(IntWritable frame,
                          Iterable<KeyValueWritable> rows,
                          Context context)
            throws IOException, InterruptedException {

        // write the rows sent from the map job into a picture
        BufferedImage img = new BufferedImage(this.width, this.height, BufferedImage.TYPE_INT_RGB);
        for (KeyValueWritable element : rows) {
            int row = element.getKey().get();
            Writable[] colorWritables = element.getValue().get();
            for (int i = 0; i < colorWritables.length; i++) {
                IntWritable color = (IntWritable) colorWritables[i];
                img.setRGB(i, row, color.get());
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
