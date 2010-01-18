package servlets;

import classes.RMIMessage;
import classes.RMIWebServer;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class CentralMessageService extends HttpServlet {

    static RMIWebServer server;
    
    @Override
    public void init(ServletConfig config) {
        server = (RMIWebServer)config.getServletContext().getAttribute("server");
    }
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        int formId = Integer.parseInt(request.getParameter("fromId"));
        List<RMIMessage> mlist = server.getMessageListFromID(formId);

        try {
            out.println("<message-list>");
            for(RMIMessage m: mlist) {
                out.println("\t<message>");
                out.println("\t\t<message-id>" + m.getId() + "</message-id>");
                out.println("\t\t<message-source>" + m.getSource() + "</message-source>");
                out.println("\t\t<message-severity>" + m.getSeverity() + "</message-severity>");
                out.println("\t\t<message-text>" + m.getMessage() + "</message-text>");
                out.println("\t</message>");
            }
            out.println("</message-list>");
        } finally {
            out.close();
        }
    }
}
