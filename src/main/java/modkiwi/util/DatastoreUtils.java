package modkiwi.util;

import modkiwi.data.GameInfo;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class DatastoreUtils {
    private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    private DatastoreUtils() {
    }

    public static List<GameInfo> allGames() {
        List<GameInfo> list = new LinkedList<GameInfo>();
        Query q = new Query("Game");
        PreparedQuery pq = datastore.prepare(q);

        for (Entity ent : pq.asIterable()) {
            list.add(new GameInfo(ent));
        }

        return list;
    }

    public static List<GameInfo> gamesByStatus(String... status) { List<GameInfo> list = new LinkedList<GameInfo>();
        Filter statusFilter = new FilterPredicate("game_status", FilterOperator.IN, Arrays.asList(status));
        Query q = new Query("Game").setFilter(statusFilter);
        PreparedQuery pq = datastore.prepare(q);

        for (Entity ent : pq.asIterable()) {
            list.add(new GameInfo(ent));
        }

        return list;
    }

    public static GameInfo loadGame(String gameid) {
        Key key = KeyFactory.createKey("Game", gameid);
        Entity ent = null;

        try {
            ent = datastore.get(key);
        } catch (EntityNotFoundException e) {
            return null;
        }

        return new GameInfo(ent);
    }

    public static int getInt(Object object) {
        return ((Number)object).intValue();
    }

    public static int getInt(Object object, int def) {
        if (object == null)
            return def;
        else
            return getInt(object);
    }

    public static long getLong(Object object) {
        return ((Number)object).longValue();
    }

    public static long getLong(Object object, long def) {
        if (object == null)
            return def;
        else
            return getLong(object);
    }
}
