package modkiwi.data;

import org.jsoup.*;
import org.jsoup.nodes.*;
import org.jsoup.select.*;

public class ThreadInfo
{
    private String id, subject;
    private int count;
    private String link;
    private ArticleInfo[] articles;

    public ThreadInfo(Element node)
    {
        id = node.attr("id");
        count = Integer.parseInt(node.attr("numarticles"));
        link = node.attr("link");
        subject = node.select("subject").first().text();
        Elements el = node.select("articles article");
        articles = new ArticleInfo[el.size()];
        int k = 0;
        for (Element e : el)
            articles[k++] = new ArticleInfo(e);
    }

    public String getId()
    {
        return id;
    }

    public String getSubject()
    {
        return subject;
    }

    public String getArticleId()
    {
        if (articles.length > 0)
            return articles[0].getId();
        else
            return null;
    }

    public ArticleInfo[] getArticles()
    {
        return articles;
    }
}
