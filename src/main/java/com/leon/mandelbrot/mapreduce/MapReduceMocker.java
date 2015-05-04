package com.leon.mandelbrot.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;

import java.util.Arrays;

public class MapReduceMocker extends MapReduce {

    private static final Log LOG = LogFactory.getLog(MapReduceMocker.class);

    @Override
    public int run(String[] args) throws Exception {
        LOG.info(Arrays.toString(args));
        switch(args[1]) {
            case "1":
                Thread.sleep(60000);
                return 0;
            case "2":
                Thread.sleep(500);
                return 1;
            default:
                throw new Exception(args[1]);
        }
    }
}
