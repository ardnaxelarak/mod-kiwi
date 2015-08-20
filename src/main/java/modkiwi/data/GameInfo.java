package modkiwi.data;

import java.util.LinkedList;
import java.util.List;

import com.google.appengine.api.datastore.Entity;

import org.apache.commons.lang3.StringUtils;

public class GameInfo
{
    private String id, acronym, statusPost, gameStatus, historyPost, signupPost, thread, gametype, index, title, lastScanned;
    private List<String> mods, players;

    public GameInfo(Entity ent)
    {
        id = ent.getKey().getName();
        acronym = (String)ent.getProperty("acronym");
        statusPost = (String)ent.getProperty("current_status");
        gameStatus = (String)ent.getProperty("game_status");
        historyPost = (String)ent.getProperty("history_post");
        signupPost = (String)ent.getProperty("signup");
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

    public String getFullTitle()
    {
        return String.format("%s #%s: %s", gametype, index, title);
    }

    public String getPlayerList()
    {
        return StringUtils.join(getPlayers(), ", ");
    }

    public String getModeratorList()
    {
        return StringUtils.join(getMods(), ", ");
    }
}
