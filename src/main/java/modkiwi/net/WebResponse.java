package modkiwi.net;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;

public abstract class WebResponse
{
    public abstract String getFinalUrl();

    public abstract String getResponseBody();

    public Document parse()
    {
        return Jsoup.parse(getResponseBody(), getFinalUrl());
    }

    public Document parse(Parser parser)
    {
        return Jsoup.parse(getResponseBody(), getFinalUrl(), parser);
    }

    @Override
    public String toString()
    {
        return getFinalUrl() + "\n------------------------\n" + getResponseBody();
    }
}
