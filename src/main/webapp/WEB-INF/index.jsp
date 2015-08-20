<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="modkiwi.data.GameInfo" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
List<GameInfo> signups = (List<GameInfo>)request.getAttribute("signups");
List<GameInfo> progress = (List<GameInfo>)request.getAttribute("progress");
%>

<html>
<head>
    <link type="text/css" rel="stylesheet" href="bgg.css" />
    <script src="webjars/jquery/2.1.4/jquery.min.js"> </script>
    <title>Modkiwi System</title>
</head>

<body>
    <h1><div id='name_span'>Modkiwi System</div></h1>
    <table border='0' cellspacing='5'>
        <tr valign='top'>
            <td valign='top'>
                <!-- Games in progress -->
                <table width='100%'class="forum_table" cellspacing="2">
                    <tr>
                        <th colspan='1'>In Progress</th>
                        <th>Moderator</th>
                    </tr>
                    <% for (GameInfo game : progress) { %>
                    <tr>
                        <td><a href='game/<%= game.getId() %>'><%= game.getFullTitle() %></td>
                        <td><%= game.getModeratorList() %></td>
                    </tr>
                    <% } %>
                </table>
            </td>
            <td valign='top'>
                <!-- Games in signups -->
                <table width='100%'class="forum_table" cellspacing="2">
                    <tr>
                        <th colspan='1'>In Signups</th>
                        <th>Moderator</th>
                    </tr>
                    <% for (GameInfo game : signups) { %>
                    <tr>
                        <td><a href='game/<%= game.getId() %>'><%= game.getFullTitle() %> (<%= game.getPlayers().size() %>)</td>
                        <td><%= game.getModeratorList() %></td>
                    </tr>
                    <% } %>
                </table>
            </td>
        </tr>
    </table>
</body>
</html>
