package modkiwi.util;

import java.util.Arrays;
import java.util.Iterator;

public final class Utils
{
    private Utils()
    {
    }

    public static String join(Object[] array, String joiner)
    {
        if (array == null || array.length == 0)
            return "";

        String result = array[0].toString();
        int len = array.length;
        for (int i = 1; i < array.length; i++)
            result += joiner + array[i].toString();

        return result;
    }

    public static String join(Iterable<?> array, String joiner)
    {
        if (array == null)
            return "";

        Iterator<?> it = array.iterator();

        if (!it.hasNext())
            return "";

        String result = it.next().toString();
        while (it.hasNext())
            result += joiner + it.next().toString();

        return result;
    }

    public static String lPad(String text, int len)
    {
        return String.format("%-" + len + "s", text);
    }

    public static String lPadUsername(String name)
    {
        final int MAX_USERNAME_LENGTH = 15;
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
            if (username.equals(user))
                return index;

            index++;
        }
        return -1;
    }
}
