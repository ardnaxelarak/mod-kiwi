package modkiwi.data;

import modkiwi.util.Logger;
import modkiwi.util.WebUtils;

import java.io.IOException;

import org.jsoup.nodes.Element;

public class GeekMailInfo
{
    private static final Logger LOGGER = new Logger(GeekMailInfo.class);

    private String id, subject, sender;
    private String content;
    private boolean read;
    private WebUtils web;

    public GeekMailInfo(WebUtils web, Element node)
    {
        this.web = web;
        sender = node.select("td.gm_prefix a").first().text();
        subject = node.select("td[style=gm_messageline] a[id^=subject_]").first().text();
        id = node.select("input[id^=msgcheck_]").first().attr("value");
        read = node.select("input[id^=msgread_]").first().attr("value").equals("1");
        content = null;
    }

    public String getId()
    {
        return id;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getSender()
    {
        return sender;
    }

    public String getContent()
    {
        if (content == null)
        {
            try
            {
                content = web.getMailContent(getId());
            }
            catch (IOException e)
            {
                LOGGER.throwing("getContent()", e);
            }
        }
        return content;
    }

    public boolean getRead()
    {
        return read;
    }
}
