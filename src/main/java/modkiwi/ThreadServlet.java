package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.data.UserInfo;
import modkiwi.util.WebUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
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
            articles.add(article);
            if (users.get(author) == null)
                users.put(author, web.getUserInfo(author));
        }

        int len = users.size();
        String[] usernames = users.keySet().toArray(new String[len]);

        Arrays.sort(usernames, String.CASE_INSENSITIVE_ORDER);
        boolean[] show = new boolean[len];
        for (int i = 0; i < len; i++)
        {
            if (username == null || username.equalsIgnoreCase(usernames[i]))
                show[i] = true;
            else
                show[i] = false;
        }

        req.setAttribute("threadinfo", ti);
        req.setAttribute("userinfo", users);
        req.setAttribute("usernames", usernames);
        req.setAttribute("showUsers", show);

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
