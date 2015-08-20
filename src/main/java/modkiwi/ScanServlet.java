package modkiwi;

import modkiwi.data.ArticleInfo;
import modkiwi.data.ThreadInfo;
import modkiwi.net.NetConnection;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.*;

public class ScanServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        resp.setContentType("text/plain");
        PrintWriter pw = resp.getWriter();

        Helper h = new Helper(new NetConnection());
        h.login();

        Filter statusFilter = new FilterPredicate("game_status",
                FilterOperator.EQUAL, "signups");
        Query q = new Query("Game")
            .setFilter(statusFilter)
            .addSort("gametype", SortDirection.ASCENDING);

        PreparedQuery pq = datastore.prepare(q);

        for (Entity e : pq.asIterable())
        {
            String thread = e.getProperty("thread").toString();
            String last_scanned = null;
            ThreadInfo ti;
            String acronym = (String)e.getProperty("acronym");
            if (e.getProperty("last_scanned") != null)
            {
                last_scanned = e.getProperty("last_scanned").toString();
                ti = h.getThread(thread, Integer.toString(Integer.parseInt(last_scanned) + 1));
            }
            else
            {
                ti = h.getThread(thread);
            }

            // System.out.printf("%d new articles for %s #%s: %s\n", ti.getArticles().length, e.getProperty("gametype"), e.getProperty("index"), e.getProperty("name"));

            if (ti.getArticles().length == 0)
                continue;

            List<String> players = (List<String>)e.getProperty("players");
            if (players == null)
                players = Collections.emptyList();
            boolean changed = false;

            ArticleInfo[] articles = ti.getArticles();
            String guesses = "";
            for (ArticleInfo article : articles)
            {
                String username = article.getUsername();
                if (username.equals(h.getUsername()))
                    continue;
                for (String command : article.getCommands())
                {
                    if (command.equalsIgnoreCase("signup"))
                    {
                        if (!players.contains(username))
                        {
                            players.add(username);
                            changed = true;
                        }
                    }
                    if (command.equalsIgnoreCase("remove"))
                    {
                        if (players.remove(username))
                            changed = true;
                    }
                    if (command.toLowerCase().startsWith("guess") &&
                            acronym != null)
                    {
                        String guess = command.substring(6);
                        String[] parts = guess.split(" ");
                        String[] aparts = acronym.split(" ");
                        int count = 0;
                        int len = Math.min(parts.length, aparts.length);
                        for (int i = 0; i < len; i++)
                            if (parts[i].equalsIgnoreCase(aparts[i]))
                                count++;

                        guesses += String.format("[q=\"%s\"][b]%s[/b][/q][color=#008800]%d / %d[/color]\n", username, guess, count, aparts.length);
                    }
                }
            }

            if (!guesses.equals(""))
            {
                h.replyThread(thread, null, guesses);
            }

            // Update post containing signup list
            if (changed && e.hasProperty("signup"))
            {
                String signup_id = e.getProperty("signup").toString();
                Collections.sort(players, String.CASE_INSENSITIVE_ORDER);
                String listText;
                listText = "[color=#008800][u]Player list according to ModKiwi:[/u]\n";
                for (String username : players)
                    listText += username + "\n";

                listText += "\n" + players.size() + " players are signed up.\n\n";
                listText += "To sign up for this game, post [b]signup[/b] in bold.\nTo remove yourself from this game, post [b]remove[/b] in bold.[/color]";
                h.edit(signup_id, "Signup List", listText);

                e.setProperty("players", players);
            }

            e.setProperty("last_scanned", articles[articles.length - 1].getId());
            datastore.put(e);
        }
    }
}
