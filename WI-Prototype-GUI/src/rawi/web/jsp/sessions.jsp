<%@ page import="rawi.web.servlets.*" %>
<%@ page import="rawi.web.classes.*" %>
<%@ page import="java.util.*" %>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<% MainBean mainBean = (MainBean) getServletContext().getAttribute("mainBean");%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Sessions page</title>
    </head>
    <body>
        <h3>Create Session</h3>

        <form action="CreateSession" method="get">

            Choose one of the models: <br />
            <select name="modelName">
                <%
                List<String> xmlList = mainBean.getXmlNamesList();

                if (!xmlList.isEmpty()) {
                    for (String xmlName : xmlList) {
                %>
                <option value="<%=xmlName%>"><%=xmlName%></option>
                <% }
            }%>
            </select> <br /><br />

            Choose one of the main servers: <br />
            <select name="mainServerIp">
                <option></option>
                <%
                List<String> mainServerList = mainBean.getListOfMainServers();

                if (!mainServerList.isEmpty()) {
                    for (String mainServerIp : mainServerList) {
                %>
                <option value="<%=mainServerIp%>"><%=mainServerIp%></option>
                <% }
            }%>
            </select>

            <br /><br />
            <input type="submit" value="Create Session"/>

        </form>

        <br />        
        <a href="index.jsp">back</a>
    </body>
</html>
