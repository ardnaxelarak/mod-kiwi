package modkiwi.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CookieMap implements Iterable<String>
{
    private Map<String, String> cookies;

    public CookieMap()
    {
        cookies = new HashMap<String, String>();
    }

    public void add(String cookie)
    {
        String key = cookie;
        String value = "";
        int index;

        index = key.indexOf(';');
        if (index >= 0)
            key = key.substring(0, index);

        index = key.indexOf('=');
        if (index >= 0)
        {
            value = key.substring(index + 1);
            key = key.substring(0, index);
        }

        cookies.put(key, value);
    }

    public void addAll(List<String> cookieList)
    {
        for (String cookie : cookieList)
            add(cookie);
    }

    public List<String> getCookies()
    {
        List<String> list = new LinkedList<String>();

        for (Map.Entry<String, String> entry : cookies.entrySet())
        {
            list.add(entry.getKey() + "=" + entry.getValue());
        }

        return list;
    }

    @Override
    public Iterator<String> iterator()
    {
        return getCookies().iterator();
    }
}
