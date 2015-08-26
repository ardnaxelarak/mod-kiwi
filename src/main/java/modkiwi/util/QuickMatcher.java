package modkiwi.util;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuickMatcher
{
    private final Pattern pattern;
    private final AtomicBoolean lock;

    private Matcher matcher;

    public QuickMatcher(String regex, boolean caseSensitive)
    {
        if (caseSensitive)
            pattern = Pattern.compile(regex);
        else
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        matcher = null;
        lock = new AtomicBoolean(false);
    }

    public QuickMatcher(String regex)
    {
        this(regex, false);
    }

    public boolean matches(CharSequence input)
    {
        while (!lock.compareAndSet(false, true))
        {
            try
            {
                lock.wait();
            }
            catch (InterruptedException e)
            {
            }
        }

        matcher = pattern.matcher(input);
        if (matcher.matches())
        {
            return true;
        }
        else
        {
            release();
            return false;
        }
    }

    public void release()
    {
        lock.set(false);
        lock.notifyAll();
    }

    public String group(int group)
    {
        return group(group, false);
    }

    public String group(int group, boolean release)
    {
        String res = matcher.group(group);
        if (release)
            release();
        return res;
    }
}
