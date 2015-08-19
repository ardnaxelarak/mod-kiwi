package modkiwi.net;

import java.io.*;
import java.net.*;
import java.util.*;

public class RequestBuilder
{
    private static final String DEFAULT_CHARSET = java.nio.charset.StandardCharsets.UTF_8.name();
    private Hashtable<String, String> params;
    private String url;
    private String requestType;
    private String charset;

    private RequestBuilder(String requestType)
    {
        params = new Hashtable<String, String>();
        url = null;
        this.requestType = requestType;
        charset = DEFAULT_CHARSET;
    }

    public static RequestBuilder get()
    {
        return new RequestBuilder("GET");
    }

    public static RequestBuilder post()
    {
        return new RequestBuilder("POST");
    }

    public RequestBuilder setUrl(String url)
    {
        this.url = url;
        return this;
    }

    public RequestBuilder addParameter(String key, String value)
    {
        params.put(key, value);
        return this;
    }

    public Request build() throws IOException
    {
        String query = "";
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            if (!query.equals(""))
                query += "&";
            query += String.format("%s=%s",
                        URLEncoder.encode(entry.getKey(), charset),
                        URLEncoder.encode(entry.getValue(), charset));
        }
        return new Request(url, query, requestType, charset);
    }
}
