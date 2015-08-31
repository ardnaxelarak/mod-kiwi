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

public class BotMdC extends GameBot
{
    public static final String LONG_NAME = "Mafia de Cuba";

    private static final Logger LOGGER = new Logger(BotMdC.class);

    private static final Pattern P_GODFATHER = Utils.pat("^godfather\\w+(\\d+)$");
    private static final Pattern P_DIAMONDS = Utils.pat("^take\\w+(\\d+)\\w+diamonds");

    private static final String STEP_PASSING = "passing";
    private static final String STEP_ACCUSING = "accusing";

    private int diamonds, faithful, agent, driver, rum;
    private int turn;
    private String[] taken;
    private boolean[] accused;
    private boolean removed;
    private String step;

    protected BotMdC(GameInfo game) throws IOException
    {
        super(game);
        if (game.getDataProperty("godfather") == null)
            game.setDataProperty("godfather", new EmbeddedEntity());
    }

    @Override
    public void createGame()
    {
        String godfather = game.getDataProperty("godfather").toString();
        int index = getPlayerIndex(godfather);
        if (index > 0)
        {
            game.getPlayers().remove(index);
            game.getPlayers().add(0, godfather);
            getPlayerData();
            updatePlayerList();
        }
    }

    private void mailContents()
    {
        StringBuilder message = new StringBuilder("[color=#008800][u]The cigar box contains:[/u]");
        if (diamonds > 0)
            message.append("\n" + diamonds + " diamond(s)");
        if (faithful > 0)
            message.append("\n" + faithful + " faithful card(s)");
        if (agent > 0)
            message.append("\n" + agent + " agent card(s)");
        if (driver > 0)
            message.append("\n" + driver + " driver card(s)");
        if (diamonds == 0 && faithful == 0 && agent == 0 && driver == 0)
            message.append("\nnothing");

        String subject = game.getPrefix() + ": Cigar Box Contents";

        try
        {
            web.geekmail(players[turn], subject, message);
        }
        catch (IOException e)
        {
            LOGGER.throwing("mailContents()", e);
        }
    }

    @Override
    protected CharSequence update()
    {
        if (game.inProgress())
        {
            if (step.equals(STEP_PASSING))
            {
                return null;
            }
            else if (step.equals(STEP_ACCUSING))
            {
                StringBuilder message = new StringBuilder(getCurrentStatus());
                message.append("\n\n[color=#008800]Godfather ");
                message.append(players[0]);
                message.append(", please [b]accuse [i]player[/i][/b].[/color]");
                return message;
            }
            else
            {
                LOGGER.warning("Unrecognized step '%s'", step);
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public void initialize(boolean fresh)
    {
        diamonds = 15;
        faithful = 5;
        agent = 2;
        driver = 2;
        rum = 2;

        taken = new String[NoP];

        step = STEP_PASSING;
        turn = 0;
    }

    private void advanceTurn(boolean fresh)
    {
        turn++;
        if (step.equals(STEP_PASSING))
        {
            if (turn == NoP)
            {
                addMessage("[color=purple][b]%s passes the cigar box back to %s, who exclaims \"My diamonds have been stolen!\"[/b][/color]", players[turn - 1], players[0]);
                step = STEP_ACCUSING;
            }
            else
            {
                addMessage("[color=purple][b]%s passes the cigar box to %s.[/color][/b]", players[turn - 1], players[turn]);
            }
        }
    }

    @Override
    protected void processMove(boolean fresh, String... move)
    {
        String first = move[0];
        if (step.equals(STEP_PASSING))
        {
            if (first.equals("diamond"))
            {
                int num = Integer.parseInt(move[1]);
                diamonds -= num;
                taken[turn] = Integer.toString(num);
                advanceTurn(fresh);
            }
            else if (first.equals("faithful"))
            {
                faithful--;
                taken[turn] = "faithful";
                advanceTurn(fresh);
            }
            else if (first.equals("driver"))
            {
                driver--;
                taken[turn] = "driver";
                advanceTurn(fresh);
            }
            else if (first.equals("agent"))
            {
                agent--;
                taken[turn] = "agent";
                advanceTurn(fresh);
            }
            else if (first.equals("street"))
            {
                taken[turn] = "street";
                advanceTurn(fresh);
            }
            else
            {
                LOGGER.warning("Invalid move string '%s'", Utils.join(move, " "));
            }
        }
        else if (step.equals(STEP_ACCUSING))
        {
            // handle stuff here
        }
        else
        {
            LOGGER.warning("Invalid move string '%s'", Utils.join(move, " "));
        }
    }

    @Override
    public void processCommand(String username, String command)
    {
        Matcher m;
        if (game.inSignups())
        {
            if ((m = P_GODFATHER.matcher(command)).matches())
            {
                int weight = Integer.parseInt(m.group(1));
                if (weight < 0 || weight > 10)
                    return;

                EmbeddedEntity ent = (EmbeddedEntity)game.getDataProperty("godfather");
                ent.setProperty(username, weight);
            }
        }
    }

    @Override
    protected void shufflePlayers()
    {
        List<String> players = game.getPlayers();
        int NoP = players.size();
        int[] weights = new int[NoP];
        EmbeddedEntity ent = (EmbeddedEntity)game.getDataProperty("godfather");
        int total = 0;
        for (int i = 0; i < NoP; i++)
        {
            weights[i] = DatastoreUtils.getInt(ent.getProperty(players.get(i)), 1);
            total += weights[i];
        }
        Random r = new Random();
        String godfather = null;
        if (total > 0)
        {
            int num = r.nextInt(total);
            for (int i = 0; i < NoP; i++)
            {
                num -= weights[i];
                if (num < 0)
                {
                    godfather = players.get(i);
                    break;
                }
            }
        }
        if (godfather == null)
        {
            godfather = players.get(r.nextInt(NoP));
        }
        Collections.shuffle(players);
        players.remove(godfather);
        players.add(0, godfather);
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
