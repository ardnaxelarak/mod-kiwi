package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.net.WebResponse;
import modkiwi.net.GAEConnection;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.*;

public class AdminServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        resp.setContentType("text/plain");
        PrintWriter pw = resp.getWriter();
        pw.println("Creating Helper...");
        Helper h = new Helper(new GAEConnection(pw));

        h.testCookies();

        pw.println("Logging in...");
        WebResponse response = h.login();
        pw.println(response.getResponseBody());

        /*
        pw.println("Sending geekmail...");
        WebResponse response = h.geekmail("Kiwi13cubed", "Test", "silly fruits!");
        */

        pw.println("geekmail sent...");
        /*
        h.replyThread("1140569", null, "huh?");
        ThreadInfo t = h.getThread("1140569");
        ArticleInfo[] a = t.getArticles();
        System.out.println(a[a.length - 1].getBody());
        */
    }
}
