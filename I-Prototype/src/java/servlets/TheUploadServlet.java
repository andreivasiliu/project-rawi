package servlets;

import classes.MainBean;
import classes.Session;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

public class TheUploadServlet extends HttpServlet {

    public String filesPath;
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String url = request.getPathInfo();
        String sessionId = url.split("/")[1];
        String fileLogicalName = url.substring(2 + sessionId.length());
        
        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if(!isMultipart) {
            throw new ServletException("No file upload request.");
        }

        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        List items = null;
        try {
            // Parse the request
            items = upload.parseRequest(request); /* FileItem */
        } catch (FileUploadException ex) {
            throw new ServletException(ex);
        }

        // Process the uploaded items
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();

            if (!item.isFormField()) {
                processUploadedFile(item, new Long(sessionId), fileLogicalName);
            }
        }
        response.sendRedirect(getServletContext().getContextPath() + "/index.jsp");
    }

//    @Override
//    public void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        doPost(request, response);
//    }

    private void processUploadedFile(FileItem item, Long sessionId,
            String fileLogicalName) throws IOException {

        InputStream uploadedStream = item.getInputStream();

        String fileName = fileLogicalName.replaceAll("/", "_");
        MainBean theBean = (MainBean)getServletContext().getAttribute("mainBean");
        Session session = theBean.getSessionById(sessionId);

        // Create or use the folder for the given file
        File folder = new File(session.folderName);
        folder.mkdir();

        // create file
        File myFile = new File(session.folderName + "/" + fileName);
        FileOutputStream fos = new FileOutputStream(myFile);
        int bytesRead;
        while ((bytesRead = uploadedStream.read()) != -1) {
            fos.write(bytesRead);
        }
        fos.close();
        uploadedStream.close();

        // add file to session's file list
        session.addFileToList(fileLogicalName, myFile);
    }
}
