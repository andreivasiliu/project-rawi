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
                    HashMap<Long, String> fileNames = s.getFileIdsAndNames();
                    if (!fileNames.isEmpty()) {
                        out.println("Files: <br />");

                        for (Long fileId : fileNames.keySet()) {
                            String fileName = fileNames.get(fileId);
                    %>
                    <li>
                        <a href="TheDownloadServlet/<%=s.getId()%>/<%= fileId%>/<%= fileName%>">
                            <%= fileId%>/<%= fileName%>
                        </a>

                        <a href="TheDownloadServlet/<%=s.getId()%>/<%= fileId%>/<%= fileName%>?delete=true">
                            <br />(Delete <%= fileId%>/<%= fileName%>)
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
