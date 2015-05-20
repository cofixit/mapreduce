package com.leon.mandelbrot.server;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.HandlerList;
import org.mortbay.jetty.handler.ResourceHandler;
import org.mortbay.jetty.servlet.ServletHandler;

public class Main {

    public static final int PORT = 1337;

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
        int port = Main.PORT;
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        new Main(port).start();
    }
}
