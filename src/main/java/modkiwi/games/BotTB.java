package modkiwi.games;

import modkiwi.data.GameInfo;
import modkiwi.util.Logger;
import modkiwi.util.Utils;
import static modkiwi.util.Constants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class BotTB extends GameBot
{
    public static final String LONG_NAME = "TimeBomb";

    private static final Logger LOGGER = new Logger(BotTB.class);

    private String[][] hands;
    private boolean[][] drawn;
    private String[] claims;
    private int success, safe, turn, round;

    protected BotTB(GameInfo game) throws IOException
    {
        super(game);
    }

    @Override
    public void createGame()
    {
        int evil, good;
        evil = (NoP + 2) / 3;
        good = NoP - evil;
        if (NoP % 3 == 1)
            good++;

        List<String> roles = new ArrayList<String>(good + evil);
        for (int i = 0; i < good; i++)
            roles.add("good");
        for (int i = 0; i < evil; i++)
            roles.add("evil");

        Collections.shuffle(roles);
        game.setDataProperty("roles", roles);

        List<Integer> initdeck = new ArrayList<Integer>(5 * NoP);
        initdeck.add(-1);
        for (int i = 0; i < NoP; i++)
            initdeck.add(NoP - i);
        while (initdeck.size() < 5 * NoP)
            initdeck.add(0);

        List<Integer> deck;

        for (int i = 0; i < 4; i++)
        {
            deck = new ArrayList<Integer>(initdeck.subList(0, NoP * (5 - i)));
            Collections.shuffle(deck);
            game.setDataProperty("round" + (i + 1) +  "deck", deck);
        }
    }

    private void sendRoles()
    {
        List<String> roles = (List<String>)game.getDataProperty("roles");
        String player, role, subject, message, modMessage;
        String goodMessage = "You are a virtuous member of the SWAT team trying to defuse the bomb.";
        String evilMessage = "You are a neferious evil terrorist trying to make the bomb blow up.";
        modMessage = "";
        for (int i = 0; i < NoP; i++)
        {
            player = players[i];
            role = roles.get(i);

            subject = game.getPrefix() + " - YOU ARE " + role.toUpperCase();
            if (role.equals("good"))
            {
                message = goodMessage;
                modMessage += "g{" + player + " - " + role + "}g\n";
            }
            else
            {
                message = evilMessage;
                modMessage += "r{" + player + " - " + role + "}r\n";
            }

            message = "[color=purple][b]" + message + "[/color][/b]";

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

    private void sendHands()
    {
        String player, message, modMessage;
        String subject = game.getPrefix() + " - Round " + round + " Hand";
        int success, safe, boom;
        modMessage = "[c]";
        for (int i = 0; i < NoP; i++)
        {
            player = players[i];

            success = 0;
            safe = 0;
            boom = 0;

            modMessage += Utils.lPadUsername(player);

            for (String card : hands[i])
            {
                if (card == null)
                {
                    modMessage += "  ";
                    LOGGER.warning("Null card found in hand of %s.", player);
                }
                else if (card.equals("success"))
                {
                    success++;
                    modMessage += " [b]g{S}g[/b]";
                }
                else if (card.equals("boom"))
                {
                    boom++;
                    modMessage += " [b][i]r{B}r[/i][/b]";
                }
                else
                {
                    safe++;
                    modMessage += " -";
                }
            }

            modMessage += "\n";
            message = "Your round " + round + " hand contains:";
            if (success > 0)
                message += "\ng{" + success + " x SUCCESS}g";
            if (safe > 0)
                message += "\nb{" + safe + " x SAFE}b";
            if (boom > 0)
                message += "\nr{" + boom + " x BOOM}r";

            try
            {
                web.geekmail(player, subject, message);
            }
            catch (IOException e)
            {
                LOGGER.throwing("sendHands()", e);
            }
        }

        modMessage += "[/c]";

        subject = game.getPrefix() + " - Round " + round + " Hands";
        try
        {
            web.geekmail(game.getNonPlayerMods(), subject, modMessage);
        }
        catch (IOException e)
        {
            LOGGER.throwing("sendRoles()", e);
        }
    }

    private void newRound(boolean fresh)
    {
        round = round + 1;

        if (round == 5)
        {
            endGame(fresh, false);
            return;
        }

        List<Number> deck = (List<Number>)game.getDataProperty("round" + round + "deck");

        int pl = 0, ind = 0;
        int cards = 6 - round;
        String cardName;
        hands = new String[NoP][cards];
        drawn = new boolean[NoP][cards];
        for (Number cardnum : deck)
        {
            int card = cardnum.intValue();
            if (card == -1)
                cardName = "boom";
            else if (card <= success)
                cardName = "safe";
            else
                cardName = "success";

            hands[pl][ind++] = cardName;
            if (ind == cards)
            {
                pl++;
                ind = 0;
            }
        }

        Arrays.fill(claims, "no claim");

        if (fresh)
        {
            sendHands();
            try
            {
                web.replyThread(game, "[color=purple][b]Round " + round + " has begun. Hands have been geekmailed to all players.[/b][/color]");
            }
            catch (IOException e)
            {
                LOGGER.throwing("newRound()", e);
            }
        }

        turn = round - 1;
    }

    @Override
    public void initialize(boolean fresh)
    {
        hands = new String[NoP][];
        claims = new String[NoP];
        success = 0;
        safe = 0;
        round = 0;

        if (fresh)
            sendRoles();

        newRound(fresh);
    }

    @Override
    protected void update()
    {
        if (game.getGameStatus().equals(STATUS_IN_PROGRESS))
        {
            String message = getCurrentStatus() + "\n\n";
            message += "[color=purple][b]" + players[turn] + " is up.[/b][/color]\n";
            message += "[color=#008800]Please [b]choose [i]player[/i] [i]position[/i][/b][/color]";

            try
            {
                web.replyThread(game, message);
            }
            catch (IOException e)
            {
                LOGGER.throwing("update()", e);
            }
        }
    }

    private void endGame(boolean fresh, boolean goodWin)
    {
        if (fresh)
        {
            String message = "[color=purple][b]The game is over. Team " + (goodWin ? "Good" : "Evil") + " (";
            List<String> winners = new LinkedList<String>();
            List<String> roles = (List<String>)game.getDataProperty("roles");
            for (int i = 0; i < NoP; i++)
            {
                if (roles.get(i).equals("good") == goodWin)
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
    protected void processMove(boolean fresh, String... move)
    {
        if (move[0].equals("snip"))
        {
            int pl = Integer.parseInt(move[1]);
            int ind = Integer.parseInt(move[2]);
            String card = hands[pl][ind];
            String message = null;

            drawn[pl][ind] = true;

            if (card.equals("success"))
            {
                message = "[color=green]" + players[turn] + " has drawn a [b]g{SUCCESS}g[/b] card![/color]";
                success++;
            }
            else if (card.equals("safe"))
            {
                message = "[color=green]" + players[turn] + " has drawn a [b]b{SAFE}b[/b] card.[/color]";
                safe++;
            }
            else if (card.equals("boom"))
            {
                message = "[color=green]" + players[turn] + " has drawn a [b][i]r{BOOM}r[/i][/b] card![/color]";
            }
            else
            {
                LOGGER.warning("Unrecognized card '%s'", card);
            }

            if (fresh)
            {
                try
                {
                    web.replyThread(game, message);
                }
                catch (IOException e)
                {
                    LOGGER.throwing("processMove()", e);
                }
            }

            if (card.equals("boom"))
                endGame(fresh, false);
            else if (success == NoP)
                endGame(fresh, true);
            else if ((success + safe) % NoP == 0)
                newRound(fresh);
            else
                turn = pl;
        }
        else if (move[0].equals("claim"))
        {
            int pnum = Integer.parseInt(move[1]);
            claims[pnum] = Utils.join(Arrays.copyOfRange(move, 2, move.length), " ");
        }
        else
        {
            LOGGER.warning("Invalid move string '%s'", Utils.join(move, " "));
        }
    }

    @Override
    public void processCommand(String username, String command)
    {
        String name, position;
        int index;
        if (command.toLowerCase().startsWith("choose "))
        {
            if (!username.equals(players[turn]))
                return;

            index = command.lastIndexOf(" ");
            if (index < 7)
            {
                LOGGER.info("%s posted invalid choose command '%s'", players[turn], command);
                return;
            }
            name = command.substring(7, command.lastIndexOf(" "));
            position = command.substring(command.lastIndexOf(" ") + 1);

            int pl = Utils.getUser(name, players);
            if (pl == -1)
            {
                LOGGER.info("%s posted unrecognized username '%s'", players[turn], name);
                return;
            }
            else if (pl == turn)
            {
                LOGGER.info("%s attempted to choose their own card", players[turn]);
                return;
            }

            int pos = -1;
            try
            {
                pos = Integer.parseInt(position) - 1;
                if (pos < 0 || pos >= hands[pl].length)
                {
                    LOGGER.info("%s posted invalid position '%s'", players[turn], position);
                    return;
                }
            }
            catch (NumberFormatException e)
            {
                LOGGER.info("%s posted invalid position '%s'", players[turn], position);
                return;
            }

            if (drawn[pl][pos])
            {
                LOGGER.info("%s attempted to draw an already-drawn card", players[turn]);
                return;
            }

            processAndAddMove("snip", Integer.toString(pl), Integer.toString(pos));
        }
        else if (command.toLowerCase().startsWith("claim "))
        {
            String claim = command.substring(6);
            index = getPlayerIndex(username);
            if (index != -1)
                processAndAddMove("claim", Integer.toString(index), claim);
        }
    }

    @Override
    public String getCurrentStatus()
    {
        String message = "[color=#008800]";
        message += "[size=14]Round " + round + " (" + (NoP * round - success - safe) + " cards remaining)[/size]\n\n";
        if (success + safe > 0)
        {
            message += "[u]Cards drawn:[/u]\n";
            if (success > 0)
                message += success + " x g{SUCCESS}g\n";
            if (safe > 0)
                message += safe + " x b{SAFE}b\n";

            message += "\n";
        }

        message += "[u]Cards remaining:[/u][c]";

        for (int i = 0; i < NoP; i++)
        {
            message += "\n" + Utils.lPadUsername(players[i]);
            for (int j = 0; j < hands[i].length; j++)
            {
                if (!drawn[i][j])
                    message += " " + (j + 1);
                else if (hands[i][j].equals("safe"))
                    message += " b{-}b";
                else if (hands[i][j].equals("success"))
                    message += " [b]g{S}g[/b]";
                else if (hands[i][j].equals("boom"))
                    message += " [b][i]r{F}r[/i][/b]";
            }
            message += " (" + claims[i] + ")";
        }
        message += "[/c][/color]";
        return message;
    }

    @Override
    public String getHistoryItem(String move)
    {
        return move;
    }
}
