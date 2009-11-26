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

public class SetMaxAgeServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        int time = Integer.parseInt(request.getParameter("expTime"));
        GetIPServlet.setExpirationTime(time);

        out.println("<p>Maximum age successfully set at: " +
                GetIPServlet.getExpirationTime() +
                " minutes. </p>");
        out.println("<a href=\"index.jsp\"> Back </a>");
    }

}
