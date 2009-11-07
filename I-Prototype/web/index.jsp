<%-- 
    Document   : index
    Created on : Nov 6, 2009, 5:03:25 PM
    Author     : Ioana
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h1>Hello World!</h1>

        <form method="post" action="TheUploadServlet" enctype='multipart/form-data'>
            <input type="file" name="myFileAttribute" /> <br />
            <input type="file" name="mySecondFileAttribute" /> <br />
            <input type="submit" value="Upload"/>
        </form><br />
        <a href="TheDownloadServlet">Download this file</a>
    </body>
</html>
