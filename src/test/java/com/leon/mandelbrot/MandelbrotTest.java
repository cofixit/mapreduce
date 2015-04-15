package com.leon.mandelbrot;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Leon on 15.04.15.
 */
public class MandelbrotTest {

    @Test
    public void testXPixelToCoordinate() throws Exception {
        Mandelbrot m = new Mandelbrot(3000, 2000, 3.5, -0.75, 0.0);
        System.out.println("x: " + m.xPixelToCoordinate(3000));
    }

    @Test
    public void testYPixelToCoordinate() throws Exception {
        Mandelbrot m = new Mandelbrot(3000, 2000, 3.5, -0.75, 0.0);
        System.out.println("y: " + m.yPixelToCoordinate(2000));
    }
}