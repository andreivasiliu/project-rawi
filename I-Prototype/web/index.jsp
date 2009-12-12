<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="servlets.*" %>
<%@ page import="classes.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<%
            MainBean mainBean = (MainBean) getServletContext().getAttribute("mainBean");
%>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>I-Prototype</title>
    </head>
    <body>

        <h3>Get lists</h3>
        <a href="GetLists?type=xml">Get list of XML names (TM names)</a> <br />
        <a href="GetLists?type=mainServer">Get list of Main Servers</a> <br />
        <br /><hr width="500" align="left"/><br />


        <h3>Start Session</h3>
        <form method="get" action="StartSession">
            XML Name: <input type="text" name="sessionXMLName" value="0" /> <br />
            <input type="submit" value="Start Session"/>
        </form>
        <br /><hr width="500" align="left"/><br />


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
        <br /><hr width="500" align="left"/><br />


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
        <br /><hr width="500" align="left"/><br />


        <h3>Central Message Service</h3>
        <form method="get" action="CentralMessageService" enctype='multipart/form-data'>
            From Index: <input type="text" name="fromId" value="0" /> <br />
            <input type="submit" value="Get Message List"/>
        </form>
      
    </body>
</html>
