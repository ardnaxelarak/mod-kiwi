package modkiwi;

import modkiwi.util.Logger;
import modkiwi.games.GameBot;
import static modkiwi.util.Constants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;

import org.reflections.Reflections;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class CreateGameServlet extends HttpServlet
{
    private static final Logger LOGGER = new Logger(CreateGameServlet.class);
    private static final Random rand = new Random();
    private static final Pattern pattern = Pattern.compile("^modkiwi\\.games\\.Bot(.*)$");

    private String generateId()
    {
        return Integer.toHexString(rand.nextInt());
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        Reflections reflections = new Reflections("modkiwi.games");
        Set<Class<? extends GameBot>> botClasses = reflections.getSubTypesOf(GameBot.class);
        Map<String, String> gametypes = new HashMap<String, String>();

        for (Class<? extends GameBot> botClass : botClasses)
        {
            Matcher m = pattern.matcher(botClass.getName());
            if (m.find())
            {
                String fullname = null;
                try
                {
                    fullname = botClass.getField("LONG_NAME").get(null).toString();
                }
                catch (NoSuchFieldException e)
                {
                    LOGGER.throwing("doGet()", e);
                    fullname = m.group(1);
                }
                catch (IllegalAccessException e)
                {
                    LOGGER.throwing("doGet()", e);
                    fullname = m.group(1);
                }
                gametypes.put(m.group(1), fullname);
            }
        }

        req.setAttribute("gametypes", gametypes);
        try
        {
            req.getRequestDispatcher("/WEB-INF/create.jsp").forward(req, resp);
        }
        catch (ServletException e)
        {
            LOGGER.throwing("doGet()", e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastore.beginTransaction();

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/plain");

        try
        {
            String id = generateId();
            Entity ent = new Entity("Game", id);
            ent.setProperty("gametype", req.getParameter("gametype"));
            ent.setProperty("index", req.getParameter("index"));
            ent.setProperty("title", req.getParameter("name"));
            ent.setProperty("acronym", req.getParameter("acronym"));
            ent.setProperty("thread", req.getParameter("thread"));
            if (req.getParameter("mods") != null)
                ent.setProperty("mods", Arrays.asList(req.getParameter("mods").split(",")));
            ent.setProperty("signup_post", req.getParameter("signup"));
            ent.setProperty("status_post", req.getParameter("status"));
            ent.setProperty("history_post", req.getParameter("history"));
            if (req.getParameter("max_players") != null)
            {
                try
                {
                    ent.setProperty("max_players", Integer.parseInt(req.getParameter("max_players")));
                }
                catch (NumberFormatException e)
                {
                    LOGGER.warning("max_players: cannot parse '%s'", req.getParameter("max_players"));
                }
            }
            else
            {
                LOGGER.info("no value for max_players");
            }
            ent.setProperty("game_status", STATUS_IN_SIGNUPS);
            datastore.put(ent);
            txn.commit();
            resp.sendRedirect("/game/" + id);
        }
        finally
        {
            if (txn.isActive())
                txn.rollback();
        }
    }
}
