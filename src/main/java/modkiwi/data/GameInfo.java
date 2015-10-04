package modkiwi.data;

import static modkiwi.util.Constants.*;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;

public class GameInfo
{
    private String id, acronym, statusPost, gameStatus, historyPost, signupPost, thread, gametype, index, title, lastScanned;
    private List<String> mods, players, moves, settings;
    private EmbeddedEntity data;
    private int maxPlayers;
    private boolean autoStart;
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

        if (ent.getProperty("auto_start") != null)
            autoStart = (boolean)ent.getProperty("auto_start");
        else
            autoStart = false;

        if (ent.getProperty("max_players") != null)
            maxPlayers = ((Number)ent.getProperty("max_players")).intValue();
        else
            maxPlayers = -1;

        mods = (List<String>)ent.getProperty("mods");
        if (mods == null)
            mods = new LinkedList<String>();

        players = (List<String>)ent.getProperty("players");
        if (players == null)
            players = new LinkedList<String>();

        moves = (List<String>)ent.getProperty("moves");
        if (moves == null)
            moves = new LinkedList<String>();

        settings = (List<String>)ent.getProperty("settings");
        if (settings == null)
            settings = new LinkedList<String>();

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

    public int getMaxPlayers()
    {
        return maxPlayers;
    }

    public List<String> getMods()
    {
        return mods;
    }

    public List<String> getPlayers()
    {
        return players;
    }

    public List<String> getNonPlayerMods()
    {
        LinkedList<String> ret = new LinkedList<String>(getMods());
        ListIterator<String> li = ret.listIterator(0);
        List<String> players = getPlayers();
        String current;
        while (li.hasNext())
        {
            current = li.next();
            for (String player : players)
            {
                if (current.equals(player))
                {
                    li.remove();
                    break;
                }
            }
        }

        return ret;
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
        ent.setProperty("auto_start", autoStart);

        if (maxPlayers > 0)
            ent.setProperty("max_players", maxPlayers);

        ent.setProperty("mods", mods);
        ent.setProperty("players", players);
        ent.setProperty("moves", moves);
		ent.setProperty("settings", settings);

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

    public void setAutoStart(boolean autoStart)
    {
        this.autoStart = autoStart;
    }

    public void setMaxPlayers(int max)
    {
        maxPlayers = max;
    }

    public boolean readyToStart()
    {
        return autoStart && maxPlayers > 0 && players.size() == maxPlayers;
    }

    public void setDataProperty(String key, Object value)
    {
        data.setProperty(key, value);
    }

    public Object getDataProperty(String key)
    {
        return data.getProperty(key);
    }

    public boolean isModerator(String username)
    {
        return getMods().contains(username);
    }

    public boolean inSignups()
    {
        return STATUS_IN_SIGNUPS.equals(getGameStatus());
    }

    public boolean inProgress()
    {
        return STATUS_IN_PROGRESS.equals(getGameStatus());
    }

    public boolean finished()
    {
        return STATUS_FINISHED.equals(getGameStatus());
    }

	public void addSetting(String setting)
	{
		if (!settings.contains(setting))
			settings.add(setting);
	}

	public void removeSetting(String setting)
	{
		settings.remove(setting);
	}

	public boolean hasSetting(String setting)
	{
		return settings.contains(setting);
	}
}
