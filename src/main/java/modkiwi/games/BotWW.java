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

	private final VoteTracker votes = new VoteTracker()
	{
		public String getVotee(int index)
		{
			return players[index];
		}

		@Override
		public List<String> voters()
		{
			List<String> voterList = new LinkedList<String>();
			for (String player : players)
				voterList.add(player);
			return voterList;
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
		}
    }

    @Override
    public void processCommand(String username, String command)
    {
        Matcher m;
		int actor = Utils.getUser(username, players);
        if (game.inProgress())
        {
			if ((m = P_VOTE.matcher(command)).matches())
			{
				if (actor != -1)
				{
					int votee = Utils.getUser(m.group(1), players);
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
			sb.append("[color=#008800]");
			sb.append(votes.getVotes());
			sb.append("[/color]");
			sb.append("\nTo ensure the latest tally is up-to-date, please [b][url=" + DOMAIN + "/scan?id=" + game.getId() + "&update=1&redirect=1]click here[/url][/b]");
			return sb;
		}

		return null;
    }

    @Override
    public String getHistoryItem(String move)
    {
        return move;
    }
}
