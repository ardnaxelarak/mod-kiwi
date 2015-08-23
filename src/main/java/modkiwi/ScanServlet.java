package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.GameInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.games.BotManager;
import modkiwi.games.GameBot;
import modkiwi.net.NetConnection;
import modkiwi.util.DatastoreUtils;
import modkiwi.util.Logger;
import modkiwi.util.Utils;
import static modkiwi.util.Constants.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.*;

public class ScanServlet extends HttpServlet
{
    private static final Logger LOGGER = new Logger(ScanServlet.class);
    private static final Object scanLock = new Object();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        synchronized (scanLock)
        {
            resp.setContentType("text/plain");
            PrintWriter pw = resp.getWriter();

            Helper h = new Helper(new NetConnection());
            h.login();

            for (GameInfo game : DatastoreUtils.gamesByStatus(STATUS_IN_SIGNUPS))
            {
                LOGGER.fine("Scanning %s (in signups)", game.getFullTitle());
                GameBot bot = BotManager.getBot(game);

                ThreadInfo ti;
                if (game.getLastScanned() != null)
                {
                    ti = h.getThread(game.getThread(), Integer.toString(Integer.parseInt(game.getLastScanned()) + 1));
                }
                else
                {
                    ti = h.getThread(game.getThread());
                }

                LOGGER.finer("%d new articles for %s", ti.getArticles().length, game.getFullTitle());

                if (ti.getArticles().length == 0)
                    continue;

                boolean changed = false;

                ArticleInfo[] articles = ti.getArticles();
                List<String> guesses = new LinkedList<String>();
                for (ArticleInfo article : articles)
                {
                    String username = article.getUsername();
                    if (username.equals(h.getUsername()))
                        continue;
                    for (String command : article.getCommands())
                    {
                        if (bot.processSignupCommand(username, command, guesses))
                            changed = true;
                    }
                }

                if (!guesses.isEmpty())
                {
                    h.replyThread(game.getThread(), null, Utils.join(guesses, "\n"));
                }

                // Update post containing signup list
                if (changed)
                {
                    bot.updatePlayerList();
                }

                if (game.readyToStart())
                {
                    LOGGER.config("%s is full! Beginning game.", game.getFullTitle());
                    bot.startGame();
                }
                else if (game.getMaxPlayers() > 0)
                {
                    LOGGER.config("%s has %d / %d players.", game.getFullTitle(), game.getPlayers().size(), game.getMaxPlayers());
                }

                game.setLastScanned(articles[articles.length - 1].getId());
                game.save();
            }

            for (GameInfo game : DatastoreUtils.gamesByStatus(STATUS_IN_PROGRESS))
            {
                LOGGER.fine("Scanning %s (in progress)", game.getFullTitle());
                GameBot bot = BotManager.getBot(game);

                ThreadInfo ti;
                if (game.getLastScanned() != null)
                {
                    ti = h.getThread(game.getThread(), Integer.toString(Integer.parseInt(game.getLastScanned()) + 1));
                }
                else
                {
                    ti = h.getThread(game.getThread());
                }

                LOGGER.finer("%d new articles for %s", ti.getArticles().length, game.getFullTitle());

                if (ti.getArticles().length == 0)
                    continue;

                boolean changed = false;

                ArticleInfo[] articles = ti.getArticles();
                for (ArticleInfo article : articles)
                {
                    String username = article.getUsername();
                    if (username.equals(h.getUsername()))
                        continue;
                    for (String command : article.getCommands())
                    {
                        bot.processCommand(username, command);
                    }
                }

                game.setLastScanned(articles[articles.length - 1].getId());
                game.save();
            }
        }
    }
}
