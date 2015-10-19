package modkiwi;

import modkiwi.data.*;
import modkiwi.games.*;
import modkiwi.games.util.*;
import modkiwi.net.*;
import modkiwi.util.*;

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
            GameBot bot = BotManager.getBot(game);
            bot.updatePlayerList();
        }
    }
}
