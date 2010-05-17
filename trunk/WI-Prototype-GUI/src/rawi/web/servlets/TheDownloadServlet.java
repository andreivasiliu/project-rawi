package rawi.web.servlets;

import rawi.web.classes.MainBean;
import rawi.web.classes.Session;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class TheDownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // get session id and file logical name
        String url = request.getPathInfo();
        String[] splittedUrl = url.split("/");

        String sessionId = splittedUrl[1];
        String fileId = splittedUrl[2];
        
        // file logical name: never used?
        String fileLogicalName = url.substring(3
                + sessionId.length() + fileId.length());

        // get session by id
        MainBean theBean = MainBean.getFromContext(getServletContext());
        Session session = theBean.getSessionById(new Long(sessionId));

        // delete or download file
        if(request.getParameter("delete") != null)
            deleteFile(session, new Long(fileId), response);
        else
            downloadFile(session, new Long(fileId), response);
    }

    private void deleteFile(Session session, long fileId,
            HttpServletResponse response) throws IOException {
        File fileToDelete = session.getFileById(fileId).theFile;
        fileToDelete.delete();
        session.deleteFileById(fileId);

        response.sendRedirect(getServletContext().getContextPath() + "/index.jsp");
    }

    private void downloadFile(Session session, long fileId,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/octet-stream");

        OutputStream out = response.getOutputStream();
        File requestedFile = session.getFileById(fileId).theFile;
        InputStream is = new FileInputStream(requestedFile);

        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        is.close();
        out.close();

    }
}
