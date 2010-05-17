/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package rawi.web.servlets;

import rawi.web.classes.MainBean;
import rawi.web.classes.Session;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.rmiinfrastructure.RMIClientModel;

/**
 *
 * @author Administrator
 */
public class StartStopSession extends HttpServlet {
   
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        long sessionId = Long.parseLong(request.getParameter("sessionId"));
        String action = request.getParameter("action");

        MainBean theBean = (MainBean) getServletContext().getAttribute("mainBean");
        Session session = theBean.getSessionById(sessionId);

        sendChangeStatusRequestToMainServer(response, session, action);

        try {
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

    private void sendChangeStatusRequestToMainServer(HttpServletResponse response,
            Session session, String action) throws IOException {
        MainServerInterface msi;
        String mainServerIp = session.mainServerIp;
        try {
            msi = new RMIClientModel<MainServerInterface>(mainServerIp,
                    Ports.MainServerPort).getInterface();
           
            if(action.equals("start"))
                msi.startSession(session.getId().toString());
            else if (action.equals("stop"))
                msi.stopSession(session.getId().toString());

        } catch (RemoteException ex) {
            response.sendError(500, "Remote exception in CreateSession.");
            ex.printStackTrace();
        } catch (NotBoundException ex) {
            response.sendError(500, "Not bound exception in CreateSession.");
            ex.printStackTrace();
        }

    }
}
