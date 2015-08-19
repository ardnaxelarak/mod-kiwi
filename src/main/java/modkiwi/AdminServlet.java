package modkiwi;

import modkiwi.data.*;
import modkiwi.net.WebResponse;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;
import javax.servlet.http.*;

import com.google.appengine.api.urlfetch.*;

public class AdminServlet extends HttpServlet
{
    private static final Logger LOG = Logger.getLogger(AdminServlet.class.getName());

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        resp.setContentType("text/plain");
        PrintWriter pw = resp.getWriter();
        pw.println("Creating Helper...");
        Helper h = new Helper(pw);

        h.testCookies();

        pw.println("Logging in...");
        WebResponse response = h.login();
        pw.println(response.getResponseBody());

        /*
        pw.println("Sending geekmail...");
        HTTPResponse response = h.geekmail("Kiwi13cubed", "Test", "silly fruits!");
        */

        /*
        Scanner sc = new Scanner(new ByteArrayInputStream(response.getContent()));
        while (sc.hasNextLine())
            pw.println(sc.nextLine());
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
