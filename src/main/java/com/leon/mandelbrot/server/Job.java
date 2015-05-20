package com.leon.mandelbrot.server;

import com.leon.mandelbrot.mapreduce.MapReduce;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.util.ToolRunner;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Job implements Runnable {

    enum Status {
        WAITING ("waiting"),
        RUNNING ("running"),
        FAILED  ("failed"),
        DONE    ("done");

        private final String value;

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
    private static final String FILE_PREFIX = "build/resources/main/result/job";
    private static final String FILE_SUFFIX = ".mp4";

    private final String width;
    private final String height;
    private final String frames;
    private final String maxIterations;

    private final String firstScale;
    private final String firstTranslateX;
    private final String firstTranslateY;
    private final String lastScale;
    private final String lastTranslateX;
    private final String lastTranslateY;
    
    private final Date created;
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
                lastTranslateY,
                FILE_PREFIX + id + FILE_SUFFIX
        };

        int result = 1;

        try {
            result = ToolRunner.run(null, new MapReduce(), args);
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
                "\"created\": \"" + (sdf.format(created)) + "\", " +
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
