package com.leon.mandelbrot.server;

import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class JobQueue extends Vector<Job> {
    private static JobQueue ourInstance = new JobQueue();

    private int index;
    private final ExecutorService executorService;

    public static JobQueue getInstance() {
        return ourInstance;
    }

    private JobQueue() {
        index = 0;
        executorService = Executors.newFixedThreadPool(1);
    }

    public int addJob(Job job) {
        add(index++, job);
        job.setId(index);
        executorService.submit(job);
        return index;
    }
}
