<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
    <link type="text/css" rel="stylesheet" href="main.css" />
    <script src="webjars/jquery/2.1.4/jquery.min.js"> </script>
</head>

<body>
    <form action="a/create" method="post">
        Game Type:
        <select name="gametype">
            <option value="TB">Time Bomb</option>
        </select>
        <br>
        Index: <input type="text" name="index"><br>
        Name: <input type="text" name="name"><br>
        Acronym: <input type="text" name="acronym"><br>
        Thread: <input type="text" name="thread"><br>
        Moderator(s): <input type="text" name="mods"><br>
        Signup Post: <input type="text" name="signup"><br>
        Status Post: <input type="text" name="status"><br>
        History Post: <input type="text" name="history"><br>
        <input type="submit" value="Submit">
    </form>
</body>
</html>
