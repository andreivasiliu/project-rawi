package servlets;

import classes.IPType;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PutIPServlet extends HttpServlet {

    private static List<IPType> idList = new LinkedList<IPType>();

    public static List<IPType> getIdList() {
        return idList;
    }

    public static void setIdList(List<IPType> idList) {
        PutIPServlet.idList = idList;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        Calendar rightNow = Calendar.getInstance();
        String name = request.getParameter("name");
        String type = request.getParameter("type");
        long time = rightNow.getTimeInMillis();
        
        IPType newEntry = new IPType(name, type, time);
        idList.add(newEntry);

        out.println("<p>" + name + " - " + type +
                "<br /> Successfully added. </p>");
        out.println("<a href=\"index.jsp\"> Back </a>");
    }

}