package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.net.GAEConnection;
import modkiwi.net.NetConnection;
import modkiwi.net.WebResponse;
import modkiwi.util.WebUtils;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.*;

public class AdminServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        WebResponse response;

        resp.setContentType("text/plain");
        PrintWriter pw = resp.getWriter();
        pw.println("Creating Helper...");
        WebUtils web = new WebUtils(new NetConnection(pw));

        pw.println("Logging in...");
        response = web.login();
        pw.println(response.getFinalUrl());

        pw.println("Sending geekmail...");
        response = web.geekmail("Kiwi13cubed", "Test", "silly fruits!");
        pw.println(response.getFinalUrl());

        pw.println("geekmail sent...");
        /*
        web.replyThread("1140569", null, "huh?");
        ThreadInfo t = web.getThread("1140569");
        ArticleInfo[] a = t.getArticles();
        System.out.println(a[a.length - 1].getBody());
        */
    }
}
