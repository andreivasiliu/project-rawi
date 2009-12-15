package servlets;

import classes.IPEntry;
import classes.TrackerBean;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PutIPServlet extends HttpServlet {

    TrackerBean tracker;

    @Override
    public void init() throws ServletException {
        super.init();
        tracker = (TrackerBean)getServletContext().getAttribute("trackerBean");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Calendar rightNow = Calendar.getInstance();
        String name = request.getParameter("name");
        String type = request.getParameter("type");

        if(name == null || name.isEmpty() || type == null || type.isEmpty())
            response.sendError(404, "Bad request");

        long time = rightNow.getTimeInMillis();
        
        IPEntry newEntry = new IPEntry(name, type, time);
        tracker.addIpToList(newEntry);

        out.println("<p>" + name + " - " + type +
                "<br /> Successfully added. </p>");
        out.println("<a href=\"index.jsp\"> Back </a>");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }

}