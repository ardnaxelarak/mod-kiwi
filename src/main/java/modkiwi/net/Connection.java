package modkiwi.net;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.*;

import com.google.appengine.api.urlfetch.*;

public class Connection
{
    private URLFetchService fetcher;
    private List<String> cookies;
    private static final Logger LOG = Logger.getLogger(Connection.class.getName());
    private PrintWriter pw = null;

    public Connection()
    {
        fetcher = URLFetchServiceFactory.getURLFetchService();
        cookies = new LinkedList<String>();
    }

    public Connection(PrintWriter pw)
    {
        this();
        this.pw = pw;
    }

    public HTTPResponse execute(Request request) throws IOException
    {
        String url = request.getUrl();
        if (request.getRequestType().equals("GET"))
            url = url + "?" + request.getQuery();
        HTTPRequest req = new HTTPRequest(new URL(url), HTTPMethod.valueOf(request.getRequestType()));

        if (cookies.size() > 0)
        {
            if (pw != null)
                pw.println("Adding cookies...");

            for (String cookie : cookies) {
                req.addHeader(new HTTPHeader("Cookie", cookie));
                if (pw != null)
                    pw.println(cookie);
            }
        }
        req.addHeader(new HTTPHeader("Accept-Charset", request.getCharset()));

        if (request.getRequestType().equals("POST"))
        {
            req.addHeader(new HTTPHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + request.getCharset()));

            req.setPayload(request.getQuery().getBytes(request.getCharset()));
        }

        HTTPResponse resp = fetcher.fetch(req);

        if (resp.getResponseCode() != HttpURLConnection.HTTP_OK)
            throw new IOException("Unexpected response code: " + resp.getResponseCode());

        if (pw != null)
            pw.println(resp.getFinalUrl());

        for (HTTPHeader header : resp.getHeadersUncombined())
        {
            if (pw != null)
                pw.printf("%s: %s\n", header.getName(), header.getValue());
            if (header.getName().toLowerCase().equals("set-cookie"))
            {
                cookies.add(header.getValue().split(";", 2)[0]);
            }
        }
        return resp;
    }
}
