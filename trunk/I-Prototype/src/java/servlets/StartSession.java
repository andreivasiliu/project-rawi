package servlets;

import classes.MainBean;
import classes.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.rmiinfrastructure.RMIClientModel;

public class StartSession extends HttpServlet {
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String xmlName = request.getParameter("sessionXMLName");
        MainBean theBean = (MainBean)getServletContext().getAttribute("mainBean");

        Long sessionId = theBean.getNextAvailableSessionId();
        String folderName = getServletContext().getRealPath("FileRepository")
                + "/Session" + Long.toString(sessionId);

        Session session = new Session(sessionId, xmlName, folderName);
        theBean.addSessionToList(session);

        // TODO check existence of xml by its name

        startSessionOnMainServer(session, request);
        
        response.sendRedirect("index.jsp");
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

    private void startSessionOnMainServer(Session session, HttpServletRequest request)
            throws ServletException {
        MainServerInterface msi;
        try {
            msi = new RMIClientModel<MainServerInterface>(Ports.MainServerPort)
                    .getInterface();
            String pageURL = request.getRequestURL().toString();
            String servletPath = request.getServletPath();
            pageURL = pageURL.substring(0, pageURL.indexOf(servletPath));

            String downloadURL = pageURL + "/TheDownloadServlet/"
                    + session.getId() + "/";
            String uploadURL = pageURL + "/TheUploadServlet/"
                    + session.getId() + "/";
            String messageCenterIp = java.net.InetAddress.getLocalHost()
                    .getHostAddress();
            msi.startSession(downloadURL, uploadURL, messageCenterIp, session.getId());
            
        } catch (UnknownHostException ex) {
            Logger.getLogger(StartSession.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            throw new ServletException("Remote exception", ex);
        } catch (NotBoundException ex) {
            throw new ServletException("Not bound exception", ex);
        }
    }

}
