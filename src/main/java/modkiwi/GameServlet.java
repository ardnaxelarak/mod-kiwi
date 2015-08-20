package modkiwi;

import modkiwi.data.*;
import modkiwi.net.NetConnection;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

public class GameServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/plain");

        String gamecode = req.getPathInfo();
        if (gamecode == null)
        {
            pw.println("null gamecode");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        gamecode = gamecode.substring(1);
        Key key = KeyFactory.createKey("Game", gamecode);
        Entity ent = null;

        try
        {
            ent = datastore.get(key);
        }
        catch (EntityNotFoundException e)
        {
            pw.println("entity not found");
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        req.setAttribute("gametype", ent.getProperty("gametype"));
        req.setAttribute("index", ent.getProperty("index"));
        req.setAttribute("title", ent.getProperty("title"));
        req.setAttribute("players", ent.getProperty("players"));
        req.setAttribute("mods", ent.getProperty("mods"));
        req.setAttribute("game_status", ent.getProperty("game_status"));
        try
        {
            req.getRequestDispatcher("/WEB-INF/game.jsp").forward(req, resp);
        }
        catch (ServletException e)
        {
            e.printStackTrace();
        }

        /*
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
                players = new LinkedList<String>();
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
        */
    }
}
