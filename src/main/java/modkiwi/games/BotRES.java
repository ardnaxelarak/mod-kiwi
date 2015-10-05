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

    private int round, subround, turn;
	private int[] proposal;
	private String[] submissions;
	private boolean[] approved, voted;
	private boolean[] canFail, isEvil;
	private boolean[] result;
	private int[] sizes, failsRequired;
	private int hammer = 5;
	private int passed, failed;

    protected BotRES(GameInfo game) throws IOException
    {
        super(game);
    }

    @Override
    public void createGame()
    {
		int good = 2 * NoP / 3;
		int evil = NoP - good;

		List<String> roles = new ArrayList<String>(NoP);
		for (int i = 0; i < good; i++)
			roles.add("good");
		for (int i = 0; i < evil; i++)
			roles.add("evil");

		Collections.shuffle(roles);
		game.setDataProperty("roles", roles);
    }

	private void sendRoles()
	{
		List<String> roles = (List<String>)game.getDataProperty("roles");
		List<String> evils = new LinkedList<String>();

		for (int i = 0; i < NoP; i++)
		{
			if (isEvil[i])
				evils.add(players[i]);
		}

		String evilInfo = "The evil players are " + Utils.join(evils, ", ");
		String modMessage = "";
		String subject, player, message, role;

		for (int i = 0; i < NoP; i++)
		{
			role = roles.get(i);
			player = players[i];

			subject = game.getPrefix() + " - YOU ARE " + role.toUpperCase();
			message = "";
			if (role.equals("good"))
			{
				message = "You are a rebel. Try to successfully complete three missions.";
			}
			else if (role.equals("evil"))
			{
				message = "You are a spy. Try to sabotage at least three missions!";
			}

			if (isEvil[i])
				modMessage += "r{" + player + " - " + role + "}r\n";
			else
				modMessage += "g{" + player + " - " + role + "}g\n";

			if (isEvil[i])
				message += "\n\n" + evilInfo;

			message = "[color=purple][b]" + message + "[/b][/color]";

			try
			{
				web.geekmail(player, subject, message);
			}
			catch (IOException e)
			{
				LOGGER.throwing("sendRoles()", e);
			}
		}

		subject = game.getPrefix() + " - Player Roles";
		try
		{
			web.geekmail(game.getNonPlayerMods(), subject, modMessage);
		}
		catch (IOException e)
		{
			LOGGER.throwing("sendRoles()", e);
		}
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

	private void endGame(boolean fresh, boolean goodWin)
	{
		if (fresh)
		{
			String message = "[color=purple][b]The game is over. Team " + (goodWin ? "Good" : "Evil") + " (";
			List<String> winners = new LinkedList<String>();
			for (int i = 0; i < NoP; i++)
			{
				if (isEvil[i] != goodWin)
					winners.add(players[i]);
			}

			message += Utils.join(winners, ", ") + ") wins![/b][/color]";

			try
			{
				web.replyThread(game, message);
			}
			catch (IOException e)
			{
				LOGGER.throwing("endGame()", e);
			}
		}
		super.endGame();
	}

    @Override
    public void initialize(boolean fresh)
    {
		List<String> roles = (List<String>)game.getDataProperty("roles");
		canFail = new boolean[NoP];
		canFail = new boolean[NoP];

		for (int i = 0; i < NoP; i++)
		{
			if (roles.get(i).startsWith("evil"))
			{
				canFail[i] = true;
				isEvil[i] = true;
			}
			else
			{
				canFail[i] = false;
				isEvil[i] = false;
			}
		}

		if (fresh)
			sendRoles();

		round = 1;
		subround = 1;
		turn = 0;
		passed = 0;
		failed = 0;
		proposal = null;
		approved = new boolean[NoP];
		voted = new boolean[NoP];
		result = new boolean[5];
		sizes = new int[5];
		failsRequired = new int[] {1, 1, 1, 1, 1};
		if (NoP >= 7)
			failsRequired[3] = 2;
    }

	private void checkVotes(boolean fresh)
	{
		StringBuilder votes = new StringBuilder();
		votes.append("[b]M" + round + "." + subround + " - " + players[turn] + ": ");
		votes.append(players[proposal[0]]);
		for (int i = 1; i < proposal.length; i++)
			votes.append(", " + players[proposal[i]]);
		votes.append("[/b]");
		int approves = 0;
		for (int i = 0; i < NoP; i++)
		{
			if (!voted[i])
				return;

			if (approved[i])
			{
				votes.append("\ng{" + players[i] + " - APPROVE}g");
				approves++;
			}
			else
				votes.append("\nr{" + players[i] + " - REJECT}r");
		}

		addMessage(votes.toString());

		if (approves > NoP / 2)
		{
			sendProposal(fresh);
		}
		else
		{
			subround++;
			turn = (turn + 1) % NoP;
		}
	}

	private void sendProposal(boolean fresh)
	{
		subround = 0;
		submissions = new String[proposal.length];
	}

	private void checkSubmissions(boolean fresh)
	{
		StringBuilder message = new StringBuilder();
		message.append("[b]M" + round + " - " + players[turn] + ": ");
		message.append(players[proposal[0]]);
		for (int i = 1; i < proposal.length; i++)
			message.append(", " + players[proposal[i]]);
		message.append(':');
		message.append("[/b]");

		String[] results = new String[submissions.length];
		int fails = 0;
		for (int i = 0; i < submissions.length; i++)
		{
			if (submissions[i] == null)
				return;

			if (submissions[i].equals("pass"))
				results[i] = "g{PASS}g";
			else if (submissions[i].equals("fail"))
			{
				results[i] = "r{FAIL}r";
				fails++;
			}
			else
				results[i] = "[ooc]UNKNOWN[/ooc]";
		}

		message.append(Utils.join(results, ", "));
		message.append("[/b]");

		addMessage(message.toString());

		if (fails >= failsRequired[round - 1])
		{
			failed++;
		}
		else
		{
			passed++;
		}

		if (failed >= 3)
		{
			endGame(fresh, false);
		}
		else if (passed >= 3)
		{
			endGame(fresh, true);
		}
		else
		{
			round++;
			subround = 1;
			proposal = null;
			turn = (turn + 1) % NoP;
		}
	}

    @Override
    protected void processMove(boolean fresh, String... move)
    {
		String first = move[0];
		if (game.inProgress())
		{
			if (first.equals("propose"))
			{
				proposal = new int[move.length - 1];
				for (int i = 0; i < proposal.length; i++)
					proposal[i] = Integer.parseInt(move[i + 1]);

				if (subround == hammer)
				{
					addMessage("[b]The proposal is automatically sent.[/b]");
					sendProposal(fresh);
				}
				else
				{
					Arrays.fill(voted, false);
				}
			}
			else if (first.equals("approve"))
			{
				int player = Integer.parseInt(move[1]);
				approved[player] = true;
				voted[player] = true;
				checkVotes(fresh);
			}
			else if (first.equals("reject"))
			{
				int player = Integer.parseInt(move[1]);
				approved[player] = false;
				voted[player] = true;
				checkVotes(fresh);
			}
		}
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
