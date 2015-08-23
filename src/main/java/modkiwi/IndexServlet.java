package modkiwi;

import modkiwi.data.GameInfo;
import static modkiwi.util.Constants.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.*;

public class IndexServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

        PrintWriter pw = resp.getWriter();
        resp.setContentType("text/plain");

        List<GameInfo> signups = new LinkedList<GameInfo>();
        List<GameInfo> progress = new LinkedList<GameInfo>();

        Query q;
        PreparedQuery pq;

        q = new Query("Game").addFilter("game_status", Query.FilterOperator.EQUAL, STATUS_IN_SIGNUPS);
        pq = datastore.prepare(q);

        for (Entity ent : pq.asIterable())
        {
            signups.add(new GameInfo(ent));
        }

        q = new Query("Game").addFilter("game_status", Query.FilterOperator.EQUAL, STATUS_IN_PROGRESS);
        pq = datastore.prepare(q);

        for (Entity ent : pq.asIterable())
        {
            progress.add(new GameInfo(ent));
        }

        req.setAttribute("signups", signups);
        req.setAttribute("progress", progress);
        try
        {
            req.getRequestDispatcher("/WEB-INF/index.jsp").forward(req, resp);
        }
        catch (ServletException e)
        {
            e.printStackTrace();
        }
    }
}
