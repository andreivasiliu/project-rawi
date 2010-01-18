/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package servlets;

import classes.TrackerBean;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SetMaxAgeServlet extends HttpServlet {

    TrackerBean tracker;

    @Override
    public void init() throws ServletException {
        super.init();
        tracker = (TrackerBean)getServletContext().getAttribute("trackerBean");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        int time = Integer.parseInt(request.getParameter("expTime"));
        tracker.setExpirationTime(time);

        out.println("<p>Maximum age successfully set at: " +
                tracker.getExpirationTime() +
                " minutes. </p>");
        out.println("<a href=\"index.jsp\"> Back </a>");
    }

}
