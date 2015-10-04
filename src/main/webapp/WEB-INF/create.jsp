<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<% Map<String, String> gametypes = (Map<String, String>)request.getAttribute("gametypes"); %>

<html>
<head>
    <link type="text/css" rel="stylesheet" href="../main.css" />
    <script src="../webjars/jquery/2.1.4/jquery.min.js"> </script>
</head>

<body>
    <form action="create" method="post">
        Game Type:
        <select name="gametype">
            <% for (Map.Entry<String, String> entry : gametypes.entrySet()) { %>
            <option value="<%= entry.getKey() %>"><%= entry.getValue() %></option>
            <% } %>
        </select>
        <br>
        Index: <input type="text" name="index">(something to identify which game it is--e.g., for TimeBomb game number 3, this would be 3. For WW, make something up, or if it's part of a set, choose something that describes that--for Single Serve 5, you might choose SS5)<br>
        Name: <input type="text" name="name"><br>
        Acronym: <input type="text" name="acronym">(if game name has an acronym, enter the full phrase here so that players can guess it)<br>
		Thread: <input type="text" name="thread">(the thread id; the thread located is at boardgamegeek.com/thread/<b><i>thread_id</i></b>/name_of_thread)<br>
        Player Count: <input type="text" name="max_players">(enter maximum number of players or leave blank)<br>
        Moderator(s): <input type="text" name="mods">(enter as comma-separated list: e.g., "Kiwi13cubed,modkiwi,linguistfromhell")<br>
        Create Signup Post: <input type="checkbox" name="signup" checked="true"><br>
        Create Status Post: <input type="checkbox" name="status"><br>
        Create History Post: <input type="checkbox" name="history"><br>
        <input type="submit" value="Submit">
    </form>
</body>
</html>
