package modkiwi.util;

import modkiwi.data.GameInfo;

import static modkiwi.util.Constants.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Pattern;

public final class Utils {
    private Utils() {
    }

    private static Random RAND = new Random();

    public static String join(Object[] array, CharSequence joiner) {
        if (array == null || array.length == 0) {
            return "";
        }

        StringBuilder result = new StringBuilder(array[0].toString());
        int len = array.length;
        for (int i = 1; i < array.length; i++) {
            result.append(joiner);
            result.append(array[i].toString());
        }

        return result.toString();
    }

    public static String join(Iterable<?> array, CharSequence joiner) {
        if (array == null) {
            return "";
        }

        Iterator<?> it = array.iterator();

        if (!it.hasNext()) {
            return "";
        }

        StringBuilder result = new StringBuilder(it.next().toString());
        while (it.hasNext()) {
            result.append(joiner);
            result.append(it.next().toString());
        }

        return result.toString();
    }

    public static <T> void shuffle(T[] array) {
        T ob;
        int len = array.length;

        for (int i = len; i > 0; i--) {
            int j = RAND.nextInt(i);
            ob = array[j];
            array[j] = array[i - 1];
            array[i - 1] = ob;
        }
    }

    public static String lPad(String text, int len) {
        return String.format("%-" + len + "s", text);
    }

    public static String lPadUsername(String name) {
        return lPad(name, MAX_USERNAME_LENGTH);
    }

    public static int getUser(String username, String[] list, GameInfo nicknameList) {
        return getUser(username, Arrays.asList(list), nicknameList);
    }

    public static int getUser(String username, String[] list) {
        return getUser(username, Arrays.asList(list));
    }

    public static int getUser(String username, Iterable<String> list, GameInfo nicknameList) {
        int nickindex = -1;
        String fullname = null;
        if (nicknameList != null) {
            fullname = nicknameList.getNickname(username);
        }

        int index = 0;
        for (String user : list) {
            if (user.equalsIgnoreCase(username))
                return index;

            if (user.equalsIgnoreCase(fullname))
                nickindex = index;

            index++;
        }
        return nickindex;
    }

    public static int getUser(String username, Iterable<String> list) {
        return getUser(username, list, null);
    }

    public static Pattern pat(String regex, boolean caseSensitive) {
        if (caseSensitive) {
            return Pattern.compile(regex);
        } else {
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        }
    }

    public static Pattern pat(String regex) {
        return pat(regex, false);
    }

    public static boolean unique(int[] array) {
        for (int i = 0; i < array.length; i++) {
            for (int j = i + 1; j < array.length; j++) {
                if (array[i] == array[j]) {
                    return false;
                }
            }
        }
        return true;
    }
}
