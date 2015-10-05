<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="modkiwi.data.ArticleInfo" %>
<%@ page import="modkiwi.data.ThreadInfo" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
List<ArticleInfo> articles = (List<ArticleInfo>)request.getAttribute("articles");
String thread = (String)request.getAttribute("thread");
%>

<html>
<head>
    <title>Modkiwi System</title>
    <script src="webjars/jquery/2.1.4/jquery.min.js"> </script>
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
    <style type="text/css">
      body { padding-top: 70px; }
    </style>
</head>

<body>
  <nav class="navbar navbar-default navbar-fixed-top">
    <div class="container">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-5" aria-expanded="false">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="#">Modkiwi System</a>
      </div>
      <div class="collapse navbar-collapse">
        <p class="navbar-text">Viewing posts for <strong><a href="/user/<%= article.getUsername() %>"><%= article.getUsername() %></a></strong></p>
      </div>
    </div>
  </nav>
  <div class="container">
    <% for (ArticleInfo article : articles) { %>
      <div class="panel panel-default"  data-parent_objectid="<%= thread %>" data-parent_objecttype="thread" data-objectid="<%= article.getId() %>" data-objecttype="article" >
        <div class="panel-body">
          <%= article.getBody() %>
        </div>
        <div class="panel-footer text-right">
          <a href="/article/<%= article.getId() %>" class="btn btn-link btn-xs"><i class="fa fa-clock-o"></i> Posted <%= article.getPostDate() %></a>
          <a href="/article/quote/<%= article.getId() %>" class="btn btn-link btn-xs"><i class="fa fa-quote-left"></i> Quote</a>
        </ul>
        </div>
      </div>
    <% } %>
  </div>
</body>
</html>