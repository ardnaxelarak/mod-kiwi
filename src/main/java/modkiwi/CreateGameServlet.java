package modkiwi;

import modkiwi.data.*;

import com.google.appengine.api.datastore.*;

import java.io.*;
import java.util.*;
import javax.servlet.http.*;

public class CreateGameServlet extends HttpServlet
{
    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastore.beginTransaction();

        try {
            Key key = datastore.allocateIds("Game", 1).getStart();
            Entity ent = new Entity("Game", key);
            ent.setProperty("gametype", req.getParameter("gametype"));
            ent.setProperty("index", req.getParameter("index"));
            ent.setProperty("name", req.getParameter("name"));
            ent.setProperty("acronym", req.getParameter("acronym"));
            ent.setProperty("thread", req.getParameter("thread"));
            if (req.getParameter("mods") == null)
                ent.setProperty("mods", new ArrayList<String>());
            else
                ent.setProperty("mods", Arrays.asList(req.getParameter("mods").split(",")));
            ent.setProperty("signup", req.getParameter("signup"));
            ent.setProperty("current_status", req.getParameter("status"));
            ent.setProperty("history", req.getParameter("history"));
            ent.setProperty("players", new ArrayList<String>());
            ent.setProperty("data", new EmbeddedEntity());
            ent.setProperty("game_status", "signups");
            datastore.put(ent);
            txn.commit();
        } finally {
            if (txn.isActive())
                txn.rollback();
        }
    }
}
