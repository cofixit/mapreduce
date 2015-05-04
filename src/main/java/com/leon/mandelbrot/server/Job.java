package com.leon.mandelbrot.server;

import com.leon.mandelbrot.mapreduce.MapReduce;
import com.leon.mandelbrot.mapreduce.MapReduceMocker;
import org.apache.hadoop.util.ToolRunner;

import java.util.Date;

public class Job implements Runnable {

    private static final String N_MAPS = "12";

    private String width;
    private String height;
    private String frames;
    private String maxIterations;

    private String firstScale;
    private String firstTranslateX;
    private String firstTranslateY;
    private String lastScale;
    private String lastTranslateX;
    private String lastTranslateY;
    
    private Date started;
    private Date finished;
    
    private boolean done;
    private boolean success;
    private int id;

    public Job(String width,
               String height,
               String frames,
               String maxIterations,
               String firstScale,
               String firstTranslateX,
               String firstTranslateY,
               String lastScale,
               String lastTranslateX,
               String lastTranslateY) {
        this.started = new Date();
        this.done = false;
        this.width = width;
        this.height = height;
        this.frames = frames;
        this.maxIterations = maxIterations;
        this.firstScale = firstScale;
        this.firstTranslateX = firstTranslateX;
        this.firstTranslateY = firstTranslateY;
        this.lastScale = lastScale;
        this.lastTranslateX = lastTranslateX;
        this.lastTranslateY = lastTranslateY;
    }

    public boolean getDone() {
        return done;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getFrames() {
        return frames;
    }

    public void setFrames(String frames) {
        this.frames = frames;
    }

    public String getMaxIterations() {
        return maxIterations;
    }

    public void setMaxIterations(String maxIterations) {
        this.maxIterations = maxIterations;
    }

    public String getFirstScale() {
        return firstScale;
    }

    public void setFirstScale(String firstScale) {
        this.firstScale = firstScale;
    }

    public String getFirstTranslateX() {
        return firstTranslateX;
    }

    public void setFirstTranslateX(String firstTranslateX) {
        this.firstTranslateX = firstTranslateX;
    }

    public String getFirstTranslateY() {
        return firstTranslateY;
    }

    public void setFirstTranslateY(String firstTranslateY) {
        this.firstTranslateY = firstTranslateY;
    }

    public String getLastScale() {
        return lastScale;
    }

    public void setLastScale(String lastScale) {
        this.lastScale = lastScale;
    }

    public String getLastTranslateX() {
        return lastTranslateX;
    }

    public void setLastTranslateX(String lastTranslateX) {
        this.lastTranslateX = lastTranslateX;
    }

    public String getLastTranslateY() {
        return lastTranslateY;
    }

    public void setLastTranslateY(String lastTranslateY) {
        this.lastTranslateY = lastTranslateY;
    }

    @Override
    public void run() {
        String[] args = new String[] {
                N_MAPS,
                width,
                height,
                frames,
                maxIterations,
                firstScale,
                firstTranslateX,
                firstTranslateY,
                lastScale,
                lastTranslateX,
                lastTranslateY
        };

        int result = 1;

        try {
            result = ToolRunner.run(null, new MapReduceMocker(), args);
        } catch(Exception e) {
            e.printStackTrace();
        } finally {
            success = result == 0;
            done = true;
            finished = new Date();
        }
    }

    @Override
    public String toString() {
        return "{" +
                "\"id\": " + id + ", " +
                "\"done\": " + done + ", " +
                "\"success\": " + success + ", " +
                "\"started\": \"" + started + "\", " +
                "\"finished\": \"" + finished + "\", " +
                "\"width\": " + width + ", " +
                "\"height\": " + height + ", " +
                "\"frames\": " + frames + ", " +
                "\"maxIterations\": " + maxIterations + ", " +
                "\"firstScale\": " + firstScale + ", " +
                "\"firstTranslateX\": " + firstTranslateX + ", " +
                "\"firstTranslateY\": " + firstTranslateY + ", " +
                "\"lastScale\": " + lastScale + ", " +
                "\"lastTranslateX\": " + lastTranslateX + ", " +
                "\"lastTranslateY\": " + lastTranslateY +
                "}";
    }

    public void setId(int id) {
        this.id = id;
    }
}
