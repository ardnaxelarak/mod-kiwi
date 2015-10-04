package modkiwi.data;

import java.util.*;
import java.util.regex.*;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class ArticleInfo
{
    String id, username, link, postdate, editdate, subject, body;
    int numedits;
    public ArticleInfo(Element node)
    {
        id = node.attr("id");
        username = node.attr("username");
        link = node.attr("link");
        postdate = node.attr("postdate");
        editdate = node.attr("editdate");
        numedits = Integer.parseInt(node.attr("numedits"));
        subject = node.select("subject").first().text();
        body = node.ownText();
    }

    public String getId()
    {
        return id;
    }

    public String getUsername()
    {
        return username;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getBody()
    {
        return body;
    }

    public String getPostDate()
    {
        return postdate;
    }

    public List<String> getCommands()
    {
        Document d = Jsoup.parseBodyFragment(getBody());
        List<String> list = new LinkedList<String>();
        Pattern p = Pattern.compile("\\[([^\\[\\]]*)\\]");
        for (Element e : d.select("b:not(div.quote b)"))
        {
            String text = e.text();
            Matcher m = p.matcher(text);
            int index = 0;
            while (m.find(index))
            {
                list.add(m.group(1).trim());
                index = m.end();
            }
            if (!m.matches())
                list.add(text.trim());
        }
        return list;
    }
}
