package modkiwi.net;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import modkiwi.net.exceptions.UnexpectedResponseCodeException;

public class NetConnection implements WebConnection
{
    private HttpURLConnection connection;
    private CookieMap cookies;
    private PrintWriter pw = null;

    private static final class Response extends WebResponse
    {
        private final HttpURLConnection connection;
        private final String responseBody;
        private final URL finalUrl;

        public Response(HttpURLConnection connection) throws IOException
        {
            this.connection = connection;
            this.finalUrl = connection.getURL();
            this.responseBody = IOUtils.toString(connection.getInputStream(), connection.getContentEncoding());
        }

        @Override
        public String getFinalUrl()
        {
            return finalUrl.toString();
        }

        @Override
        public String getResponseBody()
        {
            return responseBody;
        }
    }

    public NetConnection()
    {
        connection = null;
        cookies = new CookieMap();
    }

    public NetConnection(PrintWriter pw)
    {
        this();
        this.pw = pw;
    }

    public WebResponse execute(WebRequest request, URL url) throws IOException
    {
        if (request.getRequestType().equals("GET"))
            url = new URL(url.toString() + "?" + request.getQuery());

        connection = (HttpURLConnection)(url.openConnection());
        connection.setInstanceFollowRedirects(false);

        if (!cookies.isEmpty())
        {
            if (pw != null)
                pw.println(cookies.getCookie());
            connection.addRequestProperty("Cookie", cookies.getCookie());
        }

        connection.addRequestProperty("User-Agent", "Mozilla");
        connection.addRequestProperty("Accept-Language", "en-US,en;q=0.8");
        connection.addRequestProperty("Referer", "google.com");
        connection.setRequestProperty("Accept-Charset", request.getCharset());
        connection.setRequestMethod(request.getRequestType());

        if (request.getRequestType().equals("POST"))
        {
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + request.getCharset());

            try (OutputStream output = connection.getOutputStream()) {
                output.write(request.getQuery().getBytes(request.getCharset()));
            }
        }

        connection.connect();

        int status = connection.getResponseCode();

        if (pw != null)
            pw.printf("Response code %d\n", status);

        if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
            status == HttpURLConnection.HTTP_MOVED_PERM ||
            status == HttpURLConnection.HTTP_SEE_OTHER)
        {
            return execute(request, new URL(url, connection.getHeaderField("Location")));
        }
        else if (status != HttpURLConnection.HTTP_OK)
        {
            throw new UnexpectedResponseCodeException(connection.getResponseCode());
        }
        else
        {
            return new NetConnection.Response(connection);
        }
    }

    @Override
    public WebResponse execute(WebRequest request) throws IOException
    {
        return execute(request, new URL(request.getUrl()));
    }

    @Override
    public void saveCookies()
    {
        if (connection == null)
            throw new IllegalStateException();

        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet())
        {
            if (pw != null)
                pw.printf("%s: %s\n", entry.getKey(), entry.getValue());

            if (entry.getKey() == null)
                continue;
            if (entry.getKey().toLowerCase().startsWith("set-cookie"))
            {
                cookies.addAll(entry.getValue());
            }
        }
    }
}
