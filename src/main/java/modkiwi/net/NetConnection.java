package modkiwi.net;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class NetConnection implements WebConnection
{
    private HttpURLConnection connection;
    private CookieMap cookies;

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

    @Override
    public WebResponse execute(WebRequest request) throws IOException
    {
        String url = request.getUrl();

        if (request.getRequestType().equals("GET"))
            url = url + "?" + request.getQuery();

        connection = (HttpURLConnection)(new URL(url).openConnection());

        for (String cookie : cookies)
            connection.addRequestProperty("Cookie", cookie);

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

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            throw new IOException("Unexpected response code: " + connection.getResponseCode());

        return new NetConnection.Response(connection);
    }

    @Override
    public void saveCookies()
    {
        if (connection == null)
            throw new IllegalStateException();

        for (Map.Entry<String, List<String>> entry : connection.getHeaderFields().entrySet())
        {
            if (entry.getKey().toLowerCase().startsWith("set-cookie"))
            {
                cookies.addAll(entry.getValue());
            }
        }
    }
}
