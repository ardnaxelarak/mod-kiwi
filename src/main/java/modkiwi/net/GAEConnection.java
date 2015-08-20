package modkiwi.net;

import java.io.*;
import java.net.*;
import java.util.*;

import com.google.appengine.api.urlfetch.*;

import org.apache.commons.io.IOUtils;

public class GAEConnection implements WebConnection
{
    private URLFetchService fetcher;
    private CookieMap cookies;
    private PrintWriter pw = null;
    private HTTPResponse resp = null;

    private static class Response extends WebResponse
    {
        private final HTTPResponse response;
        private final HTTPRequest request;
        private final String responseBody;
        private URL finalUrl;

        public Response(HTTPRequest request, HTTPResponse response) throws IOException
        {
            this.request = request;
            this.response = response;
            finalUrl = request.getURL();
            if (response.getFinalUrl() != null)
                finalUrl = response.getFinalUrl();

            // determine proper encoding somehow
            this.responseBody = IOUtils.toString(response.getContent(), "UTF-8");
        }

        @Override
        public String getResponseBody()
        {
            return responseBody;
        }

        @Override
        public String getFinalUrl()
        {
            return finalUrl.toString();
        }
    }

    public GAEConnection()
    {
        fetcher = URLFetchServiceFactory.getURLFetchService();
        cookies = new CookieMap();
    }

    public GAEConnection(PrintWriter pw)
    {
        this();
        this.pw = pw;
    }

    @Override
    public WebResponse execute(WebRequest request) throws IOException
    {
        String url = request.getUrl();
        if (request.getRequestType().equals("GET"))
            url = url + "?" + request.getQuery();

        HTTPRequest req = new HTTPRequest(new URL(url), HTTPMethod.valueOf(request.getRequestType()));

        if (pw != null)
            pw.println("Adding cookies...");

        for (String cookie : cookies) {
            req.addHeader(new HTTPHeader("Cookie", cookie));
            if (pw != null)
                pw.println(cookie);
        }

        req.addHeader(new HTTPHeader("Accept-Charset", request.getCharset()));

        if (request.getRequestType().equals("POST"))
        {
            req.addHeader(new HTTPHeader("Content-Type", "application/x-www-form-urlencoded;charset=" + request.getCharset()));

            req.setPayload(request.getQuery().getBytes(request.getCharset()));
        }

        resp = fetcher.fetch(req);

        if (resp.getResponseCode() != HttpURLConnection.HTTP_OK)
            throw new IOException("Unexpected response code: " + resp.getResponseCode());

        if (pw != null)
            pw.println(resp.getFinalUrl());

        return new Response(req, resp);
    }

    @Override
    public void saveCookies()
    {
        if (resp == null)
            throw new IllegalStateException();

        for (HTTPHeader header : resp.getHeadersUncombined())
        {
            if (pw != null)
                pw.printf("%s: %s\n", header.getName(), header.getValue());
            if (header.getName().toLowerCase().startsWith("set-cookie"))
            {
                cookies.add(header.getValue());
            }
        }
    }
}
