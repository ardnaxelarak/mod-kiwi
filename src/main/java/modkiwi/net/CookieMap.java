package modkiwi.net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CookieMap implements Iterable<String>
{
    private Map<String, String> cookies;
    private List<String> order;

    public CookieMap()
    {
        cookies = new HashMap<String, String>();
        order = new LinkedList<String>();
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
        
        order.remove(key);
        order.add(key);
    }

    public void addAll(List<String> cookieList)
    {
        for (String cookie : cookieList)
            add(cookie);
    }

    public List<String> getCookies()
    {
        List<String> list = new LinkedList<String>();

        for (String key : order)
        {
            list.add(key + "=" + cookies.get(key));
        }

        return list;
    }

    public String getCookie()
    {
        String total = null;
        for (String cookie : getCookies())
        {
            if (total == null)
                total = cookie;
            else
                total += "; " + cookie;
        }
        return total;
    }

    public boolean isEmpty()
    {
        return cookies.isEmpty();
    }

    @Override
    public Iterator<String> iterator()
    {
        return getCookies().iterator();
    }
}
