package com.leon.mandelbrot.driver;

import org.apache.hadoop.util.ProgramDriver;

public class MandelbrotDriver {

    public static void main(String[] args) {
        int exitCode = -1;
        ProgramDriver p = new ProgramDriver();

        try {
            p.addClass("mapreduce", com.leon.mandelbrot.mapreduce.MapReduce.class, "Run a single MapReduce job.");
            p.addClass("server", com.leon.mandelbrot.server.Main.class, "Start the Queue Server.");
            p.addClass("animation", com.leon.mandelbrot.standard.MandelbrotAnimation.class, "Create a Mandelbrot animation.");
            p.addClass("image", com.leon.mandelbrot.standard.Mandelbrot.class, "Create a single Mandelbrot image.");

            exitCode = p.run(args);
        } catch(Throwable e) {
            e.printStackTrace();
        }

        System.exit(exitCode);
    }

}
