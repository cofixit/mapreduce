package com.leon.mandelbrot.standard;

import org.junit.Test;

public class MandelbrotAnimationTest {

    @Test
    public void testGetFactor() throws Exception {
        double firstScale = 4;
        double lastScale = 0.00001;
        int frames = 10;
        double factor = MandelbrotAnimation.getFactor(firstScale, lastScale, frames);
        for (int i = 0; i < frames; i++) {
            System.out.println("Frame #" + i + ": " + firstScale);
            firstScale *= factor;
        }
    }

    @Test
    public void testGetIncrement() throws Exception {
        double firstTX = -1.5;
        double lastTX = 1.0;
        int frames = 10;
        double increment = MandelbrotAnimation.getIncrement(firstTX, lastTX, frames);
        for (int i = 0; i < frames; i++) {
            System.out.println("Frame #" + i + ": " + firstTX);
            firstTX += increment;
        }
    }
}