package com.leon.mandelbrot.server;

import java.util.Vector;

public class JobList extends Vector<Job> {
    private static JobList ourInstance = new JobList();

    private int index;

    public static JobList getInstance() {
        return ourInstance;
    }

    private JobList() {
        index = 0;
    }

    public int addJob(Job job) {
        add(index++, job);
        job.setId(index);
        return index;
    }
}
