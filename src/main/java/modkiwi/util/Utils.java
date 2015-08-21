package modkiwi.util;

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
}
