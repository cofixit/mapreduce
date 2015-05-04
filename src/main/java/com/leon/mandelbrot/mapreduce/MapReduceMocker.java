package com.leon.mandelbrot.mapreduce;

import org.apache.log4j.Logger;

import java.util.Arrays;

public class MapReduceMocker extends MapReduce {

    @Override
    public int run(String[] args) throws Exception {
        Logger logger = Logger.getRootLogger();
        logger.info(Arrays.toString(args));
        switch(args[1]) {
            case "ok":
                Thread.sleep(60000);
                return 0;
            case "failfast":
                Thread.sleep(500);
                return 1;
            case "failslow":
                Thread.sleep(60000);
                return 1;
            case "throwfast":
                Thread.sleep(500);
                throw new Exception(args[1]);
            case "throwslow":
                Thread.sleep(60000);
                throw new Exception(args[1]);
            default:
                throw new Exception(args[1]);
        }
    }

}
