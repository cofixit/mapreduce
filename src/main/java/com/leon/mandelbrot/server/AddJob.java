package com.leon.mandelbrot.server;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class AddJob extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String width = request.getParameter("width");
        String height = request.getParameter("height");
        String frames = request.getParameter("frames");
        String maxIterations = request.getParameter("maxIterations");
        String firstScale = request.getParameter("firstScale");
        String firstTranslateX = request.getParameter("firstTranslateX");
        String firstTranslateY = request.getParameter("firstTranslateY");
        String lastScale = request.getParameter("lastScale");
        String lastTranslateX = request.getParameter("lastTranslateX");
        String lastTranslateY = request.getParameter("lastTranslateY");

        Job job = new Job(
                width,
                height,
                frames,
                maxIterations,
                firstScale,
                firstTranslateX,
                firstTranslateY,
                lastScale,
                lastTranslateX,
                lastTranslateY
        );
        int id = JobQueue.getInstance().addJob(job);

        response.setContentType("text/json");
        response.setStatus(HttpServletResponse.SC_OK);

        response.getWriter().println("{\"jobId\": " + id + "}");
    }
}
