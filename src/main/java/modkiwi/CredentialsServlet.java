package modkiwi;

import modkiwi.data.*;

import com.google.appengine.api.datastore.*;

import java.io.*;
import javax.servlet.http.*;

public class CredentialsServlet extends HttpServlet
{
    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws IOException
    {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        if (username == null || password == null)
        {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Transaction txn = datastore.beginTransaction();

        try {
            Entity ent = new Entity("Credentials", "autobot");
            ent.setProperty("username", username);
            ent.setProperty("password", password);
            datastore.put(ent);
            txn.commit();
        } finally {
            if (txn.isActive())
                txn.rollback();
        }
    }
}
