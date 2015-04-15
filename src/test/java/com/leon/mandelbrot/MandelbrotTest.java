package com.leon.mandelbrot;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Leon on 15.04.15.
 */
public class MandelbrotTest {

    @Test
    public void testSplittingTasks() {
        int nMaps = 11;
        int rows = 10;
        int frames = 10;
        long combinedRows = (long)rows * (long)frames;
        long rowsPerMapper = combinedRows / (long)nMaps;
        long unassignedRows = combinedRows % nMaps;
        /*
        if nMaps/unassignedRows is smaller than 2,
        then the rows left should be assigned to the last mapper task.
        otherwise they should be assigned to an extra mapper task.
        */
        boolean useExtraMapper = (rowsPerMapper / unassignedRows) < 2L;
        System.out.println("number of maps:   " + nMaps);
        System.out.println("number of rows:   " + rows);
        System.out.println("rows per mapper:  " + rowsPerMapper);
        System.out.println("unassigned rows:  " + unassignedRows);
        System.out.println("use extra mapper: " + useExtraMapper);

        if (useExtraMapper) nMaps++;
        System.out.println("updated nMaps: " + nMaps);
        for (int i = 0; i < nMaps; i++) {
            long offset = i*rowsPerMapper;
            long size = rowsPerMapper;
            if (i == nMaps - 1) {
                size = unassignedRows;
                if (!useExtraMapper) {
                    size += rowsPerMapper;
                }
            }
            for (long j = 0; j < size; j++) {
                long combinedRow = offset + j;
                long frame = combinedRow / rows;
                long row = combinedRow % rows;
                System.out.println();
                System.out.println("Frame: " + frame);
                System.out.println("Row: " + row);
            }
        }
    }
}