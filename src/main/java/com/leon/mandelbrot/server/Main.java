package com.leon.mandelbrot.server;

import org.apache.log4j.*;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;

public class Main {

    private final Server server;
    private final ResourceHandler resourceHandler;
    private final ServletHandler servletHandler;
    private final HandlerList mainHandler;

    public Main(int port) {
        server = new Server(port);

        resourceHandler = new ResourceHandler();
        resourceHandler.setResourceBase("build/resources/main");

        servletHandler = new ServletHandler();
        rewriteRules();

        mainHandler = new HandlerList();

        mainHandler.addHandler(resourceHandler);
        mainHandler.addHandler(servletHandler);

        server.setHandler(mainHandler);
    }

    public void rewriteRules() {
        servletHandler.addServletWithMapping(TestServlet.class,     "/testservlet");
        servletHandler.addServletWithMapping(AddJob.class,          "/addJob");
        servletHandler.addServletWithMapping(ListJobs.class,        "/listJobs");
    }

    public void start() throws Exception {
        server.start();
        server.join();
    }

    public static void main(String[] args) throws Exception {
//        Layout layout = new TTCCLayout("DATE");
//        BasicConfigurator.configure(new ConsoleAppender(layout, ConsoleAppender.SYSTEM_OUT));
//        Logger logger = Logger.getRootLogger();
//        logger.setLevel(Level.INFO);
        new Main(1337).start();
    }
}
