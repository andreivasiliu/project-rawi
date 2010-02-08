/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import classes.MainBean;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import rawi.common.MainServerInterface;
import rawi.common.Ports;
import rawi.common.ValidateXMLInfo;
import rawi.rmiinfrastructure.RMIClientModel;

/**
 *
 * @author Ioana
 */
public class ValidateXMLServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String xmlContent = request.getParameter("xml");
        String xmlName = request.getParameter("name");

        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (isMultipart) {
            try {
                FileItemFactory factory = new DiskFileItemFactory();
                ServletFileUpload upload = new ServletFileUpload(factory);

                List items = null;
                items = upload.parseRequest(request); /* FileItem */
                Iterator iter = items.iterator();

                while (iter.hasNext()) {
                    FileItem item = (FileItem) iter.next();

                    if (!item.isFormField()) {
                        xmlContent = item.getString();
                        break;
                    }
                }
            } catch (FileUploadException ex) {
                throw new ServletException(ex);
            }
        }

        if (xmlContent == null || xmlContent.isEmpty()) {
            response.sendError(404, "No XML contents given.");
            return;
        }

        if (request.getParameter("delete") != null) {
            
            String tmToDelete = request.getParameter("delete");
            System.out.println("===> 0. deleting " + tmToDelete);
            deleteTM(tmToDelete);
            response.sendRedirect("index.jsp");
        } else {
            boolean validTM = validateTM(out, xmlName, xmlContent);
            if (request.getParameter("savexml") != null && validTM) {
                saveTM(xmlName, xmlContent);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }

    private void saveTM(String xmlName, String content) {
        // save to file?
        
//        String folderName = getServletContext().getRealPath("FileRepository") + "/TransformationModels";
//        // Create or use the folder for the given file
//        File folder = new File(folderName);
//        folder.mkdir();
//
//        // create file
//        BufferedWriter writer = null;
//        try {
//            writer = new BufferedWriter(new FileWriter(folderName + "/" + xmlName));
//            writer.write(content);
//            // ?
//            writer.close(); // ? should have been written in a finally block?
//            // ?
//        } catch (IOException ex) {
//            Logger.getLogger(ValidateXMLServlet.class.getName()).log(Level.SEVERE, null, ex);
//        }

        // add xml to list of xmls
        MainBean.getFromContext(getServletContext()).addXmlToList(xmlName, content);
    }

    private boolean validateTM(PrintWriter out, String xmlName, String xmlContent)
            throws ServletException {
        MainServerInterface msi;

        ValidateXMLInfo info = null;
        try {
            msi = new RMIClientModel<MainServerInterface>(Ports.MainServerPort).getInterface();
            info = msi.validateXML(xmlContent);

            out.println("<validateXMLInfo>");
                out.println("<xml-name>" + xmlName + "</xml-name>");
                out.println("<xml-success>" + info.success +
                        "</xml-success>");
                out.println("<xml-message>" + info.message + "</xml-message>");
                out.println("<xml-nodeId>" + info.nodeID + "</xml-nodeId>");
            out.println("</validateXMLInfo>");
        } catch (RemoteException ex) {
            throw new ServletException("Remote exception", ex);
        } catch (NotBoundException ex) {
            throw new ServletException("Not bound exception", ex);
        }

        return info.success;
    }

    private void deleteTM(String name) {
        // delete file?
        System.out.println("===> 1. deleting " + name);
        MainBean.getFromContext(getServletContext()).deleteXmlByName(name);
    }
}
