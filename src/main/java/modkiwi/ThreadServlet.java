package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.util.WebUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

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

        for (ArticleInfo article : ti.getArticles())
        {
            if (username == null || username.equalsIgnoreCase(article.getUsername()))
                articles.add(article);
        }

        req.setAttribute("articles", articles);
        req.setAttribute("thread", thread);

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
