package modkiwi.util;

import static modkiwi.util.Constants.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.regex.Pattern;

public final class Utils
{
    private Utils()
    {
    }

    public static String join(Object[] array, CharSequence joiner)
    {
        if (array == null || array.length == 0)
            return "";

        StringBuilder result = new StringBuilder(array[0].toString());
        int len = array.length;
        for (int i = 1; i < array.length; i++)
        {
            result.append(joiner);
            result.append(array[i].toString());
        }

        return result.toString();
    }

    public static String join(Iterable<?> array, CharSequence joiner)
    {
        if (array == null)
            return "";

        Iterator<?> it = array.iterator();

        if (!it.hasNext())
            return "";

        StringBuilder result = new StringBuilder(it.next().toString());
        while (it.hasNext())
        {
            result.append(joiner);
            result.append(it.next().toString());
        }

        return result.toString();
    }

    public static String lPad(String text, int len)
    {
        return String.format("%-" + len + "s", text);
    }

    public static String lPadUsername(String name)
    {
        return lPad(name, MAX_USERNAME_LENGTH);
    }

    public static int getUser(String username, String[] list)
    {
        return getUser(username, Arrays.asList(list));
    }

    public static int getUser(String username, Iterable<String> list)
    {
        int index = 0;
        for (String user : list)
        {
            if (username.equalsIgnoreCase(user))
                return index;

            index++;
        }
        return -1;
    }

    public static Pattern pat(String regex, boolean caseSensitive)
    {
        if (caseSensitive)
            return Pattern.compile(regex);
        else
            return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public static Pattern pat(String regex)
    {
        return pat(regex, false);
    }
}
