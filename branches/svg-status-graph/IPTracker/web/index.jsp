<%-- 
    Document   : index
    Created on : 24.11.2009, 12:14:05
    Author     : Ioana
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="servlets.GetIPServlet" %>
<%@ page import="classes.TrackerBean" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<% TrackerBean tracker = (TrackerBean)
        getServletContext().getAttribute("trackerBean"); %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>IP Tracker</title>
    </head>
    <body>
        <h2>Hello, </h2>

        <form method="post" action="PutIPServlet" >
            <table>
                <tr>
                    <td> my name is: </td>
                    <td> <input type="text" name="name" /> </td>
                </tr>
                <tr>
                    <td>and I am a:</td>
                    <td>
                        <select name="type">
                            <option>WebServer</option>
                            <option>MainServer</option>
                            <option selected>ClusterComputer</option>
                        </select>
                    </td>
                </tr>
                <tr>
                    <td colspan="2">
                        <br /><input type="submit" value="Add me"/>
                    </td>
                </tr>                
            </table>
        </form>
        <br /><hr width="500" align="left" /><br />

        <form method="get" action="GetIPServlet">
            What is the name of all the
            <select name="type">
                <option>WebServer</option>
                <option>MainServer</option>
                <option selected>ClusterComputer</option>
            </select>
            who told you "Hello"... lately?
            <br /> <br />
            <input type="submit" value="Get names"/>
        </form>
        <br /><hr width="500" align="left"/><br />

        <form method="post" action="SetMaxAgeServlet" >
            At every request, entries older than
               <%= tracker.getExpirationTime() %>
            minutes will be deleted. <br />
            I'd like to change the expiration time to
            <input type="text" value="3" name="expTime" size="2"/>
            minutes. <br /><br />
            <input type="submit" value="Change max age"/>
        </form>
        <br /><hr width="500" align="left"/><br />

    </body>
</html>
