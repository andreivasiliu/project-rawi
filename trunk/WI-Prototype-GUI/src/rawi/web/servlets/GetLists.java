package rawi.web.servlets;

import rawi.web.classes.MainBean;
import rawi.web.classes.Session;
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
                printListOfXMLNames(out);
            } else if (request.getParameter("type").equals("mainServer")) {
                printListOfMainServers(out);
            } else if (request.getParameter("type").equals("workSessions")) {
                printListOfSessions(out);
            }
        } finally {
            out.close();
        }
    }

    public void printListOfXMLNames(PrintWriter out) {
        List<String> xmlNames = MainBean.getFromContext(getServletContext())
                .getXmlNamesList();
        out.println("<xmlList>");
        for (String xmlName : xmlNames)
            out.println("  <xml>" + xmlName + "</xml>");
        out.println("</xmlList>");

    }

    public void printListOfMainServers(PrintWriter out) {
        List<String> mainServersIp = MainBean.getFromContext(getServletContext())
                .getListOfMainServers();
        out.println("<mainServers>");
        for(String serverIp: mainServersIp)
            out.println("  <mainServerIp>" + serverIp + "</mainServerIp>");
        out.println("</mainServers>");
    }

    public void printListOfSessions(PrintWriter out) {
        List<Session> sessionList = MainBean.getFromContext(getServletContext())
                .getSessionList();

        out.println("<workSessions>");
        for(Session session: sessionList)
            out.println("  <workSession id=\"" + session.getId() + "\"" +
                        " xmlName=\"" + session.xmlName + "\"" +
                        " mainServerIp=\"" + session.mainServerIp + "\" />");
        out.println("</workSessions>");
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
