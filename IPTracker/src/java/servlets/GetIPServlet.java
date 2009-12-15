package servlets;

import classes.TrackerBean;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetIPServlet extends HttpServlet {

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
        String type = request.getParameter("type");

        tracker.updateList(rightNow);

        List<String> items = tracker.getList(type);
        if (!items.isEmpty())
            for(String elt: items)
                out.println(elt);
    }
}
