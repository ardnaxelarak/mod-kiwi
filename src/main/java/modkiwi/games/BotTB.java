package modkiwi.games;

import modkiwi.Helper;
import modkiwi.data.GameInfo;
import modkiwi.util.Logger;
import modkiwi.util.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BotTB extends GameBot
{
    private static final Logger LOGGER = new Logger(BotTB.class);

    protected BotTB(GameInfo game) throws IOException
    {
        super(game);
    }

    @Override
    public void createGame()
    {
    }

    @Override
    public void initialize(boolean fresh)
    {
    }

    @Override
    protected void update()
    {
    }

    @Override
    protected void processMove(boolean fresh, String... move)
    {
    }

    @Override
    public void processCommand(String username, String command)
    {
    }

    @Override
    public String getCurrentStatus()
    {
        return null;
    }

    @Override
    public String getHistoryItem(String move)
    {
        return move;
    }
}
