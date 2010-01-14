package servlets;

import classes.MainBean;
import classes.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.common.SessionInfo;
import rawi.rmiinfrastructure.RMIClientModel;

public class CreateSession extends HttpServlet {

    MainBean theBean;

    @Override
    public void init() {
        theBean = (MainBean) getServletContext().getAttribute("mainBean");
    }

    protected void processRequest(HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String modelName = request.getParameter("modelName");
        String mainServerIp = request.getParameter("mainServerIp");

        if (modelName == null || !existsModel(modelName)) {
            response.sendError(404, "Xml doesn't exist.");
            return;
        }

        if (mainServerIp == null || mainServerIp.isEmpty()) {
            mainServerIp = getMainServerIp();
        }

        // no main server found
        if (mainServerIp.isEmpty()) {
            response.sendError(404, "No Main Server found.");
            return;
        }

        // xml exists, main server found => create session
        Long sessionId = theBean.getNextAvailableSessionId();
        String folderName = getServletContext().getRealPath("FileRepository")
                + "/Session" + Long.toString(sessionId);

        Session session = new Session(sessionId, modelName, folderName);

        SessionInfo sessionInfo =
                createSessionOnMainServer(session, mainServerIp, request, response);
        if (sessionInfo != null) {
            theBean.addSessionToList(session);

            out.println("<sessionInfo>");
            out.println("<sessionId>" + sessionInfo.sessionId + "</sessionId>");
            out.println("<downloadUrl>" + sessionInfo.downloadUrl + "</downloadUrl>");
            out.println("<uploadUrl>" + sessionInfo.uploadUrl + "</uploadUrl>");
            out.println("<messageLogIp>" + sessionInfo.msgLogIp + "</messageLogIp>");
            out.println("</sessionInfo>");
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    // gets the ip of a running Main Server
    private String getMainServerIp() {
        List<String> mainServersIps = theBean.getListOfMainServers();
        for (String mainServerIp : mainServersIps) {
            try {
                InetAddress addr = InetAddress.getByName(mainServerIp);
                if (addr.isReachable(5000)) {
                    return mainServerIp;
                }
            } catch (IOException ex) {
            }
        }
        return "";
    }

    private boolean existsModel(String modelName) {
        if (theBean.getXmlList().containsKey(modelName)) {
            return true;
        }
        return false;
    }

    private SessionInfo createSessionOnMainServer(Session session, String mainServerIp,
            HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        MainServerInterface msi;
        try {
            msi = new RMIClientModel<MainServerInterface>(mainServerIp,
                    Ports.MainServerPort).getInterface();
            String pageURL = request.getRequestURL().toString();
            String servletPath = request.getServletPath();
            pageURL = pageURL.substring(0, pageURL.indexOf(servletPath));

            String downloadURL = pageURL + "/TheDownloadServlet/" + session.getId() + "/";
            String uploadURL = pageURL + "/TheUploadServlet/" + session.getId() + "/";
            String messageCenterIp = java.net.InetAddress.getLocalHost().getHostAddress();

            SessionInfo sessionInfo = new SessionInfo(session.getId().toString(),
                    downloadURL, uploadURL, messageCenterIp,
                    theBean.getXmlContentByName(session.xmlName));

            msi.createSession(sessionInfo);
            return sessionInfo;

        } catch (UnknownHostException ex) {
            response.sendError(500, "Unknown host exception in CreateSession.");
            ex.printStackTrace();
            return null;
        } catch (RemoteException ex) {
            response.sendError(500, "Remote exception in CreateSession.");
            ex.printStackTrace();
            return null;
        } catch (NotBoundException ex) {
            response.sendError(500, "Not bound exception in CreateSession.");
            ex.printStackTrace();
            return null;
        }
    }
}
