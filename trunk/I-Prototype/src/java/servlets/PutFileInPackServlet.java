/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import classes.MainBean;
import classes.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rawi.common.FileHandle;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.rmiinfrastructure.RMIClientModel;

/**
 *
 * @author Administrator
 */
public class PutFileInPackServlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String pageURL = request.getRequestURL().toString();
            String servletName = request.getServletPath();

            // baseURL = pageURL - ServletName (AssociateFileWithPack) + "DownloadServelt
            String baseUrl = pageURL.substring(0, pageURL.indexOf(servletName))
                    + "/TheDownloadServlet";

            String fileId = request.getParameter("fileId");
            String fileName = request.getParameter("fileName");
            FileHandle fileHandle = new FileHandle(baseUrl, fileId, fileName);

            String sessionId = request.getParameter("sessionId");
            String packId = request.getParameter("packId");        
            
            out.println(sendAssociateRequestToMS(response, sessionId, fileHandle, packId));
        } finally {
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    private boolean sendAssociateRequestToMS(HttpServletResponse response,
            String sessionId, FileHandle fileHandle, String packId) throws IOException {

        MainServerInterface msi;
        MainBean theBean = (MainBean) getServletContext().getAttribute("mainBean");
        String mainServerIp = theBean.getSessionById(Long.parseLong(sessionId))
                .mainServerIp;

        try {
            msi = new RMIClientModel<MainServerInterface>(mainServerIp,
                    Ports.MainServerPort).getInterface();
            boolean responseFormMainServer = msi.putFileInPack
                    (sessionId.toString(), fileHandle, packId);
            return responseFormMainServer;
        } catch (RemoteException ex) {
            response.sendError(500, "Remote exception in CreateSession.");
            ex.printStackTrace();
            return false;
        } catch (NotBoundException ex) {
            response.sendError(500, "Not bound exception in CreateSession.");
            ex.printStackTrace();
            return false;
        }
    }
}
