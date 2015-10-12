package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.GeekMailInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.data.GameInfo;
import modkiwi.net.GAEConnection;
import modkiwi.net.NetConnection;
import modkiwi.net.WebResponse;
import modkiwi.util.DatastoreUtils;
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

        for (GameInfo game : DatastoreUtils.allGames())
        {
            game.fixNicknames();
        }
    }
}
