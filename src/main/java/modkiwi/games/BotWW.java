package modkiwi.games;

import modkiwi.data.GameInfo;
import modkiwi.games.util.VoteTracker;
import modkiwi.util.DatastoreUtils;
import modkiwi.util.Logger;
import modkiwi.util.Utils;

import static modkiwi.util.Constants.*;

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

public class BotWW extends GameBot
{
    public static final String LONG_NAME = "Werewolf";

    private static final Logger LOGGER = new Logger(BotWW.class);

    private static final Pattern P_VOTE = Utils.pat("^vote\\s+(\\S.*)$");
    private static final Pattern P_DUSK = Utils.pat("^dusk$");
    private static final Pattern P_DAWN = Utils.pat("^dawn$");
    private static final Pattern P_KILL = Utils.pat("^kill(?:ed)?\\s+(\\S.*)$");

    private boolean tally, majorityDusk, notifyDusk, notifyDawn, voteDay, voteNight;

    private String currentState;
    private boolean[] living;

    private final VoteTracker votes = new VoteTracker()
    {
        public String getVotee(int index)
        {
            return players[index];
        }

        @Override
        public List<String> voters()
        {
            return livingPlayers();
        }
    };

    protected BotWW(GameInfo game) throws IOException
    {
        super(game);
    }

    @Override
    public void createGame()
    {
    }

    private void sendRoles()
    {
    }

    @Override
    protected CharSequence update()
    {
        if (game.inProgress())
        {
            return getCurrentStatus();
        }
        return null;
    }

    private void endGame(boolean fresh, boolean goodWin)
    {
        if (fresh)
        {
        }
        super.endGame();
    }

    @Override
    public void initialize(boolean fresh)
    {
        votes.reset();
        tally = !game.hasSetting("no_tally");
        majorityDusk = game.hasSetting("dusk_on_majority");
        notifyDusk = game.hasSetting("geekmail_mod_on_dusk");
        notifyDawn = game.hasSetting("geekmail_mod_on_dawn");
        voteDay = !game.hasSetting("no_vote_day");
        voteNight = game.hasSetting("vote_night");
        currentState = "day";

        living = new boolean[NoP];
        Arrays.fill(living, true);
    }

    @Override
    protected void processMove(boolean fresh, String... move)
    {
        String first = move[0];
        if (game.inProgress())
        {
            if (first.equals("vote"))
            {
                votes.vote(players[Integer.parseInt(move[1])], Integer.parseInt(move[2]));
            }
            else if (first.equals("dusk"))
            {
                currentState = "night";
                // notify mod if applicable
                votes.reset();
            }
            else if (first.equals("dawn"))
            {
                currentState = "day";
                // notify mod if applicable
                votes.reset();
            }
            else if (first.equals("kill"))
            {
                living[Integer.parseInt(move[1])] = false;
            }
        }
    }

    @Override
    public void processCommand(String username, String command)
    {
        Matcher m;
        int actor = Utils.getUser(username, players);
        boolean mod = game.isModerator(username);
        if (game.inProgress())
        {
            if (mod && P_DUSK.matcher(command).matches())
            {
                processAndAddMove("dusk");
            }
            else if (mod && P_DAWN.matcher(command).matches())
            {
                processAndAddMove("dawn");
            }
            else if (mod && (m = P_KILL.matcher(command)).matches())
            {
                int dead = Utils.getUser(m.group(1), players, game);
                if (dead != -1 && living[dead])
                {
                    processAndAddMove("kill", Integer.toString(dead));
                }
            }
            else if (canVote() && (m = P_VOTE.matcher(command)).matches())
            {
                if (actor >= 0 && living[actor])
                {
                    int votee = Utils.getUser(m.group(1), players, game);
                    if (votee != -1)
                    {
                        processAndAddMove("vote", Integer.toString(actor), Integer.toString(votee));
                    }
                }
            }
        }
    }

    @Override
    public CharSequence getCurrentStatus()
    {
        StringBuilder sb = new StringBuilder();

        if (game.inProgress())
        {
			if (canVote())
			{
				sb.append("[color=#008800]");
				sb.append(votes.getVotes());
				sb.append("[/color]");
				sb.append("\nTo ensure the latest tally is up-to-date, please [b][url=" + DOMAIN + "/scan?id=" + game.getId() + "&update=1&redirect=1]click here[/url][/b]");
				return sb;
			}
        }

        return null;
    }

    @Override
    public String getHistoryItem(String move)
    {
        return move;
    }

    private boolean canVote()
    {
        if (!tally)
            return false;

        if (voteDay && "day".equals(currentState))
            return true;
        if (voteNight && "night".equals(currentState))
            return true;

        return false;
    }

    private List<String> livingPlayers()
    {
        List<String> list = new LinkedList<String>();
        for (int i = 0; i < NoP; i++)
        {
            if (living[i])
                list.add(players[i]);
        }
        return list;
    }
}
