package servlets;

import classes.MainBean;
import classes.Session;
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
        String sessionId = url.split("/")[1];
        String fileLogicalName = url.substring(2 + sessionId.length());

        // get session by id
        MainBean theBean = MainBean.getFromContext(getServletContext());
        Session session = theBean.getSessionById(new Long(sessionId));

        // delete or download file
        if(request.getParameter("delete") != null)
            deleteFile(session, fileLogicalName, response);
        else
            downloadFile(session, fileLogicalName, response);
    }

    private void deleteFile(Session session, String fileLogicalName,
            HttpServletResponse response) throws IOException {
        File fileToDelete = session.getFileByName(fileLogicalName);
        fileToDelete.delete();
        session.deleteFileByName(fileLogicalName);

        response.sendRedirect(getServletContext().getContextPath() + "/index.jsp");
    }

    private void downloadFile(Session session, String fileLogicalName,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/octet-stream");

        OutputStream out = response.getOutputStream();
        File requestedFile = session.getFileByName(fileLogicalName);
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
