/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Ioana
 */
public class TheDownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //processRequest(request, response);
        response.setContentType("application/octet-stream");
        //response.setContentType("text/xml");

        OutputStream out = response.getOutputStream();
        String path = request.getParameter("selectFiles");
        System.out.println(request.getParameter("selectFiles"));

        File requestedFile = new File(path);
        InputStream is = new FileInputStream(requestedFile);
        
        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        is.close();
        out.close();

        //URL url = getServletContext().getResource(file);

        System.out.println("--> " + request.getParameter("myFileAttribute"));

    }
}
