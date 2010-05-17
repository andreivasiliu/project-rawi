package rawi.web.servlets;

import rawi.web.classes.MainBean;
import rawi.web.classes.Session;
import rawi.web.classes.UploadedFile;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
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

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        String url = request.getPathInfo();
        String sessionId = url.split("/")[1];
        String fileLogicalName = url.substring(2 + sessionId.length());
        boolean zipFile = Boolean.valueOf(request.getParameter("zipFile"));
        if (zipFile)
            System.out.println("Received a zip file.");

        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);
        if (!isMultipart) {
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
                long fileId = processUploadedFile(item, new Long(sessionId),
                        fileLogicalName, zipFile);
                out.println("File ID: " + fileId + "\n");
                break;
            }
        }

    }

//    @Override
//    public void doGet(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        doPost(request, response);
//    }
    private long processUploadedFile(FileItem item, Long sessionId,
            String fileLogicalName, boolean zipFile) throws IOException {

        InputStream uploadedStream = item.getInputStream();

        String fileName = fileLogicalName.replaceAll("/", "_");
        MainBean theBean = (MainBean) getServletContext().getAttribute("mainBean");
        Session session = theBean.getSessionById(sessionId);

        // Create or use the folder for the given file
        File folder = new File(session.folderName);
        folder.mkdirs();

        // Create the file. It's name will be id-logicalFileName
        long fileId = session.getNextAvailableFileId();
        String fullFileName = fileId + "-" + fileName;
        File myFile = new File(session.folderName + "/" + fullFileName);

        FileOutputStream fos = new FileOutputStream(myFile);
        byte[] bytes = new byte[4096];
        int bytesRead;
        while ((bytesRead = uploadedStream.read(bytes, 0, 4096)) != -1) {
            fos.write(bytes, 0, bytesRead);
        }
        fos.close();
        uploadedStream.close();

        // add file to session's file list
        session.addFileToList(new UploadedFile(fileId, fileLogicalName, myFile,
                zipFile));

        return fileId;
    }
}
