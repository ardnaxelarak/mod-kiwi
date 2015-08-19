package modkiwi;

import modkiwi.data.*;
import modkiwi.net.*;
import modkiwi.net.Connection;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.urlfetch.*;

import org.apache.http.*;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.client.protocol.*;
import org.apache.http.impl.client.*;
import org.apache.http.protocol.*;
import org.apache.http.util.*;

import org.jsoup.*;
import org.jsoup.nodes.*;

public class Helper
{
    private static final ResponseHandler<String> handler = new BasicResponseHandler();
    private HttpClient client;
    private String username;
    private Connection conn;

    private class WebResponse
    {
        private final HttpContext context;
        private final String responseBody;
        private final URI finalUrl;

        public WebResponse(HttpUriRequest request) throws IOException
        {
            context = new BasicHttpContext();
            responseBody = client.execute(request, handler, context);
            finalUrl = Helper.getFinalUrl(request, context);
        }

        public URI getFinalUrl()
        {
            return finalUrl;
        }

        public String getResponseBody()
        {
            return responseBody;
        }

        public Document parse()
        {
            return Jsoup.parse(responseBody, finalUrl.toString());
        }

        @Override
        public String toString()
        {
            return getResponseBody();
        }
    }

    public Helper()
    {
        /*
        client = HttpClientBuilder.create()
                .setRedirectStrategy(new LaxRedirectStrategy())
                .build();
                */
        client = null;
        conn = new Connection();
    }

    public Helper(PrintWriter pw)
    {
        client = null;
        conn = new Connection(pw);
    }

    public synchronized HTTPResponse login() throws IOException
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

    public synchronized HTTPResponse login(String username, String password) throws IOException
    {
        Request request = modkiwi.net.RequestBuilder.post()
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

    public synchronized HTTPResponse geekmail(String user, String subject, String content) throws IOException
    {
        Request request = modkiwi.net.RequestBuilder.post()
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
        HttpUriRequest request = RequestBuilder.post()
                .setUri("https://boardgamegeek.com/geekrecommend.php")
                .addParameter("action", "recommend")
                .addParameter("itemid", article)
                .addParameter("itemtype", "article")
                .addParameter("value", "1")
                .build();

        WebResponse response = new WebResponse(request);
    }

    public synchronized void edit(String article, String subject, String content) throws IOException
    {
        HttpUriRequest request = RequestBuilder.post()
                .setUri("https://boardgamegeek.com/article/save")
                .addParameter("action", "save")
                .addParameter("articleid", article)
                .addParameter("subject", subject)
                .addParameter("body", content)
                .addParameter("B1", "Submit")
                .build();

        WebResponse response = new WebResponse(request);
    }

    public synchronized String replyArticle(String article, String subject, String content) throws IOException
    {
        HttpUriRequest request = RequestBuilder.post()
                .setUri("https://boardgamegeek.com/article/save")
                .addParameter("action", "save")
                .addParameter("replytoid", article)
                .addParameter("subject", subject)
                .addParameter("body", content)
                .build();

        WebResponse response = new WebResponse(request);

        Pattern pattern = Pattern.compile("boardgamegeek.com/article/(\\d*)#(\\d*)$");
        Matcher matcher = pattern.matcher(response.getFinalUrl().toString());
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

        HttpUriRequest request = RequestBuilder.post()
                .setUri("https://boardgamegeek.com/article/save")
                .addParameter("action", "save")
                .addParameter("replytoid", article)
                .addParameter("subject", sub)
                .addParameter("body", content)
                .build();

        WebResponse response = new WebResponse(request);

        Pattern pattern = Pattern.compile("boardgamegeek.com/article/(\\d*)#(\\d*)$");
        Matcher matcher = pattern.matcher(response.getFinalUrl().toString());
        if (matcher.find())
            return matcher.group(1);
        else
            return null;
    }

    public synchronized ThreadInfo getThread(String thread, String article, int max) throws IOException
    {
        HttpUriRequest request = RequestBuilder.get()
                .setUri("https://boardgamegeek.com/xmlapi2/thread")
                .addParameter("id", thread)
                .addParameter("minarticle", article)
                .addParameter("count", Integer.toString(max))
                .build();

        return getThread(request);
    }

    public synchronized ThreadInfo getThread(String thread, String article) throws IOException
    {
        HttpUriRequest request = RequestBuilder.get()
                .setUri("https://boardgamegeek.com/xmlapi2/thread")
                .addParameter("id", thread)
                .addParameter("minarticleid", article)
                .build();

        return getThread(request);
    }

    public synchronized ThreadInfo getThread(String thread, int max) throws IOException
    {
        HttpUriRequest request = RequestBuilder.get()
                .setUri("https://boardgamegeek.com/xmlapi2/thread")
                .addParameter("id", thread)
                .addParameter("count", Integer.toString(max))
                .build();

        return getThread(request);
    }

    public synchronized ThreadInfo getThread(String thread) throws IOException
    {
        HttpUriRequest request = RequestBuilder.get()
                .setUri("https://boardgamegeek.com/xmlapi2/thread")
                .addParameter("id", thread)
                .build();

        return getThread(request);
    }

    public synchronized ThreadInfo getThread(HttpUriRequest request) throws IOException
    {
        WebResponse response = new WebResponse(request);
        return new ThreadInfo(response.parse().select("thread").first());
    }

    private static URI getFinalUrl(HttpUriRequest request, HttpContext context)
    {
        URI finalUrl = request.getURI();
        RedirectLocations locations = (RedirectLocations)context.getAttribute(HttpClientContext.REDIRECT_LOCATIONS);
        if (locations != null) {
            finalUrl = locations.getAll().get(locations.getAll().size() - 1);
        }
        return finalUrl;
    }

    public synchronized HTTPResponse testCookies() throws IOException
    {
        Request request = modkiwi.net.RequestBuilder.get()
                .setUrl("http://boardgamegeek.com/")
                .build();

        return conn.execute(request);
    }
}
