package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.GeekMailInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.net.GAEConnection;
import modkiwi.net.NetConnection;
import modkiwi.net.WebResponse;
import modkiwi.util.WebUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
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

        WebUtils web = new WebUtils();
        web.login();

        String id = req.getParameter("id");
        List<GeekMailInfo> list = web.getMail(id);

        for (GeekMailInfo mail : list)
        {
            pw.printf("From: %s\nSubject: %s\nID: %s\n%s\n--------------------\n", mail.getSender(), mail.getSubject(), mail.getId(), mail.getContent());
        }
    }
}
