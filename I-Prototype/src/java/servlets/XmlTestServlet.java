/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package servlets;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import rawi.common.MainServerInterface;
import rawi.common.ValidateXMLInfo;


/**
 *
 * @author Ioana
 */
public class XmlTestServlet extends HttpServlet {
   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String xml = request.getParameter("xml");

        MainServerInterface rmiServer;
        Registry registry;
        String serverAddress = "127.0.0.1";
        int serverPort = 3230;

        System.out.println("sending data to " + serverAddress + ":" + serverPort);
        try {
            // get the “registry”
            registry = LocateRegistry.getRegistry(serverAddress, serverPort);
            // look up the remote object
            rmiServer =
                    (MainServerInterface) (registry.lookup("MainServer"));
            // call the remote method
            ValidateXMLInfo info = rmiServer.validateXml(xml);

            out.println("Data sent.<br />");
            out.println("Success: " + info.success + "<br /> " +
                    "Message: " + info.message + "<br /> ");

        } catch (RemoteException e) {
            out.println("Data NOT sent. Remote exception.");
            e.printStackTrace();
        } catch (NotBoundException e) {
            out.println("Data NOT sent. Not bound exception.");
            e.printStackTrace();
            throw new ServletException(e);
        }
    }
}
