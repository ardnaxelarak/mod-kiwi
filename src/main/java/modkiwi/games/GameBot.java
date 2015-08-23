package modkiwi.games;

import modkiwi.Helper;
import modkiwi.data.GameInfo;
import modkiwi.util.Logger;
import modkiwi.util.Utils;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public abstract class GameBot
{
    private static final Logger LOGGER = new Logger(GameBot.class);

    protected GameInfo game;
    protected final Helper helper;
    protected int NoP;
    protected String[] players;

    protected GameBot(GameInfo game) throws IOException
    {
        this.game = game;
        helper = new Helper();
        helper.login();

        getPlayerData();

        if (game.getStatus().equals("progress"))
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

    protected abstract void update();

    protected abstract void processMove(boolean fresh, String... move);

    protected void processAndAddMove(String... move)
    {
        processMove(true, move);
        game.getMoves().add(Utils.join(move, " "));
        update();
        updateStatus();
    }

    public boolean processSignupCommand(String username, String command, List<String> guesses)
    {
        if (command.equalsIgnoreCase("signup"))
        {
            if (!game.getPlayers().contains(username))
            {
                game.getPlayers().add(username);
                return true;
            }
        }
        else if (command.equalsIgnoreCase("remove"))
        {
            return game.getPlayers().remove(username);
        }
        else if (command.toLowerCase().startsWith("guess") &&
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

            guesses.add(String.format("[q=\"%s\"][b]%s[/b][/q][color=#008800]%d / %d[/color]", username, guess, count, aparts.length));
        }
        else
        {
            LOGGER.finest("unregonized command '%s'", command);
        }

        return false;
    }

    public abstract void processCommand(String username, String command);

    public abstract String getCurrentStatus();

    public void updateStatus()
    {
        String status = getCurrentStatus();
        if (game.getStatusPost() != null && status != null)
        {
            try
            {
                helper.edit(game.getStatusPost(), "Current Status", getCurrentStatus());
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
            helper.edit(game.getSignupPost(), "Player List", listText);
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
        update();
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
            helper.replyThread(game.getThread(), null, "[color=purple][b]Game is over.[/b][/color]");
        }
        catch (IOException e)
        {
            LOGGER.throwing("endGame()", e);
        }

        game.setGameStatus("finished");
    }
}
