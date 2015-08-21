package modkiwi.data;

import java.util.LinkedList;
import java.util.List;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;

public class GameInfo
{
    private String id, acronym, statusPost, gameStatus, historyPost, signupPost, thread, gametype, index, title, lastScanned;
    private List<String> mods, players, moves;
    private EmbeddedEntity data;
    private final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    public GameInfo(Entity ent)
    {
        id = ent.getKey().getName();
        acronym = (String)ent.getProperty("acronym");
        statusPost = (String)ent.getProperty("status_post");
        gameStatus = (String)ent.getProperty("game_status");
        historyPost = (String)ent.getProperty("history_post");
        signupPost = (String)ent.getProperty("signup_post");
        thread = (String)ent.getProperty("thread");
        gametype = (String)ent.getProperty("gametype");
        index = (String)ent.getProperty("index");
        title = (String)ent.getProperty("title");
        lastScanned = (String)ent.getProperty("last_scanned");

        mods = (List<String>)ent.getProperty("mods");
        if (mods == null)
            mods = new LinkedList<String>();

        players = (List<String>)ent.getProperty("players");
        if (players == null)
            players = new LinkedList<String>();

        moves = (List<String>)ent.getProperty("moves");
        if (moves == null)
            moves = new LinkedList<String>();

        data = (EmbeddedEntity)ent.getProperty("data");
        if (data == null)
            data = new EmbeddedEntity();
    }

    public String getId()
    {
        return id;
    }

    public String getAcronym()
    {
        return acronym;
    }

    public String getStatusPost()
    {
        return statusPost;
    }

    public String getGameStatus()
    {
        return gameStatus;
    }

    public String getHistoryPost()
    {
        return historyPost;
    }

    public String getSignupPost()
    {
        return signupPost;
    }

    public String getThread()
    {
        return thread;
    }

    public String getGametype()
    {
        return gametype;
    }

    public String getIndex()
    {
        return index;
    }

    public String getTitle()
    {
        return title;
    }

    public String getLastScanned()
    {
        return lastScanned;
    }

    public List<String> getMods()
    {
        return mods;
    }

    public List<String> getPlayers()
    {
        return players;
    }

    public List<String> getMoves()
    {
        return moves;
    }

    public EmbeddedEntity getData()
    {
        return data;
    }

    public String getFullTitle()
    {
        return String.format("%s #%s: %s", gametype, index, title);
    }

    public String getPrefix()
    {
        return String.format("%s #%s", gametype, index);
    }

    public String getPlayerList()
    {
        if (getPlayers() == null)
            return "";

        String list = null;
        for (String player : getPlayers())
        {
            if (list == null)
                list = player;
            else
                list += ", " + player;
        }

        return list;
    }

    public String getModeratorList()
    {
        if (getMods() == null)
            return "";

        String list = null;
        for (String mod : getMods())
        {
            if (list == null)
                list = mod;
            else
                list += ", " + mod;
        }

        return list;
    }

    public void save()
    {
        Entity ent = new Entity("Game", id);
        ent.setProperty("acronym", acronym);
        ent.setProperty("game_status", gameStatus);
        ent.setProperty("status_post", statusPost);
        ent.setProperty("history_post", historyPost);
        ent.setProperty("signup_post", signupPost);
        ent.setProperty("thread", thread);
        ent.setProperty("gametype", gametype);
        ent.setProperty("index", index);
        ent.setProperty("title", title);
        ent.setProperty("last_scanned", lastScanned);

        ent.setProperty("mods", mods);
        ent.setProperty("players", players);
        ent.setProperty("moves", moves);

        ent.setProperty("data", data);

        datastore.put(ent);
    }

    public void setGameStatus(String status)
    {
        gameStatus = status;
    }

    public void setLastScanned(String id)
    {
        lastScanned = id;
    }
}
