package com.leon.mandelbrot.server;

import com.leon.mandelbrot.mapreduce.MapReduce;
import com.leon.mandelbrot.mapreduce.MapReduceMocker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.ToolRunner;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Job implements Runnable {

    enum Status {
        WAITING ("waiting"),
        RUNNING ("running"),
        FAILED  ("failed"),
        DONE    ("done");

        private String value;

        Status(String value) {
            this.value = value;
        }

        @Override
        public String toString() {
            return this.value;
        }
    }

    private static final String N_MAPS = "12";
    private static final Log LOG = LogFactory.getLog(Job.class);

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
    
    private Date created;
    private Date started;
    private Date finished;

    private Status status;

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
        this.created = new Date();
        this.status = Status.WAITING;
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
        LOG.info("Job #" + id + " " + status);
    }

    public String getStatus() {
        return status.value;
    }

    public void setStatus(Status status) {
        this.status = status;
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
        status = Status.RUNNING;
        LOG.info("Job #" + id + " " + status);
        started = new Date();

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
            if (result == 0) {
                status = Status.DONE;
            } else {
                status = Status.FAILED;
            }
            finished = new Date();
            LOG.info("Job #" + id + " " + status);
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        return "{" +
                "\"id\": " + id + ", " +
                "\"status\": \"" + status + "\", " +
                "\"created\": \"" + (created == null ? "" : sdf.format(created)) + "\", " +
                "\"started\": \"" + (started == null ? "" : sdf.format(started)) + "\", " +
                "\"finished\": \"" + (finished == null ? "" : sdf.format(finished)) + "\", " +
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
