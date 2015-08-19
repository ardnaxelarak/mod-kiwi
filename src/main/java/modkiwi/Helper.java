package modkiwi;

import modkiwi.data.*;
import modkiwi.net.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.urlfetch.*;

import org.jsoup.*;
import org.jsoup.nodes.*;

public class Helper
{
    private String username;
    private WebConnection conn;

    public Helper()
    {
        conn = new GAEConnection();
    }

    public Helper(PrintWriter pw)
    {
        conn = new GAEConnection(pw);
    }

    public synchronized WebResponse login() throws IOException
    {
        DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
        Key key = KeyFactory.createKey("Credentials", "autobot");
        try {
            Entity ent = datastore.get(key);
            this.username = ent.getProperty("username").toString();
            String password = ent.getProperty("password").toString();
            return login(username, password);
        } catch (EntityNotFoundException e) {
            return null;
        }
    }

    public synchronized WebResponse login(String username, String password) throws IOException
    {
        WebRequest request = RequestBuilder.post()
                .setUrl("http://boardgamegeek.com/login")
                .addParameter("username", username)
                .addParameter("password", password)
                .build();

        return conn.execute(request);
    }

    public String getUsername()
    {
        return username;
    }

    public synchronized WebResponse geekmail(String user, String subject, String content) throws IOException
    {
        WebRequest request = RequestBuilder.post()
                .setUrl("http://boardgamegeek.com/geekmail_controller.php")
                .addParameter("B1", "Send")
                .addParameter("action", "save")
                .addParameter("body", content)
                .addParameter("savecopy", "1")
                .addParameter("subject", subject)
                .addParameter("touser", user)
                .build();

        return conn.execute(request);
    }

    public synchronized void thumb(String article) throws IOException
    {
        WebRequest request = RequestBuilder.post()
                .setUrl("https://boardgamegeek.com/geekrecommend.php")
                .addParameter("action", "recommend")
                .addParameter("itemid", article)
                .addParameter("itemtype", "article")
                .addParameter("value", "1")
                .build();

        WebResponse response = conn.execute(request);
    }

    public synchronized void edit(String article, String subject, String content) throws IOException
    {
        WebRequest request = RequestBuilder.post()
                .setUrl("https://boardgamegeek.com/article/save")
                .addParameter("action", "save")
                .addParameter("articleid", article)
                .addParameter("subject", subject)
                .addParameter("body", content)
                .addParameter("B1", "Submit")
                .build();

        WebResponse response = conn.execute(request);
    }

    public synchronized String replyArticle(String article, String subject, String content) throws IOException
    {
        WebRequest request = RequestBuilder.post()
                .setUrl("https://boardgamegeek.com/article/save")
                .addParameter("action", "save")
                .addParameter("replytoid", article)
                .addParameter("subject", subject)
                .addParameter("body", content)
                .build();

        WebResponse response = conn.execute(request);

        Pattern pattern = Pattern.compile("boardgamegeek.com/article/(\\d*)#(\\d*)$");
        Matcher matcher = pattern.matcher(response.getFinalUrl());
        if (matcher.find())
            return matcher.group(1);
        else
            return null;
    }

    public synchronized String replyThread(String thread, String subject, String content) throws IOException
    {
        ThreadInfo t = getThread(thread, 1);
        String sub = subject;
        if (sub == null)
            sub = "Re: " + t.getSubject();

        String article = t.getArticleId();

        WebRequest request = RequestBuilder.post()
                .setUrl("https://boardgamegeek.com/article/save")
                .addParameter("action", "save")
                .addParameter("replytoid", article)
                .addParameter("subject", sub)
                .addParameter("body", content)
                .build();

        WebResponse response = conn.execute(request);

        Pattern pattern = Pattern.compile("boardgamegeek.com/article/(\\d*)#(\\d*)$");
        Matcher matcher = pattern.matcher(response.getFinalUrl());
        if (matcher.find())
            return matcher.group(1);
        else
            return null;
    }

    public synchronized ThreadInfo getThread(String thread, String article, int max) throws IOException
    {
        WebRequest request = RequestBuilder.get()
                .setUrl("https://boardgamegeek.com/xmlapi2/thread")
                .addParameter("id", thread)
                .addParameter("minarticle", article)
                .addParameter("count", Integer.toString(max))
                .build();

        return getThread(request);
    }

    public synchronized ThreadInfo getThread(String thread, String article) throws IOException
    {
        WebRequest request = RequestBuilder.get()
                .setUrl("https://boardgamegeek.com/xmlapi2/thread")
                .addParameter("id", thread)
                .addParameter("minarticleid", article)
                .build();

        return getThread(request);
    }

    public synchronized ThreadInfo getThread(String thread, int max) throws IOException
    {
        WebRequest request = RequestBuilder.get()
                .setUrl("https://boardgamegeek.com/xmlapi2/thread")
                .addParameter("id", thread)
                .addParameter("count", Integer.toString(max))
                .build();

        return getThread(request);
    }

    public synchronized ThreadInfo getThread(String thread) throws IOException
    {
        WebRequest request = RequestBuilder.get()
                .setUrl("https://boardgamegeek.com/xmlapi2/thread")
                .addParameter("id", thread)
                .build();

        return getThread(request);
    }

    public synchronized ThreadInfo getThread(WebRequest request) throws IOException
    {
        WebResponse response = conn.execute(request);
        return new ThreadInfo(response.parse().select("thread").first());
    }

    public synchronized WebResponse testCookies() throws IOException
    {
        WebRequest request = RequestBuilder.get()
                .setUrl("http://boardgamegeek.com/")
                .build();

        return conn.execute(request);
    }
}
