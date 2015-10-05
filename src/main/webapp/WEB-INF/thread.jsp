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
List<ArticleInfo> articles = (List<ArticleInfo>)request.getAttribute("articles");
String thread = (String)request.getAttribute("thread");
List<String> users = (List<String>)request.getAttribute("usernames");
Map<String, UserInfo> userinfo = (Map<String, UserInfo>)request.getAttribute("userinfo");
%>

<html>
<head>
  <script src="webjars/jquery/2.1.4/jquery.min.js"> </script>
  <link rel="stylesheet" type="text/css" href="//cf.geekdo-static.com/static/css_master2_56018f7495c65.css">
  <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/4.4.0/css/font-awesome.min.css">
    <style type="text/css">
      #name_span {
        padding: 10px;
      }
      .article .left {
        min-height: 18px;
      }
      .article .information li {
        display: inline-block;
        margin: 0 6px;
      }
    </style>
    <title>Modkiwi System</title>
</head>

<body class="yui-skin-sam">
  <div id='name_span'>
    <h1>Modkiwi System</h1>
<% if (!users.isEmpty()) { %>
    <h2>Viewing posts for (<a href="/user/<%= users.get(0) %>"><%= users.get(0) %></a>)</h2>
<% } %>
  </div>
    <div id="container">
        <div id="maincontent">
            <table width="100%">
                <tbody>
                    <tr>
                        <td valign="top">
                            <div id="main_content">
<% for (ArticleInfo article : articles) {
    UserInfo ui = userinfo.get(article.getUsername());%>
    <div class="js-rollable article " data-parent_objectid="<%= thread %>" data-parent_objecttype="thread" data-objectid="<%= article.getId() %>" data-objecttype="article" >
        <dl>
            <dd class="left">
                <div class = "avatarblock js-avatar divcenter">
                    <div>
                        <%= ui.getFirstName() %> <%= ui.getLastName() %>
                    </div>
                    <div class="username">
                        (<a href="/user/<%= article.getUsername() %>"><%= article.getUsername() %></a>)
                    </div>
                    <div>
                        <img height="64px" width="auto" src="<%= ui.getAvatarLink() %>"</img>
                    </div>
                <div>
            </dd>
            <dd class="right" ng-non-bindable="">
                <%= article.getBody() %>
            </dd>
        </dl>
        <dl>
      <dd class="left">
      </dd>
      <dd class="commands">
        <ul class="information">
          <li>
              <a href="http://boardgamegeek.com/article/<%= article.getId() %>#<%= article.getId() %>">
              <i class="fa fa-clock-o"></i>
              Posted <%= article.getPostDate() %>
            </a>
          </li>
          <li>
            <a href="http://boardgamegeek.com/article/quote/<%= article.getId() %>"><i class="fa fa-quote-left"></i> Quote</a>
          </li>
        </ul>
      </dd>
    </dl>
        <div class="clear"></div>
    </div>
<% } %>
                            </div>
                        </td/>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</body>
</html>
