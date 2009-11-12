<%-- 
    Document   : index
    Created on : Nov 6, 2009, 5:03:25 PM
    Author     : Ioana
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="servlets.TheUploadServlet" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>I-Prototype</title>
    </head>
    <body>
        <h1>Download/Upload Page</h1>

        <form method="post" action="TheUploadServlet" enctype='multipart/form-data'>
            <input type="file" name="myFileAttribute" /> <br />
            <input type="file" name="mySecondFileAttribute" /> <br />
            <input type="submit" value="Upload"/>
        </form><br />

        <ul>
            <%
            List<File> fileList = TheUploadServlet.getFileList();

            for (int i = 0; i < fileList.size(); i++) {
                File f = fileList.get(i);
                String hrefMsg = f.getName() + "?path=" + f.getPath();
            %>
                <li>
                    <a href="TheDownloadServlet/<%= hrefMsg%>">
                        <%= f.getName()%>
                    </a>
                </li>
            <% } %>
        </ul>

    </body>
</html>
