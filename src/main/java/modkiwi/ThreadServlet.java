package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.data.UserInfo;
import modkiwi.util.WebUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class ThreadServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/plain");

        WebUtils web = new WebUtils();
        List<ArticleInfo> articles = new LinkedList<ArticleInfo>();

        String thread = req.getParameter("thread");
        String username = req.getParameter("username");
        ThreadInfo ti = web.getThread(thread);

        Map<String, UserInfo> users = new HashMap<String, UserInfo>();

        for (ArticleInfo article : ti.getArticles())
        {
            String author = article.getUsername();
            if (username == null || username.equalsIgnoreCase(author))
            {
                articles.add(article);
                if (users.get(author) == null)
                    users.put(author, web.getUserInfo(author));
            }
        }

        req.setAttribute("articles", articles);
        req.setAttribute("thread", thread);
        req.setAttribute("userinfo", users);
        if (username == null)
            req.setAttribute("usernames", Collections.emptyList());
        else
            req.setAttribute("usernames", Collections.singletonList(username));

        try
        {
            req.getRequestDispatcher("/WEB-INF/thread.jsp").forward(req, resp);
        }
        catch (ServletException e)
        {
            e.printStackTrace();
        }
    }
}
