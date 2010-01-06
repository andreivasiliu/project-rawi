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
        <title>I-Prototype</title>
    </head>
    <body>

        <a href="transformationModels.jsp">Add/Validate/Delete TM</a> <br />
        <a href="sessions.jsp">Sessions section :)</a> <br />
        <a href="downUpLoad.jsp">Download/Upload files/TMs</a> <br />
        <a href="msgService.jsp">Central Message Service</a> <br />
        <br /><hr width="500" align="left"/><br />
        

        <h3>Get lists</h3>
        <a href="GetLists?type=xml">Get list of XML names (TM names)</a> <br />
        <a href="GetLists?type=mainServer">Get list of Main Servers</a> <br />
        <br /><hr width="500" align="left"/><br />
        
    </body>
</html>
