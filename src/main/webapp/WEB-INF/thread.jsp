<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="modkiwi.data.ArticleInfo" %>
<%@ page import="modkiwi.data.ThreadInfo" %>
<%@ page import="modkiwi.data.UserInfo" %>
<%@ page import="com.google.appengine.api.users.User" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<%
ThreadInfo threadinfo = (ThreadInfo)request.getAttribute("threadinfo");
String[] usernames = (String[])request.getAttribute("usernames");
boolean[] show = (boolean[])request.getAttribute("showUsers");
Map<String, UserInfo> userinfo = (Map<String, UserInfo>)request.getAttribute("userinfo");
int len = usernames.length;
%>

<html>
<head>
  <title>Modkiwi - <%= threadinfo.getSubject() %></title>
  <script src="webjars/jquery/2.1.4/jquery.min.js"> </script>
  <!-- <link rel="stylesheet" type="text/css" href="//cf.geekdo-static.com/static/css_master2_56018f7495c65.css"> -->
  <link rel="stylesheet" href="webjars/bootstrap/3.3.5/css/bootstrap.min.css">
  <link rel="stylesheet" href="webjars/font-awesome/4.4.0/css/font-awesome.min.css">
  <link rel="stylesheet" type="text/css" href="../main.css">
  <script type="text/javascript">
    function toggle(username) {
      setVis(username, !$('[data-toggle-author="' + username + '"]').hasClass("active"));
    }
    function setVis(username, show) {
      if (show) {
        $('li[data-toggle-author="' + username + '"]').addClass("active");
        $('div[data-article-author="' + username + '"]').show();
      } else {
        $('[data-toggle-author="' + username + '"]').removeClass("active");
        $('[data-article-author="' + username + '"]').hide();
      }
    }
    $(document).ready(function() {
<% for (int i = 0; i < len; i++) { %>
      setVis("<%= usernames[i] %>", <%= show[i]%>);
<% } %>
    });
  </script>
</head>
<body>
  <nav class="navbar navbar-default navbar-fixed-top">
    <div class="container-fluid">
      <div class="navbar-header">
        <button type="button" class="navbar-toggle collapsed" data-toggle="collapse" data-target="#bs-example-navbar-collapse-5" aria-expanded="false">
          <span class="sr-only">Toggle navigation</span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
          <span class="icon-bar"></span>
        </button>
        <a class="navbar-brand" href="/">Modkiwi System</a>
      </div>
      <div class="collapse navbar-collapse">
        <p class="navbar-text">
          <% if (false) { %>
          <!-- Viewing posts for <strong><%= usernames[0] %></strong> -->
          <% } %>
        </p>
      </div>
    </div>
  </nav>
  <div class="container">
      <div class="row">
    <div class="col-md-2">
      <ul class="nav nav-pills nav-stacked">
<% for (int i = 0; i < len; i++) { %>
<li role="presentation" data-toggle-author="<%= usernames[i] %>"><a onclick="toggle('<%= usernames[i] %>')" href="#"><%= usernames[i] %></a></li>
<% } %>
      </ul>
    </div>
    <div class="col-md-10">
    <div class="bgg">
      <% for (ArticleInfo article : threadinfo.getArticles()) {
        UserInfo ui = userinfo.get(article.getUsername());%>
        <div class="bgg-article " data-parent_objectid="<%= threadinfo.getId() %>" data-parent_objecttype="thread" data-objectid="<%= article.getId() %>" data-objecttype="article" data-article-author="<%= article.getUsername() %>">
          <div class = "bgg-article-avatarblock">
            <div><%= ui.getFirstName() %> <%= ui.getLastName() %></div>
            <div class="username">(<a href="/user/<%= article.getUsername() %>"><%= article.getUsername() %></a>)</div>
            <div><img height="64px" width="auto" src="<%= ui.getAvatarLink() %>"</img></div>
          </div>
          <div class="bgg-article-body"><%= article.getBody() %></div>
          <div class="bgg-article-footer">
            <ul class="information">
              <li><a href="http://boardgamegeek.com/article/<%= article.getId() %>#<%= article.getId() %>"><i class="fa fa-clock-o"></i> Posted <%= article.getPostDate() %></a></li>
              <li><a href="http://boardgamegeek.com/article/quote/<%= article.getId() %>"><i class="fa fa-quote-left"></i> Quote</a></li>
            </ul>
          </div>
          <div class="clear"></div>
        </div>
      <% } %>
    </div>
    </div>
  </div>
  </div>
</body>
</html>
