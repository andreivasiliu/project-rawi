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
        <title>I-Prototype</title>
    </head>
    <body>
        <h2>Message Administration Page</h2>

        <form method="get" action="CentralMessageService" enctype='multipart/form-data'>
            From Index: <input type="text" name="fromId" value="0" /> <br />
            <input type="submit" value="Get XML"/>
        </form><br />

    </body>
</html>
