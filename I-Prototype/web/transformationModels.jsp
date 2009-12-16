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
        <h3> Validate and / or save and / or delete TM</h3>
        <form action="ValidateXMLServlet" method="post">
            Name: <input type="text" name="name" value="mymodel.xml" /> <br /><br />
            Content: <br />
            <textarea rows="10" cols="20" name="xml">
                <hello></hello>
            </textarea> <br /><br />
            <input type="checkbox" name="savexml" value="SaveXML" /> Save TM
            <br /><br />
            <input type="submit" value="Validate TM" />
        </form>
        <ul>
            <%
            List<String> xmlList = mainBean.getXmlNamesList();

            if (!xmlList.isEmpty()) {
                out.println("The XML list: <br />");
                for (String xmlName : xmlList) {
            %>
            <li> <%=xmlName%> <br />
                <a href="ValidateXMLServlet?delete=<%=xmlName%>">
                    delete <%=xmlName%>
                </a>
                <br />
                <% }
            }%>
        </ul>

        <br />
        <a href="index.jsp">back</a>
    </body>
</html>
