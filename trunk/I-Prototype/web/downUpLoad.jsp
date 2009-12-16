<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="servlets.*" %>
<%@ page import="classes.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<% MainBean mainBean = (MainBean) getServletContext().getAttribute("mainBean"); %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h3>Download/Upload Section</h3>
        <form method="post" action="TheUploadServlet/0/name/of/the/file.txt"
              enctype='multipart/form-data'>
            <input type="file" name="myFileAttribute" /> <br />
            <input type="submit" value="Upload"/> <br />
        </form><br />

        <ul>
            <%
            List<Session> sessionList = mainBean.getSessionList();

            if (!sessionList.isEmpty()) {
                out.println("Sessions: <br />");
                for (Session s : sessionList) {

            %>
            <li> <%=s.getId()%> <br />
                <ul>
                    <%
                    List<String> fileNames = s.getFileNames();
                    if (!fileNames.isEmpty()) {
                        out.println("Files: <br />");

                        for (String fileName : fileNames) {

                    %>
                    <li>
                        <a href="TheDownloadServlet/<%=s.getId()%>/<%= fileName%>">
                            <%= fileName%>
                        </a>

                        <a href="TheDownloadServlet/<%=s.getId()%>/<%= fileName%>?delete=true">
                            <br />(Delete <%= fileName%>)
                        </a>
                    </li>
                    <% }
                    }%>
                </ul>
            </li>

            <% }
            }%>
        </ul>
    </body>
</html>
