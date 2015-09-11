package modkiwi.games;

import modkiwi.data.GameInfo;
import modkiwi.util.DatastoreUtils;
import modkiwi.util.Logger;
import modkiwi.util.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.appengine.api.datastore.EmbeddedEntity;

public class BotRES extends GameBot
{
    public static final String LONG_NAME = "The Resistance";

    private static final Logger LOGGER = new Logger(BotRES.class);

    private int ronud, turn;
    private String step;

    protected BotRES(GameInfo game) throws IOException
    {
        super(game);
    }

    @Override
    public void createGame()
    {
    }

    @Override
    protected CharSequence update()
    {
        if (game.inProgress())
        {
            return null;
        }
        return null;
    }

    @Override
    public void initialize(boolean fresh)
    {
    }

    @Override
    protected void processMove(boolean fresh, String... move)
    {
    }

    @Override
    public void processCommand(String username, String command)
    {
        Matcher m;
        if (game.inSignups())
        {
        }
    }

    @Override
    public CharSequence getCurrentStatus()
    {
        return null;
    }

    @Override
    public String getHistoryItem(String move)
    {
        return move;
    }
}
