/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import classes.MainBean;
import classes.Session;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Ioana
 */
public class DownloadXMLServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        MainBean mainBean = MainBean.getFromContext(this.getServletContext());
        String xmlContents = null;

        String xmlName = request.getParameter("name");
        String sessionId = request.getParameter("sessionId");

        if (xmlName != null) {
            xmlContents = mainBean.getXmlContentByName(xmlName);
        }
        else if (sessionId != null) {
            Session session = mainBean.getSessionById(Long.parseLong(sessionId));
            if (session != null)
                xmlContents = session.xmlContents;
        }

        if (xmlContents == null)
            response.sendError(404, "No such TransformationModel XML exists.");
        else
            out.write(xmlContents);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
