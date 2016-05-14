package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.GameInfo;
import modkiwi.data.GeekMailInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.games.BotManager;
import modkiwi.games.GameBot;
import modkiwi.util.DatastoreUtils;
import modkiwi.util.Logger;
import modkiwi.util.Utils;
import modkiwi.util.WebUtils;
import static modkiwi.util.Constants.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.http.*;

@SuppressWarnings("serial")
public class ScanServlet extends HttpServlet
{
    private static final Logger LOGGER = new Logger(ScanServlet.class);
    private static final Object scanLock = new Object();

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException {
        GameInfo single = null;
        synchronized (scanLock) {
            resp.setContentType("text/plain");
            PrintWriter pw = resp.getWriter();

            WebUtils web = new WebUtils();
            web.login();

            List<GameInfo> gameList;

            String id = req.getParameter("id");

            if (id != null) {
                single = DatastoreUtils.loadGame(id);
                if (single == null) {
                    gameList = Collections.emptyList();
                } else {
                    gameList = Collections.singletonList(single);
                }
            } else {
                gameList = DatastoreUtils.gamesByStatus(STATUS_IN_SIGNUPS, STATUS_IN_PROGRESS, STATUS_FINISHED);
            }

            for (GameInfo game : gameList) {
                LOGGER.fine("Scanning %s", game.getFullTitle());

                ThreadInfo ti;
                if (game.getLastScanned() != null) {
                    ti = web.getThread(game.getThread(), Integer.toString(Integer.parseInt(game.getLastScanned()) + 1));
                } else {
                    ti = web.getThread(game.getThread());
                }

                LinkedList<GeekMailInfo> gms = web.getMail(game.getId(), game.getLastMail());

                LOGGER.finer("%d new articles for %s", ti.getArticles().length, game.getFullTitle());
                LOGGER.finer("%d new mail for %s", gms.size(), game.getFullTitle());

                if (ti.getArticles().length == 0 && gms.size() == 0) {
                    continue;
                }

                GameBot bot = BotManager.getBot(game);
                bot.startScanning();

                ArticleInfo[] articles = ti.getArticles();
                for (ArticleInfo article : articles) {
                    String username = article.getUsername();

                    if (username.equals(web.getUsername())) {
                        continue;
                    }

                    for (String command : article.getCommands()) {
                        bot.parseCommand(username, command);
                    }
                }

                for (GeekMailInfo gm : gms) {
                    bot.parseGeekmail(gm.getSender(), gm.getSubject(), gm.getContent());
                }

                if (game == single && req.getParameter("update") != null) {
                    bot.forceUpdate();
                }

                bot.finishedScanning();
                if (articles.length > 0) {
                    game.setLastScanned(articles[articles.length - 1].getId());
                }

                if (gms.size() > 0) {
                    game.setLastMail(gms.getLast().getId());
                }

                game.save();
            }
        }

        if (req.getParameter("redirect") != null && single != null) {
            resp.sendRedirect("https://boardgamegeek.com/thread/" + single.getThread() + "/new");
        }
    }
}
