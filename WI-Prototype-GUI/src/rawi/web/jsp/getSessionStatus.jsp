<?xml version="1.0" encoding="UTF-8"?>
<%@page contentType="text/xml" pageEncoding="UTF-8"%>
<%@ page import="rawi.web.servlets.*" %>
<%@ page import="rawi.web.classes.*" %>
<%@ page import="rawi.common.*" %>
<%@ page import="rawi.common.WorkSessionStatus.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

<%!
    int checkInt(String intString, int defaultValue)
    {
        if (intString == null || intString.isEmpty())
            return defaultValue;

        try
        {
            return Integer.parseInt(intString);
        }
        catch (NumberFormatException e)
        {
            return defaultValue;
        }
    }
%>

<%
    MainBean mainBean = (MainBean) getServletContext().getAttribute("mainBean");
    Integer sessionId = checkInt(request.getParameter("sessionId"), 0);
    Integer subStateOffset = checkInt(request.getParameter("subStateOffset"), 0);
    Integer maxSubStates = checkInt(request.getParameter("maxSubStates"), 10);
    Session workSession = mainBean.getSessionById(sessionId);

    if (workSession == null)
    {
        response.sendError(404, "Work Session not found.");
        return;
    }

    WorkSessionStatus sessionStatus = mainBean.getSessionStatus(sessionId,
            subStateOffset, maxSubStates);
%>
<workSession
   xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'
   xmlns='http://www.example.org/WorkSession'
   xsi:schemaLocation='http://www.example.org/WorkSession WorkSession.xsd'>

    <globalStatus working="<%= sessionStatus.working %>" />
    <uploadUrl><%= workSession.sessionInfo.uploadUrl %></uploadUrl>

    <nodeInstances>
     <% for (Pack pack : sessionStatus.getPacks()) { %>
        <packNode id="<%= pack.id %>" subStateOffset="<%= pack.subStateOffset %>" subStateCount="<%= pack.subStates %>"
                  emptySubPacks="<%= pack.emptySubPacks %>" fullSubPacks="<%= pack.fullSubPacks %>">
         <% for (int subState = pack.subStateOffset; subState < pack.subStateOffset + pack.subStatesShown; subState++) { %>
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
        <packTransformerNode id="<%= transformer.id %>" subStateOffset="<%= transformer.subStateOffset %>" subStateCount="<%= transformer.subStates %>"
                             dnmTasks="<%= transformer.dnmTasks %>" pendingTasks="<%= transformer.pendingTasks %>" workingTasks="<%= transformer.workingTasks %>" doneTasks="<%= transformer.doneTasks %>">
         <% for (int subState = transformer.subStateOffset; subState < transformer.subStateOffset + transformer.subStatesShown; subState++) { %>
            <subState no="<%= subState %>" status="<%= transformer.getStatus(subState) %>" />
         <% } %>
        </packTransformerNode>
     <% } %>
    </nodeInstances>
</workSession>
