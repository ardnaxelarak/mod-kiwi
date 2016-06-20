package modkiwi.games;

import modkiwi.data.GameInfo;
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
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BotHan extends GameBot {
    public static final String LONG_NAME = "Hanabi";

    private static final Logger LOGGER = new Logger(BotHan.class);

    private static final Pattern P_CLUE    = Utils.pat("(?:clue|hint)\\s*(\\S+)\\s+(\\S[^,]*)");
    private static final Pattern P_DISCARD = Utils.pat("discard\\s*(\\d+)");
    private static final Pattern P_PLAY    = Utils.pat("play\\s*(\\d+)");

    private static final String[] COLORS = new String[] {"Red", "Green", "Yellow", "Blue", "White", "Multi"};

    private static final int[] BIT_COLORS = new int[] {1, 2, 4, 8, 16, 32};
    private static final int[] BIT_VALUES = new int[] {64, 128, 256, 512, 1024};

    private String[] colors;
    private String[][] hands;
    private int[][] handClues;
    private TreeMap<String, Integer> discards;
    private int[] board;
    private int turn, round, handSize;
    private int clues, fuse, maxClues;
    private LinkedList<String> deck;

    protected BotHan(GameInfo game) throws IOException {
        super(game);
    }

    private void setColors() {
        colors = Arrays.copyOf(COLORS, 5);
    }

    private int matchColor(String color) {
        for (int i = 0; i < colors.length; i++) {
            if (colors[i].equalsIgnoreCase(color)) {
                return i;
            }
            if (colors[i].substring(0, 1).equalsIgnoreCase(color)) {
                return i;
            }
        }
        return -1;
    }

    private int getColorOfCard(String card) {
        String[] parts = card.split(" ");
        return matchColor(parts[0]);
    }

    private int getValueOfCard(String card) {
        String[] parts = card.split(" ");
        return Integer.parseInt(parts[1]) - 1;
    }

    private boolean playableCard(String card) {
        int color = getColorOfCard(card);
        int value = getValueOfCard(card);

        return (board[color] == value);
    }

    @Override
    public void createGame() {
        deck = new LinkedList<String>();
        setColors();

        for (String s : colors) {
            deck.add(s + " 1");
            deck.add(s + " 1");
            deck.add(s + " 1");
            deck.add(s + " 2");
            deck.add(s + " 2");
            deck.add(s + " 3");
            deck.add(s + " 3");
            deck.add(s + " 4");
            deck.add(s + " 4");
            deck.add(s + " 5");
        }

        Collections.shuffle(deck);
        game.getData().setProperty("deck", deck);
    }

    private void mailHands() {
        String[] pmessage = new String[NoP];
        String subject = String.format("%s: Turn %d hands", game.getPrefix(), round);
        String message;

        for (int i = 0; i < NoP; i++) {
            pmessage[i] = players[i] + ":";
            for (int j = 0; j < hands[i].length; j++) {
                pmessage[i] += "\n" + (j + 1) + ": " + hands[i][j];
            }
        }

        for (int i = 0; i < NoP; i++) {
            message = "Player hands on turn " + round + ":\n";

            for (int j = 0; j < NoP; j++) {
                if (i == j)
                    continue;
                message += "\n" + pmessage[j];
            }

            try {
                web.geekmail(players[i], subject, message);
            } catch (IOException e) {
                LOGGER.throwing("mailHands()", e);
            }
        }
    }

    @Override
    protected CharSequence update() {
        if (!game.inProgress()) {
            return null;
        }

        StringBuilder message = new StringBuilder(getCurrentStatus());
        message.append('\n').append('\n');

        message.append("[color=purple][b]" + players[turn] + ": Please take one of the following actions:[/b][/color]\n[color=#008800]");
        if (clues > 0) {
            message.append("clue [i]&lt;color / number&gt;[/i] [i]&lt;player&gt;[/i]\n");
        }
        message.append("discard [i]&lt;card position&gt;[/i]\n");
        message.append("play [i]&lt;card position&gt;[/i][/color]");

        return message;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void initialize(boolean fresh) {
        setColors();

        board = new int[colors.length];
        Arrays.fill(board, 0);

        handSize = 6 - NoP / 2;
        maxClues = 8;
        clues = maxClues;
        fuse = 2;

        turn = 0;
        round = 1;

        discards = new TreeMap<String, Integer>();
        deck = new LinkedList<String>( (List<String>)game.getData().getProperty("deck"));
        dealHands(fresh);
    }

    private void dealHands(boolean fresh) {
        hands = new String[NoP][handSize];
        handClues = new int[NoP][handSize];

        for (int i = 0; i < NoP; i++) {
            for (int j = 0; j < handSize; j++) {
                hands[i][j] = drawCard();
                handClues[i][j] = 0;
            }
        }

        if (fresh) {
            mailHands();
        }
    }

    private String drawCard() {
        return deck.pop();
    }

    @Override
    protected void processMove(boolean fresh, String... move) {
        String first = move[0];
        if (first.equals("cluecolor")) {
            int color = Integer.parseInt(move[1]);
            int player = Integer.parseInt(move[2]);

            clueColor(color, player);
        } else if (first.equals("cluevalue")) {
            int value = Integer.parseInt(move[1]);
            int player = Integer.parseInt(move[2]);

            clueValue(value, player);
        } else if (first.equals("discard")) {
            int position = Integer.parseInt(move[1]);
            String card = hands[turn][position];
            addMessage("[color=purple][b]%s discards a %s.[/b][/color]", players[turn], card);

            if (discards.get(card) == null) {
                discards.put(card, 1);
            } else {
                discards.put(card, discards.get(card) + 1);
            }

            if (clues < maxClues) {
                clues++;
                addMessage("[color=#008800]A clue is regained.[/color]");
            }

            redraw(turn, position, fresh);
        } else if (first.equals("play")) {
            int position = Integer.parseInt(move[1]);
            String card = hands[turn][position];
            if (playableCard(card)) {
                addMessage("[color=purple][b]%s successfully plays a %s.[/b][/color]", players[turn], card);
                board[getColorOfCard(card)]++;

                if (getValueOfCard(card) == 4 && clues < maxClues) {
                    clues++;
                    addMessage("[color=#008800]A clue is regained.[/color]");
                }
            } else {
                addMessage("[color=purple][b]%s fails to play a %s![/b][/color]", players[turn], card);

                if (discards.get(card) == null) {
                    discards.put(card, 1);
                } else {
                    discards.put(card, discards.get(card) + 1);
                }

                fuse--;
            }

            redraw(turn, position, fresh);
        } else {
            LOGGER.warning("Invalid move string '%s'", Utils.join(move, " "));
        }
    }

    private void clueColor(int color, int player) {
        List<Integer> positions = new LinkedList<Integer>();

        for (int i = 0; i < handSize; i++) {
            String card = hands[player][i];
            if (getColorOfCard(card) == color) {
                positions.add(i + 1);
                for (int j = 0; j < colors.length; j++) {
                    if (j == color)
                        continue;

                    handClues[player][i] |= BIT_COLORS[j];
                }
            } else {
                handClues[player][i] |= BIT_COLORS[color];
            }
        }

        addMessage("[color=#008800]Positions %s of %s's hand are %s.[/color]", Utils.join(positions, ", "), players[player], colors[color]);

        clues--;

        nextTurn();
    }

    private void clueValue(int value, int player) {
        List<Integer> positions = new LinkedList<Integer>();

        for (int i = 0; i < handSize; i++) {
            String card = hands[player][i];
            if (getValueOfCard(card) == value) {
                positions.add(i + 1);
                for (int j = 0; j < 5; j++) {
                    if (j == value)
                        continue;

                    handClues[player][i] |= BIT_VALUES[j];
                }
            } else {
                handClues[player][i] |= BIT_VALUES[value];
            }
        }

        addMessage("[color=#008800]Positions %s of %s's hand are %ds.[/color]", Utils.join(positions, ", "), players[player], value + 1);

        clues--;

        nextTurn();
    }

    private void redraw(int player, int position, boolean fresh) {
        for (int i = position; i < handSize - 1; i++) {
            hands[player][i] = hands[player][i + 1];
            handClues[player][i] = handClues[player][i + 1];
        }

        hands[player][handSize - 1] = drawCard();
        handClues[player][handSize - 1] = 0;

        nextTurn();

        if (fresh) {
            mailHands();
        }
    }

    private void nextTurn() {
        turn = (turn + 1) % NoP;
        round++;
    }

    @Override
    public void processCommand(String username, String command) {
        if (!username.equals(players[turn])) {
            return;
        }

        Matcher m;

        if ((m = P_CLUE.matcher(command)).matches()) {
            int user = Utils.getUser(m.group(2), players, game);
            int color = -1;
            int num = -1;

            color = matchColor(m.group(1));
            try {
                num = Integer.parseInt(m.group(1)) - 1;
            } catch (NumberFormatException e) {
            }

            if (user < 0 || (color < 0 && num < 0))
                return;

            if (color >= 0) {
                processAndAddMove("cluecolor", Integer.toString(color), Integer.toString(user));
            } else if (num >= 0) {
                processAndAddMove("cluevalue", Integer.toString(num), Integer.toString(user));
            } else {
                return;
            }
        } else if ((m = P_DISCARD.matcher(command)).matches()) {
            int position = Integer.parseInt(m.group(1));

            if (position < 1 || position > handSize)
                return;

            processAndAddMove("discard", Integer.toString(position - 1));
        } else if ((m = P_PLAY.matcher(command)).matches()) {
            int position = Integer.parseInt(m.group(1));

            if (position < 1 || position > handSize)
                return;

            processAndAddMove("play", Integer.toString(position - 1));
        }
    }

    @Override
    public CharSequence getCurrentStatus() {
        if (game.inProgress()) {
            StringBuilder message = new StringBuilder();

            message.append("[color=#008800]");
            message.append("Clues: " + clues);
            message.append("\nRemaining incorrect plays: " + fuse);
            if (!discards.isEmpty()) {
                message.append("\n\nDiscards:");
                for (String card : discards.keySet()) {
                    message.append('\n');
                    message.append(discards.get(card) + "x " + card);
                }
            }
            message.append("\n\nBoard:");
            for (int i = 0; i < board.length; i++) {
                message.append("\n" + colors[i] + ": " + board[i]);
            }

            message.append("\n\nHands:");
            message.append("\n[c]+--------------------+");
            for (int i = 0; i < handSize; i++) {
                message.append("-------+");
            }

            message.append("\n|name                |");
            for (int i = 0; i < handSize; i++) {
                message.append("   " + (i + 1) + "   |");
            }

            message.append("\n+--------------------+");
            for (int i = 0; i < handSize; i++) {
                message.append("-------+");
            }

            for (int i = 0; i < NoP; i++) {
                message.append(String.format("\n|%-20s|", players[i]));
                for (int j = 0; j < handSize; j++) {
                    message.append(" " + getColors(handClues[i][j], false) + " |");
                }

                message.append("\n|                    |");
                for (int j = 0; j < handSize; j++) {
                    message.append(" " + getValues(handClues[i][j], false) + " |");
                }

                message.append("\n+--------------------+");
                for (int j = 0; j < handSize; j++) {
                    message.append("-------+");
                }
            }

            message.append("[/c][/color]");

            return message;
        }

        return null;
    }

    private String getColors(int card, boolean longForm) {
        List<String> list = new LinkedList<String>();
        for (int i = 0; i < colors.length; i++) {
            if ((card & BIT_COLORS[i]) == 0) {
                list.add(colors[i].substring(0, 1));
            } else {
                list.add(" ");
            }
        }
        return Utils.join(list, "");
    }

    private String getValues(int card, boolean longForm) {
        List<String> list = new LinkedList<String>();
        for (int i = 0; i < 5; i++) {
            if ((card & BIT_VALUES[i]) == 0) {
                list.add(Integer.toString(i + 1));
            } else {
                list.add(" ");
            }
        }
        return Utils.join(list, "");
    }

    private void endGame(boolean fresh, boolean win) {
        if (fresh)
        {
            String message;
            if (win) {
                int score = 0;
                for (int count : board) {
                    score += count;
                }

                message = "[color=purple][b]The game is over. Your score is " + score + ".[/b][/color]";
            } else {
                message = "[color=purple][b]The game is over. You have exploded![/b][/color]";
            }

            try {
                web.replyThread(game, message);
            } catch (IOException e) {
                LOGGER.throwing("endGame()", e);
            }
        }
        super.endGame();
    }

    @Override
    public String getHistoryItem(String move) {
        return move;
    }
}
