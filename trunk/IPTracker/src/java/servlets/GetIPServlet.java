package servlets;

import classes.IPType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetIPServlet extends HttpServlet {

    private Calendar rightNow = Calendar.getInstance();
    private static int expirationTime = 2; // in minutes

    public static void setExpirationTime(int expirationTime) {
        GetIPServlet.expirationTime = expirationTime;
    }

    public static int getExpirationTime() {
        return expirationTime;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String type = request.getParameter("type");
        type = type.substring(0, type.length() - 1); // remove the "s"

        List<IPType> ipList = PutIPServlet.getIdList();
        updateList(ipList, expirationTime);

        String items = "";
        for (IPType elt : PutIPServlet.getIdList()) {
            if (elt.type.equals(type)) {
                long age = rightNow.getTimeInMillis() - elt.time;
                int ageInSeconds = (int) TimeUnit.MILLISECONDS.toSeconds(age);
                items += "<li>" + elt.name +
                        " (" + ageInSeconds + " seconds ago)" + "</li>";
            }
        }

        if (items.isEmpty()) {
            out.println("Sorry, no " + type + " said 'Hello' in the last " +
                expirationTime + " minutes.");
            out.println("<a href=\"index.jsp\"> Back </a>");
            
        } else {
            out.println("These are the " + type + "s that said 'Hello' " +
                "in the last " + expirationTime + " minutes: <br />");
            out.println("<ul>" + items + "</ul>");
        }
    }

    private void updateList(List<IPType> ipList, int minutes) {
        List<IPType> newList = new LinkedList<IPType>();

        for (IPType elt : ipList) {
            long age = rightNow.getTimeInMillis() - elt.time;
            int ageInMinutes = (int) TimeUnit.MILLISECONDS.toMinutes(age);
            if (ageInMinutes <= minutes) {
                newList.add(elt);
            }
        }

        PutIPServlet.setIdList(ipList);
    }
}
