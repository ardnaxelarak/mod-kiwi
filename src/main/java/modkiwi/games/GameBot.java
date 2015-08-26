package modkiwi.games;

import modkiwi.data.GameInfo;
import modkiwi.util.Logger;
import modkiwi.util.Utils;
import modkiwi.util.WebUtils;
import static modkiwi.util.Constants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class GameBot
{
    private static final Logger LOGGER = new Logger(GameBot.class);

    protected GameInfo game;
    protected final WebUtils web;
    protected int NoP;
    protected String[] players;
    private boolean changed;
    protected List<String> messages = null;
    protected List<String> secretMessages = null;

    protected GameBot(GameInfo game) throws IOException
    {
        this.game = game;
        web = new WebUtils();
        web.login();

        getPlayerData();

        if (game.getGameStatus().equals(STATUS_IN_PROGRESS))
            loadGame();
    }

    private void getPlayerData()
    {
        NoP = game.getPlayers().size();
        players = new String[NoP];
        int k = 0;
        for (String name : game.getPlayers())
            players[k++] = name;
    }

    public abstract void createGame();

    public abstract void initialize(boolean fresh);

    protected abstract CharSequence update();

    private void postUpdate()
    {
        StringBuilder post = new StringBuilder();
        if (messages != null && !messages.isEmpty())
        {
            for (String message : messages)
            {
                post.append(message);
                post.append('\n');
                post.append('\n');
            }
            post.append('\n');
        }

        CharSequence update = update();
        if (update != null)
            post.append(update);

        if (post.length() > 0)
        {
            try
            {
                web.replyThread(game, post.toString());
            }
            catch (IOException e)
            {
                LOGGER.throwing("postUpdate()", e);
            }
        }
        messages.clear();
    }

    protected abstract void processMove(boolean fresh, String... move);

    protected void processAndAddMove(String... move)
    {
        processMove(true, move);
        game.getMoves().add(Utils.join(move, " "));
        changed = true;
    }

    public void startScanning()
    {
        changed = false;
        messages = new LinkedList<String>();
        secretMessages = new LinkedList<String>();
    }

    public void finishedScanning()
    {
        if (changed)
        {
            postUpdate();
            updatePlayerList();
            updateStatus();
            changed = false;
            messages = null;
            secretMessages = null;
        }
    }

    public abstract void processCommand(String username, String command);

    public void parseCommand(String username, String command)
    {
        if (command.toLowerCase().startsWith("guess") &&
                game.getAcronym() != null)
        {
            String guess = command.substring(6);
            String[] parts = guess.split(" ");
            String[] aparts = game.getAcronym().split(" ");
            int count = 0;
            int len = Math.min(parts.length, aparts.length);
            for (int i = 0; i < len; i++)
                if (parts[i].equalsIgnoreCase(aparts[i]))
                    count++;

            addMessage("[q=\"%s\"][b]%s[/b][/q][color=#008800]%d / %d[/color]", username, guess, count, aparts.length);
        }
        else if (game.getGameStatus().equals(STATUS_IN_SIGNUPS))
        {
            if (command.equalsIgnoreCase("signup"))
            {
                if (!game.getPlayers().contains(username))
                {
                    game.getPlayers().add(username);
                    changed = true;
                }
            }
            else if (command.equalsIgnoreCase("remove"))
            {
                if (game.getPlayers().remove(username))
                    changed = true;
            }
            else
            {
                processCommand(username, command);
            }
        }
        else if (game.getGameStatus().equals(STATUS_IN_PROGRESS))
        {
            if (command.equalsIgnoreCase("show status"))
            {
                changed = true;
            }
            else
            {
                processCommand(username, command);
            }
        }
        else
        {
            processCommand(username, command);
        }
    }

    public abstract String getCurrentStatus();

    public void updateStatus()
    {
        String status = getCurrentStatus();
        if (game.getStatusPost() != null && status != null)
        {
            try
            {
                web.edit(game.getStatusPost(), "Current Status", getCurrentStatus());
            }
            catch (IOException e)
            {
                LOGGER.throwing("updateStatus()", e);
            }
        }
    }

    public void updatePlayerList()
    {
        if (game.getSignupPost() == null)
            return;

        boolean signups = "signups".equals(game.getGameStatus());
        String listText;
        if (signups)
        {
            Collections.sort(game.getPlayers(), String.CASE_INSENSITIVE_ORDER);
            listText = "[color=#008800][u]Player list according to ModKiwi:[/u]\n";
            for (String username : game.getPlayers())
                listText += username + "\n";

            listText += "\n" + game.getPlayers().size() + " players are signed up.\n\n";
            listText += "To sign up for this game, post [b]signup[/b] in bold.\nTo remove yourself from this game, post [b]remove[/b] in bold.[/color]";
        }
        else
        {
            listText = "[color=#008800][u]Seating Order:[/u]";
            int k = 1;
            for (String username : game.getPlayers())
                listText += "\n" + k++ + ". " + username;

            listText += "[/color]";
        }

        try
        {
            web.edit(game.getSignupPost(), "Player List", listText);
        }
        catch (IOException e)
        {
            LOGGER.throwing("updatePlayerList()", e);
        }
    }

    public void startGame()
    {
        Collections.shuffle(game.getPlayers());
        game.setGameStatus("progress");
        updatePlayerList();
        getPlayerData();
        createGame();
        initialize(true);
        changed = true;
    }

    public void loadGame()
    {
        initialize(false);
        for (String move : game.getMoves())
        {
            processMove(false, move.split(" "));
        }
    }

    public abstract String getHistoryItem(String move);

    public void endGame()
    {
        // should probably print something more informative
        try
        {
            web.replyThread(game.getThread(), null, "[color=purple][b]Game is over.[/b][/color]");
        }
        catch (IOException e)
        {
            LOGGER.throwing("endGame()", e);
        }

        game.setGameStatus(STATUS_FINISHED);
    }

    public int getPlayerIndex(String username)
    {
        for (int i = 0; i < NoP; i++)
            if (username.equalsIgnoreCase(players[i]))
                return i;

        return -1;
    }

    protected void addMessage(String format, Object... args)
    {
        if (messages != null)
            messages.add(String.format(format, args));
    }

    protected void addSecretMessage(String format, Object... args)
    {
        if (secretMessages != null)
            secretMessages.add(String.format(format, args));
    }

    protected List<String> getSecretReceivers()
    {
        return game.getNonPlayerMods();
    }
}
