package modkiwi.util;

import modkiwi.data.GameInfo;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

import java.util.LinkedList;
import java.util.List;

public final class DatastoreUtils
{
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private DatastoreUtils()
    {
    }

    public static List<GameInfo> gamesByStatus(String status)
    {
        List<GameInfo> list = new LinkedList<GameInfo>();
        Query q = new Query("Game").addFilter("game_status", Query.FilterOperator.EQUAL, status);
        PreparedQuery pq = datastore.prepare(q);

        for (Entity ent : pq.asIterable())
        {
            list.add(new GameInfo(ent));
        }

        return list;
    }
}
