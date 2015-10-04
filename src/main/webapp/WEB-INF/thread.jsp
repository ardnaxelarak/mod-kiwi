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
    <script src="webjars/jquery/2.1.4/jquery.min.js"> </script>
	<link rel="stylesheet" type="text/css" href="//cf.geekdo-static.com/static/css_master2_56018f7495c65.css">
    <title>Modkiwi System</title>
</head>

<body class="yui-skin-sam">
    <h1><div id='name_span'>Modkiwi System</div></h1>
	<div id="container">
		<div id="maincontent">
			<table width="100%">
				<tbody>
					<tr>
						<td valign="top">
							<div id="main_content">
<% for (ArticleInfo article : articles) { %>
	<div class="js-rollable article " data-parent_objectid="<%= thread %>" data-parent_objecttype="thread" data-objectid="<%= article.getId() %>" data-objecttype="article" >
		<dl>
			<dd class="left">
				<div class = "avatarblock js-avatar divcenter">
					<div class="username">
						(<a href="/user/<%= article.getUsername() %>"><%= article.getUsername() %></a>)
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
				<ul>
				</ul>
				<ul class="information">
					<li>
						<a href="/article/<%= article.getId() %>">
							<img class="icon i_icon_minipost" title="" alt="" style=" height:9px;width:12px;" src="data:image/gif;base64,R0lGODlhAQABAIAAAP///wAAACH5BAEAAAAALAAAAAABAAEAAAICRAEAOw=="></img>
							Posted <%= article.getPostDate() %>
						</a>
					</li>
					<li>
						<ul class=<commands">
							<li>
								<a href="/article/quote/<%= article.getId() %>">Quote</a>
							</li>
						</ul>
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
