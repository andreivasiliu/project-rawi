/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
/**
 *
 * @author PIC
 */
public class ProgramDownloader extends HttpServlet
{

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try
        {
            Object mutex = new Object();
            URL url = new URL(request.getParameter("url"));

            synchronized(mutex)
            {
                //avoid concurent downloads
                downloadProgram(url, response);
            }
        }
        finally
        {
            out.close();
        }
    }

    private void downloadProgram(URL url, HttpServletResponse response) throws IOException
    {
        String programs = getServletContext().getRealPath("programs");

        //download before
        download(url, programs);

        response.setContentType("application/octet-stream");
        OutputStream out = response.getOutputStream();
        File requestedFile = new File(programs + "/" + toDiskName(url));
        InputStream is = new FileInputStream(requestedFile);

        byte[] buf = new byte[1024];
        int len;
        while ((len = is.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        is.close();
        out.close();
    }

    private void download(URL url, String path) throws IOException
    {
        String filename = toDiskName(url);
        HttpClient httpclient = new DefaultHttpClient();
        HttpGet httpget = new HttpGet(url.toString());
        HttpResponse response = httpclient.execute(httpget);
        HttpEntity entity = response.getEntity();
        if (entity != null)
        {
            InputStream instream = entity.getContent();
           
            OutputStream out = new FileOutputStream(path + "/" + toDiskName(url));
            int length;
            byte[] tmp = new byte[2048];
            while ((length = instream.read(tmp)) != -1)
            {
                out.write(tmp, 0, length);
            }
            out.close();
        }
    }

    private String toDiskName(URL url)
    {
        return url.getPath().substring(url.getPath().lastIndexOf("/") + 1) + "-" + sha(url.toString());
    }

    private String sha(String url)
    {
        try
        {
            MessageDigest md = MessageDigest.getInstance("SHA");
            return new String(md.digest(url.getBytes()));
        }
        catch (NoSuchAlgorithmException ex)
        {
            Logger.getLogger(ProgramDownloader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "234";
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>
}
