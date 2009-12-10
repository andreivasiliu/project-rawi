<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="servlets.TheUploadServlet" %>
<%@ page import="classes.*" %>
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

        <h3>Download/Upload Section</h3>
        <form method="post" action="TheUploadServlet/0/name/of/the/file.txt"
              enctype='multipart/form-data'>
            <input type="file" name="myFileAttribute" /> <br />
            <input type="submit" value="Upload"/> <br />
        </form><br />

        <ul>
            <%
            List<Session> sessionList =
                    ((MainBean) getServletContext().getAttribute("mainBean")).getSessionList();

            if (!sessionList.isEmpty()) {
                out.println("Sesiunile: <br />");
                for (Session s : sessionList) {

            %>
            <li> <%=s.getId()%> <br />
                <ul>
                    <%
                    List<String> fileNames = s.getFileNames();
                    if (!fileNames.isEmpty()) {
                        out.println("Fisierele: <br />");

                        for (String fileName : fileNames) {

                    %>
                    <li>
                        <a href="TheDownloadServlet/<%= fileName%>">
                            <%= fileName%>
                        </a>

                        <a href="TheDownloadServlet/<%= fileName%>&delete=true">
                            <br />(Delete <%= fileName%>)
                        </a>
                    </li>
                    <% } }%>
                </ul>
            </li>

            <% }
                }%>
        </ul>
        <br /><hr width="500" align="left"/><br />


        <h3> Validate and / or save XML</h3>
        <form action="ValidateXMLServlet" method="post">
            Name: <input type="text" name="name" value="mymodel.xml" /> <br /><br />
            Content: <br />
            <textarea rows="10" cols="20" name="xml">
                <transformationGraph><packNode x="180" name="Scene File1" y="40" id="1"><output node="4" /><output node="2" /></packNode><packTransformerNode x="80" name="Splitter2" y="120" id="2"><input node="1" /><output node="3" /></packTransformerNode><packNode x="80" name="3" y="210" id="3"><input node="2" /><output node="4" /><output node="6" /></packNode><packTransformerNode x="180" name="Renderer4" y="290" id="4"><input node="3" /><input node="1" /><output node="5" /></packTransformerNode><packNode x="180" name="5" y="380" id="5"><input node="4" /><output node="6" /></packNode><packTransformerNode x="80" name="Joiner6" y="450" id="6"><input node="3" /><input node="5" /><output node="7" /></packTransformerNode><packNode x="80" name="Rendered Scene7" y="560" id="7"><input node="6" /></packNode></transformationGraph>
            </textarea> <br /><br />
            <input type="checkbox" name="savexml" value="SaveXML" /> Save XML
            <br /><br />
            <input type="submit" />
        </form>
        <br /><hr width="500" align="left"/><br />


        <h3>Central Message Service</h3>
        <form method="get" action="CentralMessageService" enctype='multipart/form-data'>
            From Index: <input type="text" name="fromId" value="0" /> <br />
            <input type="submit" value="Get XML"/>
        </form>
        <br /><hr width="500" align="left"/><br />


        <h3>Start Session</h3>
        <form method="get" action="StartSession">
            XML Name: <input type="text" name="sessionXMLName" value="0" /> <br />
            <input type="submit" value="Start Session"/>
        </form>


    </body>
</html>
