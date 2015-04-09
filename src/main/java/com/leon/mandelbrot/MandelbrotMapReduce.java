package com.leon.mandelbrot;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class MandelbrotMapReduce {

    protected long width;
    protected long height;
    protected long negativeXBorder;
    protected long positiveXBorder;
    protected long negativeYBorder;
    protected long positiveYBorder;


    public static class MandelbrotParts extends Mapper<LongWritable, LongWritable, LongWritable, LongWritable> {
        @Override
        protected void map(
                LongWritable rowNumber,
                LongWritable width,
                Context context)
                throws IOException, InterruptedException {
            for (long i = 0; i < width.get(); i++) {



            }


        }



        private static double pixelToCoordinate(int pixel, double leftBorder, double rightBorder, int pixels) {
            return ((rightBorder - leftBorder) * pixel / pixels) + leftBorder;
        }
    }

}
