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

public class BotRES extends GameBot {
    public static final String LONG_NAME = "The Resistance";

    private static final Logger LOGGER = new Logger(BotRES.class);

    private static final int[][] MISSION_COUNTS = new int[][] {
            new int[] {2, 3, 2, 3, 3},
            new int[] {2, 3, 4, 3, 4},
            new int[] {2, 3, 3, 4, 4},
            new int[] {3, 4, 4, 5, 5},
            new int[] {3, 4, 4, 5, 5},
            new int[] {3, 4, 4, 5, 5},
    };

    private static final Pattern[] P_PROPOSALS = createMatchers(5);

    private int round, turn, subround;
    private int scoreGood, scoreEvil;
    private Pattern currentProposalMatcher;
    private boolean[] hasVoted, vote;
    private Submission[] submissions;
    private int[] proposal;
    private int currentSize;
    private String step;
    private List<String> roles;

    private enum Submission {
        NONE,
        SUCCESS,
        FAILURE,
    }

    private static Pattern[] createMatchers(int max) {
        Pattern[] total = new Pattern[max + 1];
        total[0] = Utils.pat("^propose$");
        String pat = "^propose\\s+(\\S[^,]*)";
        for (int i = 1; i <= max; i++) {
            total[i] = Utils.pat(pat + "$");
            pat += ",\\s*(\\S[^,]*)";
        }
        return total;
    }

    protected BotRES(GameInfo game) throws IOException {
        super(game);
    }

    @Override
    public void createGame() {
        roles = new ArrayList<String>(NoP);
        int good = NoP * 2 / 3;
        int evil = NoP - good;

        for (int i = 0; i < good; i++) {
            roles.add("good");
        }

        for (int i = 0; i < evil; i++) {
            roles.add("evil");
        }

        Collections.shuffle(roles);
        game.getData().setProperty("roles", roles);
    }

    @Override
    protected CharSequence update() {
        if (game.inProgress()) {
            StringBuilder message = new StringBuilder(getCurrentStatus());
            if (step.equals("proposal")) {
                if (message.length() > 0) {
                    message.append("\n\n");
                }
                message.append("[color=#008800]Please [b]propose player");
                for (int i = 1; i < currentSize; i++) {
                    message.append(", player");
                }
                message.append("[/b][/color]");
            }
            return message;
        }
        return null;
    }

    @Override
    public void initialize(boolean fresh) {
        scoreGood = 0;
        scoreEvil = 0;
        round = 0;
        turn = -1;

        hasVoted = new boolean[NoP];
        vote = new boolean[NoP];

        if (fresh) {
            sendRoles();
        }

        newRound(fresh);
    }

    @SuppressWarnings("unchecked")
    private void sendRoles() {
        List<String> roles = (List<String>)game.getDataProperty("roles");
        String player, role, subject, message, modMessage;
        String goodMessage = "You are a good player. Try to pass three missions to win the game!";
        String evilMessage = "You are an evil player. Try to fail three missions to win the game!";
        modMessage = "";

        for (int i = 0; i < NoP; i++) {
            player = players[i];
            role = roles.get(i);

            subject = game.getPrefix() + " - YOU ARE " + role.toUpperCase();
            if (role.equals("good")) {
                message = goodMessage;
                modMessage += "g{" + player + " - " + role + "}g\n";
            } else {
                message = evilMessage;
                modMessage += "r{" + player + " - " + role + "}r\n";
            }

            message = "[color=purple][b]" + message + "[/color][/b]";

            try {
                web.geekmail(player, subject, message);
            } catch (IOException e) {
                LOGGER.throwing("sendRoles()", e);
            }
        }

        subject = game.getPrefix() + " - Player Roles";
        try {
            web.geekmail(game.getNonPlayerMods(), subject, modMessage);
        } catch (IOException e) {
            LOGGER.throwing("sendRoles()", e);
        }
    }

    private void newRound(boolean fresh) {
        proposal = null;
        currentSize = MISSION_COUNTS[NoP - 5][round];
        currentProposalMatcher = P_PROPOSALS[currentSize];

        LOGGER.info("currentSize = %d, P_PROPOSALS[%d] = /%s/", currentSize, currentSize, currentProposalMatcher.pattern());

        round++;
        subround = 0;
        turn = (turn + 1) % NoP;

        step = "proposal";
    }

    @Override
    protected void processMove(boolean fresh, String... move) {
        String first = move[0];
        if (game.inProgress()) {
            if (first.equals("propose")) {
                proposal = new int[currentSize];
                for (int i = 0; i < currentSize; i++) {
                    proposal[i] = Integer.parseInt(move[i + 1]);
                }
                step = "voting";
            }
        }
    }

    @Override
    public void processCommand(String username, String command) {
        Matcher m;
        int actor = Utils.getUser(username, players);
        boolean mod = game.isModerator(username);
        boolean cp = (actor == turn);
        LOGGER.info("Command '%s' found by %s (%d) -- mod? = %s, cp? = %s", command, username, actor, mod ? "true" : "false", cp ? "true" : "false");

        if (game.inSignups()) {
        } else if (game.inProgress()) {
            if (step.equals("proposal") && cp && (m = currentProposalMatcher.matcher(command)).matches()) {
                LOGGER.fine("- matches P_PROPOSAL_%d", currentSize);
                int[] members = new int[currentSize];
                String[] smembers = new String[currentSize + 1];
                for (int i = 0; i < currentSize; i++) {
                    int user = Utils.getUser(m.group(i + 1), players, game);
                    LOGGER.info("  - m.group(%d) = %s", i + 1, m.group(i + 1));
                    LOGGER.info("    - user = %d", user);
                    if (user < 0)
                        return;
                    members[i] = user;
                    smembers[i + 1] = Integer.toString(user);
                }
                if (Utils.unique(members)) {
                    smembers[0] = "propose";
                    processAndAddMove(smembers);
                } else {
                    LOGGER.info("%s proposed non-unique mission: \"%s\"", players[turn], command);
                }
            }
        }
    }

    @Override
    public CharSequence getCurrentStatus() {
        if (!game.inProgress()) {
            return null;
        } else {
            StringBuilder message = new StringBuilder();

            message.append("[b][u]current score:[/u][/b]\ng{Rebels - ");
            message.append(scoreGood);
            message.append("}g\nr{Spies - ");
            message.append(scoreEvil);
            message.append("}r\n");

            if (step.equals("proposal")) {
                message.append("\n[color=purple][b]");
                message.append(players[turn]);
                message.append(" is up to propose a mission of " + currentSize + " people.[/b][/color]");
            } else if (step.equals("voting")) {
                message.append("\n[color=#008800]Voting on proposal " + (round + 1) + "." + (subround + 1) + ": ");
                message.append(players[turn] + " - ");
                for (int i = 0; i < currentSize; i++) {
                    if (i > 0) {
                        message.append(", ");
                    }
                    message.append(players[proposal[i]]);
                }
                message.append("[/color]\n[b]Voting links:[/b] [b]g{APPROVE}g[/b] / [b]r{REJECT}r[/b]");
            } else if (step.equals("submission")) {
                message.append("\n[color=#008800]Waiting for submissions for " + (round + 1) + "." + (subround + 1) + ": ");
                message.append(players[turn] + " - ");
                for (int i = 0; i < currentSize; i++) {
                    if (i > 0) {
                        message.append(", ");
                    }
                    message.append(players[proposal[i]]);
                }
                message.append("[/color]\n[b]Submission links: [b]g{PASS}g[/b] / [b]r{FAIL}r[/b]");
            }

            return message;
        }
    }

    @Override
    public String getHistoryItem(String move) {
        return move;
    }
}
