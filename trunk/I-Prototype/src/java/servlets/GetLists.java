package servlets;

import classes.MainBean;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class GetLists extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            if (request.getParameter("type").equals("xml")) {
                getListOfXMLNames(out);
            } else if (request.getParameter("type").equals("mainServer")) {
                getListOfMainServers(out);
            }
        } finally {
            out.close();
        }
    }

    public void getListOfXMLNames(PrintWriter out) {
        List<String> xmlNames = MainBean.getFromContext(getServletContext()).getXmlNamesList();
        out.println("<xmlList>");
        for (String xmlName : xmlNames) {
            out.println("<xml>" + xmlName + "</xml>");
        }
        out.println("</xmlList>");

    }

    public void getListOfMainServers(PrintWriter out) {
        out.println("<mainServers>");
        out.println("</mainServers>");
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }// </editor-fold>
}
