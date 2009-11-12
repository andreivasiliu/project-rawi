/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import java.util.UUID;

/**
 *
 * @author Ioana
 */
public class TheUploadServlet extends HttpServlet {

    public String filesPath;
    // hash map with: <userAddr, userID>
    // userID = unique - for the folder name
    private static HashMap<String, String> users = new HashMap<String, String>();
    private static List<File> fileList = new LinkedList<File>();


   @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
 
        // Check that we have a file upload request
        boolean isMultipart = ServletFileUpload.isMultipartContent(request);

        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();

        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);

        List items = null;
        try {
            // Parse the request
            items = upload.parseRequest(request); /* FileItem */
        } catch (FileUploadException ex) {
            Logger.getLogger(TheUploadServlet.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Process the uploaded items
        Iterator iter = items.iterator();
        while (iter.hasNext()) {
            FileItem item = (FileItem) iter.next();

            if (item.isFormField()) {
                processFormField(item);
            } else {
                System.out.println("==> " + request.getRemoteAddr() + " <==");
                processUploadedFile(item, request.getRemoteAddr());
            }
        }
        response.sendRedirect("index.jsp");
    }

    
    private void processFormField(FileItem item) {
        String name = item.getFieldName();
        String value = item.getString();
        System.out.println("==> Form field: name = " + name +
                "value = " + value);
    }

    private void processUploadedFile(FileItem item, String userAddr) throws IOException {
        InputStream uploadedStream = item.getInputStream();

        // Generate random numbers for  the file name,
        // and associate them to the user addr
        String userID;
        if (!users.containsKey(userAddr)) {
            userID = UUID.randomUUID().toString();
            users.put(userAddr, userID);
        }
        else
            userID = users.get(userAddr);

        String fileName = item.getName();
        if(fileName.isEmpty())
            return;

        // Create or use the folder for the given file
        String folderPath = System.getProperty("user.dir") +
                "\\Uploaded XML Files\\";
        String folderName = userID;
        File folder = new File(folderPath + folderName);
        folder.mkdir();

        System.out.println(folderPath + " - " + folderName + " - " + fileName);
        File myFile = new File(folderPath + folderName + "\\" + fileName);
        if(!fileList.contains(myFile))
            fileList.add(myFile);
        FileOutputStream fos = new FileOutputStream(myFile);
        int sth;
        while ((sth = uploadedStream.read()) != -1) {
            fos.write(sth);
        }
        fos.write(32);
        fos.close();
        uploadedStream.close();

    }

    public static List<File> getFileList() {
        return fileList;
    }

}
