<?xml version="1.0" encoding="UTF-8"?>
<%@page contentType="text/xml" pageEncoding="UTF-8"%>
<%@ page import="servlets.*" %>
<%@ page import="classes.*" %>
<%@ page import="rawi.common.*" %>
<%@ page import="rawi.common.WorkSessionStatus.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>
<%
    MainBean mainBean = (MainBean) getServletContext().getAttribute("mainBean");
    Integer sessionId = Integer.parseInt(request.getParameter("sessionId"));
    Session workSession = mainBean.getSessionById(sessionId);
    WorkSessionStatus sessionStatus = mainBean.getSessionStatus(sessionId);
%>
<workSession
   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
   xmlns='http://www.example.org/WorkSession'
   xsi:schemaLocation='http://www.example.org/WorkSession WorkSession.xsd'>

    <globalStatus working="<%= sessionStatus.working %>" />
    <uploadUrl><%= workSession.sessionInfo.uploadUrl %></uploadUrl>

    <nodeInstances>
     <% for (Pack pack : sessionStatus.getPacks()) { %>
        <packNode id="<%= pack.id %>">
         <% for (int subState = 0; subState < pack.subStates; subState++) { %>
            <subState no="<%= subState %>" status="<%= pack.getStatus(subState) %>">
             <% if (pack.status.get(subState) != PackStatus.EMPTY) { %>
             <% for (FileHandle file : pack.subStateFiles.get(subState)) { %>
                <file name="<%= file.getLogicalName() %>" id="<%= file.getId() %>"
                      url="<%= file.getFileURL() %>" />
             <% } } %>
            </subState>
         <% } %>
        </packNode>
     <% } %>

     <% for (PackTransformer transformer : sessionStatus.getPackTransformers()) { %>
     <packTransformerNode id="<%= transformer.id %>">
         <% for (int subState = 0; subState < transformer.subStates; subState++) { %>
            <subState no="<%= subState %>" status="<%= transformer.getStatus(subState) %>" />
         <% } %>
        </packTransformerNode>
     <% } %>
    </nodeInstances>
</workSession>
