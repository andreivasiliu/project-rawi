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
import java.rmi.ServerException;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.common.ValidateXMLInfo;
import rawi.rmiinfrastructure.RMIClientModel;


/**
 *
 * @author Ioana
 */
public class ValidateXMLServlet extends HttpServlet{
   
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException{
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String xml = request.getParameter("xml");
        String name = request.getParameter("name");

        MainServerInterface msi;
        try {
            msi = new RMIClientModel<MainServerInterface>(Ports.MainServerPort).getInterface();
        } catch (RemoteException ex) {
            throw new ServerException("Remote exception", ex);
        } catch (NotBoundException ex) {
            throw new ServerException("Not bound exception", ex);
        }
        ValidateXMLInfo info = msi.validateXML(xml);

        out.println("<validateXMLInfo>");
            out.println("<xml-name>" + name + "</xml-name>");
            out.println("<xml-success>" + info.success +
                    "</xml-success>");
            out.println("<xml-message>" + info.message + "</xml-message>");
            out.println("<xml-nodeId>" + info.nodeID + "</xml-nodeId>");
        out.println("</validateXMLInfo>");
    }
}