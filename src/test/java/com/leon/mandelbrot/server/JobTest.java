package com.leon.mandelbrot.server;

import org.junit.Test;

import static org.junit.Assert.*;

public class JobTest {

    @Test
    public void testEnum() {
        Job.Status status1 = Job.Status.WAITING;
        Job.Status status2 = Job.Status.RUNNING;
        Job.Status status3 = Job.Status.FAILED;
        Job.Status status4 = Job.Status.DONE;

        assertEquals("waiting", status1.toString());
        assertEquals("running", status2.toString());
        assertEquals("failed", status3.toString());
        assertEquals("done", status4.toString());

    }

}