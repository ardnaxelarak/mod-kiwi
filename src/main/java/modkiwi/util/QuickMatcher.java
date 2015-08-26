package modkiwi.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuickMatcher
{
    private final Pattern pattern;
    private Matcher matcher;

    public QuickMatcher(String regex, boolean caseSensitive)
    {
        if (caseSensitive)
            pattern = Pattern.compile(regex);
        else
            pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        matcher = null;
    }

    public QuickMatcher(String regex)
    {
        this(regex, false);
    }

    public boolean matches(CharSequence input)
    {
        matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public String group(int group)
    {
        return matcher.group(group);
    }
}
