<!DOCTYPE HTML>

<%@ page contentType="application/xhtml+xml" pageEncoding="UTF-8"%>
<%@ page import="servlets.*" %>
<%@ page import="classes.*" %>
<%@ page import="rawi.common.*" %>
<%@ page import="rawi.common.WorkSessionStatus.*" %>
<%@ page import="java.util.*" %>
<%@ page import="java.io.*" %>

<%
    MainBean mainBean = (MainBean) getServletContext().getAttribute("mainBean");

    Integer sessionId = Integer.parseInt(request.getParameter("sessionId"));
    WorkSessionStatus sessionStatus = mainBean.getSessionStatus(sessionId);

    // Constants
    final long pack_height = 40;
    final long pack_width = 125;
    final long transformer_radius = 10;

    // Figure out the size of the canvas
    long last_x = 0;
    long last_y = 0;
    
    for (Pack pack : sessionStatus.getPacks())
    {
        if (last_x < pack.x) last_x = pack.x;
        if (last_y < pack.y) last_y = pack.y;
    }

    for (PackTransformer packTransformer : sessionStatus.getPackTransformers())
    {
        if (last_x < packTransformer.x) last_x = packTransformer.x;
        if (last_y < packTransformer.y) last_y = packTransformer.y;
    }
%>

<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
    <head>
        <title>JSP Page</title>
        <style type="text/css">
            .pack {
                stroke: black;
                stroke-width: 1.5;
                fill: rgb(255,220,220);
            }

            .fullPack {
                fill: rgb(220,255,220);
            }

            .packTransformer {
                stroke: black;
                stroke-width: 1;
                fill: rgb(255,220,220);
            }

            .nodeConnection {
                stroke: black;
                stroke-width: 1;
            }
        </style>
    </head>
    <body>
        <h1>Session Status - <%= sessionId %> </h1>
        <svg xmlns="http://www.w3.org/2000/svg"
             width="<%= last_x + 150 %>" height="<%= last_y + 50 %>" version="1.1" >
            <defs>
                <g id="box1">
                    <rect class="pack" x="0" y="0" width="100" height="100"/>
                </g>
                <filter id="MyFilter" x="-20%" y="-20%" width="140%" height="140%">
                    <feFlood result="box" flood-color="black" />
                    <feFlood x="5%" y="5%" result="background" flood-color="rgb(255,220,220)" />
<%--                    <feImage xmlns:xlink="http://www.w3.org/1999/xlink"
                             result="box" xlink:href="#box1" />
--%>
                    <feMerge>
                        <feMergeNode in="box" />
                        <feMergeNode in="background" />
                        <feMergeNode in="SourceGraphic" />
                    </feMerge>
                </filter>
            </defs>

            <%
                for (WorkSessionStatus.Pack from : sessionStatus.getPacks()) {
                    for (PackTransformer to : from.getOutputs()) {
                        %>
                <line class ="nodeConnection" x1="<%= from.x %>" y1="<%= from.y %>"
                      x2="<%= to.x %>" y2="<%= to.y %>" />
                        <%
                    }
                }

                for (WorkSessionStatus.PackTransformer from :
                        sessionStatus.getPackTransformers()) {
                    for (Pack to : from.getOutputs()) {
                        %>
                <line class ="nodeConnection" x1="<%= from.x %>" y1="<%= from.y %>"
                      x2="<%= to.x %>" y2="<%= to.y %>" />
                        <%
                    }
                }

                for (WorkSessionStatus.Pack pack : sessionStatus.getPacks()) {
                    int subPacks = pack.status.size();
                    int completedSubPacks = 0;
                    for (int i = 0; i < subPacks; i++)
                    {
                        if (pack.status.get(i) == WorkSessionStatus.PackStatus.HAS_FILES)
                            completedSubPacks++;
                    }

            %>
                <rect class="pack<%= (subPacks == completedSubPacks) ? " fullPack" : "" %>"
                      x="<%= pack.x - pack_width / 2 %>"
                      y="<%= pack.y - pack_height / 2 %>"
                      width="<%= pack_width %>" height="<%= pack_height %>" />
                <g transform="translate(<%= pack.x + 2 %>, <%= pack.y + 5 %>)">
                    <text text-anchor="middle"
                          style="font-size: small; font-family: Bitstream Vera Sans">
                        <tspan x="0" y="-0.6em"><%= (pack.name != null) ? pack.name : "" %></tspan>
                        <tspan x="0" y="0.6em">(<%= completedSubPacks + "/" + subPacks %>)</tspan>
                    </text>
                </g>
            <%  } %>

            <%
                for (WorkSessionStatus.PackTransformer packTransformer :
                        sessionStatus.getPackTransformers()) {
            %>
                <circle class="packTransformer"
                        cx="<%= packTransformer.x %>" cy="<%= packTransformer.y %>"
                        r="<%= transformer_radius %>" />
                <text text-anchor="start"
                      style="font-size: small; font-family: Bitstream Vera Sans"
                      x="<%= packTransformer.x + transformer_radius + 5 %>"
                      y="<%= packTransformer.y + 5 %>">
                    <%= (packTransformer.name != null) ? packTransformer.name : "" %>
                </text>
            <%  } %>

        </svg>
    </body>
</html>
