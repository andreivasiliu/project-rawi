<%-- 
    Document   : msgService
    Created on : 14-Dec-2009, 14:17:17
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
        <h3>Central Message Service</h3>
        <form method="get" action="CentralMessageService" enctype='multipart/form-data'>
            From Index: <input type="text" name="fromId" value="0" /> <br />
            <input type="submit" value="Get Message List"/>
        </form>
    </body>
</html>
